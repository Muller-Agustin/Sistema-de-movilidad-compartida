package UsuariosCuenta;

import java.time.LocalDateTime;

// RF03: carga de saldo con un medio de pago registrado. Suma al saldo.

public class Recarga extends Movimiento {
    private static final long serialVersionUID = 1L;
    private MedioPago medio;

    public Recarga(LocalDateTime fecha, double importe, MedioPago medio) {
        super(fecha, importe, "Recarga con " + medio.getDescripcion());
        this.medio = medio;
    }

    public MedioPago getMedio() {
        return medio;
    }

    @Override
    public double getEfectoSobreSaldo() {
        return getImporte();
    }

    @Override
    public String getTipo() {
        return "RECARGA";
    }
}