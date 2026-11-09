package Excepciones;

// RN01, RN02 y RN04: se lanza cuando un usuario no puede retirar un vehículo.

public class RetiroNoPermitidoExcepcion extends Exception {
    public RetiroNoPermitidoExcepcion(String mensaje) {
        super(mensaje);
    }
}