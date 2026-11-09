package EstacionesTraslados;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Excepciones.EstacionLlenaExcepcion;
import VehiculoMantenimiento.Vehiculo;

// Punto fijo donde se retiran y devuelven los vehículos. Tiene una capacidad máxima
// y genera alertas de redistribución según su ocupación.

public class Estacion implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double OCUPACION_MINIMA = 20;
    private static final double OCUPACION_MAXIMA = 90;

    private String codigo;
    private String direccion;
    private int capacidadMaxima;
    private Coordenada coordenada;
    private List<Vehiculo> vehiculos;
    private List<AlertaRedistribucion> alertas;
    private TipoAlerta alertaActiva;

    public Estacion(String codigo, String direccion, int capacidadMaxima, Coordenada coordenada) {
        this.codigo = codigo;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.coordenada = coordenada;
        this.vehiculos = new ArrayList<>();
        this.alertas = new ArrayList<>();
    }

    public boolean estaLlena() {
        return vehiculos.size() >= capacidadMaxima;
    }

    public int getLugaresLibres() {
        return capacidadMaxima - vehiculos.size();
    }

    public double getPorcentajeOcupacion() {
        return vehiculos.size() * 100.0 / capacidadMaxima;
    }

    // Vehículos que un usuario podría retirar ahora (RN07 y RN08)
    public List<Vehiculo> getVehiculosDisponibles() {
        List<Vehiculo> disponibles = new ArrayList<>();
        for (Vehiculo v : vehiculos) {
            if (v.puedeRetirarse()) {
                disponibles.add(v);
            }
        }
        return disponibles;
    }

    // RN13: no se puede dejar un vehículo en una estación llena
    public void recibirVehiculo(Vehiculo v, LocalDateTime fecha) throws EstacionLlenaExcepcion {
        if (estaLlena()) {
            throw new EstacionLlenaExcepcion("La estación " + codigo + " está llena");
        }
        vehiculos.add(v);
        v.setEstacion(this);
        verificarOcupacion(fecha);
    }

    // Saca el vehículo de la estación (por un retiro o un traslado)
    public void liberarVehiculo(Vehiculo v, LocalDateTime fecha) {
        if (!vehiculos.remove(v)) {
            throw new IllegalArgumentException("El vehículo " + v.getCodigo() + " no está en la estación " + codigo);
        }
        v.setEstacion(null);
        verificarOcupacion(fecha);
    }

    // RN16: genera una alerta cuando la estación entra en zona de alerta (menos del 20 %
    // o más del 90 %). S9: mientras siga en esa zona no repite la alerta.
    private void verificarOcupacion(LocalDateTime fecha) {
        TipoAlerta tipo = null;
        if (getPorcentajeOcupacion() < OCUPACION_MINIMA) {
            tipo = TipoAlerta.VACIA;
        } else if (getPorcentajeOcupacion() > OCUPACION_MAXIMA) {
            tipo = TipoAlerta.SATURADA;
        }
        if (tipo != null && tipo != alertaActiva) {
            alertas.add(new AlertaRedistribucion(this, tipo, fecha));
        }
        alertaActiva = tipo;
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
        return vehiculos;
    }

    public List<AlertaRedistribucion> getAlertas() {
        return alertas;
    }

    @Override
    public String toString() {
        return codigo + " - " + direccion + " (" + vehiculos.size() + "/" + capacidadMaxima + ")";
    }
}