import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class CompiladorGUI extends JFrame {
    private final JTextArea areaCodigo;
    private final JButton botonCompilar;
    private final JTable tablaSimbolos, tablaErrores;
    private final DefaultTableModel modeloTablaSimbolos, modeloTablaErrores;

    private static final String REGEX_VARIABLE = "^JSJ[a-zA-Z][0-9]+$"; // Actualiza la regex si lo necesitas.
    private final java.util.List<Error> tablaErroresList = new ArrayList<>();
    private final Map<String, String> tablaSimbolosMap = new HashMap<>();

    public CompiladorGUI() {
        setTitle("Compilador - Análisis Semántico");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panelPrincipal, BorderLayout.CENTER);

        // Botón para compilar el código
        botonCompilar = new JButton("Compilar");
        botonCompilar.setFont(new Font("Arial", Font.BOLD, 16));
        botonCompilar.setBackground(new Color(70, 130, 180));
        botonCompilar.setForeground(Color.WHITE);
        botonCompilar.setFocusPainted(false);
        botonCompilar.addActionListener(this::analizarCodigo);
        JPanel panelBoton = new JPanel();
        panelBoton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panelBoton.add(botonCompilar);
        panelPrincipal.add(panelBoton, BorderLayout.NORTH);

        // Área de texto para el código
        areaCodigo = new JTextArea(10, 40); // Ajustar tamaño del área de texto
        areaCodigo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        areaCodigo.setLineWrap(true);
        areaCodigo.setWrapStyleWord(true);
        JScrollPane scrollCodigo = new JScrollPane(areaCodigo);
        panelPrincipal.add(scrollCodigo, BorderLayout.CENTER);

        // Panel para las tablas
        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 10, 10)); // Cambiar a GridLayout horizontal
        panelTablas.setPreferredSize(new Dimension(800, 400));
        panelTablas.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelPrincipal.add(panelTablas, BorderLayout.SOUTH);

        // Tabla de símbolos
        modeloTablaSimbolos = new DefaultTableModel(new String[] { "Lexema", "Tipo de dato" }, 0);
        tablaSimbolos = new JTable(modeloTablaSimbolos);
        tablaSimbolos.setFillsViewportHeight(true);
        tablaSimbolos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ajustarAnchoColumnas(tablaSimbolos);
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        panelTablas.add(scrollSimbolos);

        // Tabla de errores
        modeloTablaErrores = new DefaultTableModel(new String[] { "Token de error", "Lexema", "Línea", "Descripción" },
                0);
        tablaErrores = new JTable(modeloTablaErrores);
        tablaErrores.setFillsViewportHeight(true);
        tablaErrores.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ajustarAnchoColumnas(tablaErrores);
        JScrollPane scrollErrores = new JScrollPane(tablaErrores);
        panelTablas.add(scrollErrores);
    }

    private void analizarCodigo(ActionEvent e) {
        tablaSimbolosMap.clear();
        tablaErroresList.clear();
        modeloTablaSimbolos.setRowCount(0);
        modeloTablaErrores.setRowCount(0);

        String[] lineas = areaCodigo.getText().split("\n");
        for (int i = 0; i < lineas.length; i++) {
            procesarLinea(lineas[i].trim(), i + 1);
        }

        tablaSimbolosMap.forEach((key, value) -> modeloTablaSimbolos.addRow(new Object[] { key, value }));
        tablaErroresList.forEach(error -> modeloTablaErrores
                .addRow(new Object[] { error.token, error.lexema, error.linea, error.descripcion }));

        ajustarAnchoColumnas(tablaSimbolos);
        ajustarAnchoColumnas(tablaErrores);
    }

    private void procesarLinea(String linea, int numeroLinea) {
        if (linea.isEmpty())
            return;

        if (linea.matches("^(ENTERO|FLOTANTE|CADENA)\\s*=.*;$")) {
            declararVariables(linea, numeroLinea);
        } else if (linea.matches("^JSJ[a-zA-Z][0-9]+\\s*=.*;$")) {
            validarAsignaciones(linea, numeroLinea);
        } else {
            agregarError("Sintaxis inválida", linea, numeroLinea, "Formato incorrecto.");
        }
    }

    private void declararVariables(String linea, int numeroLinea) {
        String[] partes = linea.replace(";", "").split("=");
        String tipo = partes[0].trim();
        String[] variables = partes[1].split(",");

        for (String var : variables) {
            var = var.trim();
            if (!var.matches(REGEX_VARIABLE)) {
                agregarError("Variable inválida", var, numeroLinea, "No cumple con el formato JSJ[a-zA-Z][0-9]+.");
            } else {
                tablaSimbolosMap.put(var, tipo);
            }
        }
    }

    private void validarAsignaciones(String linea, int numeroLinea) {
        String[] partes = linea.replace(";", "").split("=");
        String variable = partes[0].trim();
        String expresion = partes[1].trim();

        if (!tablaSimbolosMap.containsKey(variable)) {
            agregarError("Variable no definida", variable, numeroLinea, "La variable no ha sido declarada.");
            return;
        }

        String tipoVariable = tablaSimbolosMap.get(variable);
        String tipoExpresion = obtenerTipoExpresion(expresion, numeroLinea);

        if (tipoExpresion != null && !tipoVariable.equals(tipoExpresion)) {
            agregarError("Incompatibilidad de tipos", expresion, numeroLinea,
                    "No se puede asignar un valor de tipo '" + tipoExpresion + "' a una variable de tipo '"
                            + tipoVariable + "'.");
        }
    }

    private String obtenerTipoExpresion(String expresion, int numeroLinea) {
        if (expresion.startsWith("\"") && expresion.endsWith("\""))
            return "CADENA";
        if (expresion.matches("\\d+"))
            return "ENTERO";
        if (expresion.matches("\\d+\\.\\d+"))
            return "FLOTANTE";

        String[] operadores = { "+", "-", "*", "/" };
        for (String op : operadores) {
            if (expresion.contains(op)) {
                String[] partes = expresion.split("\\" + op);
                if (partes.length == 2) {
                    String tipo1 = obtenerTipoExpresion(partes[0].trim(), numeroLinea);
                    String tipo2 = obtenerTipoExpresion(partes[1].trim(), numeroLinea);
                    if (tipo1 == null || tipo2 == null)
                        return null;
                    if (!tipo1.equals(tipo2)) {
                        agregarError("Incompatibilidad de tipos", expresion, numeroLinea,
                                "No se puede realizar la operación entre '" + tipo1 + "' y '" + tipo2 + "'.");
                        return null;
                    }
                    return tipo1;
                }
            }
        }

        if (tablaSimbolosMap.containsKey(expresion)) {
            return tablaSimbolosMap.get(expresion);
        }

        agregarError("Variable no definida", expresion, numeroLinea, "La variable no ha sido declarada.");
        return null;
    }

    private void agregarError(String token, String lexema, int linea, String descripcion) {
        tablaErroresList.add(new Error(token, lexema, linea, descripcion));
    }

    private static class Error {
        String token, lexema, descripcion;
        int linea;

        public Error(String token, String lexema, int linea, String descripcion) {
            this.token = token;
            this.lexema = lexema;
            this.linea = linea;
            this.descripcion = descripcion;
        }
    }

    private void ajustarAnchoColumnas(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompiladorGUI().setVisible(true));
    }
}