package modelo.estaciones;

import java.util.ArrayList;
import java.util.List;
import modelo.Vehiculo;

public class Estacion {
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

    public String getCodigo() {
        return codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public List<Vehiculo> getVehiculos() {
        return List.copyOf(vehiculos);
    }

    public List<AlertaRedistribucion> getAlertasRedistribucion() {
        return List.copyOf(alertasRedistribucion);
    }

    public boolean estaLlena() {
        return false;
    }

    public double getPorcentajeOcupacion() {
        return 0.0;
    }

    public void recibirVehiculo(Vehiculo v) {
    }

    public void liberarVehiculo(Vehiculo v) {
    }
}
