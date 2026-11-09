package datos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import modelo.datos.ExportadorCSV;
import modelo.excepciones.PersistenciaExcepcion;
import modelo.reportes.Reporte;
import pruebas.Verificador;

public class ExportadorCSVPrueba {

    public static void ejecutar(Verificador v) {
        formato(v);
        archivo(v);
    }

    private static Reporte reporteDeEjemplo() {
        Reporte reporte = new Reporte("Título que no se exporta", "Nombre", "Nota");
        reporte.agregarFila("Ana", "ok");
        reporte.agregarFila("Beto; Jr.", "dijo \"hola\"");
        reporte.agregarFila("=SUMA(1)", "línea1\nlínea2");
        reporte.agregarFila("+54", "@user");
        return reporte;
    }

    private static void formato(Verificador v) {
        ExportadorCSV exportador = new ExportadorCSV();
        List<String> lineas = exportador.lineas(reporteDeEjemplo());
        v.verificarNumero("CSV: encabezados + 4 filas", 5, lineas.size());
        v.verificarIgual("CSV: la primera línea son los encabezados (sin el título)", "Nombre;Nota", lineas.get(0));
        v.verificarIgual("CSV: campos simples separados por ';'", "Ana;ok", lineas.get(1));
        v.verificarIgual("CSV: un campo con ';' o comillas va entre comillas y las comillas se duplican",
                "\"Beto; Jr.\";\"dijo \"\"hola\"\"\"", lineas.get(2));
        v.verificarIgual("CSV: un campo que empieza con '=' se neutraliza y uno con salto de línea va entre comillas",
                "'=SUMA(1);\"línea1\nlínea2\"", lineas.get(3));
        v.verificarIgual("CSV: '+' y '@' al principio también se neutralizan", "'+54;'@user", lineas.get(4));
        v.verificarIgual("CSV: el texto es todas las líneas con salto al final",
                String.join("\n", lineas) + "\n", exportador.aTexto(reporteDeEjemplo()));

        Reporte vacio = new Reporte("Vacío", "A", "B");
        v.verificarNumero("CSV: un reporte sin filas exporta solo los encabezados", 1, exportador.lineas(vacio).size());
    }

    private static void archivo(Verificador v) {
        File carpeta = new File(System.getProperty("java.io.tmpdir"), "movilidad-csv-" + System.nanoTime());
        File archivoComun = new File(carpeta, "ocupado.txt");
        try {
            ExportadorCSV exportador = new ExportadorCSV();
            File destino = new File(carpeta, "reportes" + File.separator + "ejemplo.csv");
            exportador.exportar(reporteDeEjemplo(), destino.getPath());

            v.verificarVerdadero("Exportar: crea la carpeta y el archivo", destino.isFile());
            try (FileInputStream bytes = new FileInputStream(destino)) {
                v.verificarVerdadero("Exportar: el archivo empieza con la marca BOM de UTF-8 (EF BB BF)",
                        bytes.read() == 0xEF && bytes.read() == 0xBB && bytes.read() == 0xBF);
            }
            List<String> leidas = leerLineas(destino);
            v.verificarNumero("Exportar: el salto de línea dentro de comillas ocupa 2 líneas del archivo", 6, leidas.size());
            v.verificarIgual("Exportar: la primera línea es la de encabezados", "\uFEFFNombre;Nota", leidas.get(0));
            v.verificarIgual("Exportar: se conservan las tildes (UTF-8)", "'=SUMA(1);\"línea1", leidas.get(3));

            Reporte corto = new Reporte("Corto", "X");
            corto.agregarFila("1");
            exportador.exportar(corto, destino.getPath());
            v.verificarNumero("Exportar: reemplaza el archivo anterior", 2, leerLineas(destino).size());

            archivoComun.createNewFile();
            v.verificarExcepcion("Exportar: si no se puede escribir, falla con PersistenciaExcepcion", PersistenciaExcepcion.class,
                    () -> exportador.exportar(corto, archivoComun.getPath() + File.separator + "x.csv"));
        } catch (IOException e) {
            v.verificarVerdadero("No se pudo leer o crear un archivo de prueba: " + e.getMessage(), false);
        } finally {
            borrar(carpeta);
        }
    }

    private static List<String> leerLineas(File archivo) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }
        }
        return lineas;
    }

    private static void borrar(File archivo) {
        File[] hijos = archivo.listFiles();
        if (hijos != null) {
            for (File hijo : hijos) {
                borrar(hijo);
            }
        }
        archivo.delete();
    }
}