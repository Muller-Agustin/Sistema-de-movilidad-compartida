package modelo;

import java.util.ArrayList;
import java.util.List;
import modelo.mantenimiento.OrdenMantenimiento;

public class Tecnico extends Personal {
    private final List<OrdenMantenimiento> ordenes;

    public Tecnico() {
        ordenes = new ArrayList<>();
    }

    public Tecnico(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
        ordenes = new ArrayList<>();
    }

    public void asignarOrden(OrdenMantenimiento orden) {
        if (orden != null && !ordenes.contains(orden)) {
            ordenes.add(orden);
        }
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
        return new ArrayList<>(ordenes);
    }

    public List<OrdenMantenimiento> getOrdenesAbiertas() {
        List<OrdenMantenimiento> abiertas = new ArrayList<>();
        for (OrdenMantenimiento orden : ordenes) {
            if (orden.estaAbierta()) {
                abiertas.add(orden);
            }
        }
        return abiertas;
    }

    public List<OrdenMantenimiento> getOrdenesCerradas() {
        List<OrdenMantenimiento> cerradas = new ArrayList<>();
        for (OrdenMantenimiento orden : ordenes) {
            if (!orden.estaAbierta()) {
                cerradas.add(orden);
            }
        }
        return cerradas;
    }

    public void cerrarOrden(OrdenMantenimiento orden, java.time.LocalDate fecha) {
        if (orden == null || !ordenes.contains(orden)) {
            throw new IllegalArgumentException("La orden no pertenece al técnico");
        }
        orden.cerrar(fecha);
    }
}
