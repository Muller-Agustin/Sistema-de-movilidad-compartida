package ViajesTarifas;

import java.io.Serializable;
import java.time.LocalDate;

import VehiculoMantenimiento.TipoVehiculo;

// RN17: la tarifa es un cargo de desbloqueo más un precio por minuto, según el tipo de vehículo.
// Cada tarifa rige desde una fecha (RF16).

public class Tarifa implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int LIMITE_MINUTOS = 120;        // RN19: tiempo máximo normal de un viaje
    public static final double RECARGO_EXCESO = 0.5;     // RN19: 50 % sobre los minutos excedentes
    public static final int MINUTOS_SALDO_MINIMO = 30;   // RN01: saldo mínimo = 30 minutos de uso

    private TipoVehiculo tipoVehiculo;
    private double cargoDesbloqueo;
    private double precioPorMinuto;
    private LocalDate fechaVigencia;

    public Tarifa(TipoVehiculo tipoVehiculo, double cargoDesbloqueo, double precioPorMinuto, LocalDate fechaVigencia) {
        this.tipoVehiculo = tipoVehiculo;
        this.cargoDesbloqueo = cargoDesbloqueo;
        this.precioPorMinuto = precioPorMinuto;
        this.fechaVigencia = fechaVigencia;
    }

    // Hasta 120 minutos se cobra el precio normal; los minutos que pasan de 120 tienen 50 % de recargo
    public double calcularCosto(int minutos) {
        int normales = Math.min(minutos, LIMITE_MINUTOS);
        int excedentes = minutos - normales;
        return cargoDesbloqueo
                + precioPorMinuto * normales
                + precioPorMinuto * (1 + RECARGO_EXCESO) * excedentes;
    }

    // RN01 y S1: el saldo mínimo para retirar es el costo de un viaje de 30 minutos
    public double calcularSaldoMinimo() {
        return calcularCosto(MINUTOS_SALDO_MINIMO);
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

    @Override
    public String toString() {
        return tipoVehiculo + ": $" + cargoDesbloqueo + " + $" + precioPorMinuto + "/min (desde " + fechaVigencia + ")";
    }
}