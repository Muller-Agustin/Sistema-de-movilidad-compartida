package modelo.datos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import modelo.excepciones.PersistenciaExcepcion;
import modelo.reportes.Reporte;

// RF32 (exportar): guarda un reporte en un archivo CSV. Usa ";" como separador y UTF-8 con marca BOM,
// que es lo que necesita Excel en español para abrir bien las tildes y separar las columnas.
public class ExportadorCSV {
    public static final String SEPARADOR = ";";
    private static final char BOM = '\uFEFF';

    // Las líneas del archivo: primero los encabezados y después una línea por fila (sin el título)
    public List<String> lineas(Reporte reporte) {
        List<String> lineas = new ArrayList<>();
        lineas.add(armarLinea(reporte.getEncabezados()));
        for (List<String> fila : reporte.getFilas()) {
            lineas.add(armarLinea(fila));
        }
        return lineas;
    }

    public String aTexto(Reporte reporte) {
        StringBuilder texto = new StringBuilder();
        for (String linea : lineas(reporte)) {
            texto.append(linea).append("\n");
        }
        return texto.toString();
    }

    // Crea la carpeta si hace falta y reemplaza el archivo si ya existía
    public void exportar(Reporte reporte, String ruta) {
        File archivo = new File(ruta);
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
            escritor.write(BOM);
            for (String linea : lineas(reporte)) {
                escritor.write(linea);
                escritor.newLine();
            }
        } catch (IOException e) {
            throw new PersistenciaExcepcion("Error al exportar " + ruta + ": " + e.getMessage());
        }
    }

    private String armarLinea(List<String> celdas) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < celdas.size(); i++) {
            if (i > 0) {
                linea.append(SEPARADOR);
            }
            linea.append(escapar(celdas.get(i)));
        }
        return linea.toString();
    }

    // Un campo con ";", comillas o saltos de línea va entre comillas (y las comillas se duplican).
    // Si empieza con = + @ Excel lo tomaría como una fórmula, así que se le antepone un apóstrofe.
    private String escapar(String campo) {
        if (campo.startsWith("=") || campo.startsWith("+") || campo.startsWith("@")) {
            campo = "'" + campo;
        }
        boolean necesitaComillas = campo.contains(SEPARADOR) || campo.contains("\"")
                || campo.contains("\n") || campo.contains("\r");
        if (necesitaComillas) {
            return "\"" + campo.replace("\"", "\"\"") + "\"";
        }
        return campo;
    }
}