package modelo.personal;

import modelo.mantenimiento.OrdenMantenimiento;
import modelo.mantenimiento.Reportante;
import modelo.mantenimiento.ReporteFalla;
import modelo.vehiculos.Vehiculo;

import java.time.LocalDate;

public class Operador extends Personal implements Reportante {
    public Operador(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
    }

    @Override
    public String getRol() {
        return "Operador";
    }

    // RF18: el operador también puede reportar fallas.
    @Override
    public ReporteFalla reportarFalla(Vehiculo vehiculo, String descripcion, LocalDate fecha) {
        ReporteFalla reporte = new ReporteFalla(vehiculo, this, fecha, descripcion);
        vehiculo.agregarReporte(reporte);
        return reporte;
    }

    @Override
    public String getIdentificacion() {
        return "Operador " + getNombre() + " (legajo " + getLegajo() + ")";
    }

    // RF19: genera la orden, la asigna al técnico y manda el vehículo a reparación.
    public OrdenMantenimiento generarOrden(Vehiculo vehiculo, Tecnico tecnico, LocalDate fecha) {
        OrdenMantenimiento orden = new OrdenMantenimiento(vehiculo, tecnico, fecha);
        vehiculo.enviarAReparacion();
        return orden;
    }
}
