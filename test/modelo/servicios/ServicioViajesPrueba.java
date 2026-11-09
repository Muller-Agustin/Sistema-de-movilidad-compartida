package modelo.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.cuentas.EstadoUsuario;
import modelo.cuentas.Tarjeta;
import modelo.cuentas.Usuario;
import modelo.estaciones.Estacion;
import modelo.excepciones.EstacionLlenaExcepcion;
import modelo.excepciones.RetiroNoPermitidoExcepcion;
import modelo.BicicletaComun;
import modelo.EstadoVehiculo;
import modelo.Operador;
import modelo.tarifas.CatalogoTarifas;
import modelo.tarifas.PromoPlanMensual;
import modelo.tarifas.PromoPrimerViaje;
import modelo.tarifas.Promocion;
import modelo.viajes.EstadoViaje;
import modelo.viajes.Viaje;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class ServicioViajesPrueba {
    private static final LocalDate DESDE = LocalDate.of(2026, 1, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 12, 31);

    // Todo lo que necesita cada prueba: un reloj, el servicio, dos estaciones, una bici en la A y un usuario
    private static class Escenario {
        final RelojSimulado reloj = new RelojSimulado(ViajesDePrueba.INICIO);
        final Estacion a = ViajesDePrueba.estacion("A", 10);
        final Estacion b = ViajesDePrueba.estacion("B", 10);
        final BicicletaComun bici = ViajesDePrueba.bicicleta();
        final Usuario usuario;
        final ServicioViajes servicio;

        Escenario(Usuario usuario, double saldoInicial, boolean conPromociones) {
            this.usuario = usuario;
            List<Promocion> promos = new ArrayList<>();
            if (conPromociones) {
                promos.add(new PromoPrimerViaje("Primer viaje", DESDE, HASTA));
                promos.add(new PromoPlanMensual("Plan mensual", DESDE, HASTA));
            }
            servicio = new ServicioViajes(new CalculadoraDeTarifa(promos),
                    CatalogoTarifas.conValoresDeReferencia(DESDE), reloj);
            a.recibirVehiculo(bici, reloj.ahora());
            if (saldoInicial > 0) {
                Tarjeta tarjeta = new Tarjeta("Ana", "1234");
                usuario.getCuenta().agregarMedioPago(tarjeta);
                usuario.recargarSaldo(saldoInicial, tarjeta, reloj.ahora());
            }
        }
    }

    public static void ejecutar(Verificador v) {
        retirar(v);
        retirosRechazados(v);
        devolverConPromociones(v);
        devolverSinPromociones(v);
        estacionLlena(v);
        cancelar(v);
        vehiculosPerdidos(v);
        ajustes(v);
    }

    private static void retirar(Verificador v) {
        Escenario e = new Escenario(ViajesDePrueba.usuario(), 1000, false);
        Viaje viaje = e.servicio.retirar(e.usuario, e.bici, e.a);

        v.verificarIgual("Retirar: el viaje queda en curso", EstadoViaje.EN_CURSO, viaje.getEstado());
        v.verificarIgual("Retirar: el vehículo queda en uso", EstadoVehiculo.EN_USO, e.bici.getEstado());
        v.verificarNumero("Retirar: la estación queda sin el vehículo", 0, e.a.getVehiculos().size());
        v.verificarVerdadero("Retirar: el vehículo ya no está en ninguna estación (RN12)", e.bici.getEstacion() == null);
        v.verificarVerdadero("Retirar: el usuario tiene un viaje en curso", e.usuario.tieneViajeEnCurso());
        v.verificarNumero("Retirar: se toma la tarifa vigente ($20 por minuto)", 20, viaje.getTarifa().getPrecioPorMinuto());
        v.verificarNumero("Retirar: el servicio lista 1 viaje en curso", 1, e.servicio.getViajesEnCurso().size());
    }

    private static void retirosRechazados(Verificador v) {
        // RN01: sin saldo mínimo (800 para una bici común)
        Escenario sinSaldo = new Escenario(ViajesDePrueba.usuario(), 0, false);
        v.verificarExcepcion("Retirar: sin saldo falla", RetiroNoPermitidoExcepcion.class,
                () -> sinSaldo.servicio.retirar(sinSaldo.usuario, sinSaldo.bici, sinSaldo.a));
        v.verificarIgual("Retirar: si falla el vehículo sigue disponible", EstadoVehiculo.DISPONIBLE, sinSaldo.bici.getEstado());
        v.verificarNumero("Retirar: si falla el vehículo sigue en la estación", 1, sinSaldo.a.getVehiculos().size());

        Escenario justo = new Escenario(ViajesDePrueba.usuario(), 799, false);
        v.verificarExcepcion("Retirar: con 799 falla (mínimo 800)", RetiroNoPermitidoExcepcion.class,
                () -> justo.servicio.retirar(justo.usuario, justo.bici, justo.a));

        // RN02: ya tiene un viaje en curso
        Escenario conViaje = new Escenario(ViajesDePrueba.usuario(), 5000, false);
        BicicletaComun otraBici = ViajesDePrueba.bicicleta();
        conViaje.a.recibirVehiculo(otraBici, conViaje.reloj.ahora());
        conViaje.servicio.retirar(conViaje.usuario, conViaje.bici, conViaje.a);
        v.verificarExcepcion("Retirar: con un viaje en curso falla", RetiroNoPermitidoExcepcion.class,
                () -> conViaje.servicio.retirar(conViaje.usuario, otraBici, conViaje.a));

        // RN07: el vehículo tiene que estar disponible
        Escenario enReparacion = new Escenario(ViajesDePrueba.usuario(), 1000, false);
                try {
                        var setter = enReparacion.bici.getClass().getDeclaredMethod("setEstado", EstadoVehiculo.class);
                        setter.setAccessible(true);
                        setter.invoke(enReparacion.bici, EstadoVehiculo.EN_REPARACION);
                } catch (ReflectiveOperationException ex) {
                        throw new AssertionError(ex);
                }
        v.verificarExcepcion("Retirar: un vehículo en reparación falla", IllegalStateException.class,
                () -> enReparacion.servicio.retirar(enReparacion.usuario, enReparacion.bici, enReparacion.a));

        // el vehículo tiene que estar en la estación indicada
        Escenario otraEstacion = new Escenario(ViajesDePrueba.usuario(), 1000, false);
        v.verificarExcepcion("Retirar: de una estación donde no está falla", IllegalArgumentException.class,
                () -> otraEstacion.servicio.retirar(otraEstacion.usuario, otraEstacion.bici, otraEstacion.b));

        // RN04: usuario inhabilitado
        Escenario inhabilitado = new Escenario(ViajesDePrueba.usuario(), 0, false);
        inhabilitado.usuario.cobrar(100, "Deuda", inhabilitado.reloj.ahora());
        v.verificarExcepcion("Retirar: un usuario inhabilitado falla", RetiroNoPermitidoExcepcion.class,
                () -> inhabilitado.servicio.retirar(inhabilitado.usuario, inhabilitado.bici, inhabilitado.a));
    }

    private static void devolverConPromociones(Verificador v) {
        // primer viaje de 20 min: cuesta 600, la promo descuenta 500 => se paga 100, saldo 900
        Escenario e = new Escenario(ViajesDePrueba.usuario(), 1000, true);
        Viaje viaje = e.servicio.retirar(e.usuario, e.bici, e.a);
        e.reloj.avanzarMinutos(20);
        double importe = e.servicio.devolver(viaje, e.b);

        v.verificarNumero("Devolver: primer viaje de 20 min se paga 100", 100, importe);
        v.verificarNumero("Devolver: el saldo queda en 900", 900, e.usuario.getCuenta().getSaldo());
        v.verificarIgual("Devolver: el viaje queda finalizado", EstadoViaje.FINALIZADO, viaje.getEstado());
        v.verificarVerdadero("Devolver: el vehículo queda en la estación B", e.bici.getEstacion() == e.b);
        v.verificarIgual("Devolver: el vehículo queda disponible", EstadoVehiculo.DISPONIBLE, e.bici.getEstado());
        v.verificarVerdadero("Devolver: el viaje guarda la promoción aplicada",
                viaje.getPromocion() != null && viaje.getPromocion().getNombre().equals("Primer viaje"));
        v.verificarNumero("Devolver: el viaje guarda el importe cobrado", 100, viaje.getImporteCobrado());
        v.verificarNumero("Devolver: el historial tiene la recarga y el débito", 2, e.usuario.getCuenta().getMovimientos().size());
        v.verificarFalso("Devolver: el usuario ya no tiene viaje en curso", e.usuario.tieneViajeEnCurso());

        // primer viaje de 10 min: cuesta 400 y es todo gratis => no se registra ningún débito
        Escenario gratis = new Escenario(ViajesDePrueba.usuario(), 1000, true);
        Viaje corto = gratis.servicio.retirar(gratis.usuario, gratis.bici, gratis.a);
        gratis.reloj.avanzarMinutos(10);
        v.verificarNumero("Devolver: primer viaje de 10 min es gratis", 0, gratis.servicio.devolver(corto, gratis.b));
        v.verificarNumero("Devolver: el saldo no cambia", 1000, gratis.usuario.getCuenta().getSaldo());
        v.verificarNumero("Devolver: no se registra un débito en cero", 1, gratis.usuario.getCuenta().getMovimientos().size());

        // con plan mensual: el viaje de 20 min queda cubierto y consume 1 de los 20 viajes
        Escenario conPlan = new Escenario(ViajesDePrueba.usuarioConPlan(), 1000, true);
        Viaje delPlan = conPlan.servicio.retirar(conPlan.usuario, conPlan.bici, conPlan.a);
        conPlan.reloj.avanzarMinutos(20);
        v.verificarNumero("Devolver: viaje de 20 min con plan se paga 0", 0, conPlan.servicio.devolver(delPlan, conPlan.b));
        v.verificarNumero("Devolver: el plan queda con 19 viajes", 19,
                conPlan.usuario.getPlanMensual().getViajesRestantes(conPlan.reloj.ahora().toLocalDate()));
    }

    private static void devolverSinPromociones(Verificador v) {
        // 25 min sin promociones: 200 + 25*20 = 700 => saldo 300
        Escenario e = new Escenario(ViajesDePrueba.usuario(), 1000, false);
        Viaje viaje = e.servicio.retirar(e.usuario, e.bici, e.a);
        e.reloj.avanzarMinutos(25);
        v.verificarNumero("Devolver: 25 min sin promociones = 700", 700, e.servicio.devolver(viaje, e.b));
        v.verificarNumero("Devolver: el saldo queda en 300", 300, e.usuario.getCuenta().getSaldo());
        v.verificarVerdadero("Devolver: sin promoción aplicable el viaje no guarda ninguna", viaje.getPromocion() == null);

        // RN03: 60 min = 200 + 1200 = 1400, con saldo 800 queda en -600 => inhabilitado
        Escenario negativo = new Escenario(ViajesDePrueba.usuario(), 800, false);
        Viaje largo = negativo.servicio.retirar(negativo.usuario, negativo.bici, negativo.a);
        negativo.reloj.avanzarMinutos(60);
        negativo.servicio.devolver(largo, negativo.b);
        v.verificarNumero("Devolver: 800 - 1400 deja saldo -600", -600, negativo.usuario.getCuenta().getSaldo());
        v.verificarIgual("Devolver: con saldo negativo el usuario queda inhabilitado", EstadoUsuario.INHABILITADO,
                negativo.usuario.getEstado());
    }

    // RN13 + RN15: estación llena y 10 minutos de gracia
    private static void estacionLlena(Verificador v) {
        Escenario e = new Escenario(ViajesDePrueba.usuario(), 1000, false);
        Estacion chica = ViajesDePrueba.estacion("CHICA", 1);
        chica.recibirVehiculo(ViajesDePrueba.bicicleta(), e.reloj.ahora());
        Estacion libre = ViajesDePrueba.estacion("LIBRE", 10);

        Viaje viaje = e.servicio.retirar(e.usuario, e.bici, e.a);
        e.reloj.avanzarMinutos(25);
        v.verificarExcepcion("Estación llena: no se puede devolver", EstacionLlenaExcepcion.class,
                () -> e.servicio.devolver(viaje, chica));
        v.verificarIgual("Estación llena: el viaje sigue en curso", EstadoViaje.EN_CURSO, viaje.getEstado());
        v.verificarNumero("Estación llena: el saldo no se toca", 1000, e.usuario.getCuenta().getSaldo());

        // total 32 min, 7 de gracia desde el intento => 25 min = 200 + 500 = 700
        e.reloj.avanzarMinutos(7);
        v.verificarNumero("Estación llena: 32 min con 7 de gracia se cobran como 25 = 700", 700,
                e.servicio.devolver(viaje, libre));
    }

    // RN18: cancelar sin cargo
    private static void cancelar(Verificador v) {
        Escenario e = new Escenario(ViajesDePrueba.usuario(), 1000, false);
        Viaje viaje = e.servicio.retirar(e.usuario, e.bici, e.a);
        e.reloj.avanzarMinutos(1);
        e.servicio.cancelar(viaje);

        v.verificarIgual("Cancelar: el viaje queda cancelado", EstadoViaje.CANCELADO, viaje.getEstado());
        v.verificarNumero("Cancelar: no se cobra nada", 1000, e.usuario.getCuenta().getSaldo());
        v.verificarVerdadero("Cancelar: el vehículo vuelve a la estación A", e.bici.getEstacion() == e.a);
        v.verificarIgual("Cancelar: el vehículo queda disponible", EstadoVehiculo.DISPONIBLE, e.bici.getEstado());
        v.verificarFalso("Cancelar: el usuario queda libre para retirar de nuevo", e.usuario.tieneViajeEnCurso());

        Escenario tarde = new Escenario(ViajesDePrueba.usuario(), 1000, false);
        Viaje otro = tarde.servicio.retirar(tarde.usuario, tarde.bici, tarde.a);
        tarde.reloj.avanzarMinutos(3);
        v.verificarExcepcion("Cancelar: a los 3 minutos ya no se puede", IllegalStateException.class,
                () -> tarde.servicio.cancelar(otro));
    }

    // RF13 + RN20 + S4: 24 horas sin devolver
    private static void vehiculosPerdidos(Verificador v) {
        Escenario e = new Escenario(ViajesDePrueba.usuario(), 1000, false);
        Viaje viaje = e.servicio.retirar(e.usuario, e.bici, e.a);

        e.reloj.avanzarMinutos(23 * 60 + 59);
        v.verificarNumero("Perdidos: a las 23:59 no hay ninguno", 0, e.servicio.detectarViajesPerdidos().size());

        e.reloj.avanzarMinutos(1);
        v.verificarNumero("Perdidos: a las 24:00 hay 1", 1, e.servicio.detectarViajesPerdidos().size());
        v.verificarIgual("Perdidos: el viaje queda perdido", EstadoViaje.PERDIDO, viaje.getEstado());
        v.verificarIgual("Perdidos: el vehículo queda fuera de servicio", EstadoVehiculo.FUERA_DE_SERVICIO, e.bici.getEstado());
        v.verificarNumero("Perdidos: 1000 - 20000 deja saldo -19000", -19000, e.usuario.getCuenta().getSaldo());
        v.verificarIgual("Perdidos: el usuario queda inhabilitado", EstadoUsuario.INHABILITADO, e.usuario.getEstado());
        v.verificarNumero("Perdidos: el viaje guarda el cargo de 20000", 20000, viaje.getImporteCobrado());
        v.verificarNumero("Perdidos: una segunda revisión no vuelve a cobrar", 0, e.servicio.detectarViajesPerdidos().size());
    }

    // RF14 + RN24 + S16: ajustes sobre viajes cerrados
    private static void ajustes(Verificador v) {
        Operador operador = new Operador("L-1", "Luis", "1122334455");
        Escenario e = new Escenario(ViajesDePrueba.usuario(), 1000, false);
        Viaje viaje = e.servicio.retirar(e.usuario, e.bici, e.a);

        v.verificarExcepcion("Ajuste: un viaje en curso no se ajusta", IllegalStateException.class,
                () -> e.servicio.registrarAjuste(viaje, operador, 100, "Prueba"));

        e.reloj.avanzarMinutos(25);
        e.servicio.devolver(viaje, e.b);                       // 700 => saldo 300
        e.servicio.registrarAjuste(viaje, operador, 200, "Cobro de más");
        v.verificarNumero("Ajuste: un crédito de 200 deja saldo 500", 500, e.usuario.getCuenta().getSaldo());
        v.verificarNumero("Ajuste: el viaje guarda la nota", 1, viaje.getNotasAjuste().size());
        v.verificarNumero("Ajuste: el viaje cerrado no se modifica (RN24)", 700, viaje.getImporteCobrado());

        e.servicio.registrarAjuste(viaje, operador, -600, "Cobro de menos");
        v.verificarNumero("Ajuste: un débito de 600 deja saldo -100", -100, e.usuario.getCuenta().getSaldo());
        v.verificarIgual("Ajuste: un saldo negativo inhabilita al usuario", EstadoUsuario.INHABILITADO, e.usuario.getEstado());

        v.verificarExcepcion("Ajuste: importe cero falla", IllegalArgumentException.class,
                () -> e.servicio.registrarAjuste(viaje, operador, 0, "Nada"));
        v.verificarExcepcion("Ajuste: sin motivo falla", IllegalArgumentException.class,
                () -> e.servicio.registrarAjuste(viaje, operador, 50, " "));
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}