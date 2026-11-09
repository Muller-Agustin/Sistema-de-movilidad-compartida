package modelo;

import java.time.LocalDate;

import modelo.vehiculos.Vehiculo;

public interface Reportante {
    ReporteFalla reportarFalla(Vehiculo vehiculo, String descripcion, LocalDate fecha);

    String getIdentificacion();
}
