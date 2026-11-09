package modelo.excepciones;

// Se lanza cuando un archivo de datos no se puede leer o guardar, o tiene un contenido inválido.
public class PersistenciaExcepcion extends RuntimeException {
    public PersistenciaExcepcion(String message) {
        super(message);
    }
}