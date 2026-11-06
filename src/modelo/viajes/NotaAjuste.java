package modelo.viajes;

import java.time.LocalDateTime;
import modelo.Operador;

public class NotaAjuste {
    private final LocalDateTime fecha;
    private final double importe;
    private final String motivo;
    private final Operador operador;

    public NotaAjuste(Operador operador, LocalDateTime fecha, double importe, String motivo) {
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
