package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Vehiculo {
    private String codigo;
    private EstadoVehiculo estado;
    private Estacion estacion;
    private List<ReporteFalla> reportesFalla;
    private List<OrdenMantenimiento> ordenesMantenimiento;

    protected Vehiculo(String codigo, EstadoVehiculo estado) {
        this.codigo = codigo;
        this.estado = estado;
        this.reportesFalla = new ArrayList<>();
        this.ordenesMantenimiento = new ArrayList<>();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
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

    public void setReportesFalla(List<ReporteFalla> reportesFalla) {
        this.reportesFalla = reportesFalla;
    }

    public List<OrdenMantenimiento> getOrdenesMantenimiento() {
        return ordenesMantenimiento;
    }

    public void setOrdenesMantenimiento(List<OrdenMantenimiento> ordenesMantenimiento) {
        this.ordenesMantenimiento = ordenesMantenimiento;
    }

    public abstract TipoVehiculo getTipo();

    public boolean puedeRetirarse() {
        return false;
    }

    public int contarFallasRecientes(LocalDate hoy) {
        return 0;
    }
}
