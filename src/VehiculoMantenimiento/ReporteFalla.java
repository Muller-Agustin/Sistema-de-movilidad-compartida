package VehiculoMantenimiento;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// RF18: reporte de una falla de un vehículo, hecho por un usuario o por un operador.

public class ReporteFalla implements Serializable {
    private static final long serialVersionUID = 1L;
    private Vehiculo vehiculo;
    private Reportante reportante;
    private LocalDate fecha;
    private String descripcion;

    public ReporteFalla(Vehiculo vehiculo, Reportante reportante, LocalDate fecha, String descripcion) {
        this.vehiculo = vehiculo;
        this.reportante = reportante;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    // true si el reporte se hizo dentro de los últimos "dias" días contando desde "hoy"
    public boolean esReciente(LocalDate hoy, int dias) {
        long diferencia = ChronoUnit.DAYS.between(fecha, hoy);
        return diferencia >= 0 && diferencia <= dias;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
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
}