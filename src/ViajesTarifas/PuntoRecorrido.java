package ViajesTarifas;

import java.io.Serializable;
import java.time.LocalDateTime;

import EstacionesTraslados.Coordenada;

// Punto del recorrido registrado mientras el viaje está en curso (coordenada y hora).

public class PuntoRecorrido implements Serializable {
    private static final long serialVersionUID = 1L;
    private Coordenada coordenada;
    private LocalDateTime hora;

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