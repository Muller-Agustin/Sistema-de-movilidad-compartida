package UsuariosCuenta;

public class Tarjeta extends MedioPago {
    private final String titular;
    private final String ultimosDigitos;

    public Tarjeta(String titular, String ultimosDigitos) {
        if (titular == null || titular.isBlank()) {
            throw new IllegalArgumentException("El titular es obligatorio");
        }
        if (ultimosDigitos == null || !ultimosDigitos.matches("\\d{4}")) {
            throw new IllegalArgumentException("Se deben indicar los últimos 4 dígitos de la tarjeta");
        }
        this.titular = titular;
        this.ultimosDigitos = ultimosDigitos;
    }

    public String getTitular() { return titular; }
    public String getUltimosDigitos() { return ultimosDigitos; }

    @Override
    public String getDescripcion() {
        return "tarjeta terminada en " + ultimosDigitos;
    }
}