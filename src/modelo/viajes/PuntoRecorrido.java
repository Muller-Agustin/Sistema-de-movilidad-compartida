package modelo.viajes;

import java.time.LocalDateTime;
import modelo.estaciones.Coordenada;

public class PuntoRecorrido {
    private final Coordenada coordenada;
    private final LocalDateTime hora;

    public PuntoRecorrido(Coordenada coordenada, LocalDateTime hora) {
        this.coordenada = coordenada;
        this.hora = hora;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public LocalDateTime getHora() {
        return hora;
    }
}
