package Excepciones;

// RN13: se lanza cuando se intenta dejar un vehículo en una estación que ya no tiene lugar.

public class EstacionLlenaExcepcion extends Exception {
    public EstacionLlenaExcepcion(String mensaje) {
        super(mensaje);
    }
}