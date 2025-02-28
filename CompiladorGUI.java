import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CompiladorGUI extends JFrame {

    // Componentes de la interfaz gráfica
    private final JTextArea areaCodigo;
    private final JButton botonAnalizar;
    private final JTable tablaSimbolos;
    private final JTable tablaErrores;
    private final DefaultTableModel modeloTablaSimbolos;
    private final DefaultTableModel modeloTablaErrores;

    // Expresión regular para validar las variables
    private static final String REGEX_VARIABLE = "^JSJ[a-z][0-9]+$";

    // Tabla de símbolos y tabla de errores
    private final Map<String, String> tablaSimbolosMap = new HashMap<>();
    private final List<Error> tablaErroresList = new ArrayList<>();

    public CompiladorGUI() {
        // Configuración de la ventana principal
        setTitle("Compilador - Análisis Semántico");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Área de texto para introducir el código
        areaCodigo = new JTextArea();
        areaCodigo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollCodigo = new JScrollPane(areaCodigo);
        scrollCodigo.setPreferredSize(new Dimension(800, 300));
        add(scrollCodigo, BorderLayout.CENTER);

        // Botón para analizar el código
        botonAnalizar = new JButton("Analizar Código");
        botonAnalizar.addActionListener((@SuppressWarnings("unused") ActionEvent e) -> {
            analizarCodigo();
        });
        JPanel panelBoton = new JPanel();
        panelBoton.add(botonAnalizar);
        add(panelBoton, BorderLayout.SOUTH);

        // Panel para las tablas
        JPanel panelTablas = new JPanel(new GridLayout(2, 1));
        panelTablas.setPreferredSize(new Dimension(800, 300));

        // Tabla de símbolos
        modeloTablaSimbolos = new DefaultTableModel();
        modeloTablaSimbolos.addColumn("Lexema");
        modeloTablaSimbolos.addColumn("Tipo de dato");
        tablaSimbolos = new JTable(modeloTablaSimbolos);
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        panelTablas.add(scrollSimbolos);

        // Tabla de errores
        modeloTablaErrores = new DefaultTableModel();
        modeloTablaErrores.addColumn("Token de error");
        modeloTablaErrores.addColumn("Lexema");
        modeloTablaErrores.addColumn("Línea");
        modeloTablaErrores.addColumn("Descripción");
        tablaErrores = new JTable(modeloTablaErrores);
        JScrollPane scrollErrores = new JScrollPane(tablaErrores);
        panelTablas.add(scrollErrores);

        add(panelTablas, BorderLayout.EAST);
    }

    private void analizarCodigo() {
        // Limpiar tablas y estructuras de datos
        tablaSimbolosMap.clear();
        tablaErroresList.clear();
        modeloTablaSimbolos.setRowCount(0);
        modeloTablaErrores.setRowCount(0);

        // Obtener el código del área de texto
        String[] lineasCodigo = areaCodigo.getText().split("\n");

        // Procesar cada línea del código
        for (int i = 0; i < lineasCodigo.length; i++) {
            procesarLinea(lineasCodigo[i], i + 1);
        }

        // Mostrar la tabla de símbolos
        for (Map.Entry<String, String> entry : tablaSimbolosMap.entrySet()) {
            modeloTablaSimbolos.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }

        // Mostrar la tabla de errores
        for (Error error : tablaErroresList) {
            modeloTablaErrores.addRow(new Object[] { error.token, error.lexema, error.linea, error.descripcion });
        }
    }

    private void procesarLinea(String linea, int numeroLinea) {
        // Eliminar espacios en blanco y separar por "="
        linea = linea.replaceAll("\\s", "");
        String[] partes = linea.split("=");

        if (partes.length != 2) {
            agregarError("Sintaxis inválida", linea, numeroLinea, "La línea no tiene el formato correcto.");
            return;
        }

        String tipo = partes[0];
        String[] variables = partes[1].split(",");

        // Validar el tipo de dato
        if (tipo.equals("ENTERO") || tipo.equals("FLOTANTE") || tipo.equals("CADENA")) {
            // Declaración de variables
            for (String var : variables) {
                var = var.replaceAll(";", ""); // Eliminar el punto y coma
                if (!validarVariable(var)) {
                    agregarError("Variable inválida", var, numeroLinea,
                            "La variable no cumple con la regla JSJ[a-z][0-9]+.");
                } else {
                    tablaSimbolosMap.put(var, tipo);
                }
            }
        } else {
            // Asignación o expresión
            validarAsignaciones(linea, numeroLinea);
        }
    }

    private void validarAsignaciones(String linea, int numeroLinea) {
        String[] partes = linea.split("=");
        if (partes.length == 2) {
            String variable = partes[0].trim();
            String expresion = partes[1].trim();

            // Verificar si la variable está definida
            if (!tablaSimbolosMap.containsKey(variable)) {
                agregarError("Variable no definida", variable, numeroLinea, "La variable no ha sido declarada.");
                return;
            }

            // Validar la expresión
            String tipoExpresion = obtenerTipoExpresion(expresion, numeroLinea);
            if (tipoExpresion != null) {
                String tipoVariable = tablaSimbolosMap.get(variable);
                if (!tipoVariable.equals(tipoExpresion)) {
                    agregarError("Incompatibilidad de tipos", expresion, numeroLinea,
                            "No se puede asignar una expresión de tipo " + tipoExpresion + " a una variable de tipo "
                                    + tipoVariable + ".");
                }
            }
        }
    }

    private String obtenerTipoExpresion(String expresion, int numeroLinea) {
        String[] tokens = expresion.split("[+\\-*/]");
        String tipoExpresion = null;

        for (String token : tokens) {
            token = token.trim();
            String tipoToken = tablaSimbolosMap.get(token);

            if (tipoToken == null) {
                if (validarVariable(token)) {
                    agregarError("Variable no definida", token, numeroLinea, "La variable no ha sido declarada.");
                    return null;
                } else {
                    try {
                        Double.parseDouble(token);
                        tipoToken = token.contains(".") ? "FLOTANTE" : "ENTERO";
                    } catch (NumberFormatException e) {
                        agregarError("Variable no definida", token, numeroLinea, "La variable no ha sido declarada.");
                        return null;
                    }
                }
            }

            if (tipoExpresion == null) {
                tipoExpresion = tipoToken;
            } else if (!tipoExpresion.equals(tipoToken)) {
                agregarError("Incompatibilidad de tipos", expresion, numeroLinea,
                        "La expresión contiene tipos incompatibles.");
                return null;
            }
        }

        return tipoExpresion;
    }

    private boolean validarVariable(String variable) {
        Pattern pattern = Pattern.compile(REGEX_VARIABLE);
        Matcher matcher = pattern.matcher(variable);
        return matcher.matches();
    }

    private void agregarError(String token, String lexema, int linea, String descripcion) {
        Error error = new Error(token, lexema, linea, descripcion);
        tablaErroresList.add(error);
    }

    // Clase para representar un error
    private static class Error {
        String token;
        String lexema;
        int linea;
        String descripcion;

        public Error(String token, String lexema, int linea, String descripcion) {
            this.token = token;
            this.lexema = lexema;
            this.linea = linea;
            this.descripcion = descripcion;
        }
    }

    public static void main(String[] args) {
        // Crear y mostrar la interfaz gráfica
        SwingUtilities.invokeLater(() -> {
            new CompiladorGUI().setVisible(true);
        });
    }
}