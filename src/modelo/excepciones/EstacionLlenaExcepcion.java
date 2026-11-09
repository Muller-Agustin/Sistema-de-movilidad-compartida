package modelo.excepciones;

// RN13: se lanza cuando se intenta dejar un vehículo en una estación que ya no tiene lugar.
public class EstacionLlenaExcepcion extends RuntimeException {
    public EstacionLlenaExcepcion(String message) {
        super(message);
    }
}