package modelo.personal;
import modelo.mantenimiento.OrdenMantenimiento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Tecnico extends Personal {
    private List<OrdenMantenimiento> ordenes;

    public Tecnico(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
        this.ordenes = new ArrayList<>();
    }

    @Override
    public String getRol() {
        return "Técnico";
    }

    public void asignarOrden(OrdenMantenimiento orden) {
        if (orden != null && !ordenes.contains(orden)) {
            ordenes.add(orden);
        }
    }

    // RF20: el técnico cierra solo las órdenes que tiene asignadas.
    public void cerrarOrden(OrdenMantenimiento orden, LocalDate fecha) {
        if (!ordenes.contains(orden)) {
            throw new IllegalArgumentException("La orden no está asignada a este técnico");
        }
        orden.cerrar(fecha);
    }

    // RF31
    public List<OrdenMantenimiento> getOrdenesAbiertas() {
        List<OrdenMantenimiento> abiertas = new ArrayList<>();
        for (OrdenMantenimiento orden : ordenes) {
            if (orden.estaAbierta()) {
                abiertas.add(orden);
            }
        }
        return abiertas;
    }

    // RF32
    public List<OrdenMantenimiento> getOrdenesCerradas() {
        List<OrdenMantenimiento> cerradas = new ArrayList<>();
        for (OrdenMantenimiento orden : ordenes) {
            if (!orden.estaAbierta()) {
                cerradas.add(orden);
            }
        }
        return cerradas;
    }

    // Se usa para asignar automáticamente la orden al técnico con menos órdenes abiertas.
    public int contarOrdenesAbiertas() {
        return getOrdenesAbiertas().size();
    }

    public List<OrdenMantenimiento> getOrdenes() { return new ArrayList<>(ordenes); }
}
