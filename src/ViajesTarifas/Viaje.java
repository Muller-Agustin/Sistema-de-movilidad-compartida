package ViajesTarifas;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import EstacionesTraslados.Coordenada;
import EstacionesTraslados.Estacion;
import Excepciones.EstacionLlenaExcepcion;
import UsuariosCuenta.Usuario;
import VehiculoMantenimiento.Vehiculo;

// Viaje de un usuario con un vehículo: empieza al retirarlo en una estación y termina al
// devolverlo. Experto (GRASP): calcula su duración, su distancia y su importe.

public class Viaje implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int LIMITE_CANCELACION_MINUTOS = 2;     // RN18
    public static final int LIMITE_PERDIDA_HORAS = 24;          // RN20
    public static final int MINUTOS_EXTRA_ESTACION_LLENA = 10;  // RN15
    public static final double CARGO_VEHICULO_PERDIDO = 20000;  // RN20 y S4

    private int numero;
    private Usuario usuario;
    private Vehiculo vehiculo;
    private Tarifa tarifa;
    private Estacion retiro;
    private Estacion devolucion;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private EstadoViaje estado;
    private List<PuntoRecorrido> puntosRecorrido;
    private List<NotaAjuste> notasAjuste;
    private Promocion promocion;
    private double descuento;
    private double importe;
    private LocalDateTime intentoFallidoDevolucion;

    public Viaje(int numero, Usuario usuario, Vehiculo vehiculo, Estacion retiro, Tarifa tarifa, LocalDateTime inicio) {
        this.numero = numero;
        this.usuario = usuario;
        this.vehiculo = vehiculo;
        this.retiro = retiro;
        this.tarifa = tarifa;
        this.inicio = inicio;
        this.estado = EstadoViaje.EN_CURSO;
        this.puntosRecorrido = new ArrayList<>();
        this.notasAjuste = new ArrayList<>();
    }

    // Mientras el viaje está en curso se registran los puntos del recorrido
    public void registrarPunto(Coordenada coordenada, LocalDateTime hora) {
        puntosRecorrido.add(new PuntoRecorrido(coordenada, hora));
    }

    // RN15: el usuario encontró la estación llena. Desde este momento tiene 10 minutos
    // sin cargo para llegar a otra estación. Solo cuenta el primer intento.
    public void otorgarMinutosExtra(LocalDateTime hora) {
        if (intentoFallidoDevolucion == null) {
            intentoFallidoDevolucion = hora;
        }
    }

    // RF08: cierra el viaje en la estación de devolución y suma los km al vehículo
    public void finalizar(Estacion estacion, LocalDateTime horaFin) {
        this.devolucion = estacion;
        this.fin = horaFin;
        this.estado = EstadoViaje.FINALIZADO;
        vehiculo.sumarKm(getDistanciaKm());
    }

    // RN23: de las promociones vigentes y aplicables, elige la que más descuenta (no se acumulan)
    public void aplicarMejorPromocion(List<Promocion> promociones) {
        LocalDate fecha = inicio.toLocalDate();
        for (Promocion p : promociones) {
            if (p.estaVigente(fecha) && p.esAplicable(this)) {
                double descuentoPromo = p.calcularDescuento(this);
                if (descuentoPromo > descuento) {
                    promocion = p;
                    descuento = descuentoPromo;
                }
            }
        }
        if (promocion != null) {
            promocion.registrarUso(this);
        }
    }

    // RN17 y RN19: costo según la tarifa, menos el descuento de la promoción elegida
    public double calcularImporte() {
        double importeBase = tarifa.calcularCosto(getDuracionMinutos());
        importe = Math.max(0, importeBase - descuento);
        return importe;
    }

    // Minutos a cobrar, redondeados hacia arriba (S10: 10 min y 1 segundo se cobran 11).
    // RN15: si el usuario encontró una estación llena, hasta 10 minutos desde ese intento no se cobran.
    public int getDuracionMinutos() {
        long segundos = Duration.between(inicio, fin).getSeconds();
        if (intentoFallidoDevolucion != null) {
            long segundosExtra = Duration.between(intentoFallidoDevolucion, fin).getSeconds();
            segundos -= Math.min(segundosExtra, MINUTOS_EXTRA_ESTACION_LLENA * 60);
        }
        return (int) ((segundos + 59) / 60);
    }

    // S18: km del recorrido, sumando los tramos entre el retiro, los puntos y la devolución
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

    // RN18: solo se cancela dentro de los 2 minutos; el vehículo vuelve a la estación de retiro sin cargo
    public void cancelar(LocalDateTime ahora) throws EstacionLlenaExcepcion {
        if (Duration.between(inicio, ahora).toMinutes() >= LIMITE_CANCELACION_MINUTOS) {
            throw new IllegalStateException("Pasaron más de " + LIMITE_CANCELACION_MINUTOS + " minutos: el viaje no se puede cancelar");
        }
        retiro.recibirVehiculo(vehiculo, ahora);
        vehiculo.marcarDisponible();
        fin = ahora;
        estado = EstadoViaje.CANCELADO;
    }

    // RN20: true si el viaje sigue abierto después de 24 horas
    public boolean superaLimitePerdida(LocalDateTime ahora) {
        return estado == EstadoViaje.EN_CURSO && Duration.between(inicio, ahora).toHours() >= LIMITE_PERDIDA_HORAS;
    }

    // RN20 y S2: el vehículo se da por perdido, queda fuera de servicio y se cobra un cargo fijo
    public void marcarPerdido(LocalDateTime ahora) {
        fin = ahora;
        estado = EstadoViaje.PERDIDO;
        importe = CARGO_VEHICULO_PERDIDO;
        vehiculo.darDeBaja();
    }

    // RN24: el viaje cerrado no se modifica; la corrección queda en una nota aparte
    public void agregarNotaAjuste(NotaAjuste nota) {
        notasAjuste.add(nota);
    }

    public int getNumero() {
        return numero;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public Estacion getRetiro() {
        return retiro;
    }

    public Estacion getDevolucion() {
        return devolucion;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public EstadoViaje getEstado() {
        return estado;
    }

    public List<PuntoRecorrido> getPuntosRecorrido() {
        return puntosRecorrido;
    }

    public List<NotaAjuste> getNotasAjuste() {
        return notasAjuste;
    }

    public Promocion getPromocion() {
        return promocion;
    }

    public double getImporte() {
        return importe;
    }

    @Override
    public String toString() {
        return "Viaje " + numero + " - " + usuario.getNombre() + " - " + vehiculo.getCodigo() + " - " + estado;
    }
}