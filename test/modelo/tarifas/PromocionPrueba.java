package modelo.tarifas;

import java.time.LocalDate;
import modelo.cuentas.Usuario;
import modelo.estaciones.Estacion;
import modelo.viajes.Viaje;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class PromocionPrueba {
    private static final LocalDate DESDE = LocalDate.of(2026, 1, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 12, 31);

    public static void ejecutar(Verificador v) {
        primerViaje(v);
        planMensual(v);
        estacionBajaOcupacion(v);
        vigencia(v);
    }

    private static void primerViaje(Verificador v) {
        PromoPrimerViaje promo = new PromoPrimerViaje("Primer viaje", DESDE, HASTA);

        // RN21 + S6: bici 10 min cuesta 400, todo gratis
        Viaje corto = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), 10);
        v.verificarVerdadero("Primer viaje: aplica al primer viaje", promo.esAplicable(corto));
        v.verificarNumero("Primer viaje: 10 min (400) se descuenta todo", 400, promo.calcularDescuento(corto));

        // 20 min cuesta 600; gratis 15 min = 200 + 15*20 = 500; se paga 100 (5 min a $20)
        Viaje largo = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), 20);
        v.verificarNumero("Primer viaje: 20 min (600) descuenta 500", 500, promo.calcularDescuento(largo));

        // un segundo viaje del mismo usuario ya no es el primero
        Usuario usuario = ViajesDePrueba.usuario();
        Viaje primero = ViajesDePrueba.viajeFinalizado(usuario, ViajesDePrueba.INICIO, 10, ViajesDePrueba.estacion("D1", 10));
        Viaje segundo = ViajesDePrueba.viajeFinalizado(usuario, ViajesDePrueba.INICIO.plusDays(1), 10, ViajesDePrueba.estacion("D2", 10));
        v.verificarVerdadero("Primer viaje: el primero sigue siendo el primero", promo.esAplicable(primero));
        v.verificarFalso("Primer viaje: el segundo no aplica", promo.esAplicable(segundo));
        v.verificarNumero("Primer viaje: el segundo no descuenta", 0, promo.calcularDescuento(segundo));

        // un viaje en curso no se puede descontar
        Usuario otro = ViajesDePrueba.usuario();
        Viaje enCurso = new Viaje(otro, ViajesDePrueba.bicicleta(), ViajesDePrueba.tarifaBicicleta(),
                ViajesDePrueba.estacion("R", 10), ViajesDePrueba.INICIO);
        v.verificarFalso("Primer viaje: un viaje en curso no aplica", promo.esAplicable(enCurso));
    }

    private static void planMensual(Verificador v) {
        PromoPlanMensual promo = new PromoPlanMensual("Plan mensual", DESDE, HASTA);

        // 25 min cuesta 200 + 500 = 700, el plan cubre hasta 30 min => todo
        Viaje corto = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuarioConPlan(), 25);
        v.verificarVerdadero("Plan: aplica con plan y viajes disponibles", promo.esAplicable(corto));
        v.verificarNumero("Plan: 25 min (700) se descuenta todo", 700, promo.calcularDescuento(corto));

        // 40 min cuesta 200 + 800 = 1000; cubre 30 min = 800; se paga 200 (10 min a $20)
        Viaje largo = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuarioConPlan(), 40);
        v.verificarNumero("Plan: 40 min (1000) descuenta 800", 800, promo.calcularDescuento(largo));

        // sin plan no aplica
        Viaje sinPlan = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), 25);
        v.verificarFalso("Plan: sin plan no aplica", promo.esAplicable(sinPlan));

        // con los 20 viajes del mes usados, no aplica
        Usuario agotado = ViajesDePrueba.usuarioConPlan();
        for (int i = 0; i < 20; i++) {
            agotado.getPlanMensual().registrarViaje(ViajesDePrueba.INICIO.toLocalDate());
        }
        Viaje viajeAgotado = ViajesDePrueba.viajeFinalizado(agotado, 25);
        v.verificarFalso("Plan: con 20 viajes usados no aplica", promo.esAplicable(viajeAgotado));

        // registrarUso consume un viaje del plan
        Usuario usuario = ViajesDePrueba.usuarioConPlan();
        Viaje viaje = ViajesDePrueba.viajeFinalizado(usuario, 25);
        promo.registrarUso(viaje);
        v.verificarNumero("Plan: registrarUso deja 19 viajes", 19,
                usuario.getPlanMensual().getViajesRestantes(ViajesDePrueba.INICIO.toLocalDate()));
    }

    private static void estacionBajaOcupacion(Verificador v) {
        PromoEstacionBajaOcupacion promo = new PromoEstacionBajaOcupacion("Baja ocupación", DESDE, HASTA);

        // estación de 10 lugares vacía: antes de devolver tenía 0% => aplica; 20% de 400 = 80
        Viaje enVacia = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), ViajesDePrueba.INICIO, 10,
                ViajesDePrueba.estacion("VACIA", 10));
        v.verificarVerdadero("Baja ocupación: estación vacía aplica", promo.esAplicable(enVacia));
        v.verificarNumero("Baja ocupación: 20% de 400 = 80", 80, promo.calcularDescuento(enVacia));

        // estación con 3 de 10 antes de devolver (30%) => no aplica
        Estacion con3 = ViajesDePrueba.estacion("CON3", 10);
        for (int i = 0; i < 3; i++) {
            con3.recibirVehiculo(ViajesDePrueba.bicicleta());
        }
        Viaje enCon3 = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), ViajesDePrueba.INICIO, 10, con3);
        v.verificarFalso("Baja ocupación: estación al 30% no aplica", promo.esAplicable(enCon3));

        // estación con 1 de 10 antes de devolver (10%) => aplica
        Estacion con1 = ViajesDePrueba.estacion("CON1", 10);
        con1.recibirVehiculo(ViajesDePrueba.bicicleta());
        Viaje enCon1 = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), ViajesDePrueba.INICIO, 10, con1);
        v.verificarVerdadero("Baja ocupación: estación al 10% aplica", promo.esAplicable(enCon1));
    }

    private static void vigencia(Verificador v) {
        PromoPrimerViaje vencida = new PromoPrimerViaje("Vencida", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        Viaje viaje = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), 10);
        v.verificarFalso("Vigencia: una promoción vencida no aplica", vencida.esAplicable(viaje));

        v.verificarExcepcion("Vigencia: fin anterior al inicio falla", IllegalArgumentException.class,
                () -> new PromoPrimerViaje("Mal", HASTA, DESDE));
        v.verificarExcepcion("Vigencia: sin nombre falla", IllegalArgumentException.class,
                () -> new PromoPrimerViaje(" ", DESDE, HASTA));
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}