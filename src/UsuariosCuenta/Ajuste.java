package UsuariosCuenta;

import java.time.LocalDateTime;

// RN24: corrección de un viaje cerrado hecha por el operador.
// Importe positivo = crédito a favor del usuario; negativo = débito.

public class Ajuste extends Movimiento {
    private static final long serialVersionUID = 1L;

    public Ajuste(LocalDateTime fecha, double importe, String motivo) {
        super(fecha, importe, motivo);
    }

    @Override
    public double getEfectoSobreSaldo() {
        return getImporte();
    }

    @Override
    public String getTipo() {
        return "AJUSTE";
    }
}