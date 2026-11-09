package VehiculoMantenimiento;

import java.time.LocalDate;

public class BicicletaElectrica extends VehiculoElectrico {
    private final double autonomiaKm;

    public BicicletaElectrica(String codigo, LocalDate fechaAlta, int nivelBateria, double autonomiaKm) {
        super(codigo, fechaAlta, nivelBateria);
        if (autonomiaKm <= 0) {
            throw new IllegalArgumentException("La autonomía debe ser mayor a cero");
        }
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
