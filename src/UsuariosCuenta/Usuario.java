package UsuariosCuenta;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Excepciones.RetiroNoPermitidoExcepcion;
import Excepciones.SaldoInsuficienteExcepcion;
import VehiculoMantenimiento.Reportante;
import VehiculoMantenimiento.ReporteFalla;
import VehiculoMantenimiento.Vehiculo;
import ViajesTarifas.EstadoViaje;
import ViajesTarifas.Viaje;

// Persona registrada en el servicio. Tiene una cuenta con saldo, puede tener plan mensual
// y guarda su historial de viajes. Experto (GRASP): decide por sí mismo si puede retirar.

public class Usuario implements Reportante, Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private String documento;
    private String email;
    private LocalDate fechaAlta;
    private boolean habilitado;
    private Cuenta cuenta;
    private PlanMensual planMensual;
    private List<Viaje> viajes;

    // RF01: el usuario se crea habilitado y con saldo 0
    public Usuario(String nombre, String documento, String email, LocalDate fechaAlta) {
        this.nombre = nombre;
        this.documento = documento;
        this.email = email;
        this.fechaAlta = fechaAlta;
        this.habilitado = true;
        this.cuenta = new Cuenta();
        this.viajes = new ArrayList<>();
    }

    public boolean tieneViajeEnCurso() {
        for (Viaje viaje : viajes) {
            if (viaje.getEstado() == EstadoViaje.EN_CURSO) {
                return true;
            }
        }
        return false;
    }

    // RN01, RN02 y RN04: si alguna regla no se cumple, lanza la excepción con el motivo
    public void validarRetiro(double saldoMinimo) throws RetiroNoPermitidoExcepcion {
        if (!habilitado) {
            throw new RetiroNoPermitidoExcepcion("El usuario está inhabilitado hasta regularizar su saldo");
        }
        if (tieneViajeEnCurso()) {
            throw new RetiroNoPermitidoExcepcion("El usuario ya tiene un viaje en curso");
        }
        if (!cuenta.tieneSaldoSuficiente(saldoMinimo)) {
            throw new RetiroNoPermitidoExcepcion("Saldo insuficiente: se necesitan al menos $" + saldoMinimo);
        }
    }

    public void agregarViaje(Viaje viaje) {
        viajes.add(viaje);
    }

    // RF03: si la recarga deja el saldo en cero o más, el usuario se vuelve a habilitar
    public void recargarSaldo(double importe, MedioPago medio, LocalDateTime fecha) {
        cuenta.recargar(importe, medio, fecha);
        actualizarHabilitacion();
    }

    // RN03: si después de cobrar el saldo queda negativo, el usuario queda inhabilitado
    public void cobrar(double importe, String concepto, LocalDateTime fecha) {
        cuenta.debitar(importe, concepto, fecha);
        actualizarHabilitacion();
    }

    // RN24: el ajuste del operador también puede habilitar o inhabilitar al usuario
    public void ajustarSaldo(double importe, String motivo, LocalDateTime fecha) {
        cuenta.ajustar(importe, motivo, fecha);
        actualizarHabilitacion();
    }

    private void actualizarHabilitacion() {
        habilitado = cuenta.getSaldo() >= 0;
    }

    // RF05: cobra el precio del plan y lo deja activo
    public void suscribirPlan(LocalDateTime fecha) throws SaldoInsuficienteExcepcion {
        if (planMensual != null) {
            throw new IllegalStateException("El usuario ya tiene un plan mensual");
        }
        if (!cuenta.tieneSaldoSuficiente(PlanMensual.PRECIO)) {
            throw new SaldoInsuficienteExcepcion("Se necesitan $" + PlanMensual.PRECIO + " para suscribirse al plan");
        }
        cobrar(PlanMensual.PRECIO, "Suscripción al plan mensual", fecha);
        planMensual = new PlanMensual(fecha.toLocalDate());
    }

    // RF18: el usuario también puede reportar una falla
    @Override
    public ReporteFalla reportarFalla(Vehiculo vehiculo, String descripcion, LocalDate fecha) {
        ReporteFalla reporte = new ReporteFalla(vehiculo, this, fecha, descripcion);
        vehiculo.agregarReporte(reporte);
        return reporte;
    }

    @Override
    public String getIdentificacion() {
        return "Usuario " + nombre + " (documento " + documento + ")";
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public PlanMensual getPlanMensual() {
        return planMensual;
    }

    public List<Viaje> getViajes() {
        return viajes;
    }

    @Override
    public String toString() {
        return nombre + " (" + documento + ")";
    }
}