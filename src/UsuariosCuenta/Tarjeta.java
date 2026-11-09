package UsuariosCuenta;

// Medio de pago: tarjeta. Solo se guardan el titular y los últimos 4 dígitos.

public class Tarjeta extends MedioPago {
    private static final long serialVersionUID = 1L;
    private String titular;
    private String ultimosDigitos;

    public Tarjeta(String titular, String ultimosDigitos) {
        this.titular = titular;
        this.ultimosDigitos = ultimosDigitos;
    }

    public String getTitular() {
        return titular;
    }

    public String getUltimosDigitos() {
        return ultimosDigitos;
    }

    @Override
    public String getDescripcion() {
        return "tarjeta terminada en " + ultimosDigitos;
    }
}