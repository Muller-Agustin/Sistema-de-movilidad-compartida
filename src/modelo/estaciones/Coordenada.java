package modelo.estaciones;

public final class Coordenada {
    private static final double RADIO_TIERRA_KM = 6371;

    private final double latitud;
    private final double longitud;

    public Coordenada(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    // Distancia en kilómetros entre dos coordenadas (fórmula de haversine, S18)
    public double distanciaKm(Coordenada otra) {
        double dLat = Math.toRadians(otra.latitud - latitud);
        double dLon = Math.toRadians(otra.longitud - longitud);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(otra.latitud))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}