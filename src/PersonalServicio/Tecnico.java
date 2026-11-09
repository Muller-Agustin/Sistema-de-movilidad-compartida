package PersonalServicio;

import java.util.ArrayList;
import java.util.List;

import VehiculoMantenimiento.OrdenMantenimiento;

// Técnico de mantenimiento: tiene asignadas órdenes de mantenimiento (RF19).

public class Tecnico extends Personal {
    private static final long serialVersionUID = 1L;
    private List<OrdenMantenimiento> ordenes;

    public Tecnico(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
        this.ordenes = new ArrayList<>();
    }

    public void asignarOrden(OrdenMantenimiento orden) {
        ordenes.add(orden);
    }

    public int contarOrdenesAbiertas() {
        int cantidad = 0;
        for (OrdenMantenimiento orden : ordenes) {
            if (orden.estaAbierta()) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public List<OrdenMantenimiento> getOrdenes() {
        return ordenes;
    }
}