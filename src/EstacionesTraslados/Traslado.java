package EstacionesTraslados;

import java.time.LocalDateTime;
import java.util.List;

import PersonalServicio.Operador;
import VehiculoMantenimiento.Vehiculo;

// RF23: registro de una redistribución de vehículos entre dos estaciones.
public class Traslado {
    private final Estacion origen;
    private final Estacion destino;
    private final List<Vehiculo> vehiculos;
    private final LocalDateTime fecha;
    private final Operador operador;

    public Traslado(Estacion origen, Estacion destino, List<Vehiculo> vehiculos, LocalDateTime fecha, Operador operador) {
        if (origen == null || destino == null || operador == null || fecha == null) {
            throw new IllegalArgumentException("El origen, el destino, la fecha y el operador son obligatorios");
        }
        if (origen == destino) {
            throw new IllegalArgumentException("El origen y el destino deben ser distintos");
        }
        if (vehiculos == null || vehiculos.isEmpty()) {
            throw new IllegalArgumentException("Hay que trasladar al menos un vehículo");
        }
        this.origen = origen;
        this.destino = destino;
        this.vehiculos = List.copyOf(vehiculos);
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
}