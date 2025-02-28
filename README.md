# 📚 Documentación del Proyecto de Compilador

## 📝 Descripción del Proyecto

Este proyecto consiste en un compilador que realiza análisis semántico de un lenguaje de programación específico. El compilador verifica la validez de las variables y las asignaciones en el código de entrada, asegurándose de que cumplan con las reglas semánticas definidas.

## 📐 Reglas Semánticas

### 🔍 Expresión Regular para Variables

Las variables deben seguir la siguiente expresión regular:

```
RegEx: JSJ(a-z)(0-9)+
```

Esto significa que una variable debe comenzar con "JSJ", seguida de una letra minúscula (a-z) y luego uno o más dígitos (0-9).

### 🖋️ Declaración de Variables

Las variables se pueden declarar de la siguiente manera:

```
ENTERO = JSJa1, JSJb4, JSJc5;
FLOTANTE = JSJa2, JSJb6, JSJc7;
CADENA = "JSJa3, JSJb8, JSJc9";
```

### ⚠️ Errores Comunes

- **Variable inválida**: La variable no cumple con la expresión regular.
- **Tipo de dato inválido**: El tipo de dato no es válido.
- **Variable no definida**: La variable no ha sido declarada antes de su uso.
- **Incompatibilidad de tipos**: Se intenta asignar un valor de un tipo diferente al tipo de la variable.

### 🛠️ Ejemplos de Errores

- `ENTERO = JSJ1;` - Error: No cumple con la regla de poner una letra después de "JSJ".
- `FLOTANTE = JSJa;` - Error: No pone un número después de la letra.
- `CADENA = A12;` - Error: No tiene el prefijo "JSJ" y no está entre comillas.

## 🖥️ Interfaz Gráfica de Usuario (GUI)

La interfaz gráfica permite al usuario introducir el código en un área de texto y analizarlo para detectar errores semánticos. La GUI muestra dos tablas: una tabla de símbolos y una tabla de errores.

### 🧩 Componentes de la GUI

- **📝 Área de texto**: Para introducir el código.
- **🔍 Botón "Analizar Código"**: Para iniciar el análisis del código.
- **📊 Tabla de símbolos**: Muestra los lexemas y sus tipos de dato.
- **🚨 Tabla de errores**: Muestra los errores encontrados, incluyendo el token de error, el lexema, la línea y la descripción.

## 📂 Estructura del Proyecto

El proyecto "java_compiler" se organiza en una estructura de directorios que refleja las diferentes funcionalidades del compilador.

### 📁 Paquetes y Clases Principales

#### `compiler/`

Contiene las clases centrales del compilador.

- **CompiladorGUI.java**: Maneja la lógica principal de compilación y la interfaz gráfica de usuario (GUI) del compilador.

#### `errors/`

Incluye clases relacionadas con el manejo de errores.

- **Error.java**: Define los diferentes tipos de errores que el compilador puede detectar.

## 🔧 Herramientas y Librerías Utilizadas

- **Java Swing**: Para la creación de la interfaz gráfica de usuario.
- **Java AWT**: Para la gestión de eventos y componentes gráficos.
- **Java Util**: Para el manejo de estructuras de datos como listas y mapas.
- **Java Regex**: Para la validación de patrones de expresiones regulares.

## 📜 Descripción de los Componentes Principales

### 1. CompiladorGUI.java

Esta clase extiende `JFrame` y representa la ventana principal de la aplicación. Se encarga de configurar la GUI, incluyendo la aplicación de un tema oscuro. La ventana principal contiene:

- **Área de texto (areaCodigo)**: Permite al usuario ingresar el código fuente que será analizado.
- **Botón "Analizar Código" (botonAnalizar)**: Inicia el análisis del código ingresado.
- **Paneles de tablas**: Muestra la tabla de símbolos (`tablaSimbolos`) y la tabla de errores (`tablaErrores`), proporcionando retroalimentación en tiempo real sobre el análisis del código.

### 2. Error.java

Define los diferentes tipos de errores que el compilador puede detectar. Cada tipo de error tiene un token asociado y una descripción. La clase proporciona métodos para obtener el token y la descripción del error, permitiendo una gestión estructurada y coherente de los distintos errores semánticos y sintácticos que pueden ocurrir durante la compilación.

## 🚀 Ejecución del Proyecto

Para ejecutar el proyecto, simplemente compile y ejecute la clase `CompiladorGUI`. Esto abrirá la interfaz gráfica donde puede introducir el código y analizarlo para detectar errores semánticos.

## 🎯 Conclusión

Este proyecto de compilador proporciona una herramienta útil para verificar la validez de las variables y las asignaciones en un lenguaje de programación específico. La interfaz gráfica facilita la interacción del usuario y la visualización de los resultados del análisis semántico.

## 🔗 Enlace al Repositorio

Consultar el repositorio en GitHub: [Compilador_1](https://github.com/DARKTOTEM2703/Comp-lador_1)
