package modelo.datos;

import java.util.ArrayList;
import java.util.List;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;

// estaciones.txt -> codigo;direccion;capacidad;latitud;longitud
// Los vehículos de cada estación se guardan en vehiculos.txt. Las alertas no se guardan (S23).
public class EstacionRepositorioArchivo extends RepositorioArchivo<Estacion> {
    private static final String ARCHIVO = "estaciones.txt";

    public EstacionRepositorioArchivo(String carpeta) {
        super(carpeta);
    }

    @Override
    public List<Estacion> leerTodos() {
        return leer(ARCHIVO, c -> new Estacion(c[0], textoONull(c[1]), Integer.parseInt(c[2]),
                new Coordenada(Double.parseDouble(c[3]), Double.parseDouble(c[4]))));
    }

    @Override
    public void guardarTodos(List<Estacion> estaciones) {
        List<String> lineas = new ArrayList<>();
        for (Estacion e : estaciones) {
            lineas.add(unir(e.getCodigo(), e.getDireccion(), e.getCapacidadMaxima(),
                    e.getCoordenada().getLatitud(), e.getCoordenada().getLongitud()));
        }
        escribir(ARCHIVO, lineas);
    }
}