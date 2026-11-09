package modelo.viajes;

import java.time.LocalDateTime;
import modelo.BicicletaComun;
import modelo.EstadoVehiculo;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;
import modelo.excepciones.EstacionLlenaExcepcion;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class ViajePrueba {
    private static final LocalDateTime INICIO = ViajesDePrueba.INICIO;

    public static void ejecutar(Verificador v) {
        minutosCobrables(v);
        finalizar(v);
        estacionLlena(v);
        cancelar(v);
        distancia(v);
        vehiculoPerdido(v);
        cobro(v);
    }

    private static Viaje nuevoViaje() {
        return new Viaje(ViajesDePrueba.usuario(), ViajesDePrueba.bicicleta(),
                ViajesDePrueba.tarifaBicicleta(), ViajesDePrueba.estacion("RET", 10), INICIO);
    }

    private static void minutosCobrables(Verificador v) {
        Viaje exacto = nuevoViaje();
        exacto.finalizar(ViajesDePrueba.estacion("D1", 10), INICIO.plusMinutes(10));
        v.verificarNumero("Viaje: 10 min exactos = 10 minutos cobrables", 10, exacto.getMinutosCobrables());

        // S10: 10 min y 1 segundo se redondean hacia arriba
        Viaje conSegundo = nuevoViaje();
        conSegundo.finalizar(ViajesDePrueba.estacion("D2", 10), INICIO.plusMinutes(10).plusSeconds(1));
        v.verificarNumero("Viaje: 10 min 1 s = 11 minutos cobrables", 11, conSegundo.getMinutosCobrables());

        Viaje casiUnMinuto = nuevoViaje();
        casiUnMinuto.finalizar(ViajesDePrueba.estacion("D3", 10), INICIO.plusSeconds(59));
        v.verificarNumero("Viaje: 59 s = 1 minuto cobrable", 1, casiUnMinuto.getMinutosCobrables());

        Viaje enCurso = nuevoViaje();
        v.verificarExcepcion("Viaje: en curso no tiene minutos cobrables", IllegalStateException.class,
                () -> enCurso.getMinutosCobrables());
    }

    private static void finalizar(Verificador v) {
        Estacion destino = ViajesDePrueba.estacion("DEST", 10);
        Viaje viaje = nuevoViaje();
        viaje.getVehiculo().marcarEnUso();
        viaje.finalizar(destino, INICIO.plusMinutes(15));

        v.verificarIgual("Viaje: al devolver queda finalizado", EstadoViaje.FINALIZADO, viaje.getEstado());
        v.verificarIgual("Viaje: el vehículo vuelve a estar disponible", EstadoVehiculo.DISPONIBLE,
                viaje.getVehiculo().getEstado());
        v.verificarVerdadero("Viaje: el vehículo queda en la estación de devolución",
                viaje.getVehiculo().getEstacion() == destino);
        v.verificarExcepcion("Viaje: no se puede finalizar dos veces", IllegalStateException.class,
                () -> viaje.finalizar(ViajesDePrueba.estacion("OTRA", 10), INICIO.plusMinutes(20)));

        Viaje invertido = nuevoViaje();
        v.verificarExcepcion("Viaje: no puede terminar antes de empezar", IllegalArgumentException.class,
                () -> invertido.finalizar(ViajesDePrueba.estacion("D", 10), INICIO.minusMinutes(1)));
    }

    // RN13 + RN15 + S8: estación llena, y hasta 10 minutos sin cargo desde el primer intento fallido
    private static void estacionLlena(Verificador v) {
        Estacion llena = ViajesDePrueba.estacion("LLENA", 1);
        llena.recibirVehiculo(ViajesDePrueba.bicicleta(), INICIO);

        Viaje viaje = nuevoViaje();
        v.verificarExcepcion("Viaje: devolver en estación llena falla", EstacionLlenaExcepcion.class,
                () -> viaje.finalizar(llena, INICIO.plusMinutes(25)));
        v.verificarIgual("Viaje: tras el intento fallido sigue en curso", EstadoViaje.EN_CURSO, viaje.getEstado());

        // intento fallido a los 25 min, termina a los 32: total 32, gracia = 7 => se cobran 25
        viaje.finalizar(ViajesDePrueba.estacion("LIBRE", 10), INICIO.plusMinutes(32));
        v.verificarNumero("Viaje: 32 min con 7 de gracia = 25 cobrables", 25, viaje.getMinutosCobrables());

        // intento a los 20 min, termina a los 45: total 45, gracia tope 10 => se cobran 35
        Viaje largo = nuevoViaje();
        v.verificarExcepcion("Viaje: segundo viaje también encuentra la estación llena", EstacionLlenaExcepcion.class,
                () -> largo.finalizar(llena, INICIO.plusMinutes(20)));
        largo.finalizar(ViajesDePrueba.estacion("LIBRE2", 10), INICIO.plusMinutes(45));
        v.verificarNumero("Viaje: 45 min con gracia tope de 10 = 35 cobrables", 35, largo.getMinutosCobrables());

        // un segundo intento fallido no reinicia la gracia: intentos a los 20 y 30, termina a los 35
        Viaje reintento = nuevoViaje();
        v.verificarExcepcion("Viaje: primer intento falla", EstacionLlenaExcepcion.class,
                () -> reintento.finalizar(llena, INICIO.plusMinutes(20)));
        v.verificarExcepcion("Viaje: segundo intento falla", EstacionLlenaExcepcion.class,
                () -> reintento.finalizar(llena, INICIO.plusMinutes(30)));
        reintento.finalizar(ViajesDePrueba.estacion("LIBRE3", 10), INICIO.plusMinutes(35));
        v.verificarNumero("Viaje: gracia contada desde el primer intento = 35 - 10 = 25", 25,
                reintento.getMinutosCobrables());
    }

    // RN18: se cancela dentro de los 2 minutos
    private static void cancelar(Verificador v) {
        Estacion retiro = ViajesDePrueba.estacion("RET", 10);
        Viaje enTiempo = new Viaje(ViajesDePrueba.usuario(), ViajesDePrueba.bicicleta(),
                ViajesDePrueba.tarifaBicicleta(), retiro, INICIO);
        enTiempo.getVehiculo().marcarEnUso();
        enTiempo.cancelar(INICIO.plusSeconds(90));
        v.verificarIgual("Cancelar: a 1:30 queda cancelado", EstadoViaje.CANCELADO, enTiempo.getEstado());
        v.verificarVerdadero("Cancelar: el vehículo vuelve a la estación de retiro",
                enTiempo.getVehiculo().getEstacion() == retiro);
        v.verificarIgual("Cancelar: el vehículo queda disponible", EstadoVehiculo.DISPONIBLE,
                enTiempo.getVehiculo().getEstado());

        Viaje justo = nuevoViaje();
        justo.cancelar(INICIO.plusMinutes(2));
        v.verificarIgual("Cancelar: a los 2:00 exactos todavía se puede", EstadoViaje.CANCELADO, justo.getEstado());

        Viaje tarde = nuevoViaje();
        v.verificarExcepcion("Cancelar: a los 2:01 ya no se puede", IllegalStateException.class,
                () -> tarde.cancelar(INICIO.plusMinutes(2).plusSeconds(1)));

        Estacion retiroLleno = ViajesDePrueba.estacion("CHICA", 1);
        Viaje sinLugar = new Viaje(ViajesDePrueba.usuario(), ViajesDePrueba.bicicleta(),
                ViajesDePrueba.tarifaBicicleta(), retiroLleno, INICIO);
        retiroLleno.recibirVehiculo(ViajesDePrueba.bicicleta(), INICIO);
        v.verificarExcepcion("Cancelar: si el lugar se ocupó, falla", EstacionLlenaExcepcion.class,
                () -> sinLugar.cancelar(INICIO.plusMinutes(1)));
        v.verificarIgual("Cancelar: si falla el viaje sigue en curso", EstadoViaje.EN_CURSO, sinLugar.getEstado());
    }

    // RF09 + RF08 + S18: kilómetros del recorrido
    private static void distancia(Verificador v) {
        Estacion retiro = ViajesDePrueba.estacion("A", 10, 0, 0);
        Estacion destino = ViajesDePrueba.estacion("B", 10, 0, 1);
        BicicletaComun bici = ViajesDePrueba.bicicleta();
        Viaje viaje = new Viaje(ViajesDePrueba.usuario(), bici, ViajesDePrueba.tarifaBicicleta(), retiro, INICIO);

        v.verificarNumero("Distancia: sin puntos ni devolución es 0", 0, viaje.getDistanciaKm());

        viaje.registrarPunto(new Coordenada(0, 0.5), INICIO.plusMinutes(5));
        v.verificarNumero("Distancia: se guarda el punto del recorrido", 1, viaje.getPuntosRecorrido().size());
        v.verificarAproximado("Distancia: hasta el punto, medio grado = 55,6 km", 55.6, viaje.getDistanciaKm(), 0.05);

        viaje.finalizar(destino, INICIO.plusMinutes(10));
        v.verificarAproximado("Distancia: total retiro -> punto -> devolución = 111,19 km", 111.19,
                viaje.getDistanciaKm(), 0.01);
        v.verificarExcepcion("Distancia: no se registran puntos en un viaje cerrado", IllegalStateException.class,
                () -> viaje.registrarPunto(new Coordenada(0, 0.7), INICIO.plusMinutes(11)));
    }

    // RN20 + S2: 24 horas sin devolver
    private static void vehiculoPerdido(Verificador v) {
        Viaje viaje = nuevoViaje();
        viaje.getVehiculo().marcarEnUso();
        LocalDateTime casi = INICIO.plusHours(23).plusMinutes(59);
        LocalDateTime justo = INICIO.plusHours(24);

        v.verificarFalso("Perdido: a las 23:59 todavía no", viaje.superaLimitePerdida(casi));
        v.verificarExcepcion("Perdido: antes de las 24 horas no se puede marcar", IllegalStateException.class,
                () -> viaje.marcarPerdido(casi));
        v.verificarVerdadero("Perdido: a las 24:00 sí", viaje.superaLimitePerdida(justo));

        viaje.marcarPerdido(justo);
        v.verificarIgual("Perdido: el viaje queda perdido", EstadoViaje.PERDIDO, viaje.getEstado());
        v.verificarIgual("Perdido: el vehículo queda fuera de servicio", EstadoVehiculo.FUERA_DE_SERVICIO,
                viaje.getVehiculo().getEstado());
        v.verificarFalso("Perdido: un viaje ya perdido no vuelve a marcarse", viaje.superaLimitePerdida(justo.plusHours(5)));
    }

    private static void cobro(Verificador v) {
        Viaje viaje = nuevoViaje();
        v.verificarExcepcion("Cobro: no se registra en un viaje en curso", IllegalStateException.class,
                () -> viaje.registrarCobro(100));
        viaje.finalizar(ViajesDePrueba.estacion("D", 10), INICIO.plusMinutes(10));
        viaje.registrarCobro(400);
        v.verificarNumero("Cobro: queda registrado el importe cobrado", 400, viaje.getImporteCobrado());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}