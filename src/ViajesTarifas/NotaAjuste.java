package ViajesTarifas;

import java.io.Serializable;
import java.time.LocalDateTime;

import PersonalServicio.Operador;

// RN24: corrección que un operador hace sobre un viaje ya cerrado (el viaje no se modifica).
// Importe positivo = crédito para el usuario; negativo = débito.

public class NotaAjuste implements Serializable {
    private static final long serialVersionUID = 1L;
    private Operador operador;
    private LocalDateTime fecha;
    private double importe;
    private String motivo;

    public NotaAjuste(Operador operador, LocalDateTime fecha, double importe, String motivo) {
        this.operador = operador;
        this.fecha = fecha;
        this.importe = importe;
        this.motivo = motivo;
    }

    public Operador getOperador() {
        return operador;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getImporte() {
        return importe;
    }

    public String getMotivo() {
        return motivo;
    }

    @Override
    public String toString() {
        return fecha + " - " + importe + " - " + motivo + " (" + operador.getNombre() + ")";
    }
}