package UsuariosCuenta;

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

public class Usuario implements Reportante {
    private String nombre;
    private final String documento;
    private String email;
    private final LocalDate fechaAlta;
    private EstadoUsuario estado;
    private final Cuenta cuenta;
    private PlanMensual planMensual;
    private final List<Viaje> viajes;

    // RF01: el usuario se crea habilitado y con saldo 0
    public Usuario(String nombre, String documento, String email, LocalDate fechaAlta) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("El correo electrónico no es válido");
        }
        if (fechaAlta == null) {
            throw new IllegalArgumentException("La fecha de alta es obligatoria");
        }
        this.nombre = nombre;
        this.documento = documento;
        this.email = email;
        this.fechaAlta = fechaAlta;
        this.estado = EstadoUsuario.HABILITADO;
        this.cuenta = new Cuenta(0);
        this.viajes = new ArrayList<>();
    }

    // GETTERS
    public String getNombre() { return nombre; }
    public String getDocumento() { return documento; }
    public String getEmail() { return email; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public EstadoUsuario getEstado() { return estado; }
    public Cuenta getCuenta() { return cuenta; }
    public PlanMensual getPlanMensual() { return planMensual; }
    public List<Viaje> getViajes() { return List.copyOf(viajes); }

    // SETTERS
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }

    public void agregarViaje(Viaje viaje) {
        viajes.add(viaje);
    }

    public boolean estaHabilitado() {
        return estado == EstadoUsuario.HABILITADO;
    }

    public boolean tieneViajeEnCurso() {
        for (Viaje viaje : viajes) {
            if (viaje.getEstado() == EstadoViaje.EN_CURSO) {
                return true;
            }
        }
        return false;
    }

    // RN01, RN02 y RN04: true si cumple las tres condiciones para retirar
    public boolean puedeRetirar(double minimo) {
        return estaHabilitado() && !tieneViajeEnCurso() && cuenta.tieneSaldoSuficiente(minimo);
    }

    // igual que puedeRetirar, pero explica con una excepción cuál regla no se cumple
    public void validarRetiro(double minimo) {
        if (!estaHabilitado()) {
            throw new RetiroNoPermitidoExcepcion("El usuario está inhabilitado hasta regularizar su saldo");
        }
        if (tieneViajeEnCurso()) {
            throw new RetiroNoPermitidoExcepcion("El usuario ya tiene un viaje en curso");
        }
        if (!cuenta.tieneSaldoSuficiente(minimo)) {
            throw new RetiroNoPermitidoExcepcion("Saldo insuficiente: se necesitan al menos $" + minimo);
        }
    }

    // RF03 + S12: al recargar y dejar el saldo en cero o más, el usuario se rehabilita
    public void recargarSaldo(double importe, MedioPago medio, LocalDateTime fecha) {
        cuenta.recargar(importe, medio, fecha);
        actualizarEstado();
    }

    // RN03: si después de cobrar el saldo queda negativo, el usuario se inhabilita
    public void cobrar(double importe, String concepto, LocalDateTime fecha) {
        cuenta.debitar(importe, concepto, fecha);
        actualizarEstado();
    }

    // S16: un ajuste del operador (crédito o débito) también puede habilitar o inhabilitar al usuario
    public void ajustarSaldo(double importe, String motivo, LocalDateTime fecha) {
        cuenta.ajustar(importe, motivo, fecha);
        actualizarEstado();
    }

    private void actualizarEstado() {
        if (cuenta.getSaldo() < 0) {
            estado = EstadoUsuario.INHABILITADO;
        } else {
            estado = EstadoUsuario.HABILITADO;
        }
    }

    // RF05: cobra el precio del plan (S15) y lo deja activo
    public void suscribirPlan(LocalDateTime fecha) {
        if (planMensual != null) {
            throw new IllegalStateException("El usuario ya tiene un plan mensual");
        }
        if (!cuenta.tieneSaldoSuficiente(PlanMensual.PRECIO)) {
            throw new SaldoInsuficienteExcepcion("Se necesitan $" + PlanMensual.PRECIO + " para suscribirse al plan");
        }
        cobrar(PlanMensual.PRECIO, "Suscripción al plan mensual", fecha);
        planMensual = new PlanMensual(fecha.toLocalDate());
    }

    // Lo usa la persistencia al cargar un usuario que ya tenía plan: no vuelve a cobrar la suscripción.
    public void restaurarPlanMensual(PlanMensual plan) {
        this.planMensual = plan;
    }

    // RF18: el usuario también puede reportar fallas.
    @Override
    public ReporteFalla reportarFalla(Vehiculo vehiculo, String descripcion, LocalDate fecha) {
        if (vehiculo == null || descripcion == null || descripcion.isBlank() || fecha == null) {
            throw new IllegalArgumentException("El vehículo, la descripción y la fecha son obligatorios");
        }
        ReporteFalla reporte = new ReporteFalla(vehiculo, this, fecha, descripcion);
        vehiculo.agregarReporte(reporte);
        return reporte;
    }

    @Override
    public String getIdentificacion() {
        return "Usuario " + nombre + " (documento " + documento + ")";
    }
}