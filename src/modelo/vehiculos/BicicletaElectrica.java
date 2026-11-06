package modelo.vehiculos;

import java.time.LocalDate;

public class BicicletaElectrica extends VehiculoElectrico {
    private double autonomiaKm;

    public BicicletaElectrica(String codigo, LocalDate fechaAlta, int nivelBateria, double autonomiaKm) {
        super(codigo, fechaAlta, nivelBateria);
        if (autonomiaKm <= 0) {
            throw new IllegalArgumentException("La autonomía debe ser positiva");
        }
        this.autonomiaKm = autonomiaKm;
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.BICICLETA_ELECTRICA;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " - autonomía " + autonomiaKm + " km";
    }

    public double getAutonomiaKm() { return autonomiaKm; }
}