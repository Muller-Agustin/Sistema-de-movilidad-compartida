package modelo.tarifas;

import java.time.LocalDate;
import modelo.viajes.Viaje;

public abstract class Promocion {
    private final String nombre;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    protected Promocion(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public abstract boolean esAplicable(Viaje v);

    public abstract double calcularDescuento(Viaje v);
}
