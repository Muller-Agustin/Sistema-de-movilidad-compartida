package modelo.excepciones;

public class RetiroNoPermitidoExcepcion extends RuntimeException {
    public RetiroNoPermitidoExcepcion(String message) {
        super(message);
    }
}
// RN01 RN02 y RN04: se lanza cuando un usuario no puede retirar un vehículo.
