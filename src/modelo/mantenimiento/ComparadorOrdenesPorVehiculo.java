package modelo.mantenimiento;

import java.util.Comparator;

// RF28, RF31 y RF32: ordena las órdenes por código de vehículo y, para el mismo vehículo, por fecha de apertura.
public class ComparadorOrdenesPorVehiculo implements Comparator<OrdenMantenimiento> {
    @Override
    public int compare(OrdenMantenimiento o1, OrdenMantenimiento o2) {
        int resultado = o1.getVehiculo().compareTo(o2.getVehiculo());
        if (resultado == 0) {
            resultado = o1.getFechaApertura().compareTo(o2.getFechaApertura());
        }
        return resultado;
    }
}