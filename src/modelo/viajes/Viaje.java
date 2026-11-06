package modelo.viajes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.vehiculos.EstadoVehiculo;
import modelo.Usuario;
import modelo.vehiculos.Vehiculo;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;
import modelo.tarifas.Promocion;
import modelo.tarifas.Tarifa;

public class Viaje {
    private static final int LIMITE_CANCELACION_MINUTOS = 2;
    private static final int LIMITE_PERDIDA_HORAS = 24;

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

    // SETTERS
    public void setPromocion(Promocion promocion) { this.promocion = promocion; }
    public void agregarNotaAjuste(NotaAjuste nota) { notasAjuste.add(nota); }


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

    // cierra el viaje y deja el vehiculo en la estacion de devolucion
    public void finalizar(Estacion estacion, LocalDateTime fin) {
        validarEnCurso();
        if (estacion.estaLlena()) {
            throw new IllegalStateException("La estación está llena");
        }
        this.fin = fin;
        this.devolucion = estacion;
        this.estado = EstadoViaje.FINALIZADO;
        vehiculo.marcarDisponible();
        estacion.recibirVehiculo(vehiculo);
    }

    // solo se cancela dentro de los 2 minutos y el vehiculo vuelve al retiro
    public void cancelar(LocalDateTime ahora) {
        validarEnCurso();
        if (Duration.between(inicio, ahora).toMinutes() >= LIMITE_CANCELACION_MINUTOS) {
            throw new IllegalStateException("Pasaron más de " + LIMITE_CANCELACION_MINUTOS + " minutos: el viaje no se puede cancelar");
        }
        this.fin = ahora;
        this.estado = EstadoViaje.CANCELADO;
        vehiculo.marcarDisponible();
        retiro.recibirVehiculo(vehiculo);
    }

    // true si el viaje sigue abierto pasadas las 24 horas
    public boolean superaLimitePerdida(LocalDateTime ahora) {
        return estado == EstadoViaje.EN_CURSO
                && Duration.between(inicio, ahora).toHours() >= LIMITE_PERDIDA_HORAS;
    }
}
