package VehiculoMantenimiento;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import EstacionesTraslados.Estacion;

// Vehículo del servicio. Es abstracta: cada tipo (bicicleta común, bicicleta eléctrica
// o monopatín) define su tipo y sus datos propios.
// Orden natural (Comparable): por código.

public abstract class Vehiculo implements Comparable<Vehiculo>, Serializable {
    private static final long serialVersionUID = 1L;
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

    // RF11: se crea disponible y con 0 km
    public Vehiculo(String codigo, LocalDate fechaAlta) {
        this.codigo = codigo;
        this.fechaAlta = fechaAlta;
        this.kmRecorridos = 0;
        this.estado = EstadoVehiculo.DISPONIBLE;
        this.reportesFalla = new ArrayList<>();
        this.ordenesMantenimiento = new ArrayList<>();
    }

    // Polimorfismo (GRASP): cada subclase devuelve su tipo
    public abstract TipoVehiculo getTipo();

    // RN07: solo puede retirarse un vehículo disponible
    public boolean puedeRetirarse() {
        return estado == EstadoVehiculo.DISPONIBLE;
    }

        public void marcarEnUso() {
        estado = EstadoVehiculo.EN_USO;
    }

    // S20: si quedó un mantenimiento pendiente, al volver del viaje pasa a reparación
    public void marcarDisponible() {
        if (mantenimientoPendiente) {
            mantenimientoPendiente = false;
            estado = EstadoVehiculo.EN_REPARACION;
        } else {
            estado = EstadoVehiculo.DISPONIBLE;
        }
    }

    // S20: si el vehículo está en un viaje no se lo frena; queda pendiente hasta que lo devuelvan
    public void enviarAReparacion() {
        if (estado == EstadoVehiculo.EN_USO) {
            mantenimientoPendiente = true;
        } else {
            estado = EstadoVehiculo.EN_REPARACION;
        }
    }

    // También se usa para los vehículos perdidos (RN20)
    public void darDeBaja() {
        estado = EstadoVehiculo.FUERA_DE_SERVICIO;
    }

    // RF08: al devolver el vehículo se suman los km del viaje
    public void sumarKm(double km) {
        kmRecorridos += km;
    }

    // RN10: al llegar a 3 reportes en los últimos 30 días, pasa a reparación
    public void agregarReporte(ReporteFalla reporte) {
        reportesFalla.add(reporte);
        if (requiereMantenimientoObligatorio(reporte.getFecha())) {
            enviarAReparacion();
        }
    }

    public boolean requiereMantenimientoObligatorio(LocalDate hoy) {
        return contarFallasRecientes(hoy) >= MAX_FALLAS;
    }

    // Cuenta los reportes de los últimos 30 días que todavía no fueron atendidos
    // (los anteriores a la última reparación ya se resolvieron y no cuentan).
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

    // Fecha de cierre de la orden cerrada más reciente (null si nunca se reparó)
    private LocalDate getFechaUltimaReparacion() {
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

    public void agregarOrden(OrdenMantenimiento orden) {
        ordenesMantenimiento.add(orden);
    }

    public int getCantidadFallas() {
        return reportesFalla.size();
    }

    // Lo usan las subclases eléctricas para pasar a batería baja y volver
    protected void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    @Override
    public int compareTo(Vehiculo otro) {
        return this.codigo.compareTo(otro.codigo);
    }

    @Override
    public String toString() {
        return getTipo() + " " + codigo + " - " + estado + " - " + kmRecorridos + " km";
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

    public List<ReporteFalla> getReportesFalla() {
        return reportesFalla;
    }

    public List<OrdenMantenimiento> getOrdenesMantenimiento() {
        return ordenesMantenimiento;
    }
}