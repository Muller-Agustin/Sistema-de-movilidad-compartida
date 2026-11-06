package modelo;

import modelo.vehiculos.Vehiculo;

import java.util.ArrayList;
import java.util.List;

public class Estacion {
    private int capacidadMaxima;
    private Coordenada coordenada;
    private List<Vehiculo> vehiculos;
    private List<AlertaRedistribucion> alertasRedistribucion;

    public Estacion(int capacidadMaxima, Coordenada coordenada) {
        this.capacidadMaxima = capacidadMaxima;
        this.coordenada = coordenada;
        this.vehiculos = new ArrayList<>();
        this.alertasRedistribucion = new ArrayList<>();
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public void setCoordenada(Coordenada coordenada) {
        this.coordenada = coordenada;
    }

    public List<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public void setVehiculos(List<Vehiculo> vehiculos) {
        this.vehiculos = vehiculos;
    }

    public List<AlertaRedistribucion> getAlertasRedistribucion() {
        return alertasRedistribucion;
    }

    public void setAlertasRedistribucion(List<AlertaRedistribucion> alertasRedistribucion) {
        this.alertasRedistribucion = alertasRedistribucion;
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
