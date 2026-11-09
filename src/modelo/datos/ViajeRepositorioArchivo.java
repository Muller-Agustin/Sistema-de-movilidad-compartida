package modelo.datos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.TipoVehiculo;
import modelo.cuentas.Usuario;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;
import modelo.tarifas.Promocion;
import modelo.tarifas.Tarifa;
import modelo.vehiculos.Vehiculo;
import modelo.viajes.EstadoViaje;
import modelo.viajes.NotaAjuste;
import modelo.viajes.PuntoRecorrido;
import modelo.viajes.Viaje;

import java.time.LocalDate;

// Guarda los viajes en tres archivos:
//   viajes.txt -> documento;codigoVehiculo;tipoTarifa;vigenciaTarifa;inicio;fin;estado;promocion;
//                 estacionRetiro;estacionDevolucion;intentoFallido;importeCobrado
//   puntos.txt -> numeroDeViaje;latitud;longitud;hora            (el viaje se cuenta desde 0)
//   notas.txt  -> numeroDeViaje;legajoOperador;fecha;importe;motivo
// Necesita que ya estén cargados usuarios, personal, estaciones, vehículos, tarifas y promociones.
public class ViajeRepositorioArchivo extends RepositorioArchivo<Viaje> {
    private static final String VIAJES = "viajes.txt";
    private static final String PUNTOS = "puntos.txt";
    private static final String NOTAS = "notas.txt";

    private final DatosDelSistema datos;

    public ViajeRepositorioArchivo(String carpeta, DatosDelSistema datos) {
        super(carpeta);
        this.datos = datos;
    }

    @Override
    public List<Viaje> leerTodos() {
        Map<Integer, List<String[]>> puntosPorViaje = agruparPorViaje(PUNTOS);
        Map<Integer, List<String[]>> notasPorViaje = agruparPorViaje(NOTAS);

        List<Viaje> viajes = new ArrayList<>();
        recorrer(VIAJES, c -> viajes.add(crearViaje(c, puntosPorViaje.get(viajes.size()), notasPorViaje.get(viajes.size()))));
        return viajes;
    }

    private Map<Integer, List<String[]>> agruparPorViaje(String archivo) {
        Map<Integer, List<String[]>> grupos = new HashMap<>();
        recorrer(archivo, c -> grupos.computeIfAbsent(Integer.parseInt(c[0]), k -> new ArrayList<>()).add(c));
        return grupos;
    }

    private Viaje crearViaje(String[] c, List<String[]> puntos, List<String[]> notas) {
        Usuario usuario = datos.obtenerUsuario(c[0]);
        Vehiculo vehiculo = datos.obtenerVehiculo(c[1]);
        Tarifa tarifa = datos.obtenerTarifa(TipoVehiculo.valueOf(c[2]), LocalDate.parse(c[3]));
        Viaje viaje = new Viaje(usuario, vehiculo, tarifa, datos.obtenerEstacion(c[8]), LocalDateTime.parse(c[4]));

        // los puntos se agregan mientras el viaje todavía figura en curso
        if (puntos != null) {
            for (String[] p : puntos) {
                viaje.registrarPunto(new Coordenada(Double.parseDouble(p[1]), Double.parseDouble(p[2])),
                        LocalDateTime.parse(p[3]));
            }
        }
        if (notas != null) {
            for (String[] n : notas) {
                viaje.agregarNotaAjuste(new NotaAjuste(datos.obtenerOperador(n[1]),
                        LocalDateTime.parse(n[2]), Double.parseDouble(n[3]), n[4]));
            }
        }
        if (!esVacio(c[7])) {
            viaje.setPromocion(datos.obtenerPromocion(c[7]));
        }
        Estacion devolucion = esVacio(c[9]) ? null : datos.obtenerEstacion(c[9]);
        viaje.restaurarEstado(EstadoViaje.valueOf(c[6]), fechaHoraONull(c[5]), devolucion,
                fechaHoraONull(c[10]), Double.parseDouble(c[11]));
        usuario.agregarViaje(viaje);
        return viaje;
    }

    @Override
    public void guardarTodos(List<Viaje> viajes) {
        List<String> lineasViajes = new ArrayList<>();
        List<String> lineasPuntos = new ArrayList<>();
        List<String> lineasNotas = new ArrayList<>();
        for (int i = 0; i < viajes.size(); i++) {
            Viaje v = viajes.get(i);
            lineasViajes.add(lineaDeViaje(v));
            for (PuntoRecorrido p : v.getPuntosRecorrido()) {
                lineasPuntos.add(unir(i, p.getCoordenada().getLatitud(), p.getCoordenada().getLongitud(), p.getHora()));
            }
            for (NotaAjuste n : v.getNotasAjuste()) {
                lineasNotas.add(unir(i, n.getOperador().getLegajo(), n.getFecha(), n.getImporte(), n.getMotivo()));
            }
        }
        escribir(VIAJES, lineasViajes);
        escribir(PUNTOS, lineasPuntos);
        escribir(NOTAS, lineasNotas);
    }

    private String lineaDeViaje(Viaje v) {
        Promocion promocion = v.getPromocion();
        String nombrePromocion = promocion == null ? null : promocion.getNombre();
        Estacion devolucion = v.getDevolucion();
        String codigoDevolucion = devolucion == null ? null : devolucion.getCodigo();
        return unir(v.getUsuario().getDocumento(), v.getVehiculo().getCodigo(),
                v.getTarifa().getTipoVehiculo(), v.getTarifa().getFechaVigencia(),
                v.getInicio(), v.getFin(), v.getEstado(), nombrePromocion,
                v.getRetiro().getCodigo(), codigoDevolucion,
                v.getIntentoFallidoDevolucion(), v.getImporteCobrado());
    }
}