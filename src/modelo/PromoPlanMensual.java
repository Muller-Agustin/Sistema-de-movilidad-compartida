package modelo;

import java.time.LocalDate;

public class PromoPlanMensual extends Promocion {
    public PromoPlanMensual(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
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
