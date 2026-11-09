package modelo.servicios;

import java.util.ArrayList;
import java.util.List;
import modelo.tarifas.Promocion;
import modelo.viajes.Viaje;

// GRASP Fabricación pura: concentra el cálculo de cuánto cuesta un viaje terminado.
public class CalculadoraDeTarifa {
    public static final double CARGO_VEHICULO_PERDIDO = 20000;   // RN20 + S4

    private final List<Promocion> promociones;

    public CalculadoraDeTarifa(List<Promocion> promociones) {
        this.promociones = new ArrayList<>(promociones);
    }

    // costo del viaje sin promociones: lo define la tarifa del viaje
    public double calcularSinPromocion(Viaje viaje) {
        return viaje.getTarifa().calcularCosto(viaje.getMinutosCobrables());
    }

    // RN23 + S7: las promociones no se acumulan. Se elige la que más descuenta, o null si ninguna aplica.
    public Promocion elegirPromocion(Viaje viaje) {
        Promocion mejor = null;
        double mejorDescuento = 0;
        for (Promocion p : promociones) {
            if (p.esAplicable(viaje)) {
                double descuento = p.calcularDescuento(viaje);
                if (descuento > mejorDescuento) {
                    mejor = p;
                    mejorDescuento = descuento;
                }
            }
        }
        return mejor;
    }

    // importe final a cobrar: costo sin promoción menos el descuento de la promoción elegida
    public double calcular(Viaje viaje) {
        double costo = calcularSinPromocion(viaje);
        Promocion elegida = elegirPromocion(viaje);
        if (elegida == null) {
            return costo;
        }
        return Math.max(0, costo - elegida.calcularDescuento(viaje));
    }

    // RN20: si el vehículo no se devuelve en 24 horas se cobra un cargo fijo
    public double getCargoVehiculoPerdido() {
        return CARGO_VEHICULO_PERDIDO;
    }
}