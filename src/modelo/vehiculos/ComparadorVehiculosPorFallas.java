package modelo.vehiculos;

import java.util.Comparator;

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
