package excepciones;

public class VehiculoNoDisponibleException extends RuntimeException {
    public VehiculoNoDisponibleException(String mensaje) {
        super(mensaje);
    }

}
