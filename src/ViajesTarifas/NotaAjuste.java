package ViajesTarifas;

import java.time.LocalDateTime;

import PersonalServicio.Operador;

// RF14 + RN24 + S16: corrección que un operador hace sobre un viaje ya cerrado.
// Importe positivo = crédito para el usuario, negativo = débito.
public class NotaAjuste {
    private final LocalDateTime fecha;
    private final double importe;
    private final String motivo;
    private final Operador operador;

    public NotaAjuste(Operador operador, LocalDateTime fecha, double importe, String motivo) {
        if (operador == null || fecha == null) {
            throw new IllegalArgumentException("El operador y la fecha son obligatorios");
        }
        if (importe == 0) {
            throw new IllegalArgumentException("El importe del ajuste no puede ser cero");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }
        this.operador = operador;
        this.fecha = fecha;
        this.importe = importe;
        this.motivo = motivo;
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

    public Operador getOperador() {
        return operador;
    }
}