package modelo.estaciones;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.vehiculos.Vehiculo;

public class Estacion {
    private static final double OCUPACION_MINIMA = 20;
    private static final double OCUPACION_MAXIMA = 90;

    private final String codigo;
    private final String direccion;
    private final int capacidadMaxima;
    private final Coordenada coordenada;
    private final List<Vehiculo> vehiculos;
    private final List<AlertaRedistribucion> alertasRedistribucion;

    public Estacion(String codigo, String direccion, int capacidadMaxima, Coordenada coordenada) {
        this.codigo = codigo;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.coordenada = coordenada;
        this.vehiculos = new ArrayList<>();
        this.alertasRedistribucion = new ArrayList<>();
    }

    // GETTERS
    public String getCodigo() { return codigo; }
    public String getDireccion() { return direccion; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public Coordenada getCoordenada() { return coordenada; }
    public List<Vehiculo> getVehiculos() { return List.copyOf(vehiculos); }
    public List<AlertaRedistribucion> getAlertasRedistribucion() { return List.copyOf(alertasRedistribucion); }


    // true si ya no entran mas vehiculos
    public boolean estaLlena() {
        return vehiculos.size() >= capacidadMaxima;
    }

    // porcentaje de vehiculos guardados sobre la capacidad
    public double getPorcentajeOcupacion() {
        return vehiculos.size() * 100.0 / capacidadMaxima;
    }

    // guarda el vehiculo en la estacion si hay lugar
    public void recibirVehiculo(Vehiculo v) {
        if (estaLlena()) {
            throw new IllegalStateException("La estación está llena");
        }
        vehiculos.add(v);
        v.setEstacion(this);
        verificarOcupacion();
    }

    // saca el vehiculo de la estacion (retiro o traslado)
    public void liberarVehiculo(Vehiculo v) {
        if (!vehiculos.remove(v)) {
            throw new IllegalArgumentException("El vehículo no está en esta estación");
        }
        v.setEstacion(null);
        verificarOcupacion();
    }

    // genera una alerta si la ocupacion es menor al 20% o mayor al 90%
    private void verificarOcupacion() {
        double ocupacion = getPorcentajeOcupacion();
        if (ocupacion < OCUPACION_MINIMA) {
            alertasRedistribucion.add(new AlertaRedistribucion(this, TipoAlerta.VACIA, LocalDateTime.now()));
        } else if (ocupacion > OCUPACION_MAXIMA) {
            alertasRedistribucion.add(new AlertaRedistribucion(this, TipoAlerta.SATURADA, LocalDateTime.now()));
        }
    }
}
