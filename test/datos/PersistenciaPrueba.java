package datos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.Administrador;
import modelo.EstadoVehiculo;
import modelo.Operador;
import modelo.Personal;
import modelo.Tecnico;
import modelo.TipoVehiculo;
import modelo.cuentas.BilleteraVirtual;
import modelo.cuentas.EstadoUsuario;
import modelo.cuentas.Tarjeta;
import modelo.cuentas.Usuario;
import modelo.datos.AlmacenDeDatos;
import modelo.datos.DatosDelSistema;
import modelo.datos.Repositorio;
import modelo.datos.TarifaRepositorioArchivo;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;
import modelo.excepciones.PersistenciaExcepcion;
import modelo.mantenimiento.OrdenMantenimiento;
import modelo.servicios.CalculadoraDeTarifa;
import modelo.servicios.RelojSimulado;
import modelo.servicios.ServicioMantenimiento;
import modelo.servicios.ServicioVehiculos;
import modelo.servicios.ServicioViajes;
import modelo.tarifas.CatalogoTarifas;
import modelo.tarifas.PromoEstacionBajaOcupacion;
import modelo.tarifas.PromoPlanMensual;
import modelo.tarifas.PromoPrimerViaje;
import modelo.tarifas.Promocion;
import modelo.tarifas.Tarifa;
import modelo.BicicletaComun;
import modelo.BicicletaElectrica;
import modelo.vehiculos.Monopatin;
import modelo.viajes.EstadoViaje;
import modelo.viajes.Viaje;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class PersistenciaPrueba {
    private static final String[] ARCHIVOS = {
        "tarifas.txt", "promociones.txt", "personal.txt", "usuarios.txt", "mediospago.txt", "movimientos.txt",
        "estaciones.txt", "vehiculos.txt", "reportes.txt", "ordenes.txt", "tareas.txt",
        "viajes.txt", "puntos.txt", "notas.txt"
    };

    public static void ejecutar(Verificador v) {
        repositorioSimple(v);
        carpetaVacia(v);
        sistemaCompleto(v);
        archivosInvalidos(v);
    }

    // ---------- herramientas de prueba ----------

    private static File carpetaTemporal() {
        File carpeta = new File(System.getProperty("java.io.tmpdir"), "movilidad-prueba-" + System.nanoTime());
        carpeta.mkdirs();
        return carpeta;
    }

    private static void borrar(File carpeta) {
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                archivo.delete();
            }
        }
        carpeta.delete();
    }

    private static String leerTexto(File archivo) throws IOException {
        StringBuilder texto = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                texto.append(linea).append("\n");
            }
        }
        return texto.toString();
    }

    private static void escribirTexto(File archivo, String contenido) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
            escritor.write(contenido);
        }
    }

    // ---------- pruebas ----------

    // el resto del sistema usa la interfaz Repositorio (DIP)
    private static void repositorioSimple(Verificador v) {
        File carpeta = carpetaTemporal();
        try {
            Repositorio<Tarifa> repo = new TarifaRepositorioArchivo(carpeta.getPath());
            v.verificarNumero("Repositorio: sin archivo no hay datos", 0, repo.leerTodos().size());

            List<Tarifa> tarifas = new ArrayList<>();
            tarifas.add(new Tarifa(TipoVehiculo.MONOPATIN, 300, 50, LocalDate.of(2026, 3, 1)));
            tarifas.add(new Tarifa(TipoVehiculo.BICICLETA_COMUN, 200, 20.5, LocalDate.of(2026, 1, 1)));
            repo.guardarTodos(tarifas);

            List<Tarifa> leidas = repo.leerTodos();
            v.verificarNumero("Repositorio: lee las 2 tarifas guardadas", 2, leidas.size());
            v.verificarIgual("Repositorio: conserva el tipo", TipoVehiculo.MONOPATIN, leidas.get(0).getTipoVehiculo());
            v.verificarNumero("Repositorio: conserva el desbloqueo", 300, leidas.get(0).getCargoDesbloqueo());
            v.verificarNumero("Repositorio: conserva los decimales", 20.5, leidas.get(1).getPrecioPorMinuto());
            v.verificarIgual("Repositorio: conserva la vigencia", LocalDate.of(2026, 3, 1), leidas.get(0).getFechaVigencia());

            repo.guardarTodos(new ArrayList<>());
            v.verificarNumero("Repositorio: guardar una lista vacía borra los datos", 0, repo.leerTodos().size());
        } finally {
            borrar(carpeta);
        }
    }

    private static void carpetaVacia(Verificador v) {
        File inexistente = new File(System.getProperty("java.io.tmpdir"), "movilidad-no-existe-" + System.nanoTime());
        DatosDelSistema datos = new AlmacenDeDatos(inexistente.getPath()).cargar();
        v.verificarNumero("Carga vacía: sin usuarios", 0, datos.getUsuarios().size());
        v.verificarNumero("Carga vacía: sin vehículos", 0, datos.getVehiculos().size());
        v.verificarNumero("Carga vacía: sin viajes", 0, datos.getViajes().size());
        v.verificarNumero("Carga vacía: sin tarifas", 0, datos.getTarifas().getTarifas().size());
    }

    // Un sistema armado con los servicios: se guarda, se carga y se compara
    private static DatosDelSistema armarSistema() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);
        RelojSimulado reloj = new RelojSimulado(ViajesDePrueba.INICIO);
        DatosDelSistema datos = new DatosDelSistema();

        datos.setTarifas(CatalogoTarifas.conValoresDeReferencia(desde));
        List<Promocion> promociones = new ArrayList<>();
        promociones.add(new PromoPrimerViaje("Primer viaje", desde, hasta));
        promociones.add(new PromoPlanMensual("Plan mensual", desde, hasta));
        promociones.add(new PromoEstacionBajaOcupacion("Baja ocupación", desde, hasta));
        datos.setPromociones(promociones);

        Operador operador = new Operador("L-1", "Luis", "1133334444");
        Tecnico tecnico1 = new Tecnico("T-1", "Tomás", "1155556666");
        Tecnico tecnico2 = new Tecnico("T-2", "Tania", "1177778888");
        List<Personal> personal = new ArrayList<>();
        personal.add(operador);
        personal.add(tecnico1);
        personal.add(tecnico2);
        personal.add(new Administrador("A-1", "Alba", "1100001111"));
        datos.setPersonal(personal);

        ServicioVehiculos servicioVehiculos = new ServicioVehiculos(reloj);
        ServicioMantenimiento mantenimiento = new ServicioMantenimiento(reloj);
        mantenimiento.agregarTecnico(tecnico1);
        mantenimiento.agregarTecnico(tecnico2);
        ServicioViajes servicioViajes = new ServicioViajes(new CalculadoraDeTarifa(promociones), datos.getTarifas(), reloj);

        Estacion a = new Estacion("A", "Calle A 100", 10, new Coordenada(0, 0));
        Estacion b = new Estacion("B", "Calle B 200", 10, new Coordenada(0, 1));
        List<Estacion> estaciones = new ArrayList<>();
        estaciones.add(a);
        estaciones.add(b);
        datos.setEstaciones(estaciones);

        BicicletaComun comun = servicioVehiculos.registrarBicicletaComun("BC-1", 26, a);
        BicicletaElectrica electrica = servicioVehiculos.registrarBicicletaElectrica("BE-1", 80, 40, a);
        Monopatin monopatin = servicioVehiculos.registrarMonopatin("MP-1", 90, 25, b);

        Usuario ana = new Usuario("Ana", "12345678", "ana@mail.com", LocalDate.of(2026, 11, 1));
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(ana);
        datos.setUsuarios(usuarios);
        Tarjeta tarjeta = new Tarjeta("Ana Gómez", "1234");
        ana.getCuenta().agregarMedioPago(tarjeta);
        ana.getCuenta().agregarMedioPago(new BilleteraVirtual("ana.mp"));
        ana.recargarSaldo(10000, tarjeta, reloj.ahora());
        ana.suscribirPlan(reloj.ahora());                         // 10000 - 8000 = 2000

        // viaje 1: 20 minutos de A a B, con un punto en el medio; el plan lo cubre (se paga 0)
        Viaje viaje1 = servicioViajes.retirar(ana, comun, a);
        reloj.avanzarMinutos(10);
        viaje1.registrarPunto(new Coordenada(0, 0.5), reloj.ahora());
        reloj.avanzarMinutos(10);
        servicioViajes.devolver(viaje1, b);
        servicioViajes.registrarAjuste(viaje1, operador, 200, "Cobro de más; revisar");   // saldo 2200

        // viaje 2: queda en curso con el monopatín
        servicioViajes.retirar(ana, monopatin, b);

        // 3 fallas en la bicicleta eléctrica: orden automática que se cierra; y una orden manual abierta
        mantenimiento.reportarFalla(ana, electrica, "Freno");
        mantenimiento.reportarFalla(ana, electrica, "Luz");
        mantenimiento.reportarFalla(ana, electrica, "Motor");
        OrdenMantenimiento primera = mantenimiento.getOrdenes().get(0);
        List<String> tareas = new ArrayList<>();
        tareas.add("Cambio de motor");
        List<String> repuestos = new ArrayList<>();
        repuestos.add("Motor 250W");
        mantenimiento.cerrarOrden(tecnico1, primera, tareas, repuestos);
        mantenimiento.generarOrden(operador, comun, tecnico2);

        datos.setVehiculos(servicioVehiculos.getVehiculos());
        datos.setViajes(servicioViajes.getViajes());
        datos.setOrdenes(mantenimiento.getOrdenes());
        return datos;
    }

    private static void sistemaCompleto(Verificador v) {
        File carpeta1 = carpetaTemporal();
        File carpeta2 = carpetaTemporal();
        try {
            new AlmacenDeDatos(carpeta1.getPath()).guardar(armarSistema());
            DatosDelSistema c = new AlmacenDeDatos(carpeta1.getPath()).cargar();

            // datos básicos
            v.verificarNumero("Carga: 3 tarifas", 3, c.getTarifas().getTarifas().size());
            v.verificarNumero("Carga: 3 promociones", 3, c.getPromociones().size());
            v.verificarIgual("Carga: la promoción conserva su tipo", PromoPlanMensual.class, c.obtenerPromocion("Plan mensual").getClass());
            v.verificarNumero("Carga: 4 personas del personal", 4, c.getPersonal().size());
            v.verificarIgual("Carga: el técnico sigue siendo técnico", "Tomás", c.obtenerTecnico("T-1").getNombre());
            v.verificarNumero("Carga: 2 estaciones", 2, c.getEstaciones().size());
            v.verificarIgual("Carga: la estación conserva la dirección", "Calle B 200", c.obtenerEstacion("B").getDireccion());

            // usuario: el saldo se recalcula con los movimientos
            Usuario ana = c.obtenerUsuario("12345678");
            v.verificarNumero("Usuario: saldo 10000 - 8000 + 200 = 2200", 2200, ana.getCuenta().getSaldo());
            v.verificarIgual("Usuario: sigue habilitada", EstadoUsuario.HABILITADO, ana.getEstado());
            v.verificarNumero("Usuario: 3 movimientos (recarga, plan, ajuste)", 3, ana.getCuenta().getMovimientos().size());
            v.verificarNumero("Usuario: 2 medios de pago", 2, ana.getCuenta().getMediosPago().size());
            v.verificarNumero("Usuario: al plan le quedan 19 viajes", 19, ana.getPlanMensual().getViajesRestantes(LocalDate.of(2026, 11, 9)));
            v.verificarNumero("Usuario: 2 viajes", 2, ana.getViajes().size());
            v.verificarVerdadero("Usuario: tiene un viaje en curso", ana.tieneViajeEnCurso());

            // viajes
            v.verificarNumero("Carga: 2 viajes", 2, c.getViajes().size());
            Viaje terminado = c.getViajes().get(0);
            v.verificarIgual("Viaje 1: finalizado", EstadoViaje.FINALIZADO, terminado.getEstado());
            v.verificarIgual("Viaje 1: conserva la promoción", "Plan mensual", terminado.getPromocion().getNombre());
            v.verificarNumero("Viaje 1: importe cobrado 0", 0, terminado.getImporteCobrado());
            v.verificarNumero("Viaje 1: 20 minutos cobrables", 20, terminado.getMinutosCobrables());
            v.verificarVerdadero("Viaje 1: devuelto en B", terminado.getDevolucion() == c.obtenerEstacion("B"));
            v.verificarNumero("Viaje 1: 1 punto del recorrido", 1, terminado.getPuntosRecorrido().size());
            v.verificarAproximado("Viaje 1: 111,19 km", 111.19, terminado.getDistanciaKm(), 0.01);
            v.verificarNumero("Viaje 1: 1 nota de ajuste", 1, terminado.getNotasAjuste().size());
            v.verificarNumero("Viaje 1: nota de +200", 200, terminado.getNotasAjuste().get(0).getImporte());
            v.verificarIgual("Viaje 1: el ';' del motivo se reemplaza por ','", "Cobro de más, revisar",
                    terminado.getNotasAjuste().get(0).getMotivo());
            v.verificarVerdadero("Viaje 1: la nota conserva al operador", terminado.getNotasAjuste().get(0).getOperador() == c.obtenerOperador("L-1"));
            Viaje enCurso = c.getViajes().get(1);
            v.verificarIgual("Viaje 2: en curso", EstadoViaje.EN_CURSO, enCurso.getEstado());
            v.verificarVerdadero("Viaje 2: sin fin ni devolución", enCurso.getFin() == null && enCurso.getDevolucion() == null);
            v.verificarIgual("Viaje 2: es del monopatín", "MP-1", enCurso.getVehiculo().getCodigo());

            // vehículos
            v.verificarNumero("Carga: 3 vehículos", 3, c.getVehiculos().size());
            BicicletaComun comun = (BicicletaComun) c.obtenerVehiculo("BC-1");
            v.verificarIgual("BC-1: en reparación (orden manual abierta)", EstadoVehiculo.EN_REPARACION, comun.getEstado());
            v.verificarVerdadero("BC-1: guardada en B", comun.getEstacion() == c.obtenerEstacion("B"));
            v.verificarAproximado("BC-1: conserva los km", 111.19, comun.getKmRecorridos(), 0.01);
            v.verificarNumero("BC-1: conserva el rodado", 26, comun.getRodado());
            v.verificarVerdadero("BC-1: tiene una orden abierta", comun.tieneOrdenAbierta());
            BicicletaElectrica electrica = (BicicletaElectrica) c.obtenerVehiculo("BE-1");
            v.verificarIgual("BE-1: disponible después de la reparación", EstadoVehiculo.DISPONIBLE, electrica.getEstado());
            v.verificarNumero("BE-1: 3 reportes de falla", 3, electrica.getCantidadFallas());
            v.verificarNumero("BE-1: conserva la autonomía", 40, electrica.getAutonomiaKm());
            v.verificarNumero("BE-1: conserva la batería", 80, electrica.getNivelBateria());
            v.verificarVerdadero("BE-1: conserva la fecha de la última reparación",
                    LocalDate.of(2026, 11, 9).equals(electrica.getFechaUltimaReparacion()));
            Monopatin monopatin = (Monopatin) c.obtenerVehiculo("MP-1");
            v.verificarIgual("MP-1: en uso", EstadoVehiculo.EN_USO, monopatin.getEstado());
            v.verificarVerdadero("MP-1: no está en ninguna estación", monopatin.getEstacion() == null);
            v.verificarNumero("MP-1: conserva la velocidad máxima", 25, monopatin.getVelocidadMaxima());

            // estaciones: cada una tiene sus vehículos y no se generaron alertas al cargar
            v.verificarNumero("Estación A: tiene 1 vehículo", 1, c.obtenerEstacion("A").getVehiculos().size());
            v.verificarNumero("Estación B: tiene 1 vehículo", 1, c.obtenerEstacion("B").getVehiculos().size());
            v.verificarNumero("Estación A: la carga no genera alertas", 0, c.obtenerEstacion("A").getAlertasRedistribucion().size());

            // órdenes
            v.verificarNumero("Carga: 2 órdenes", 2, c.getOrdenes().size());
            OrdenMantenimiento cerrada = c.getOrdenes().get(0);
            v.verificarFalso("Orden 1: cerrada", cerrada.estaAbierta());
            v.verificarVerdadero("Orden 1: conserva la fecha de cierre", LocalDate.of(2026, 11, 9).equals(cerrada.getFechaCierre()));
            v.verificarIgual("Orden 1: conserva la tarea", "Cambio de motor", cerrada.getTareasRealizadas().get(0));
            v.verificarIgual("Orden 1: conserva el repuesto", "Motor 250W", cerrada.getRepuestosUtilizados().get(0));
            v.verificarVerdadero("Orden 2: abierta del técnico 2",
                    c.getOrdenes().get(1).estaAbierta() && c.getOrdenes().get(1).getTecnico() == c.obtenerTecnico("T-2"));
            v.verificarNumero("Técnico 2: 1 orden abierta", 1, c.obtenerTecnico("T-2").contarOrdenesAbiertas());
            v.verificarNumero("Técnico 1: 1 orden cerrada", 1, c.obtenerTecnico("T-1").getOrdenesCerradas().size());

            // las reglas siguen funcionando con lo cargado: el monopatín vuelve 30 minutos después de las 10:20
            RelojSimulado reloj = new RelojSimulado(ViajesDePrueba.INICIO.plusMinutes(50));
            ServicioViajes servicio = new ServicioViajes(new CalculadoraDeTarifa(new ArrayList<>()), c.getTarifas(), reloj);
            double importe = servicio.devolver(enCurso, c.obtenerEstacion("A"));
            v.verificarNumero("Después de cargar: 30 min de monopatín = 300 + 30*50 = 1800", 1800, importe);
            v.verificarNumero("Después de cargar: saldo 2200 - 1800 = 400", 400, ana.getCuenta().getSaldo());
            v.verificarIgual("Después de cargar: el monopatín queda disponible", EstadoVehiculo.DISPONIBLE, monopatin.getEstado());

            // guardar lo cargado da exactamente los mismos archivos (antes de las operaciones de arriba)
            DatosDelSistema otraCarga = new AlmacenDeDatos(carpeta1.getPath()).cargar();
            new AlmacenDeDatos(carpeta2.getPath()).guardar(otraCarga);
            for (String nombre : ARCHIVOS) {
                v.verificarIgual("Archivo " + nombre + ": cargar y volver a guardar no cambia nada",
                        leerTexto(new File(carpeta1, nombre)), leerTexto(new File(carpeta2, nombre)));
            }
        } catch (IOException e) {
            v.verificarVerdadero("No se pudo leer un archivo de prueba: " + e.getMessage(), false);
        } finally {
            borrar(carpeta1);
            borrar(carpeta2);
        }
    }

    private static void archivosInvalidos(Verificador v) {
        File carpeta = carpetaTemporal();
        try {
            // línea inválida: avisa archivo y número de línea
            escribirTexto(new File(carpeta, "tarifas.txt"),
                    "MONOPATIN;300;50;2026-01-01\nTIPO_RARO;1;2;2026-01-01\n");
            String mensaje = "";
            try {
                new AlmacenDeDatos(carpeta.getPath()).cargar();
            } catch (PersistenciaExcepcion e) {
                mensaje = e.getMessage();
            }
            v.verificarVerdadero("Archivo inválido: el mensaje nombra el archivo y la línea",
                    mensaje.contains("tarifas.txt") && mensaje.contains("línea 2"));
            v.verificarExcepcion("Archivo inválido: la carga falla con PersistenciaExcepcion", PersistenciaExcepcion.class,
                    () -> new AlmacenDeDatos(carpeta.getPath()).cargar());

            // referencia a una estación que no existe
            escribirTexto(new File(carpeta, "tarifas.txt"), "");
            escribirTexto(new File(carpeta, "vehiculos.txt"),
                    "BICICLETA_COMUN;BC-1;2026-01-01;0.0;DISPONIBLE;false;ZZ;26;-\n");
            String mensaje2 = "";
            try {
                new AlmacenDeDatos(carpeta.getPath()).cargar();
            } catch (PersistenciaExcepcion e) {
                mensaje2 = e.getMessage();
            }
            v.verificarVerdadero("Referencia rota: el mensaje nombra la estación inexistente",
                    mensaje2.contains("ZZ") && mensaje2.contains("vehiculos.txt"));

            // más vehículos que la capacidad de la estación
            escribirTexto(new File(carpeta, "estaciones.txt"), "A;Calle A;1;0.0;0.0\n");
            escribirTexto(new File(carpeta, "vehiculos.txt"),
                    "BICICLETA_COMUN;BC-1;2026-01-01;0.0;DISPONIBLE;false;A;26;-\n"
                  + "BICICLETA_COMUN;BC-2;2026-01-01;0.0;DISPONIBLE;false;A;26;-\n");
            v.verificarExcepcion("Estación sin lugar: la carga falla", PersistenciaExcepcion.class,
                    () -> new AlmacenDeDatos(carpeta.getPath()).cargar());
        } catch (IOException e) {
            v.verificarVerdadero("No se pudo escribir un archivo de prueba: " + e.getMessage(), false);
        } finally {
            borrar(carpeta);
        }
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}