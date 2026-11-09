package pruebas;

import java.time.LocalDate;
import modelo.cuentas.PlanMensual;

public class PlanMensualPrueba {
    private static final LocalDate NOVIEMBRE = LocalDate.of(2026, 11, 9);
    private static final LocalDate DICIEMBRE = LocalDate.of(2026, 12, 2);

    public static void ejecutar(Verificador v) {
        unPlanNuevoTieneLos20ViajesDisponibles(v);
        cadaViajeDescuentaUnoDelMes(v);
        despuesDelViajeNumero20YaNoHayDisponibles(v);
        elContadorSeReiniciaAlCambiarDeMes(v);
    }

    private static void unPlanNuevoTieneLos20ViajesDisponibles(Verificador v) {
        PlanMensual plan = new PlanMensual(NOVIEMBRE);
        v.verificarNumero("Plan: un plan nuevo tiene 20 viajes", 20, plan.getViajesRestantes(NOVIEMBRE));
    }

    private static void cadaViajeDescuentaUnoDelMes(Verificador v) {
        PlanMensual plan = new PlanMensual(NOVIEMBRE);
        plan.registrarViaje(NOVIEMBRE);
        plan.registrarViaje(NOVIEMBRE);
        v.verificarNumero("Plan: 2 viajes usados dejan 18", 18, plan.getViajesRestantes(NOVIEMBRE));
    }

    private static void despuesDelViajeNumero20YaNoHayDisponibles(Verificador v) {
        PlanMensual plan = new PlanMensual(NOVIEMBRE);
        for (int i = 0; i < 20; i++) {
            plan.registrarViaje(NOVIEMBRE);
        }
        v.verificarFalso("Plan: tras 20 viajes no quedan disponibles", plan.tieneViajesDisponibles(NOVIEMBRE));
        v.verificarExcepcion("Plan: el viaje 21 falla", IllegalStateException.class,
                () -> plan.registrarViaje(NOVIEMBRE));
    }

    private static void elContadorSeReiniciaAlCambiarDeMes(Verificador v) {
        PlanMensual plan = new PlanMensual(NOVIEMBRE);
        for (int i = 0; i < 20; i++) {
            plan.registrarViaje(NOVIEMBRE);
        }
        v.verificarVerdadero("Plan: en diciembre vuelve a haber viajes", plan.tieneViajesDisponibles(DICIEMBRE));
        v.verificarNumero("Plan: en diciembre hay 20 viajes", 20, plan.getViajesRestantes(DICIEMBRE));

        plan.registrarViaje(DICIEMBRE);
        v.verificarNumero("Plan: 1 viaje en diciembre deja 19", 19, plan.getViajesRestantes(DICIEMBRE));
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}