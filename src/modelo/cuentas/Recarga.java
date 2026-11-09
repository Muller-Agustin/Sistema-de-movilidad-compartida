package modelo.cuentas;

import java.time.LocalDateTime;

public class Recarga extends Movimiento {
    private final MedioPago medio;

    public Recarga(LocalDateTime fecha, double importe, MedioPago medio) {
        super(fecha, importe, "Recarga con " + descripcionDe(medio));
        if (importe <= 0) {
            throw new IllegalArgumentException("El importe de la recarga debe ser mayor a cero");
        }
        this.medio = medio;
    }

    private static String descripcionDe(MedioPago medio) {
        if (medio == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio");
        }
        return medio.getDescripcion();
    }

    public MedioPago getMedio() { return medio; }

    @Override
    public double getEfectoSobreSaldo() { return getImporte(); }

    @Override
    public String getTipo() { return "RECARGA"; }
}