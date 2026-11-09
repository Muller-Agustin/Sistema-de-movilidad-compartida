package ViajesTarifas;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import EstacionesTraslados.Coordenada;
import EstacionesTraslados.Estacion;
import Excepciones.EstacionLlenaExcepcion;
import UsuariosCuenta.Usuario;
import VehiculoMantenimiento.Vehiculo;

public class Viaje {
    private static final int LIMITE_CANCELACION_MINUTOS = 2;
    private static final int LIMITE_PERDIDA_HORAS = 24;
    private static final int GRACIA_ESTACION_LLENA_MINUTOS = 10;

    private final LocalDateTime inicio;
    private LocalDateTime fin;
    private EstadoViaje estado;
    private final Usuario usuario;
    private final List<PuntoRecorrido> puntosRecorrido;
    private final List<NotaAjuste> notasAjuste;
    private final Vehiculo vehiculo;
    private final Tarifa tarifa;
    private Promocion promocion;
    private final Estacion retiro;
    private Estacion devolucion;
    private LocalDateTime intentoFallidoDevolucion;
    private double importeCobrado;

    public Viaje(Usuario usuario, Vehiculo vehiculo, Tarifa tarifa, Estacion retiro, LocalDateTime inicio) {
        this.usuario = usuario;
        this.vehiculo = vehiculo;
        this.tarifa = tarifa;
        this.retiro = retiro;
        this.inicio = inicio;
        this.estado = EstadoViaje.EN_CURSO;
        this.puntosRecorrido = new ArrayList<>();
        this.notasAjuste = new ArrayList<>();
    }

    // GETTERS
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFin() { return fin; }
    public EstadoViaje getEstado() { return estado; }
    public Usuario getUsuario() { return usuario; }
    public List<PuntoRecorrido> getPuntosRecorrido() { return List.copyOf(puntosRecorrido); }
    public List<NotaAjuste> getNotasAjuste() { return List.copyOf(notasAjuste); }
    public Vehiculo getVehiculo() { return vehiculo; }
    public Tarifa getTarifa() { return tarifa; }
    public Promocion getPromocion() { return promocion; }
    public Estacion getRetiro() { return retiro; }
    public Estacion getDevolucion() { return devolucion; }
    public double getImporteCobrado() { return importeCobrado; }
    public LocalDateTime getIntentoFallidoDevolucion() { return intentoFallidoDevolucion; }

    // SETTERS
    public void setPromocion(Promocion promocion) { this.promocion = promocion; }
    public void agregarNotaAjuste(NotaAjuste nota) { notasAjuste.add(nota); }

    // el servicio guarda acá cuánto se le cobró al usuario por este viaje (RN24: no se modifica después)
    public void registrarCobro(double importe) {
        if (estado == EstadoViaje.EN_CURSO) {
            throw new IllegalStateException("El viaje todavía está en curso");
        }
        this.importeCobrado = importe;
    }

    // Lo usa la persistencia al cargar un viaje ya guardado: deja el viaje como estaba,
    // sin volver a ejecutar las reglas ni mover el vehículo o las estaciones.
    public void restaurarEstado(EstadoViaje estado, LocalDateTime fin, Estacion devolucion,
                                LocalDateTime intentoFallidoDevolucion, double importeCobrado) {
        this.estado = estado;
        this.fin = fin;
        this.devolucion = devolucion;
        this.intentoFallidoDevolucion = intentoFallidoDevolucion;
        this.importeCobrado = importeCobrado;
    }

    // S10 + S8: minutos del viaje redondeados hacia arriba (10 min y 1 segundo se cobran como 11).
    // Si el usuario encontró la estación llena, hasta 10 minutos desde ese intento no se cobran.
    public int getMinutosCobrables() {
        if (fin == null) {
            throw new IllegalStateException("El viaje todavía no terminó");
        }
        long segundos = Duration.between(inicio, fin).getSeconds();
        segundos -= segundosDeGracia();
        return (int) ((segundos + 59) / 60);
    }

