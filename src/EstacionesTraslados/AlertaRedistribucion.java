package EstacionesTraslados;

import java.time.LocalDateTime;

public class AlertaRedistribucion {
    private final Estacion estacion;
    private final TipoAlerta tipo;
    private final LocalDateTime fecha;

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
}
