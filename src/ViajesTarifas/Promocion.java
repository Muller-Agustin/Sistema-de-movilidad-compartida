package ViajesTarifas;

import java.io.Serializable;
import java.time.LocalDate;

// RF17: una promoción tiene nombre y período de vigencia. Es abstracta: cada tipo de
// promoción define cuándo aplica y cuánto descuenta.
// OCP: para agregar una promoción nueva se crea otra subclase, sin tocar Viaje.

public abstract class Promocion implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public Promocion(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    // Polimorfismo (GRASP): cada subclase responde a su manera
    public abstract boolean esAplicable(Viaje viaje);

    // Cuánto dinero se descuenta del costo del viaje
    public abstract double calcularDescuento(Viaje viaje);

    // Se llama cuando la promoción fue la elegida. Por defecto no hace nada.
    public void registrarUso(Viaje viaje) {
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

    @Override
    public String toString() {
        return nombre;
    }
}