    // RN15: segundos sin cargo por haber encontrado la estación llena (máximo 10 minutos)
    private long segundosDeGracia() {
        if (intentoFallidoDevolucion == null) {
            return 0;
        }
        long desdeElIntento = Duration.between(intentoFallidoDevolucion, fin).getSeconds();
        return Math.min(GRACIA_ESTACION_LLENA_MINUTOS * 60L, Math.max(0, desdeElIntento));
    }

    // S18: kilómetros del recorrido, sumando los tramos entre la estación de retiro, los puntos
    // registrados y la estación de devolución (si ya se devolvió)
    public double getDistanciaKm() {
        double total = 0;
        Coordenada anterior = retiro.getCoordenada();
        for (PuntoRecorrido punto : puntosRecorrido) {
            total += anterior.distanciaKm(punto.getCoordenada());
            anterior = punto.getCoordenada();
        }
        if (devolucion != null) {
            total += anterior.distanciaKm(devolucion.getCoordenada());
        }
        return total;
    }

    // valida que el viaje siga en curso
    private void validarEnCurso() {
        if (estado != EstadoViaje.EN_CURSO) {
            throw new IllegalStateException("El viaje no está en curso");
        }
    }

    // guarda un punto del recorrido mientras el viaje sigue en curso, se ejecuta cada segundo
    public void registrarPunto(Coordenada c, LocalDateTime hora) {
        validarEnCurso();
        puntosRecorrido.add(new PuntoRecorrido(c, hora));
    }

    // cierra el viaje y deja el vehiculo en la estacion de devolucion (RN13, RN15, RF08).
    // Si la estación está llena, anota el primer intento fallido y lanza una excepción.
    public void finalizar(Estacion estacion, LocalDateTime fin) {
        validarEnCurso();
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El viaje no puede terminar antes de empezar");
        }
        if (estacion.estaLlena()) {
            if (intentoFallidoDevolucion == null) {
                intentoFallidoDevolucion = fin;
            }
            throw new EstacionLlenaExcepcion("La estación " + estacion.getCodigo() + " está llena");
        }
        this.fin = fin;
        this.devolucion = estacion;
        this.estado = EstadoViaje.FINALIZADO;
        vehiculo.sumarKm(getDistanciaKm());
        vehiculo.marcarDisponible();
        estacion.recibirVehiculo(vehiculo, fin);
    }

    // RN18: solo se cancela dentro de los 2 minutos y el vehiculo vuelve a la estación de retiro
    public void cancelar(LocalDateTime ahora) {
        validarEnCurso();
        long segundos = Duration.between(inicio, ahora).getSeconds();
        if (segundos > LIMITE_CANCELACION_MINUTOS * 60L) {
            throw new IllegalStateException("Pasaron más de " + LIMITE_CANCELACION_MINUTOS + " minutos: el viaje no se puede cancelar");
        }
        if (retiro.estaLlena()) {
            throw new EstacionLlenaExcepcion("La estación " + retiro.getCodigo() + " está llena");
        }
        this.fin = ahora;
        this.estado = EstadoViaje.CANCELADO;
        vehiculo.marcarDisponible();
        retiro.recibirVehiculo(vehiculo, ahora);
    }

    // true si el viaje sigue abierto pasadas las 24 horas
    public boolean superaLimitePerdida(LocalDateTime ahora) {
        return estado == EstadoViaje.EN_CURSO
                && Duration.between(inicio, ahora).toHours() >= LIMITE_PERDIDA_HORAS;
    }

    // RN20 + S2: el vehículo no volvió en 24 horas, se lo da por perdido y queda fuera de servicio
    public void marcarPerdido(LocalDateTime ahora) {
        validarEnCurso();
        if (!superaLimitePerdida(ahora)) {
            throw new IllegalStateException("El viaje todavía no superó las " + LIMITE_PERDIDA_HORAS + " horas");
        }
        this.estado = EstadoViaje.PERDIDO;
        vehiculo.darDeBaja();
    }
}