package Excepciones;

// Se lanza cuando el usuario no tiene saldo suficiente para una operación.

public class SaldoInsuficienteExcepcion extends Exception {
    public SaldoInsuficienteExcepcion(String mensaje) {
        super(mensaje);
    }
}