package EstacionesTraslados;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import VehiculoMantenimiento.Vehiculo;

public class Estacion {
    private static final double OCUPACION_MINIMA = 20;
    private static final double OCUPACION_MAXIMA = 90;

    private final String codigo;
    private final String direccion;
    private final int capacidadMaxima;
    private final Coordenada coordenada;
    private final List<Vehiculo> vehiculos;
    private final List<AlertaRedistribucion> alertasRedistribucion;
    private TipoAlerta alertaActiva;

    public Estacion(String codigo, String direccion, int capacidadMaxima, Coordenada coordenada) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        if (capacidadMaxima <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a cero");
        }
        if (coordenada == null) {
            throw new IllegalArgumentException("La coordenada es obligatoria");
        }
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

    // vehículos que un usuario podría retirar ahora (RN07 y RN08)
    public List<Vehiculo> getVehiculosDisponibles() {
        List<Vehiculo> disponibles = new ArrayList<>();
        for (Vehiculo v : vehiculos) {
            if (v.puedeRetirarse()) {
                disponibles.add(v);
            }
        }
        return disponibles;
    }

    public void recibirVehiculo(Vehiculo v) {
        recibirVehiculo(v, LocalDateTime.now());
    }

    // guarda el vehiculo en la estacion si hay lugar
    public void recibirVehiculo(Vehiculo v, LocalDateTime fecha) {
        if (estaLlena()) {
            throw new IllegalStateException("La estación está llena");
        }
        vehiculos.add(v);
        v.setEstacion(this);
        verificarOcupacion(fecha);
    }

    public void liberarVehiculo(Vehiculo v) {
        liberarVehiculo(v, LocalDateTime.now());
    }

    // saca el vehiculo de la estacion (retiro o traslado)
    public void liberarVehiculo(Vehiculo v, LocalDateTime fecha) {
        if (!vehiculos.remove(v)) {
            throw new IllegalArgumentException("El vehículo no está en esta estación");
        }
        v.setEstacion(null);
        verificarOcupacion(fecha);
    }

    // Lo usa la persistencia al cargar los datos: guarda el vehículo sin generar alertas nuevas.
    public void restaurarVehiculo(Vehiculo v) {
        if (estaLlena()) {
            throw new IllegalStateException("La estación " + codigo + " no tiene lugar para el vehículo " + v.getCodigo());
        }
        vehiculos.add(v);
        v.setEstacion(this);
        alertaActiva = zonaDeOcupacion();
    }

    // RN16 + S9: genera una alerta cuando la estación entra en zona de alerta (menos del 20% o más
    // del 90%). Mientras siga en esa zona no repite la alerta: vuelve a avisar si sale y vuelve a entrar.
    private void verificarOcupacion(LocalDateTime fecha) {
        TipoAlerta tipo = zonaDeOcupacion();
        if (tipo != null && tipo != alertaActiva) {
            alertasRedistribucion.add(new AlertaRedistribucion(this, tipo, fecha));
        }
        alertaActiva = tipo;
    }

    // VACIA, SATURADA o null si la ocupación está en la zona normal
    private TipoAlerta zonaDeOcupacion() {
        double ocupacion = getPorcentajeOcupacion();
        if (ocupacion < OCUPACION_MINIMA) {
            return TipoAlerta.VACIA;
        }
        if (ocupacion > OCUPACION_MAXIMA) {
            return TipoAlerta.SATURADA;
        }
        return null;
    }
}