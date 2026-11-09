package modelo;

import modelo.vehiculos.Vehiculo;
import java.time.LocalDate;

public class ReporteFalla {
    private Vehiculo vehiculo;
    private Reportante reportante;
    private LocalDate fecha;
    private String descripcion;

    public ReporteFalla() {
    }

    public ReporteFalla(Vehiculo vehiculo, Reportante reportante,
            LocalDate fecha, String descripcion) {
        this.vehiculo = vehiculo;
        this.reportante = reportante;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Reportante getReportante() {
        return reportante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esReciente(LocalDate hoy, int dias) {
        if (hoy == null || fecha == null || dias < 0) {
            return false;
        }
        long diferencia = hoy.toEpochDay() - fecha.toEpochDay();
        return diferencia >= 0 && diferencia <= dias;
    }
}
