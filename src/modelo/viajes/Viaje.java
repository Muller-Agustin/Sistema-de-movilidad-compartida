package modelo.viajes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.Usuario;
import modelo.Vehiculo;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;
import modelo.tarifas.Promocion;
import modelo.tarifas.Tarifa;

public class Viaje {
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

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public EstadoViaje getEstado() {
        return estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<PuntoRecorrido> getPuntosRecorrido() {
        return List.copyOf(puntosRecorrido);
    }

    public List<NotaAjuste> getNotasAjuste() {
        return List.copyOf(notasAjuste);
    }

    public void agregarNotaAjuste(NotaAjuste nota) {
        notasAjuste.add(nota);
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public Promocion getPromocion() {
        return promocion;
    }

    public void setPromocion(Promocion promocion) {
        this.promocion = promocion;
    }

    public Estacion getRetiro() {
        return retiro;
    }

    public Estacion getDevolucion() {
        return devolucion;
    }

    public void registrarPunto(Coordenada c, LocalDateTime hora) {
    }

    public void finalizar(Estacion estacion, LocalDateTime fin) {
    }

    public void cancelar(LocalDateTime ahora) {
    }

    public boolean superaLimitePerdida(LocalDateTime ahora) {
        return false;
    }
}
