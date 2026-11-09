import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import EstacionesTraslados.Coordenada;
import EstacionesTraslados.Estacion;
import EstacionesTraslados.TipoAlerta;
import Excepciones.EstacionLlenaExcepcion;
import Excepciones.OrdenIncompletaExcepcion;
import Excepciones.RetiroNoPermitidoExcepcion;
import Excepciones.VehiculoNoDisponibleExcepcion;
import GeneralPersistencia.GestorArchivos;
import GeneralPersistencia.SistemaMovilidad;
import PersonalServicio.Tecnico;
import UsuariosCuenta.Tarjeta;
import UsuariosCuenta.Usuario;
import VehiculoMantenimiento.BicicletaComun;
import VehiculoMantenimiento.BicicletaElectrica;
import VehiculoMantenimiento.ControladorMantenimiento;
import VehiculoMantenimiento.EstadoVehiculo;
import VehiculoMantenimiento.OrdenMantenimiento;
import VehiculoMantenimiento.TipoVehiculo;
import VehiculoMantenimiento.Vehiculo;
import ViajesTarifas.EstadoViaje;
import ViajesTarifas.PromoPlanMensual;
import ViajesTarifas.PromoPrimerViaje;
import ViajesTarifas.Promocion;
import ViajesTarifas.Tarifa;
import ViajesTarifas.Viaje;

// Pruebas unitarias de las reglas de negocio. Se ejecutan con el main de esta clase
// (sin JUnit). Cada prueba usa horarios fijos para que el resultado sea siempre el mismo.

