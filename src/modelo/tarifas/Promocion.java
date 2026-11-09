package modelo.tarifas;

import java.time.LocalDate;
import modelo.viajes.Viaje;

// RF17: una promoción tiene nombre y período de vigencia. Cada tipo define cuándo aplica y cuánto descuenta.
public abstract class Promocion {
    private final String nombre;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    protected Promocion(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (fechaInicio == null || fechaFin == null || fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("El período de vigencia no es válido");
        }
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

    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    public abstract boolean esAplicable(Viaje v);

    // Cuánto dinero se descuenta del costo del viaje (nunca más que el costo)
    public abstract double calcularDescuento(Viaje v);

    // Se llama cuando la promoción fue la elegida para cobrar el viaje. Por defecto no hace nada.
    public void registrarUso(Viaje v) {
    }

    // costo del viaje sin ninguna promoción
    protected double costoSinPromocion(Viaje v) {
        return v.getTarifa().calcularCosto(v.getMinutosCobrables());
    }
}