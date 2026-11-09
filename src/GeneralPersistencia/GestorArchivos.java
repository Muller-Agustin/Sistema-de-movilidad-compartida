package GeneralPersistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Fabricación pura (GRASP) y SRP: se ocupa solo de guardar y cargar los datos en un archivo,
// así las clases del negocio no saben nada de archivos.
// Los datos se guardan en binario (serialización de SistemaMovilidad).

public class GestorArchivos {
    private String rutaArchivo;

    public GestorArchivos(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public boolean existenDatos() {
        return new File(rutaArchivo).exists();
    }

    // Guarda todo el sistema. Si la carpeta no existe, la crea.
    public void guardar(SistemaMovilidad sistema) throws IOException {
        File archivo = new File(rutaArchivo);
        if (archivo.getParentFile() != null) {
            archivo.getParentFile().mkdirs();
        }
        ObjectOutputStream salida = null;
        try {
            salida = new ObjectOutputStream(new FileOutputStream(archivo));
            salida.writeObject(sistema);
        } finally {
            // finally se ejecuta siempre, haya error o no: así el archivo nunca queda abierto
            if (salida != null) {
                salida.close();
            }
        }
    }

    // Carga el sistema guardado. Si todavía no hay archivo, o no se puede leer,
    // devuelve un sistema nuevo y vacío.
    public SistemaMovilidad cargar() {
        if (!existenDatos()) {
            return new SistemaMovilidad();
        }
        ObjectInputStream entrada = null;
        try {
            entrada = new ObjectInputStream(new FileInputStream(rutaArchivo));
            return (SistemaMovilidad) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No se pudieron leer los datos guardados (" + e.getMessage() + "). Se empieza con datos nuevos.");
            return new SistemaMovilidad();
        } finally {
            if (entrada != null) {
                try {
                    entrada.close();
                } catch (IOException e) {
                    System.out.println("No se pudo cerrar el archivo: " + e.getMessage());
                }
            }
        }
    }
}