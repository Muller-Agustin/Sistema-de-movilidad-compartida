package modelo.tarifas;

import java.time.LocalDate;
import modelo.TipoVehiculo;
import pruebas.Verificador;

public class CatalogoTarifasPrueba {

    public static void ejecutar(Verificador v) {
        CatalogoTarifas catalogo = new CatalogoTarifas();
        Tarifa vieja = new Tarifa(TipoVehiculo.BICICLETA_COMUN, 200, 20, LocalDate.of(2026, 1, 1));
        Tarifa nueva = new Tarifa(TipoVehiculo.BICICLETA_COMUN, 250, 25, LocalDate.of(2026, 10, 1));
        catalogo.agregar(vieja);
        catalogo.agregar(nueva);

        // S11: gana la vigencia más reciente que sea menor o igual a la fecha
        v.verificarVerdadero("Catálogo: el 30/9 rige la tarifa vieja",
                catalogo.tarifaVigente(TipoVehiculo.BICICLETA_COMUN, LocalDate.of(2026, 9, 30)) == vieja);
        v.verificarVerdadero("Catálogo: el 1/10 rige la tarifa nueva",
                catalogo.tarifaVigente(TipoVehiculo.BICICLETA_COMUN, LocalDate.of(2026, 10, 1)) == nueva);
        v.verificarVerdadero("Catálogo: en noviembre sigue la nueva",
                catalogo.tarifaVigente(TipoVehiculo.BICICLETA_COMUN, LocalDate.of(2026, 11, 9)) == nueva);

        v.verificarExcepcion("Catálogo: antes de la primera vigencia falla", IllegalStateException.class,
                () -> catalogo.tarifaVigente(TipoVehiculo.BICICLETA_COMUN, LocalDate.of(2025, 12, 31)));
        v.verificarExcepcion("Catálogo: un tipo sin tarifa falla", IllegalStateException.class,
                () -> catalogo.tarifaVigente(TipoVehiculo.MONOPATIN, LocalDate.of(2026, 11, 9)));

        CatalogoTarifas referencia = CatalogoTarifas.conValoresDeReferencia(LocalDate.of(2026, 1, 1));
        LocalDate hoy = LocalDate.of(2026, 11, 9);
        v.verificarNumero("Catálogo de referencia: 3 tarifas", 3, referencia.getTarifas().size());
        v.verificarNumero("Catálogo de referencia: bici eléctrica $45 por minuto", 45,
                referencia.tarifaVigente(TipoVehiculo.BICICLETA_ELECTRICA, hoy).getPrecioPorMinuto());
        v.verificarNumero("Catálogo de referencia: monopatín desbloqueo $300", 300,
                referencia.tarifaVigente(TipoVehiculo.MONOPATIN, hoy).getCargoDesbloqueo());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}