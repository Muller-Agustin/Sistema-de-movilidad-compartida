package VehiculoMantenimiento;

import java.util.Comparator;

// Orden personalizado (Comparator) para el reporte RF27: primero los vehículos con más fallas.
// Si empatan, se usa el orden natural de Vehiculo (por código).

public class ComparadorVehiculosPorFallas implements Comparator<Vehiculo> {
    @Override
    public int compare(Vehiculo primero, Vehiculo segundo) {
        int porFallas = Integer.compare(segundo.getCantidadFallas(), primero.getCantidadFallas());
        if (porFallas != 0) {
            return porFallas;
        }
        return primero.compareTo(segundo);
    }
}