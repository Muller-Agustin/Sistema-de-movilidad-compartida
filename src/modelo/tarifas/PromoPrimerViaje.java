package modelo.tarifas;

import java.time.LocalDate;
import modelo.viajes.Viaje;

public class PromoPrimerViaje extends Promocion {
    public PromoPrimerViaje(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        super(nombre, fechaInicio, fechaFin);
    }

    @Override
    public boolean esAplicable(Viaje v) {
        return false;
    }

    @Override
    public double calcularDescuento(Viaje v) {
        return 0.0;
    }
}
