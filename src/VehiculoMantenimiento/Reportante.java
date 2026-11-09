package VehiculoMantenimiento;

import java.time.LocalDate;

public interface Reportante {
    ReporteFalla reportarFalla(Vehiculo vehiculo, String descripcion, LocalDate fecha);

    String getIdentificacion();
}
