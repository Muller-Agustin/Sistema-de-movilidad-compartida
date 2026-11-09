package UsuariosCuenta;

import java.time.LocalDateTime;

// S16: corrección de un viaje cerrado. El importe es positivo (crédito) o negativo (débito).
public class Ajuste extends Movimiento {

    public Ajuste(LocalDateTime fecha, double importe, String motivo) {
        super(fecha, importe, motivo);
        if (importe == 0) {
            throw new IllegalArgumentException("El importe del ajuste no puede ser cero");
        }
    }

    @Override
    public double getEfectoSobreSaldo() { return getImporte(); }

    @Override
    public String getTipo() { return "AJUSTE"; }
}