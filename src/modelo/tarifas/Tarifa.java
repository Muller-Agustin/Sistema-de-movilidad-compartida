package modelo.tarifas;

import java.time.LocalDate;
import modelo.TipoVehiculo;

public class Tarifa {
    private final TipoVehiculo tipoVehiculo;
    private final double cargoDesbloqueo;
    private final double precioPorMinuto;
    private final LocalDate fechaVigencia;

    public Tarifa(TipoVehiculo tipoVehiculo, double cargoDesbloqueo, double precioPorMinuto, LocalDate fechaVigencia) {
        this.tipoVehiculo = tipoVehiculo;
        this.cargoDesbloqueo = cargoDesbloqueo;
        this.precioPorMinuto = precioPorMinuto;
        this.fechaVigencia = fechaVigencia;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public double getCargoDesbloqueo() {
        return cargoDesbloqueo;
    }

    public double getPrecioPorMinuto() {
        return precioPorMinuto;
    }

    public LocalDate getFechaVigencia() {
        return fechaVigencia;
    }

    public double calcularCosto(int minutos) {
        return 0.0;
    }
}
