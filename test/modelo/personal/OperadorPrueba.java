package modelo.personal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.BicicletaComun;
import modelo.Operador;
import modelo.estaciones.Estacion;
import modelo.estaciones.TipoAlerta;
import modelo.estaciones.Traslado;
import modelo.vehiculos.Vehiculo;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class OperadorPrueba {
    private static final LocalDateTime AHORA = ViajesDePrueba.INICIO;

    public static void ejecutar(Verificador v) {
        Operador operador = new Operador("L-1", "Luis", "1122334455");

        // RF23: origen con 3 bicis, destino de 5 lugares vacío; se mueven 2
        Estacion origen = ViajesDePrueba.estacion("ORIGEN", 10);
        Estacion destino = ViajesDePrueba.estacion("DESTINO", 5);
        List<BicicletaComun> bicis = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BicicletaComun b = ViajesDePrueba.bicicleta();
            bicis.add(b);
            origen.recibirVehiculo(b, AHORA);
        }
        List<Vehiculo> aMover = new ArrayList<>();
        aMover.add(bicis.get(0));
        aMover.add(bicis.get(1));

        Traslado traslado = operador.redistribuir(origen, destino, aMover, AHORA);

        v.verificarNumero("Redistribuir: el origen queda con 1 vehículo", 1, origen.getVehiculos().size());
        v.verificarNumero("Redistribuir: el destino queda con 2 vehículos", 2, destino.getVehiculos().size());
        v.verificarVerdadero("Redistribuir: el vehículo movido ahora está en el destino",
                bicis.get(0).getEstacion() == destino);
        v.verificarNumero("Redistribuir: el traslado registra 2 vehículos", 2, traslado.getVehiculos().size());
        v.verificarVerdadero("Redistribuir: el traslado registra al operador", traslado.getOperador() == operador);
        v.verificarVerdadero("Redistribuir: el traslado registra origen y destino",
                traslado.getOrigen() == origen && traslado.getDestino() == destino);
        // el origen quedó con 1 de 10 (10%): entró en zona "vacía"
        int ultima = origen.getAlertasRedistribucion().size() - 1;
        v.verificarIgual("Redistribuir: el origen queda con alerta de estación vacía", TipoAlerta.VACIA,
                origen.getAlertasRedistribucion().get(ultima).getTipo());

        // un vehículo que no está en el origen invalida todo el traslado
        Estacion otra = ViajesDePrueba.estacion("OTRA", 10);
        List<Vehiculo> ajeno = new ArrayList<>();
        ajeno.add(bicis.get(2));
        ajeno.add(ViajesDePrueba.bicicleta());
        v.verificarExcepcion("Redistribuir: un vehículo que no está en el origen falla", IllegalArgumentException.class,
                () -> operador.redistribuir(origen, otra, ajeno, AHORA));
        v.verificarNumero("Redistribuir: si falla no se mueve ninguno (origen)", 1, origen.getVehiculos().size());
        v.verificarNumero("Redistribuir: si falla no se mueve ninguno (otra)", 0, otra.getVehiculos().size());

        // destino sin lugar suficiente: no se mueve nada
        Estacion chica = ViajesDePrueba.estacion("CHICA", 1);
        List<Vehiculo> dos = new ArrayList<>();
        dos.add(bicis.get(0));
        dos.add(bicis.get(1));
        v.verificarExcepcion("Redistribuir: destino sin lugar falla", IllegalArgumentException.class,
                () -> operador.redistribuir(destino, chica, dos, AHORA));
        v.verificarNumero("Redistribuir: si falla por lugar, el origen conserva sus 2", 2, destino.getVehiculos().size());

        v.verificarExcepcion("Redistribuir: mismo origen y destino falla", IllegalArgumentException.class,
                () -> operador.redistribuir(origen, origen, dos, AHORA));
        v.verificarExcepcion("Redistribuir: sin vehículos falla", IllegalArgumentException.class,
                () -> operador.redistribuir(origen, otra, new ArrayList<>(), AHORA));
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}