package EstacionesTraslados;

import java.io.Serializable;

// Ubicación geográfica (latitud y longitud) de una estación o de un punto del recorrido.

public class Coordenada implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double RADIO_TIERRA_KM = 6371;

    private double latitud;
    private double longitud;

    public Coordenada(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // S18: distancia en km entre dos coordenadas (fórmula de haversine).
    // Se usa para calcular la distancia recorrida en un viaje.
    public double distanciaKm(Coordenada otra) {
        double dLat = Math.toRadians(otra.latitud - latitud);
        double dLon = Math.toRadians(otra.longitud - longitud);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(otra.latitud))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    @Override
    public String toString() {
        return "(" + latitud + ", " + longitud + ")";
    }
}