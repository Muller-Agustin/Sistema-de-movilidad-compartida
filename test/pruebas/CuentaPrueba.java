package pruebas;

import java.time.LocalDateTime;
import modelo.cuentas.BilleteraVirtual;
import modelo.cuentas.Cuenta;
import modelo.cuentas.MedioPago;
import modelo.cuentas.Movimiento;
import modelo.cuentas.Tarjeta;

public class CuentaPrueba {
    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 9, 10, 0);

    public static void ejecutar(Verificador v) {
        unaCuentaNuevaEmpiezaConElSaldoIndicado(v);
        recargarSumaAlSaldoYRegistraElMovimiento(v);
        noSePuedeRecargarConUnMedioNoRegistrado(v);
        noSePuedeRecargarUnImporteNoPositivo(v);
        debitarPermiteQueElSaldoQuedeNegativo(v);
        elAjusteSumaOResta(v);
        tieneSaldoSuficienteComparaContraElMinimo(v);
        elSaldoEsLaSumaDeSusMovimientos(v);
    }

    private static void unaCuentaNuevaEmpiezaConElSaldoIndicado(Verificador v) {
        v.verificarNumero("Cuenta: saldo inicial 0", 0, new Cuenta(0).getSaldo());
    }

    private static void recargarSumaAlSaldoYRegistraElMovimiento(Verificador v) {
        Cuenta cuenta = new Cuenta(0);
        MedioPago tarjeta = new Tarjeta("Ana", "1234");
        cuenta.agregarMedioPago(tarjeta);

        cuenta.recargar(1000, tarjeta, FECHA);

        v.verificarNumero("Cuenta: recargar 1000 deja saldo 1000", 1000, cuenta.getSaldo());
        v.verificarNumero("Cuenta: la recarga queda en el historial", 1, cuenta.getMovimientos().size());
    }

    private static void noSePuedeRecargarConUnMedioNoRegistrado(Verificador v) {
        Cuenta cuenta = new Cuenta(0);
        v.verificarExcepcion("Cuenta: recargar con medio no registrado falla",
                IllegalArgumentException.class,
                () -> cuenta.recargar(1000, new Tarjeta("Ana", "1234"), FECHA));
    }

    private static void noSePuedeRecargarUnImporteNoPositivo(Verificador v) {
        Cuenta cuenta = new Cuenta(0);
        MedioPago billetera = new BilleteraVirtual("ana.mp");
        cuenta.agregarMedioPago(billetera);
        v.verificarExcepcion("Cuenta: recargar 0 falla", IllegalArgumentException.class,
                () -> cuenta.recargar(0, billetera, FECHA));
        v.verificarExcepcion("Cuenta: recargar importe negativo falla", IllegalArgumentException.class,
                () -> cuenta.recargar(-50, billetera, FECHA));
    }

    private static void debitarPermiteQueElSaldoQuedeNegativo(Verificador v) {
        Cuenta cuenta = new Cuenta(100);
        cuenta.debitar(300, "Viaje", FECHA);
        v.verificarNumero("Cuenta: 100 - 300 deja saldo -200", -200, cuenta.getSaldo());
    }

    private static void elAjusteSumaOResta(Verificador v) {
        Cuenta cuenta = new Cuenta(0);
        cuenta.ajustar(150, "Corrección a favor", FECHA);
        cuenta.ajustar(-50, "Corrección en contra", FECHA);
        v.verificarNumero("Cuenta: ajustes +150 y -50 dejan saldo 100", 100, cuenta.getSaldo());
    }

    private static void tieneSaldoSuficienteComparaContraElMinimo(Verificador v) {
        Cuenta cuenta = new Cuenta(500);
        v.verificarVerdadero("Cuenta: saldo 500 alcanza para un mínimo de 500", cuenta.tieneSaldoSuficiente(500));
        v.verificarFalso("Cuenta: saldo 500 no alcanza para un mínimo de 501", cuenta.tieneSaldoSuficiente(501));
    }

    private static void elSaldoEsLaSumaDeSusMovimientos(Verificador v) {
        Cuenta cuenta = new Cuenta(0);
        MedioPago tarjeta = new Tarjeta("Ana", "1234");
        cuenta.agregarMedioPago(tarjeta);
        cuenta.recargar(1000, tarjeta, FECHA);
        cuenta.debitar(300, "Viaje", FECHA);
        cuenta.ajustar(50, "Ajuste", FECHA);

        double suma = 0;
        for (Movimiento m : cuenta.getMovimientos()) {
            suma += m.getEfectoSobreSaldo();
        }
        v.verificarNumero("Cuenta: el saldo coincide con la suma de movimientos", suma, cuenta.getSaldo());
        v.verificarNumero("Cuenta: 1000 - 300 + 50 deja saldo 750", 750, cuenta.getSaldo());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}