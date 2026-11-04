package modelo.estaciones;

import java.time.LocalDateTime;
import java.util.List;
import modelo.Operador;
import modelo.Vehiculo;

public class Traslado {
    private final Estacion origen;
    private final Estacion destino;
    private final List<Vehiculo> vehiculos;
    private final LocalDateTime fecha;
    private final Operador operador;

    public Traslado(Estacion origen, Estacion destino, List<Vehiculo> vehiculos, LocalDateTime fecha, Operador operador) {
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
