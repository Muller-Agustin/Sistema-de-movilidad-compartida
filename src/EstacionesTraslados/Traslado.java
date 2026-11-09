package EstacionesTraslados;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import PersonalServicio.Operador;
import VehiculoMantenimiento.Vehiculo;

// RF23: registro de una redistribución de vehículos entre dos estaciones.

public class Traslado implements Serializable {
    private static final long serialVersionUID = 1L;
    private Estacion origen;
    private Estacion destino;
    private List<Vehiculo> vehiculos;
    private LocalDateTime fecha;
    private Operador operador;

    public Traslado(Estacion origen, Estacion destino, List<Vehiculo> vehiculos, LocalDateTime fecha, Operador operador) {
        this.origen = origen;
        this.destino = destino;
        this.vehiculos = new ArrayList<>(vehiculos);
        this.fecha = fecha;
        this.operador = operador;
    }

    public Estacion getOrigen() {
        return origen;
    }

    public Estacion getDestino() {
        return destino;
    }

    public List<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Operador getOperador() {
        return operador;
    }

    @Override
    public String toString() {
        return fecha + " - " + origen.getCodigo() + " -> " + destino.getCodigo()
                + " (" + vehiculos.size() + " vehículos, " + operador.getNombre() + ")";
    }
}