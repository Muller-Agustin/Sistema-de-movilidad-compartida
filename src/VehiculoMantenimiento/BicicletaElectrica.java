package VehiculoMantenimiento;

import java.time.LocalDate;

// Bicicleta eléctrica: tiene batería (heredada) y una autonomía en km.

public class BicicletaElectrica extends VehiculoElectrico {
    private static final long serialVersionUID = 1L;
    private double autonomiaKm;

    public BicicletaElectrica(String codigo, LocalDate fechaAlta, int nivelBateria, double autonomiaKm) {
        super(codigo, fechaAlta, nivelBateria);
        this.autonomiaKm = autonomiaKm;
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.BICICLETA_ELECTRICA;
    }

    public double getAutonomiaKm() {
        return autonomiaKm;
    }
}