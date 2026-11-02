package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Viaje {
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private EstadoViaje estado;
    private Usuario usuario;
    private List<PuntoRecorrido> puntosRecorrido;
    private List<NotaAjuste> notasAjuste;
    private Vehiculo vehiculo;
    private Tarifa tarifa;
    private Promocion promocion;
    private Estacion retiro;

    public Viaje(LocalDateTime inicio, LocalDateTime fin, EstadoViaje estado, Vehiculo vehiculo, Tarifa tarifa, Estacion retiro) {
        this.inicio = inicio;
        this.fin = fin;
        this.estado = estado;
        this.vehiculo = vehiculo;
        this.tarifa = tarifa;
        this.retiro = retiro;
        this.puntosRecorrido = new ArrayList<>();
        this.notasAjuste = new ArrayList<>();
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public EstadoViaje getEstado() {
        return estado;
    }

    public void setEstado(EstadoViaje estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<PuntoRecorrido> getPuntosRecorrido() {
        return puntosRecorrido;
    }

    public void setPuntosRecorrido(List<PuntoRecorrido> puntosRecorrido) {
        this.puntosRecorrido = puntosRecorrido;
    }

    public List<NotaAjuste> getNotasAjuste() {
        return notasAjuste;
    }

    public void setNotasAjuste(List<NotaAjuste> notasAjuste) {
        this.notasAjuste = notasAjuste;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public void setTarifa(Tarifa tarifa) {
        this.tarifa = tarifa;
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

    public void setRetiro(Estacion retiro) {
        this.retiro = retiro;
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
