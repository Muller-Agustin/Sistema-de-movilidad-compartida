package modelo.tarifas;

import java.time.LocalDate;
import modelo.estaciones.Estacion;
import modelo.viajes.EstadoViaje;
import modelo.viajes.Viaje;

// RF30 (S17): si el usuario devuelve el vehículo en una estación que estaba por debajo del
// 20% de ocupación, se descuenta el 20% del costo del viaje. Ayuda a redistribuir vehículos.
public class PromoEstacionBajaOcupacion extends Promocion {
    public static final double OCUPACION_MAXIMA_PARA_DESCUENTO = 20;
    public static final double PORCENTAJE_DESCUENTO = 0.20;

    public PromoEstacionBajaOcupacion(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        super(nombre, fechaInicio, fechaFin);
    }

    @Override
    public boolean esAplicable(Viaje v) {
        return v.getEstado() == EstadoViaje.FINALIZADO
                && estaVigente(v.getInicio().toLocalDate())
                && v.getDevolucion() != null
                && ocupacionAntesDeDevolver(v) < OCUPACION_MAXIMA_PARA_DESCUENTO;
    }

    // al terminar el viaje el vehículo ya está en la estación, así que se lo descuenta
    private double ocupacionAntesDeDevolver(Viaje v) {
        Estacion estacion = v.getDevolucion();
        int vehiculos = estacion.getVehiculos().size();
        if (estacion.getVehiculos().contains(v.getVehiculo())) {
            vehiculos--;
        }
        return vehiculos * 100.0 / estacion.getCapacidadMaxima();
    }

    @Override
    public double calcularDescuento(Viaje v) {
        if (!esAplicable(v)) {
            return 0.0;
        }
        return costoSinPromocion(v) * PORCENTAJE_DESCUENTO;
    }
}