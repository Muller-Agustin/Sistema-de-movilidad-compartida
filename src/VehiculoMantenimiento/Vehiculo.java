package VehiculoMantenimiento;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import EstacionesTraslados.Estacion;
import Excepciones.VehiculoNoDisponibleExcepcion;

public abstract class Vehiculo implements Comparable<Vehiculo> {
    public static final int MAX_FALLAS = 3;
    public static final int DIAS_CONTROL_FALLAS = 30;

    private String codigo;
    private LocalDate fechaAlta;
    private double kmRecorridos;
    private EstadoVehiculo estado;
    private Estacion estacion;
    private List<ReporteFalla> reportesFalla;
    private List<OrdenMantenimiento> ordenesMantenimiento;
    private boolean mantenimientoPendiente;

    // RF11: se crea disponible y con 0 km.
    protected Vehiculo(String codigo, LocalDate fechaAlta) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        if (fechaAlta == null) {
            throw new IllegalArgumentException("La fecha de alta es obligatoria");
        }
        this.codigo = codigo;
        this.fechaAlta = fechaAlta;
        this.kmRecorridos = 0;
        this.estado = EstadoVehiculo.DISPONIBLE;
        this.reportesFalla = new ArrayList<>();
        this.ordenesMantenimiento = new ArrayList<>();
    }

    public abstract TipoVehiculo getTipo();

    // RN07: solo puede retirarse un vehículo disponible.
    public boolean puedeRetirarse() {
        return estado == EstadoVehiculo.DISPONIBLE;
    }

    public void marcarEnUso() {
        if (!puedeRetirarse()) {
            throw new VehiculoNoDisponibleExcepcion(
                    "El vehículo " + codigo + " no está disponible (" + estado + ")");
        }
        setEstado(EstadoVehiculo.EN_USO);
    }

    // Si quedó un mantenimiento pendiente (S20), al volver del viaje pasa a reparación en vez de disponible.
    public void marcarDisponible() {
        if (mantenimientoPendiente) {
            mantenimientoPendiente = false;
            setEstado(EstadoVehiculo.EN_REPARACION);
        } else {
            setEstado(EstadoVehiculo.DISPONIBLE);
        }
    }

    // S20: si el vehículo está en un viaje no se lo frena: queda pendiente hasta que lo devuelvan.
    public void enviarAReparacion() {
        if (estado == EstadoVehiculo.EN_USO) {
            mantenimientoPendiente = true;
        } else {
            setEstado(EstadoVehiculo.EN_REPARACION);
        }
    }

    // S2: también se usa para los vehículos perdidos (RN20).
    public void darDeBaja() {
        setEstado(EstadoVehiculo.FUERA_DE_SERVICIO);
    }

    // RF08: al devolver el vehículo se suman los km del viaje.
    public void sumarKm(double km) {
        if (km < 0) {
            throw new IllegalArgumentException("Los km no pueden ser negativos");
        }
        kmRecorridos += km;
    }

    // RN10 y S3: al llegar a 3 reportes en los últimos 30 días, pasa a reparación.
    public void agregarReporte(ReporteFalla reporte) {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte es obligatorio");
        }
        reportesFalla.add(reporte);
        if (requiereMantenimientoObligatorio(reporte.getFecha())) {
            enviarAReparacion();
        }
    }

    // S20: los reportes hechos hasta el día en que se cerró la última reparación ya fueron atendidos y no cuentan.
    public int contarFallasRecientes(LocalDate hoy) {
        LocalDate ultimaReparacion = getFechaUltimaReparacion();
        int cantidad = 0;
        for (ReporteFalla reporte : reportesFalla) {
            boolean atendido = ultimaReparacion != null && !reporte.getFecha().isAfter(ultimaReparacion);
            if (!atendido && reporte.esReciente(hoy, DIAS_CONTROL_FALLAS)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // fecha de cierre de la orden cerrada más reciente (null si nunca se reparó)
    public LocalDate getFechaUltimaReparacion() {
        LocalDate ultima = null;
        for (OrdenMantenimiento orden : ordenesMantenimiento) {
            if (!orden.estaAbierta() && (ultima == null || orden.getFechaCierre().isAfter(ultima))) {
                ultima = orden.getFechaCierre();
            }
        }
        return ultima;
    }

    public boolean tieneOrdenAbierta() {
        for (OrdenMantenimiento orden : ordenesMantenimiento) {
            if (orden.estaAbierta()) {
                return true;
            }
        }
        return false;
    }

    public boolean tieneMantenimientoPendiente() {
        return mantenimientoPendiente;
    }

    public boolean requiereMantenimientoObligatorio(LocalDate hoy) {
        return contarFallasRecientes(hoy) >= MAX_FALLAS;
    }

    public void agregarOrden(OrdenMantenimiento orden) {
        if (orden != null && !ordenesMantenimiento.contains(orden)) {
            ordenesMantenimiento.add(orden);
        }
    }

    // Los dos métodos siguientes los usa la persistencia al cargar un vehículo ya guardado:
    // dejan el estado y los reportes como estaban, sin volver a ejecutar las reglas.
    public void restaurarEstado(EstadoVehiculo estado, boolean mantenimientoPendiente) {
        this.estado = estado;
        this.mantenimientoPendiente = mantenimientoPendiente;
    }

    public void restaurarReporte(ReporteFalla reporte) {
        reportesFalla.add(reporte);
    }

    public int getCantidadFallas() {
        return reportesFalla.size();
    }

    // protected: solo esta clase y sus subclases cambian el estado directamente.
    protected void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return getTipo() + " " + codigo + " - " + estado
                + " - " + kmRecorridos + " km";
    }

    // Orden natural: por código.
    @Override
    public int compareTo(Vehiculo otro) {
        return this.codigo.compareTo(otro.codigo);
    }

    @Override
    public String toString() {
        return getDescripcion();
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public double getKmRecorridos() {
        return kmRecorridos;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public void setEstacion(Estacion estacion) {
        this.estacion = estacion;
    }

    // Se devuelven copias para que nadie modifique las listas desde afuera.
    public List<ReporteFalla> getReportesFalla() {
        return new ArrayList<>(reportesFalla);
    }

    public List<OrdenMantenimiento> getOrdenesMantenimiento() {
        return new ArrayList<>(ordenesMantenimiento);
    }
}