package modelo.datos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.tarifas.PromoEstacionBajaOcupacion;
import modelo.tarifas.PromoPlanMensual;
import modelo.tarifas.PromoPrimerViaje;
import modelo.tarifas.Promocion;

// promociones.txt -> clase;nombre;fechaInicio;fechaFin
public class PromocionRepositorioArchivo extends RepositorioArchivo<Promocion> {
    private static final String ARCHIVO = "promociones.txt";

    public PromocionRepositorioArchivo(String carpeta) {
        super(carpeta);
    }

    @Override
    public List<Promocion> leerTodos() {
        return leer(ARCHIVO, c -> crear(c[0], c[1], LocalDate.parse(c[2]), LocalDate.parse(c[3])));
    }

    private Promocion crear(String clase, String nombre, LocalDate inicio, LocalDate fin) {
        switch (clase) {
            case "PromoPrimerViaje":
                return new PromoPrimerViaje(nombre, inicio, fin);
            case "PromoPlanMensual":
                return new PromoPlanMensual(nombre, inicio, fin);
            case "PromoEstacionBajaOcupacion":
                return new PromoEstacionBajaOcupacion(nombre, inicio, fin);
            default:
                throw new IllegalArgumentException("Tipo de promoción desconocido: " + clase);
        }
    }

    @Override
    public void guardarTodos(List<Promocion> promociones) {
        List<String> lineas = new ArrayList<>();
        for (Promocion p : promociones) {
            lineas.add(unir(p.getClass().getSimpleName(), p.getNombre(), p.getFechaInicio(), p.getFechaFin()));
        }
        escribir(ARCHIVO, lineas);
    }
}