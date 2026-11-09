package pruebas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import modelo.EstadoVehiculo;
import modelo.BicicletaComun;
import modelo.TipoVehiculo;
import modelo.cuentas.Tarjeta;
import modelo.cuentas.Usuario;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;
import modelo.tarifas.Tarifa;
import modelo.viajes.Viaje;

// Arma usuarios, estaciones y viajes para no repetir el mismo código en cada prueba.
public class ViajesDePrueba {
    public static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 9, 10, 0);
    private static int contadorBicis = 0;

    // bicicleta común: desbloqueo $200 + $20 por minuto (valores de referencia RN17)
    public static Tarifa tarifaBicicleta() {
        return new Tarifa(TipoVehiculo.BICICLETA_COMUN, 200, 20, LocalDate.of(2026, 1, 1));
    }

    public static Usuario usuario() {
        return new Usuario("Ana", "12345678", "ana@mail.com", LocalDate.of(2026, 11, 1));
    }

    // usuario con 8000 de saldo recargado y suscripto al plan (queda con saldo 0)
    public static Usuario usuarioConPlan() {
        Usuario usuario = usuario();
        Tarjeta tarjeta = new Tarjeta("Ana", "1234");
        usuario.getCuenta().agregarMedioPago(tarjeta);
        usuario.recargarSaldo(8000, tarjeta, INICIO);
        usuario.suscribirPlan(INICIO);
        return usuario;
    }

    public static Estacion estacion(String codigo, int capacidad) {
        return new Estacion(codigo, "Calle " + codigo, capacidad, new Coordenada(0, 0));
    }

    // estación en una coordenada puntual (para probar distancias)
    public static Estacion estacion(String codigo, int capacidad, double latitud, double longitud) {
        return new Estacion(codigo, "Calle " + codigo, capacidad, new Coordenada(latitud, longitud));
    }

    public static BicicletaComun bicicleta() {
        contadorBicis++;
        return new BicicletaComun("BC-" + contadorBicis, LocalDate.of(2026, 1, 1), 26);
    }

    // viaje de bicicleta común que empieza en INICIO y dura "minutos" minutos
    public static Viaje viajeFinalizado(Usuario usuario, int minutos) {
        return viajeFinalizado(usuario, INICIO, minutos, estacion("DEV", 10));
    }

    public static Viaje viajeFinalizado(Usuario usuario, LocalDateTime inicio, int minutos, Estacion devolucion) {
        Viaje viaje = new Viaje(usuario, bicicleta(), tarifaBicicleta(), estacion("RET", 10), inicio);
        usuario.agregarViaje(viaje);
        viaje.finalizar(devolucion, inicio.plusMinutes(minutos));
        return viaje;
    }
}