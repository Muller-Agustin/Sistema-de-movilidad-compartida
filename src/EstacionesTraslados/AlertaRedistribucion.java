package EstacionesTraslados;

import java.io.Serializable;
import java.time.LocalDateTime;

// RN16: aviso para el operador de que una estación quedó casi vacía o casi llena.

public class AlertaRedistribucion implements Serializable {
    private static final long serialVersionUID = 1L;
    private Estacion estacion;
    private TipoAlerta tipo;
    private LocalDateTime fecha;

    public AlertaRedistribucion(Estacion estacion, TipoAlerta tipo, LocalDateTime fecha) {
        this.estacion = estacion;
        this.tipo = tipo;
        this.fecha = fecha;
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return fecha + " - estación " + estacion.getCodigo() + " " + tipo;
    }
}