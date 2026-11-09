package UsuariosCuenta;

import java.time.LocalDateTime;

// Cobro de un viaje o de la suscripción al plan. Resta del saldo.

public class Debito extends Movimiento {
    private static final long serialVersionUID = 1L;

    public Debito(LocalDateTime fecha, double importe, String concepto) {
        super(fecha, importe, concepto);
    }

    @Override
    public double getEfectoSobreSaldo() {
        return -getImporte();
    }

    @Override
    public String getTipo() {
        return "DEBITO";
    }
}