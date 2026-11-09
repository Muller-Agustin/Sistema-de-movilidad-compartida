package VehiculoMantenimiento;

import java.time.LocalDate;

// Interfaz para quien puede reportar una falla de un vehículo (RF18).
// La implementan Usuario y Operador, que no tienen una clase padre en común.

public interface Reportante {
    ReporteFalla reportarFalla(Vehiculo vehiculo, String descripcion, LocalDate fecha);

    String getIdentificacion();
}