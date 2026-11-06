package modelo.mantenimiento;

import modelo.vehiculos.Vehiculo;
import java.time.LocalDate;

public class ReporteFalla {
    private Vehiculo vehiculo;
    private Reportante autor;
    private LocalDate fecha;
    private String descripcion;

    public ReporteFalla(Vehiculo vehiculo, Reportante autor, LocalDate fecha, String descripcion) {
        if (vehiculo == null || autor == null || fecha == null) {
            throw new IllegalArgumentException("El vehículo, el autor y la fecha son obligatorios");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        this.vehiculo = vehiculo;
        this.autor = autor;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    // S3: el reporte cuenta si está dentro de los últimos "dias" días hasta "hoy".
    public boolean esReciente(LocalDate hoy, int dias) {
        return !fecha.isAfter(hoy) && !fecha.isBefore(hoy.minusDays(dias));
    }

    @Override
    public String toString() {
        return fecha + " - " + vehiculo.getCodigo() + " - " + descripcion
                + " (reportado por " + autor.getIdentificacion() + ")";
    }

    public Vehiculo getVehiculo() { return vehiculo; }
    public Reportante getAutor() { return autor; }
    public LocalDate getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
}