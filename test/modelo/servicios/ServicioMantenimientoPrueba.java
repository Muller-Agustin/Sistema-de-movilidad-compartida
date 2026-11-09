package modelo.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.cuentas.Tarjeta;
import modelo.cuentas.Usuario;
import modelo.estaciones.Estacion;
import modelo.excepciones.OrdenIncompletaExcepcion;
import modelo.mantenimiento.OrdenMantenimiento;
import modelo.EstadoVehiculo;
import modelo.Operador;
import modelo.Tecnico;
import modelo.BicicletaComun;
import modelo.tarifas.CatalogoTarifas;
import modelo.viajes.Viaje;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class ServicioMantenimientoPrueba {

    private static class Escenario {
        final RelojSimulado reloj = new RelojSimulado(ViajesDePrueba.INICIO);
        final ServicioMantenimiento servicio = new ServicioMantenimiento(reloj);
        final Tecnico t1 = new Tecnico("T-1", "Tomás", "1111");
        final Tecnico t2 = new Tecnico("T-2", "Tania", "2222");
        final Operador operador = new Operador("L-1", "Luis", "3333");
        final Usuario usuario = ViajesDePrueba.usuario();

        Escenario(boolean conTecnicos) {
            if (conTecnicos) {
                servicio.agregarTecnico(t1);
                servicio.agregarTecnico(t2);
            }
        }

        BicicletaComun bici(String codigo) {
            return new BicicletaComun(codigo, LocalDate.of(2026, 1, 1), 26);
        }

        // 3 reportes en 3 días seguidos
        void reportarTres(BicicletaComun bici) {
            servicio.reportarFalla(usuario, bici, "Freno");
            reloj.avanzarHoras(24);
            servicio.reportarFalla(usuario, bici, "Cadena");
            reloj.avanzarHoras(24);
            servicio.reportarFalla(usuario, bici, "Rueda");
        }
    }

    public static void ejecutar(Verificador v) {
        asignacion(v);
        ordenAutomatica(v);
        sinTecnicos(v);
        ordenManual(v);
        cierre(v);
        fallaDuranteUnViaje(v);
    }

    // RF31
    private static void asignacion(Verificador v) {
        Escenario vacio = new Escenario(false);
        v.verificarExcepcion("Asignación: sin técnicos falla", IllegalStateException.class,
                () -> vacio.servicio.tecnicoConMenosOrdenes());

        Escenario e = new Escenario(true);
        v.verificarVerdadero("Asignación: con empate gana el primero registrado", e.servicio.tecnicoConMenosOrdenes() == e.t1);
        e.servicio.generarOrdenAutomatica(e.operador, e.bici("BC-1"));
        v.verificarVerdadero("Asignación: la primera orden va al técnico 1", e.t1.contarOrdenesAbiertas() == 1);
        v.verificarVerdadero("Asignación: ahora el técnico 2 tiene menos", e.servicio.tecnicoConMenosOrdenes() == e.t2);
        e.servicio.generarOrdenAutomatica(e.operador, e.bici("BC-2"));
        v.verificarNumero("Asignación: la segunda va al técnico 2", 1, e.t2.contarOrdenesAbiertas());
        e.servicio.generarOrdenAutomatica(e.operador, e.bici("BC-3"));
        v.verificarNumero("Asignación: con 1 y 1 la tercera vuelve al técnico 1", 2, e.t1.contarOrdenesAbiertas());

        v.verificarExcepcion("Asignación: legajo repetido falla", IllegalArgumentException.class,
                () -> e.servicio.agregarTecnico(new Tecnico("T-1", "Otro", "9999")));
    }

    // RN10 + S3: la tercera falla en 30 días genera la orden
    private static void ordenAutomatica(Verificador v) {
        Escenario e = new Escenario(true);
        BicicletaComun bici = e.bici("BC-1");
        e.servicio.reportarFalla(e.usuario, bici, "Freno");
        e.reloj.avanzarHoras(24);
        e.servicio.reportarFalla(e.usuario, bici, "Cadena");
        v.verificarNumero("Automática: con 2 fallas no hay orden", 0, e.servicio.getOrdenes().size());
        v.verificarIgual("Automática: con 2 fallas sigue disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        e.reloj.avanzarHoras(24);
        e.servicio.reportarFalla(e.usuario, bici, "Rueda");

        v.verificarNumero("Automática: la tercera falla genera 1 orden", 1, e.servicio.getOrdenes().size());
        v.verificarIgual("Automática: el vehículo pasa a reparación", EstadoVehiculo.EN_REPARACION, bici.getEstado());
        v.verificarVerdadero("Automática: se asigna al técnico con menos órdenes", e.servicio.getOrdenes().get(0).getTecnico() == e.t1);
        v.verificarVerdadero("Automática: la fecha es la del sistema",
                LocalDate.of(2026, 11, 11).equals(e.servicio.getOrdenes().get(0).getFechaApertura()));

        e.servicio.reportarFalla(e.usuario, bici, "Otra");
        v.verificarNumero("Automática: una cuarta falla no repite la orden", 1, e.servicio.getOrdenes().size());

        BicicletaComun segunda = e.bici("BC-2");
        e.reportarTres(segunda);
        v.verificarVerdadero("Automática: la siguiente orden va al otro técnico", e.t2.contarOrdenesAbiertas() == 1);
        v.verificarNumero("Automática: hay 2 órdenes abiertas", 2, e.servicio.getOrdenesAbiertas().size());

        // un operador también puede reportar
        BicicletaComun tercera = e.bici("BC-3");
        e.servicio.reportarFalla(e.operador, tercera, "Asiento");
        v.verificarNumero("Automática: el reporte del operador cuenta", 1, tercera.getCantidadFallas());
    }

    private static void sinTecnicos(Verificador v) {
        Escenario e = new Escenario(false);
        BicicletaComun bici = e.bici("BC-1");
        e.reportarTres(bici);
        v.verificarIgual("Sin técnicos: el vehículo queda en reparación", EstadoVehiculo.EN_REPARACION, bici.getEstado());
        v.verificarNumero("Sin técnicos: no se genera ninguna orden", 0, e.servicio.getOrdenes().size());

        e.servicio.agregarTecnico(e.t1);
        e.servicio.generarOrdenAutomatica(e.operador, bici);
        v.verificarNumero("Sin técnicos: después el operador genera la orden", 1, e.servicio.getOrdenes().size());
    }

    // RF19
    private static void ordenManual(Verificador v) {
        Escenario e = new Escenario(true);
        BicicletaComun bici = e.bici("BC-1");
        OrdenMantenimiento orden = e.servicio.generarOrden(e.operador, bici, e.t2);

        v.verificarVerdadero("Manual: queda asignada al técnico elegido", orden.getTecnico() == e.t2);
        v.verificarIgual("Manual: el vehículo pasa a reparación", EstadoVehiculo.EN_REPARACION, bici.getEstado());
        v.verificarNumero("Manual: el servicio la registra", 1, e.servicio.getOrdenes().size());
        v.verificarExcepcion("Manual: otra orden abierta del mismo vehículo falla", IllegalStateException.class,
                () -> e.servicio.generarOrden(e.operador, bici, e.t1));
        v.verificarExcepcion("Manual: un técnico no registrado falla", IllegalArgumentException.class,
                () -> e.servicio.generarOrden(e.operador, e.bici("BC-2"), new Tecnico("T-9", "Ajeno", "0")));
        v.verificarNumero("Manual: las rechazadas no se registran", 1, e.servicio.getOrdenes().size());
    }

    // RF20 + RN11
    private static void cierre(Verificador v) {
        Escenario e = new Escenario(true);
        BicicletaComun bici = e.bici("BC-1");
        e.reportarTres(bici);                       // fallas el 9, 10 y 11/11 => orden el 11/11
        OrdenMantenimiento orden = e.servicio.getOrdenes().get(0);

        v.verificarExcepcion("Cierre: otro técnico no la cierra", IllegalArgumentException.class,
                () -> e.servicio.cerrarOrden(e.t2, orden, List.of("Algo"), List.of()));
        v.verificarExcepcion("Cierre: sin tareas no se cierra (RN11)", OrdenIncompletaExcepcion.class,
                () -> e.servicio.cerrarOrden(e.t1, orden, new ArrayList<>(), List.of("Cable")));

        v.verificarNumero("Cierre: el intento fallido no deja repuestos cargados", 0, orden.getRepuestosUtilizados().size());

        e.reloj.avanzarHoras(24);                   // 12/11
        e.servicio.cerrarOrden(e.t1, orden, List.of("Cambio de cable", "Ajuste de frenos"), List.of("Cable", "Pastillas"));
        v.verificarFalso("Cierre: la orden queda cerrada", orden.estaAbierta());
        v.verificarVerdadero("Cierre: la fecha es la del sistema", LocalDate.of(2026, 11, 12).equals(orden.getFechaCierre()));
        v.verificarNumero("Cierre: se registran las 2 tareas", 2, orden.getTareasRealizadas().size());
        v.verificarNumero("Cierre: se registran los 2 repuestos", 2, orden.getRepuestosUtilizados().size());
        v.verificarIgual("Cierre: el vehículo vuelve a estar disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        v.verificarNumero("Cierre: ya no hay órdenes abiertas", 0, e.servicio.getOrdenesAbiertas().size());
        v.verificarNumero("Cierre: el técnico tiene 1 cerrada", 1, e.t1.getOrdenesCerradas().size());

        // S20: los reportes anteriores ya están atendidos
        e.reloj.avanzarHoras(24);                   // 13/11
        e.servicio.reportarFalla(e.usuario, bici, "Nueva falla");
        v.verificarIgual("Cierre: una falla nueva no la manda otra vez a reparación", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        v.verificarNumero("Cierre: no se generó otra orden", 1, e.servicio.getOrdenes().size());
    }

    // S20: de punta a punta, con la falla llegando en pleno viaje
    private static void fallaDuranteUnViaje(Verificador v) {
        Escenario e = new Escenario(true);
        LocalDate desde = LocalDate.of(2026, 1, 1);
        ServicioViajes viajes = new ServicioViajes(new CalculadoraDeTarifa(new ArrayList<>()),
                CatalogoTarifas.conValoresDeReferencia(desde), e.reloj);
        Estacion a = ViajesDePrueba.estacion("A", 10);
        Estacion b = ViajesDePrueba.estacion("B", 10);
        BicicletaComun bici = e.bici("BC-1");
        a.recibirVehiculo(bici, e.reloj.ahora());
        Tarjeta tarjeta = new Tarjeta("Ana", "1234");
        e.usuario.getCuenta().agregarMedioPago(tarjeta);
        e.usuario.recargarSaldo(2000, tarjeta, e.reloj.ahora());

        Viaje viaje = viajes.retirar(e.usuario, bici, a);
        e.servicio.reportarFalla(e.usuario, bici, "Freno");
        e.servicio.reportarFalla(e.usuario, bici, "Cadena");
        e.servicio.reportarFalla(e.usuario, bici, "Rueda");

        v.verificarIgual("Viaje con falla: el viaje no se corta", EstadoVehiculo.EN_USO, bici.getEstado());
        v.verificarNumero("Viaje con falla: la orden se genera igual", 1, e.servicio.getOrdenes().size());
        OrdenMantenimiento orden = e.servicio.getOrdenes().get(0);
        v.verificarExcepcion("Viaje con falla: no se cierra mientras está en viaje", IllegalStateException.class,
                () -> e.servicio.cerrarOrden(e.t1, orden, List.of("Ajuste"), List.of()));
        v.verificarNumero("Viaje con falla: el cierre fallido no deja tareas cargadas", 0, orden.getTareasRealizadas().size());

        e.reloj.avanzarMinutos(20);
        viajes.devolver(viaje, b);
        v.verificarIgual("Viaje con falla: al devolverla va a reparación", EstadoVehiculo.EN_REPARACION, bici.getEstado());
        v.verificarNumero("Viaje con falla: la estación B no la ofrece", 0, b.getVehiculosDisponibles().size());
        v.verificarNumero("Viaje con falla: pero la tiene guardada", 1, b.getVehiculos().size());

        e.servicio.cerrarOrden(e.t1, orden, List.of("Ajuste general"), List.of());
        v.verificarIgual("Viaje con falla: reparada vuelve a estar disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        v.verificarNumero("Viaje con falla: la estación B la ofrece de nuevo", 1, b.getVehiculosDisponibles().size());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}