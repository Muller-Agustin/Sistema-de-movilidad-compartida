package modelo.vehiculos;


import modelo.estaciones.Estacion;
import modelo.excepciones.VehiculoNoDisponibleExcepcion;
import modelo.mantenimiento.OrdenMantenimiento;
import modelo.mantenimiento.ReporteFalla;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
                    "El vehículo " + codigo + " no está disponible (" + estado.getDescripcion() + ")");
        }
        setEstado(EstadoVehiculo.EN_USO);
    }

    public void marcarDisponible() {
        setEstado(EstadoVehiculo.DISPONIBLE);
    }

    public void enviarAReparacion() {
        setEstado(EstadoVehiculo.EN_REPARACION);
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

    public int contarFallasRecientes(LocalDate hoy) {
        int cantidad = 0;
        for (ReporteFalla reporte : reportesFalla) {
            if (reporte.esReciente(hoy, DIAS_CONTROL_FALLAS)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public boolean requiereMantenimientoObligatorio(LocalDate hoy) {
        return contarFallasRecientes(hoy) >= MAX_FALLAS;
    }

    public void agregarOrden(OrdenMantenimiento orden) {
        if (orden != null && !ordenesMantenimiento.contains(orden)) {
            ordenesMantenimiento.add(orden);
        }
    }

    public int getCantidadFallas() {
        return reportesFalla.size();
    }

    // protected: solo esta clase y sus subclases cambian el estado directamente.
    protected void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return getTipo().getDescripcion() + " " + codigo + " - " + estado.getDescripcion()
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