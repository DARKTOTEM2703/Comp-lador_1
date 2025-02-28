import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class CompiladorGUI extends JFrame {
    private final JTextArea areaCodigo;
    private final JButton botonAnalizar;
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

        areaCodigo = new JTextArea();
        areaCodigo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollCodigo = new JScrollPane(areaCodigo);
        scrollCodigo.setPreferredSize(new Dimension(300, 200)); // Ajustar el tamaño del área de texto
        add(scrollCodigo, BorderLayout.CENTER);

        botonAnalizar = new JButton("Analizar Código");
        botonAnalizar.addActionListener(this::analizarCodigo);
        add(botonAnalizar, BorderLayout.SOUTH);

        JPanel panelTablas = new JPanel(new GridLayout(2, 1));
        panelTablas.setPreferredSize(new Dimension(1550, 400)); // Ajustar el tamaño de las tablas
        modeloTablaSimbolos = new DefaultTableModel(new String[] { "Lexema", "Tipo de dato" }, 0);
        tablaSimbolos = new JTable(modeloTablaSimbolos);
        panelTablas.add(new JScrollPane(tablaSimbolos));

        modeloTablaErrores = new DefaultTableModel(new String[] { "Token de error", "Lexema", "Línea", "Descripción" },
                0);
        tablaErrores = new JTable(modeloTablaErrores);
        panelTablas.add(new JScrollPane(tablaErrores));

        add(panelTablas, BorderLayout.EAST);
    }

    private void ajustarAnchoColumnas(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 300; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            table.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompiladorGUI().setVisible(true));
    }
}