public class PruebasUnitarias {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 1);
    private static final LocalDateTime HORA = LocalDateTime.of(2026, 11, 9, 10, 0);

    private static int aprobadas = 0;
    private static int falladas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Pruebas unitarias - Movilidad compartida ===\n");

        pruebaTarifa();
        pruebaSaldoMinimo();
        pruebaViajeEnCurso();
        pruebaBateriaBaja();
        pruebaPrimerViaje();
        pruebaPlanMensual();
        pruebaSaldoNegativo();
        pruebaEstacionLlena();
        pruebaCancelacion();
        pruebaTresFallas();
        pruebaCierreOrden();
        pruebaAlertaRedistribucion();
        pruebaOrdenamiento();
        pruebaPersistencia();

        System.out.println("\nResultado: " + aprobadas + " aprobadas, " + falladas + " falladas.");
    }

    // Muestra el resultado de una verificación
    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            aprobadas++;
            System.out.println("  OK     " + descripcion);
        } else {
            falladas++;
            System.out.println("  FALLA  " + descripcion);
        }
    }

    // Datos que usan varias pruebas
    private static Tarifa tarifaBici() {
        return new Tarifa(TipoVehiculo.BICICLETA_COMUN, 200, 20, DIA);
    }

    private static Estacion estacion(String codigo, int capacidad) {
        return new Estacion(codigo, "Calle " + codigo, capacidad, new Coordenada(-34.60, -58.38));
    }

    private static Usuario usuarioConSaldo(double saldo) {
        Usuario usuario = new Usuario("Ana", "30111222", "ana@mail.com", DIA);
        Tarjeta tarjeta = new Tarjeta("Ana", "1234");
        usuario.getCuenta().agregarMedioPago(tarjeta);
        if (saldo > 0) {
            usuario.recargarSaldo(saldo, tarjeta, HORA);
        }
        return usuario;
    }

    private static List<Promocion> promociones() {
        List<Promocion> lista = new ArrayList<>();
        lista.add(new PromoPrimerViaje("Primer viaje", DIA, DIA.plusYears(1)));
        lista.add(new PromoPlanMensual("Plan mensual", DIA, DIA.plusYears(1)));
        return lista;
    }

    // TU-01 | RN17 y RN19: desbloqueo + precio por minuto; después de 120 min, 50 % de recargo
    private static void pruebaTarifa() {
        System.out.println("TU-01 Tarifa (RN17, RN19)");
        Tarifa tarifa = tarifaBici();
        verificar("30 minutos cuestan 200 + 20 x 30 = 800", tarifa.calcularCosto(30) == 800);
        verificar("130 minutos cuestan 200 + 20 x 120 + 30 x 10 = 2900", tarifa.calcularCosto(130) == 2900);
    }

    // TU-02 | RN01: sin el saldo mínimo (30 minutos de uso) no se puede retirar
    private static void pruebaSaldoMinimo() throws Exception {
        System.out.println("TU-02 Saldo mínimo para retirar (RN01)");
        Estacion e1 = estacion("E1", 10);
        Vehiculo bici = new BicicletaComun("B1", DIA, 26);
        e1.recibirVehiculo(bici, HORA);
        Usuario usuario = usuarioConSaldo(799);
        try {
            usuario.iniciarViaje(1, bici, e1, tarifaBici(), HORA);
            verificar("con $799 se rechaza el retiro", false);
        } catch (RetiroNoPermitidoExcepcion ex) {
            verificar("con $799 se rechaza el retiro (" + ex.getMessage() + ")", true);
        }
        verificar("la bicicleta sigue disponible en la estación", bici.getEstado() == EstadoVehiculo.DISPONIBLE && bici.getEstacion() == e1);
    }

    // TU-03 | RN02: no se puede tener dos viajes abiertos
    private static void pruebaViajeEnCurso() throws Exception {
        System.out.println("TU-03 Un solo viaje en curso (RN02)");
        Estacion e1 = estacion("E1", 10);
        Vehiculo bici1 = new BicicletaComun("B1", DIA, 26);
        Vehiculo bici2 = new BicicletaComun("B2", DIA, 26);
        e1.recibirVehiculo(bici1, HORA);
        e1.recibirVehiculo(bici2, HORA);
        Usuario usuario = usuarioConSaldo(5000);
        usuario.iniciarViaje(1, bici1, e1, tarifaBici(), HORA);
        verificar("el primer retiro deja la bicicleta en uso y fuera de la estación",
                bici1.getEstado() == EstadoVehiculo.EN_USO && bici1.getEstacion() == null);
        try {
            usuario.iniciarViaje(2, bici2, e1, tarifaBici(), HORA);
            verificar("el segundo retiro se rechaza", false);
        } catch (RetiroNoPermitidoExcepcion ex) {
            verificar("el segundo retiro se rechaza", true);
        }
    }

    // TU-04 | RN08: un vehículo eléctrico con menos del 15 % de batería no se ofrece
    private static void pruebaBateriaBaja() throws Exception {
        System.out.println("TU-04 Batería baja (RN08)");
        Estacion e1 = estacion("E1", 10);
        BicicletaElectrica electrica = new BicicletaElectrica("BE1", DIA, 10, 60);
        e1.recibirVehiculo(electrica, HORA);
        verificar("con 10 % queda en BATERIA_BAJA", electrica.getEstado() == EstadoVehiculo.BATERIA_BAJA);
        try {
            usuarioConSaldo(5000).iniciarViaje(1, electrica, e1, tarifaBici(), HORA);
            verificar("no se puede retirar", false);
        } catch (VehiculoNoDisponibleExcepcion ex) {
            verificar("no se puede retirar", true);
        }
        electrica.recargarBateria();
        verificar("al recargar vuelve a DISPONIBLE", electrica.getEstado() == EstadoVehiculo.DISPONIBLE);
    }

    // TU-05 | RN21: el primer viaje es gratis hasta 15 minutos
    private static void pruebaPrimerViaje() throws Exception {
        System.out.println("TU-05 Primer viaje gratis (RN21)");
        Estacion e1 = estacion("E1", 10);
        Vehiculo bici = new BicicletaComun("B1", DIA, 26);
        e1.recibirVehiculo(bici, HORA);
        Usuario usuario = usuarioConSaldo(5000);
        usuario.iniciarViaje(1, bici, e1, tarifaBici(), HORA);
        double importe = usuario.finalizarViaje(e1, promociones(), HORA.plusMinutes(20));
        verificar("20 minutos: 600 - 500 (15 min gratis) = 100", importe == 100);
        verificar("el saldo baja de 5000 a 4900", usuario.getCuenta().getSaldo() == 4900);

        usuario.iniciarViaje(2, bici, e1, tarifaBici(), HORA.plusHours(1));
        importe = usuario.finalizarViaje(e1, promociones(), HORA.plusHours(1).plusMinutes(20));
        verificar("el segundo viaje de 20 minutos ya se cobra completo (600)", importe == 600);
    }

    // TU-06 | RN22 y RN23: el plan cubre 30 minutos; las promociones no se acumulan
    private static void pruebaPlanMensual() throws Exception {
        System.out.println("TU-06 Plan mensual (RN22, RN23)");
        Estacion e1 = estacion("E1", 10);
        Vehiculo bici = new BicicletaComun("B1", DIA, 26);
        e1.recibirVehiculo(bici, HORA);
        Usuario usuario = usuarioConSaldo(10000);
        usuario.suscribirPlan(HORA);
        verificar("la suscripción cobra 8000", usuario.getCuenta().getSaldo() == 2000);

        usuario.iniciarViaje(1, bici, e1, tarifaBici(), HORA);
        Viaje viaje = usuario.getViajeEnCurso();
        double importe = usuario.finalizarViaje(e1, promociones(), HORA.plusMinutes(40));
        verificar("40 minutos: 1000 - 800 (plan) = 200, más favorable que el primer viaje", importe == 200);
        verificar("se eligió el plan mensual", viaje.getPromocion() instanceof PromoPlanMensual);
        verificar("el plan cuenta 1 viaje usado", usuario.getPlanMensual().getViajesUsados() == 1);
    }

    // TU-07 | RN03: si el saldo queda negativo, el usuario queda inhabilitado
    private static void pruebaSaldoNegativo() {
        System.out.println("TU-07 Saldo negativo (RN03)");
        Usuario usuario = usuarioConSaldo(1000);
        usuario.cobrar(1500, "Viaje", HORA);
        verificar("saldo -500 e inhabilitado", usuario.getCuenta().getSaldo() == -500 && !usuario.estaHabilitado());
        usuario.recargarSaldo(500, usuario.getCuenta().getMediosPago().get(0), HORA);
        verificar("al recargar y quedar en 0 se vuelve a habilitar", usuario.estaHabilitado());
    }

    // TU-08 | RN13 y RN15: no se devuelve en una estación llena; se dan 10 minutos sin cargo
    private static void pruebaEstacionLlena() throws Exception {
        System.out.println("TU-08 Estación llena (RN13, RN15)");
        Estacion e1 = estacion("E1", 10);
        Estacion llena = estacion("E2", 1);
        Vehiculo bici = new BicicletaComun("B1", DIA, 26);
        e1.recibirVehiculo(bici, HORA);
        llena.recibirVehiculo(new BicicletaComun("B2", DIA, 26), HORA);
        Usuario usuario = usuarioConSaldo(5000);
        List<Promocion> sinPromociones = new ArrayList<>();   // para ver solo el efecto de los 10 minutos
        usuario.iniciarViaje(1, bici, e1, tarifaBici(), HORA);
        try {
            usuario.finalizarViaje(llena, sinPromociones, HORA.plusMinutes(30));
            verificar("devolver en la estación llena se rechaza", false);
        } catch (EstacionLlenaExcepcion ex) {
            verificar("devolver en la estación llena se rechaza", true);
        }
        double importe = usuario.finalizarViaje(e1, sinPromociones, HORA.plusMinutes(38));
        verificar("38 minutos - 8 sin cargo = 30 minutos: 800", importe == 800);
    }

    // TU-09 | RN18: un retiro se cancela sin cargo solo dentro de los 2 minutos
    private static void pruebaCancelacion() throws Exception {
        System.out.println("TU-09 Cancelación (RN18)");
        Estacion e1 = estacion("E1", 10);
        Vehiculo bici = new BicicletaComun("B1", DIA, 26);
        e1.recibirVehiculo(bici, HORA);
        Usuario usuario = usuarioConSaldo(5000);
        Viaje viaje = usuario.iniciarViaje(1, bici, e1, tarifaBici(), HORA);
        try {
            viaje.cancelar(HORA.plusMinutes(3));
            verificar("a los 3 minutos no se puede cancelar", false);
        } catch (IllegalStateException ex) {
            verificar("a los 3 minutos no se puede cancelar", true);
        }
        viaje.cancelar(HORA.plusMinutes(1));
        verificar("al minuto se cancela y la bicicleta vuelve a la estación",
                viaje.getEstado() == EstadoViaje.CANCELADO && bici.getEstacion() == e1);
        verificar("no se cobró nada", usuario.getCuenta().getSaldo() == 5000);
    }

    // TU-10 | RN10: con 3 reportes de falla en 30 días el vehículo pasa a mantenimiento
    private static void pruebaTresFallas() throws Exception {
        System.out.println("TU-10 Tres fallas (RN10)");
        ControladorMantenimiento controlador = new ControladorMantenimiento();
        controlador.registrarTecnico(new Tecnico("T1", "Carlos", "111"));
        Vehiculo bici = new BicicletaComun("B1", DIA, 26);
        controlador.registrarVehiculo(bici, estacion("E1", 10));
        Usuario usuario = usuarioConSaldo(0);
        controlador.reportarFalla(usuario, bici, "freno");
        controlador.reportarFalla(usuario, bici, "cadena");
        verificar("con 2 fallas sigue disponible", bici.getEstado() == EstadoVehiculo.DISPONIBLE);
        controlador.reportarFalla(usuario, bici, "rueda");
        verificar("con 3 fallas pasa a EN_REPARACION", bici.getEstado() == EstadoVehiculo.EN_REPARACION);
        verificar("se generó una orden automática", controlador.listarOrdenesAbiertas().size() == 1);
    }

    // TU-11 | RN11: una orden no se cierra sin tareas realizadas
    private static void pruebaCierreOrden() throws Exception {
        System.out.println("TU-11 Cierre de orden (RN11)");
        Vehiculo bici = new BicicletaComun("B1", DIA, 26);
        bici.enviarAReparacion();
        OrdenMantenimiento orden = new OrdenMantenimiento(bici, new Tecnico("T1", "Carlos", "111"), DIA);
        try {
            orden.cerrar(DIA.plusDays(1), new ArrayList<>(), new ArrayList<>());
            verificar("sin tareas no se cierra", false);
        } catch (OrdenIncompletaExcepcion ex) {
            verificar("sin tareas no se cierra (" + ex.getMessage() + ")", true);
        }
        List<String> tareas = new ArrayList<>();
        tareas.add("cambio de freno");
        orden.cerrar(DIA.plusDays(1), tareas, new ArrayList<>());
        verificar("con tareas se cierra y el vehículo vuelve a DISPONIBLE",
                !orden.estaAbierta() && bici.getEstado() == EstadoVehiculo.DISPONIBLE);
    }

    // TU-12 | RN16: menos del 20 % o más del 90 % de ocupación genera una alerta
    private static void pruebaAlertaRedistribucion() throws Exception {
        System.out.println("TU-12 Alertas de redistribución (RN16)");
        Estacion e1 = estacion("E1", 10);
        e1.recibirVehiculo(new BicicletaComun("B1", DIA, 26), HORA);
        verificar("con 1 de 10 (10 %) hay alerta VACIA", e1.getAlertas().get(0).getTipo() == TipoAlerta.VACIA);
        for (int i = 2; i <= 10; i++) {
            e1.recibirVehiculo(new BicicletaComun("B" + i, DIA, 26), HORA);
        }
        verificar("con 10 de 10 (100 %) hay alerta SATURADA", e1.getAlertas().get(1).getTipo() == TipoAlerta.SATURADA);
        verificar("solo 2 alertas: no se repiten mientras sigue en la misma zona", e1.getAlertas().size() == 2);
    }

    // TU-13 | Comparable (orden natural por código) y Comparator (más fallas primero)
    private static void pruebaOrdenamiento() throws Exception {
        System.out.println("TU-13 Ordenamiento (Comparable y Comparator)");
        ControladorMantenimiento controlador = new ControladorMantenimiento();
        Estacion e1 = estacion("E1", 10);
        Vehiculo b3 = new BicicletaComun("B3", DIA, 26);
        Vehiculo b1 = new BicicletaComun("B1", DIA, 26);
        Vehiculo b2 = new BicicletaComun("B2", DIA, 26);
        controlador.registrarVehiculo(b3, e1);
        controlador.registrarVehiculo(b1, e1);
        controlador.registrarVehiculo(b2, e1);
        Usuario usuario = usuarioConSaldo(0);
        controlador.reportarFalla(usuario, b2, "freno");
        controlador.reportarFalla(usuario, b2, "luz");
        controlador.reportarFalla(usuario, b3, "cadena");
        List<Vehiculo> porCodigo = controlador.listarVehiculos();
        verificar("orden natural: B1, B2, B3", porCodigo.get(0) == b1 && porCodigo.get(1) == b2 && porCodigo.get(2) == b3);
        List<Vehiculo> porFallas = controlador.listarVehiculosConMasFallas();
        verificar("por fallas: B2 (2), B3 (1), B1 (0)", porFallas.get(0) == b2 && porFallas.get(1) == b3 && porFallas.get(2) == b1);
    }

    // TU-14 | Persistencia: lo guardado se recupera al cargar, con las relaciones intactas
    private static void pruebaPersistencia() throws Exception {
        System.out.println("TU-14 Persistencia en archivo");
        String ruta = "datos_prueba/sistema_prueba.dat";
        GestorArchivos gestor = new GestorArchivos(ruta);
        SistemaMovilidad sistema = new SistemaMovilidad();
        Estacion e1 = estacion("E1", 10);
        sistema.getEstaciones().registrarEstacion(e1);
        sistema.getMantenimiento().registrarVehiculo(new BicicletaComun("B1", DIA, 26), e1);
        sistema.getUsuarios().registrarUsuario(usuarioConSaldo(1234));
        gestor.guardar(sistema);

        SistemaMovilidad cargado = gestor.cargar();
        Usuario usuario = cargado.getUsuarios().buscarUsuario("30111222");
        Vehiculo bici = cargado.getMantenimiento().buscarVehiculo("B1");
        verificar("se recupera el usuario con su saldo", usuario != null && usuario.getCuenta().getSaldo() == 1234);
        verificar("el vehículo sigue en su estación (mismo objeto)",
                bici.getEstacion() == cargado.getEstaciones().buscarEstacion("E1"));

        new File(ruta).delete();
        new File("datos_prueba").delete();
    }
}