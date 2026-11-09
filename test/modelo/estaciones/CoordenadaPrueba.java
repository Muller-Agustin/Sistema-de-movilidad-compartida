package modelo.estaciones;

import pruebas.Verificador;

public class CoordenadaPrueba {

    public static void ejecutar(Verificador v) {
        Coordenada origen = new Coordenada(0, 0);
        Coordenada unGradoAlEste = new Coordenada(0, 1);

        // un grado sobre el ecuador son unos 111,19 km
        v.verificarAproximado("Coordenada: 1 grado de longitud en el ecuador = 111,19 km",
                111.19, origen.distanciaKm(unGradoAlEste), 0.01);
        v.verificarNumero("Coordenada: la distancia a sí misma es 0", 0, origen.distanciaKm(origen));
        v.verificarAproximado("Coordenada: la distancia es la misma en los dos sentidos",
                origen.distanciaKm(unGradoAlEste), unGradoAlEste.distanciaKm(origen), 0.0001);
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}