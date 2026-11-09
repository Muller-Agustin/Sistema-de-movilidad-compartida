package modelo.estaciones;

import java.util.Comparator;

// RF29: ordena las estaciones de la más cercana a la más lejana de una ubicación. Si empatan, por código.
public class ComparadorEstacionesPorDistancia implements Comparator<Estacion> {
    private final Coordenada ubicacion;

    public ComparadorEstacionesPorDistancia(Coordenada ubicacion) {
        if (ubicacion == null) {
            throw new IllegalArgumentException("La ubicación es obligatoria");
        }
        this.ubicacion = ubicacion;
    }

    @Override
    public int compare(Estacion e1, Estacion e2) {
        int resultado = Double.compare(e1.getCoordenada().distanciaKm(ubicacion), e2.getCoordenada().distanciaKm(ubicacion));
        if (resultado == 0) {
            resultado = e1.getCodigo().compareTo(e2.getCodigo());
        }
        return resultado;
    }
}