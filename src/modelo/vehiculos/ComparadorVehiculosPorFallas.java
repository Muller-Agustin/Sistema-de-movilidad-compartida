package modelo.vehiculos;

import java.util.Comparator;

// RF26: ordena de mayor a menor cantidad de fallas. Si empatan, por código.
public class ComparadorVehiculosPorFallas implements Comparator<Vehiculo> {
    @Override
    public int compare(Vehiculo v1, Vehiculo v2) {
        int resultado = Integer.compare(v2.getCantidadFallas(), v1.getCantidadFallas());
        if (resultado == 0) {
            resultado = v1.compareTo(v2);
        }
        return resultado;
    }
}