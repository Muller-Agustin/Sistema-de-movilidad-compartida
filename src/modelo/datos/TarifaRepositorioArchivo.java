package modelo.datos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.TipoVehiculo;
import modelo.tarifas.Tarifa;

// tarifas.txt -> tipo;cargoDesbloqueo;precioPorMinuto;fechaVigencia
public class TarifaRepositorioArchivo extends RepositorioArchivo<Tarifa> {
    private static final String ARCHIVO = "tarifas.txt";

    public TarifaRepositorioArchivo(String carpeta) {
        super(carpeta);
    }

    @Override
    public List<Tarifa> leerTodos() {
        return leer(ARCHIVO, c -> new Tarifa(TipoVehiculo.valueOf(c[0]),
                Double.parseDouble(c[1]), Double.parseDouble(c[2]), LocalDate.parse(c[3])));
    }

    @Override
    public void guardarTodos(List<Tarifa> tarifas) {
        List<String> lineas = new ArrayList<>();
        for (Tarifa t : tarifas) {
            lineas.add(unir(t.getTipoVehiculo(), t.getCargoDesbloqueo(), t.getPrecioPorMinuto(), t.getFechaVigencia()));
        }
        escribir(ARCHIVO, lineas);
    }
}