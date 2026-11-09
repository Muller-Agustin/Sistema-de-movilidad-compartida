package UsuariosCuenta;

import java.io.Serializable;
import java.time.LocalDateTime;

// RF04: movimiento de la cuenta del usuario (recarga, débito o ajuste).

public abstract class Movimiento implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDateTime fecha;
    private double importe;
    private String concepto;

    public Movimiento(LocalDateTime fecha, double importe, String concepto) {
        this.fecha = fecha;
        this.importe = importe;
        this.concepto = concepto;
    }

    // Polimorfismo (GRASP): cada tipo de movimiento decide si suma o resta al saldo.
    public abstract double getEfectoSobreSaldo();

    public abstract String getTipo();

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getImporte() {
        return importe;
    }

    public String getConcepto() {
        return concepto;
    }

    @Override
    public String toString() {
        return fecha + " | " + getTipo() + " | " + getEfectoSobreSaldo() + " | " + concepto;
    }
}