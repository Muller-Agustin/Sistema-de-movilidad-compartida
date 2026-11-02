package modelo;

import java.util.ArrayList;
import java.util.List;

public class Usuario implements Reportante {
    private String nombre;
    private String documento;
    private EstadoUsuario estado;
    private Cuenta cuenta;
    private PlanMensual planMensual;
    private List<Viaje> viajes;

    public Usuario(String nombre, String documento, EstadoUsuario estado, Cuenta cuenta) {
        this.nombre = nombre;
        this.documento = documento;
        this.estado = estado;
        this.cuenta = cuenta;
        this.viajes = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public PlanMensual getPlanMensual() {
        return planMensual;
    }

    public void setPlanMensual(PlanMensual planMensual) {
        this.planMensual = planMensual;
    }

    public List<Viaje> getViajes() {
        return viajes;
    }

    public void setViajes(List<Viaje> viajes) {
        this.viajes = viajes;
    }

    public boolean puedeRetirar(double minimo) {
        return false;
    }

    public boolean estaHabilitado() {
        return false;
    }

    public void suscribirPlan(PlanMensual plan) {
    }
}
