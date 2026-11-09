package modelo.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.tarifas.Promocion;
import modelo.tarifas.PromoPlanMensual;
import modelo.tarifas.PromoPrimerViaje;
import modelo.viajes.Viaje;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class CalculadoraDeTarifaPrueba {
    private static final LocalDate DESDE = LocalDate.of(2026, 1, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 12, 31);

    public static void ejecutar(Verificador v) {
        CalculadoraDeTarifa sinPromos = new CalculadoraDeTarifa(new ArrayList<>());

        Viaje viaje = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), 150);
        v.verificarNumero("Calculadora: 150 min sin promos = 3500", 3500, sinPromos.calcular(viaje));
        v.verificarVerdadero("Calculadora: sin promos no elige ninguna", sinPromos.elegirPromocion(viaje) == null);

        List<Promocion> promos = new ArrayList<>();
        PromoPrimerViaje primer = new PromoPrimerViaje("Primer viaje", DESDE, HASTA);
        PromoPlanMensual plan = new PromoPlanMensual("Plan mensual", DESDE, HASTA);
        promos.add(primer);
        promos.add(plan);
        CalculadoraDeTarifa calculadora = new CalculadoraDeTarifa(promos);

        // usuario nuevo sin plan, 20 min (600): solo aplica el primer viaje => 600 - 500 = 100
        Viaje soloPrimero = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), 20);
        v.verificarNumero("Calculadora: 20 min con primer viaje = 100", 100, calculadora.calcular(soloPrimero));
        v.verificarVerdadero("Calculadora: elige la promo de primer viaje",
                calculadora.elegirPromocion(soloPrimero) == primer);

        // usuario nuevo CON plan, 20 min (600): primer viaje descuenta 500, plan descuenta 600
        // S7: no se acumulan, se aplica la más favorable (plan) => 0, no negativo
        Viaje ambas = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuarioConPlan(), 20);
        v.verificarVerdadero("Calculadora: entre dos promos elige la que más descuenta",
                calculadora.elegirPromocion(ambas) == plan);
        v.verificarNumero("Calculadora: 20 min con ambas promos = 0 (no se acumulan)", 0, calculadora.calcular(ambas));

        // plan con 40 min (1000): descuenta 800 => se pagan 200
        Viaje largoConPlan = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuarioConPlan(), 40);
        v.verificarNumero("Calculadora: 40 min con plan = 200", 200, calculadora.calcular(largoConPlan));

        // viaje sin ninguna promoción aplicable (segundo viaje, sin plan): paga el costo completo
        Viaje segundo = ViajesDePrueba.viajeFinalizado(ViajesDePrueba.usuario(), 10);
        Viaje tercero = ViajesDePrueba.viajeFinalizado(segundo.getUsuario(),
                ViajesDePrueba.INICIO.plusDays(1), 10, ViajesDePrueba.estacion("D3", 10));
        v.verificarNumero("Calculadora: sin promo aplicable paga 400", 400, calculadora.calcular(tercero));

        // RN20 + S4
        v.verificarNumero("Calculadora: cargo por vehículo perdido $20000", 20000, calculadora.getCargoVehiculoPerdido());

        // un viaje que no terminó no se puede calcular
        Viaje enCurso = new Viaje(ViajesDePrueba.usuario(), ViajesDePrueba.bicicleta(),
                ViajesDePrueba.tarifaBicicleta(), ViajesDePrueba.estacion("R", 10), ViajesDePrueba.INICIO);
        v.verificarExcepcion("Calculadora: un viaje en curso no se calcula", IllegalStateException.class,
                () -> calculadora.calcular(enCurso));
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}