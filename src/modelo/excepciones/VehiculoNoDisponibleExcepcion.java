package modelo.excepciones;

// RN07: se lanza cuando se intenta usar un vehículo que no está disponible.

public class VehiculoNoDisponibleExcepcion extends RuntimeException {
    public VehiculoNoDisponibleExcepcion(String message) {
        super(message);
    }
}
