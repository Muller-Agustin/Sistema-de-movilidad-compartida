package modelo.mantenimiento;

import modelo.vehiculos.Vehiculo;

import java.time.LocalDate;

// Lo implementan Usuario y Operador: los dos pueden reportar fallas
// y no comparten una superclase.
public interface Reportante {

    ReporteFalla reportarFalla(Vehiculo vehiculo, String descripcion, LocalDate fecha);

    String getIdentificacion();
}