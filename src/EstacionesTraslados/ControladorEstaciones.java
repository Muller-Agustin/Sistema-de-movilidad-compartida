package EstacionesTraslados;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Excepciones.EstacionLlenaExcepcion;
import PersonalServicio.Operador;
import VehiculoMantenimiento.Vehiculo;

// Controlador (GRASP) del módulo de estaciones: alta y consulta de estaciones,
// alertas de redistribución y traslados de vehículos.

public class ControladorEstaciones implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Estacion> estaciones;
    private List<Traslado> traslados;

    public ControladorEstaciones() {
        this.estaciones = new ArrayList<>();
        this.traslados = new ArrayList<>();
    }

    // RF22: falla si el código ya existe
    public void registrarEstacion(Estacion estacion) {
        if (buscarEstacion(estacion.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe una estación con el código " + estacion.getCodigo());
        }
        estaciones.add(estacion);
    }

    // Devuelve null si no existe
    public Estacion buscarEstacion(String codigo) {
        for (Estacion e : estaciones) {
            if (e.getCodigo().equals(codigo)) {
                return e;
            }
        }
        return null;
    }

    // RN16: todas las alertas de redistribución de todas las estaciones
    public List<AlertaRedistribucion> listarAlertas() {
        List<AlertaRedistribucion> todas = new ArrayList<>();
        for (Estacion e : estaciones) {
            todas.addAll(e.getAlertas());
        }
        return todas;
    }

    // RF23: el operador traslada vehículos de una estación a otra
    public Traslado redistribuir(Operador operador, Estacion origen, Estacion destino, List<Vehiculo> vehiculos)
            throws EstacionLlenaExcepcion {
        Traslado traslado = operador.redistribuir(origen, destino, vehiculos, LocalDateTime.now());
        traslados.add(traslado);
        return traslado;
    }

    public List<Estacion> getEstaciones() {
        return estaciones;
    }

    public List<Traslado> getTraslados() {
        return traslados;
    }
}