package modelo.tarifas;

import java.time.LocalDate;
import modelo.TipoVehiculo;

// RN17: la tarifa es un cargo de desbloqueo más un precio por minuto, según el tipo de vehículo.
public class Tarifa {
    public static final int LIMITE_MINUTOS = 120;        // RN19: tiempo máximo normal de un viaje
    public static final double RECARGO_EXCESO = 0.5;     // RN19 + S5: 50% sobre los minutos excedentes
    public static final int MINUTOS_SALDO_MINIMO = 30;   // RN01 + S1: saldo mínimo para retirar

    private final TipoVehiculo tipoVehiculo;
    private final double cargoDesbloqueo;
    private final double precioPorMinuto;
    private final LocalDate fechaVigencia;

    public Tarifa(TipoVehiculo tipoVehiculo, double cargoDesbloqueo, double precioPorMinuto, LocalDate fechaVigencia) {
        if (tipoVehiculo == null || fechaVigencia == null) {
            throw new IllegalArgumentException("El tipo de vehículo y la fecha de vigencia son obligatorios");
        }
        if (cargoDesbloqueo < 0 || precioPorMinuto < 0) {
            throw new IllegalArgumentException("Los precios no pueden ser negativos");
        }
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

    // Costo de un viaje de "minutos" minutos. Hasta 120 min se cobra normal; los minutos
    // que pasan de 120 se cobran con 50% de recargo.
    public double calcularCosto(int minutos) {
        if (minutos < 0) {
            throw new IllegalArgumentException("Los minutos no pueden ser negativos");
        }
        int normales = Math.min(minutos, LIMITE_MINUTOS);
        int excedentes = minutos - normales;
        return cargoDesbloqueo
                + precioPorMinuto * normales
                + precioPorMinuto * (1 + RECARGO_EXCESO) * excedentes;
    }

    // S1: el saldo mínimo para retirar es el desbloqueo más 30 minutos de este tipo de vehículo
    public double getSaldoMinimoParaRetirar() {
        return calcularCosto(MINUTOS_SALDO_MINIMO);
    }
}