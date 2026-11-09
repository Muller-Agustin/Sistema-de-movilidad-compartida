package modelo.datos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.excepciones.PersistenciaExcepcion;

// Base de los repositorios que guardan en archivos de texto: una línea por dato, campos separados por ";".
// Acá está todo el manejo de archivos; cada subclase solo sabe pasar su objeto a texto y de texto a objeto.
public abstract class RepositorioArchivo<T> implements Repositorio<T> {
    protected static final String SEPARADOR = ";";
    protected static final String VACIO = "-";

    private final String carpeta;

    // Convierte los campos de una línea en un objeto
    protected interface Convertidor<R> {
        R convertir(String[] campos);
    }

    // Hace algo con los campos de una línea (por ejemplo, aplicarlos sobre un objeto ya creado)
    protected interface Aplicador {
        void aplicar(String[] campos);
    }

    protected RepositorioArchivo(String carpeta) {
        this.carpeta = carpeta;
    }

    // Lee el archivo línea por línea. Si todavía no existe, no hay datos y no hace nada.
    // Si una línea es inválida, avisa en qué archivo y en qué línea está el problema.
    protected void recorrer(String nombreArchivo, Aplicador aplicador) {
        String ruta = carpeta + File.separator + nombreArchivo;
        int numeroLinea = 0;
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (!linea.isBlank()) {
                    aplicador.aplicar(linea.split(SEPARADOR, -1));
                }
            }
        } catch (FileNotFoundException e) {
            // el archivo todavía no existe: no hay nada guardado
        } catch (IOException e) {
            throw new PersistenciaExcepcion("Error al leer " + ruta + ": " + e.getMessage());
        } catch (RuntimeException e) {
            throw new PersistenciaExcepcion("Archivo " + ruta + ", línea " + numeroLinea + ": " + e.getMessage());
        }
    }

    protected <R> List<R> leer(String nombreArchivo, Convertidor<R> convertidor) {
        List<R> resultado = new ArrayList<>();
        recorrer(nombreArchivo, campos -> resultado.add(convertidor.convertir(campos)));
        return resultado;
    }

    // Reemplaza el contenido del archivo por las líneas indicadas (crea la carpeta si hace falta)
    protected void escribir(String nombreArchivo, List<String> lineas) {
        new File(carpeta).mkdirs();
        String ruta = carpeta + File.separator + nombreArchivo;
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ruta, StandardCharsets.UTF_8))) {
            for (String linea : lineas) {
                escritor.write(linea);
                escritor.newLine();
            }
        } catch (IOException e) {
            throw new PersistenciaExcepcion("Error al guardar " + ruta + ": " + e.getMessage());
        }
    }

    // Arma una línea con los campos separados por ";". Un campo null o vacío se guarda como "-", y los ";"
    // y saltos de línea del texto se reemplazan para que no rompan el formato.
    protected static String unir(Object... campos) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                linea.append(SEPARADOR);
            }
            linea.append(limpiar(campos[i]));
        }
        return linea.toString();
    }

    private static String limpiar(Object campo) {
        if (campo == null) {
            return VACIO;
        }
        String texto = String.valueOf(campo).replace(SEPARADOR, ",").replace("\r", " ").replace("\n", " ");
        return texto.isEmpty() ? VACIO : texto;
    }

    protected static boolean esVacio(String campo) {
        return campo.equals(VACIO);
    }

    protected static String textoONull(String campo) {
        return esVacio(campo) ? null : campo;
    }

    protected static LocalDateTime fechaHoraONull(String campo) {
        return esVacio(campo) ? null : LocalDateTime.parse(campo);
    }
}