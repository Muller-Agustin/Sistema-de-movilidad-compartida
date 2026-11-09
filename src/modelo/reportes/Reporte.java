package modelo.reportes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Un reporte es una tabla: un título, los nombres de las columnas y las filas de texto.
// El mismo reporte se muestra por consola (aTexto) o se exporta a CSV (ExportadorCSV).
public class Reporte {
    private static final String VACIO = "-";

    private final String titulo;
    private final List<String> encabezados;
    private final List<List<String>> filas;

    public Reporte(String titulo, String... encabezados) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        if (encabezados.length == 0) {
            throw new IllegalArgumentException("El reporte necesita al menos una columna");
        }
        this.titulo = titulo;
        this.encabezados = List.of(encabezados);
        this.filas = new ArrayList<>();
    }

    // Una celda null o vacía se muestra como "-"
    public void agregarFila(String... celdas) {
        if (celdas.length != encabezados.size()) {
            throw new IllegalArgumentException("La fila tiene " + celdas.length + " celdas y el reporte tiene "
                    + encabezados.size() + " columnas");
        }
        List<String> fila = new ArrayList<>(Arrays.asList(celdas));
        for (int i = 0; i < fila.size(); i++) {
            String celda = fila.get(i);
            if (celda == null || celda.isEmpty()) {
                fila.set(i, VACIO);
            }
        }
        filas.add(fila);
    }

    public String getTitulo() { return titulo; }
    public List<String> getEncabezados() { return encabezados; }
    public int cantidadFilas() { return filas.size(); }

    public List<List<String>> getFilas() {
        List<List<String>> copia = new ArrayList<>();
        for (List<String> fila : filas) {
            copia.add(List.copyOf(fila));
        }
        return copia;
    }

    // Tabla con las columnas alineadas, para mostrar por consola
    public String aTexto() {
        StringBuilder texto = new StringBuilder(titulo).append("\n");
        if (filas.isEmpty()) {
            return texto.append("(sin datos)\n").toString();
        }
        int[] anchos = new int[encabezados.size()];
        for (int i = 0; i < anchos.length; i++) {
            anchos[i] = encabezados.get(i).length();
            for (List<String> fila : filas) {
                anchos[i] = Math.max(anchos[i], fila.get(i).length());
            }
        }
        agregarLinea(texto, encabezados, anchos);
        StringBuilder guiones = new StringBuilder();
        for (int i = 0; i < anchos.length; i++) {
            if (i > 0) {
                guiones.append("-+-");
            }
            guiones.append("-".repeat(anchos[i]));
        }
        texto.append(guiones).append("\n");
        for (List<String> fila : filas) {
            agregarLinea(texto, fila, anchos);
        }
        return texto.toString();
    }

    private void agregarLinea(StringBuilder texto, List<String> celdas, int[] anchos) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < celdas.size(); i++) {
            if (i > 0) {
                linea.append(" | ");
            }
            linea.append(String.format("%-" + anchos[i] + "s", celdas.get(i)));
        }
        texto.append(linea.toString().stripTrailing()).append("\n");
    }
}