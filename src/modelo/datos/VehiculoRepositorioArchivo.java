package modelo.datos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.BicicletaComun;
import modelo.BicicletaElectrica;
import modelo.EstadoVehiculo;
import modelo.Operador;
import modelo.ReporteFalla;
import modelo.Reportante;
import modelo.TipoVehiculo;
import modelo.cuentas.Usuario;
import modelo.excepciones.PersistenciaExcepcion;
import modelo.vehiculos.Monopatin;
import modelo.vehiculos.Vehiculo;

// Guarda los vehículos en dos archivos:
//   vehiculos.txt -> tipo;codigo;fechaAlta;km;estado;mantenimientoPendiente;estacion;dato1;dato2
//                    (dato1 y dato2 según el tipo: rodado | batería y autonomía | batería y velocidad máxima)
//   reportes.txt  -> codigoVehiculo;tipoAutor;idAutor;fecha;descripcion
// Necesita que ya estén cargados los usuarios, el personal y las estaciones.
public class VehiculoRepositorioArchivo extends RepositorioArchivo<Vehiculo> {
    private static final String VEHICULOS = "vehiculos.txt";
    private static final String REPORTES = "reportes.txt";

    private final DatosDelSistema datos;

    public VehiculoRepositorioArchivo(String carpeta, DatosDelSistema datos) {
        super(carpeta);
        this.datos = datos;
    }

    @Override
    public List<Vehiculo> leerTodos() {
        List<Vehiculo> vehiculos = leer(VEHICULOS, c -> crearVehiculo(c));
        Map<String, Vehiculo> porCodigo = new HashMap<>();
        for (Vehiculo v : vehiculos) {
            porCodigo.put(v.getCodigo(), v);
        }
        recorrer(REPORTES, c -> {
            Vehiculo vehiculo = porCodigo.get(c[0]);
            if (vehiculo == null) {
                throw new PersistenciaExcepcion("No existe el vehículo " + c[0]);
            }
            vehiculo.restaurarReporte(new ReporteFalla(vehiculo, autorDe(c[1], c[2]), LocalDate.parse(c[3]), c[4]));
        });
        return vehiculos;
    }

    private Vehiculo crearVehiculo(String[] c) {
        TipoVehiculo tipo = TipoVehiculo.valueOf(c[0]);
        LocalDate alta = LocalDate.parse(c[2]);
        Vehiculo vehiculo;
        switch (tipo) {
            case BICICLETA_COMUN:
                vehiculo = new BicicletaComun(c[1], alta, Integer.parseInt(c[7]));
                break;
            case BICICLETA_ELECTRICA:
                vehiculo = new BicicletaElectrica(c[1], alta, Integer.parseInt(c[7]), Double.parseDouble(c[8]));
                break;
            case MONOPATIN:
                vehiculo = new Monopatin(c[1], alta, Integer.parseInt(c[7]), Double.parseDouble(c[8]));
                break;
            default:
                throw new IllegalArgumentException("Tipo de vehículo desconocido: " + c[0]);
        }
        vehiculo.sumarKm(Double.parseDouble(c[3]));
        vehiculo.restaurarEstado(EstadoVehiculo.valueOf(c[4]), Boolean.parseBoolean(c[5]));
        if (!esVacio(c[6])) {
            datos.obtenerEstacion(c[6]).restaurarVehiculo(vehiculo);
        }
        return vehiculo;
    }

    private Reportante autorDe(String tipo, String id) {
        switch (tipo) {
            case "USUARIO":
                return datos.obtenerUsuario(id);
            case "OPERADOR":
                return datos.obtenerOperador(id);
            default:
                throw new IllegalArgumentException("Tipo de autor desconocido: " + tipo);
        }
    }

    @Override
    public void guardarTodos(List<Vehiculo> vehiculos) {
        List<String> lineasVehiculos = new ArrayList<>();
        List<String> lineasReportes = new ArrayList<>();
        for (Vehiculo v : vehiculos) {
            lineasVehiculos.add(lineaDeVehiculo(v));
            for (ReporteFalla r : v.getReportesFalla()) {
                lineasReportes.add(lineaDeReporte(v, r));
            }
        }
        escribir(VEHICULOS, lineasVehiculos);
        escribir(REPORTES, lineasReportes);
    }

    private String lineaDeVehiculo(Vehiculo v) {
        Object dato1 = VACIO;
        Object dato2 = VACIO;
        if (v instanceof BicicletaComun) {
            dato1 = ((BicicletaComun) v).getRodado();
        } else if (v instanceof BicicletaElectrica) {
            BicicletaElectrica b = (BicicletaElectrica) v;
            dato1 = b.getNivelBateria();
            dato2 = b.getAutonomiaKm();
        } else if (v instanceof Monopatin) {
            Monopatin m = (Monopatin) v;
            dato1 = m.getNivelBateria();
            dato2 = m.getVelocidadMaxima();
        }
        String estacion = v.getEstacion() == null ? null : v.getEstacion().getCodigo();
        return unir(v.getTipo(), v.getCodigo(), v.getFechaAlta(), v.getKmRecorridos(), v.getEstado(),
                v.tieneMantenimientoPendiente(), estacion, dato1, dato2);
    }

    private String lineaDeReporte(Vehiculo v, ReporteFalla r) {
        Reportante autor = r.getReportante();
        if (autor instanceof Usuario) {
            return unir(v.getCodigo(), "USUARIO", ((Usuario) autor).getDocumento(), r.getFecha(), r.getDescripcion());
        }
        if (autor instanceof Operador) {
            return unir(v.getCodigo(), "OPERADOR", ((Operador) autor).getLegajo(), r.getFecha(), r.getDescripcion());
        }
        throw new PersistenciaExcepcion("Autor de reporte no soportado: " + autor.getIdentificacion());
    }
}