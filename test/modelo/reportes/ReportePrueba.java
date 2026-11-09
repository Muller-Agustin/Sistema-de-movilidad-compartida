package modelo.reportes;

import pruebas.Verificador;

public class ReportePrueba {

    public static void ejecutar(Verificador v) {
        creacion(v);
        filas(v);
        texto(v);
    }

    private static void creacion(Verificador v) {
        Reporte reporte = new Reporte("Título", "A", "B");
        v.verificarIgual("Reporte: guarda el título", "Título", reporte.getTitulo());
        v.verificarIgual("Reporte: guarda los encabezados", java.util.List.of("A", "B"), reporte.getEncabezados());
        v.verificarNumero("Reporte: nace sin filas", 0, reporte.cantidadFilas());
        v.verificarExcepcion("Reporte: el título es obligatorio", IllegalArgumentException.class,
                () -> new Reporte(" ", "A"));
        v.verificarExcepcion("Reporte: necesita al menos una columna", IllegalArgumentException.class,
                () -> new Reporte("Título"));
    }

    private static void filas(Verificador v) {
        Reporte reporte = new Reporte("Título", "A", "B");
        reporte.agregarFila("1", "22");
        reporte.agregarFila(null, "");
        v.verificarNumero("Reporte: 2 filas", 2, reporte.cantidadFilas());
        v.verificarIgual("Reporte: guarda la fila", java.util.List.of("1", "22"), reporte.getFilas().get(0));
        v.verificarIgual("Reporte: una celda null o vacía se guarda como '-'", java.util.List.of("-", "-"), reporte.getFilas().get(1));
        v.verificarExcepcion("Reporte: una fila con menos celdas falla", IllegalArgumentException.class,
                () -> reporte.agregarFila("1"));
        v.verificarExcepcion("Reporte: una fila con más celdas falla", IllegalArgumentException.class,
                () -> reporte.agregarFila("1", "2", "3"));
        v.verificarNumero("Reporte: las filas inválidas no se agregan", 2, reporte.cantidadFilas());
        v.verificarExcepcion("Reporte: las filas devueltas no se pueden modificar", UnsupportedOperationException.class,
                () -> reporte.getFilas().get(0).add("x"));
        v.verificarExcepcion("Reporte: los encabezados devueltos no se pueden modificar", UnsupportedOperationException.class,
                () -> reporte.getEncabezados().add("x"));
    }

    private static void texto(Verificador v) {
        Reporte reporte = new Reporte("T", "A", "B");
        v.verificarIgual("Reporte sin filas: avisa que no hay datos", "T\n(sin datos)\n", reporte.aTexto());

        reporte.agregarFila("1", "22");
        reporte.agregarFila(null, "");
        // la columna B mide 2 por la celda "22"; la columna A mide 1
        v.verificarIgual("Reporte: tabla con las columnas alineadas",
                "T\nA | B\n--+---\n1 | 22\n- | -\n", reporte.aTexto());

        Reporte ancho = new Reporte("T", "Nombre", "N");
        ancho.agregarFila("Ana", "5");
        v.verificarIgual("Reporte: la columna se ensancha hasta el encabezado",
                "T\nNombre | N\n-------+--\nAna    | 5\n", ancho.aTexto());
    }
}