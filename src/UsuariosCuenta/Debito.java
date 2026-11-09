package UsuariosCuenta;

import java.time.LocalDateTime;

public class Debito extends Movimiento {

    public Debito(LocalDateTime fecha, double importe, String concepto) {
        super(fecha, importe, concepto);
        if (importe <= 0) {
            throw new IllegalArgumentException("El importe del débito debe ser mayor a cero");
        }
    }

    @Override
    public double getEfectoSobreSaldo() { return -getImporte(); }

    @Override
    public String getTipo() { return "DEBITO"; }
}