package UsuariosCuenta;

import java.time.LocalDateTime;

// Un movimiento de la cuenta del usuario (RF04). Ordenados por fecha (orden natural).
public abstract class Movimiento implements Comparable<Movimiento> {
    private final LocalDateTime fecha;
    private final double importe;
    private final String concepto;

    protected Movimiento(LocalDateTime fecha, double importe, String concepto) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }
        this.fecha = fecha;
        this.importe = importe;
        this.concepto = concepto;
    }

    // Cuánto suma (+) o resta (-) este movimiento al saldo. Cada tipo lo define a su manera.
    public abstract double getEfectoSobreSaldo();

    public abstract String getTipo();

    public LocalDateTime getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public String getConcepto() { return concepto; }

    @Override
    public int compareTo(Movimiento otro) {
        return fecha.compareTo(otro.fecha);
    }

    @Override
    public String toString() {
        return fecha + " | " + getTipo() + " | " + getEfectoSobreSaldo() + " | " + concepto;
    }
}