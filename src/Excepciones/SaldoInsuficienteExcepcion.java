package Excepciones;

public class SaldoInsuficienteExcepcion extends RuntimeException {
    public SaldoInsuficienteExcepcion(String message) {
        super(message);
    }
}
// Se lanza cuando el usuario no tiene saldo para una operacion
