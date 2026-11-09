package modelo.tarifas;

import java.time.LocalDate;
import modelo.TipoVehiculo;
import pruebas.Verificador;

public class TarifaPrueba {
    private static final LocalDate DESDE = LocalDate.of(2026, 1, 1);

    public static void ejecutar(Verificador v) {
        Tarifa bici = new Tarifa(TipoVehiculo.BICICLETA_COMUN, 200, 20, DESDE);
        Tarifa monopatin = new Tarifa(TipoVehiculo.MONOPATIN, 300, 50, DESDE);

        v.verificarNumero("Tarifa: bici 0 min = solo desbloqueo 200", 200, bici.calcularCosto(0));
        v.verificarNumero("Tarifa: bici 10 min = 200 + 10*20 = 400", 400, bici.calcularCosto(10));
        v.verificarNumero("Tarifa: monopatín 10 min = 300 + 10*50 = 800", 800, monopatin.calcularCosto(10));
        v.verificarNumero("Tarifa: bici 120 min = 200 + 120*20 = 2600", 2600, bici.calcularCosto(120));
        // RN19: 120 normales + 30 con recargo 50% => 200 + 2400 + 30*20*1.5 = 3500
        v.verificarNumero("Tarifa: bici 150 min = 3500 (30 min con recargo)", 3500, bici.calcularCosto(150));
        v.verificarNumero("Tarifa: bici 121 min = 2600 + 30 = 2630", 2630, bici.calcularCosto(121));

        // S1: desbloqueo + 30 minutos
        v.verificarNumero("Tarifa: saldo mínimo bici = 200 + 30*20 = 800", 800, bici.getSaldoMinimoParaRetirar());
        v.verificarNumero("Tarifa: saldo mínimo monopatín = 300 + 30*50 = 1800", 1800, monopatin.getSaldoMinimoParaRetirar());

        v.verificarExcepcion("Tarifa: minutos negativos fallan", IllegalArgumentException.class,
                () -> bici.calcularCosto(-1));
        v.verificarExcepcion("Tarifa: precio negativo falla", IllegalArgumentException.class,
                () -> new Tarifa(TipoVehiculo.MONOPATIN, -1, 50, DESDE));
        v.verificarExcepcion("Tarifa: sin tipo de vehículo falla", IllegalArgumentException.class,
                () -> new Tarifa(null, 200, 20, DESDE));
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}