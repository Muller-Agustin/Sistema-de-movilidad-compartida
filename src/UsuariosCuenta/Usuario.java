package UsuariosCuenta;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import EstacionesTraslados.Estacion;
import Excepciones.EstacionLlenaExcepcion;
import Excepciones.RetiroNoPermitidoExcepcion;
import Excepciones.SaldoInsuficienteExcepcion;
import Excepciones.VehiculoNoDisponibleExcepcion;
import VehiculoMantenimiento.Reportante;
import VehiculoMantenimiento.ReporteFalla;
import VehiculoMantenimiento.Vehiculo;
import ViajesTarifas.EstadoViaje;
import ViajesTarifas.Promocion;
import ViajesTarifas.Tarifa;
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

    public boolean estaHabilitado() {
        return habilitado;
    }

    // Devuelve el viaje en curso, o null si no tiene ninguno
    public Viaje getViajeEnCurso() {
        for (Viaje viaje : viajes) {
            if (viaje.getEstado() == EstadoViaje.EN_CURSO) {
                return viaje;
            }
        }
        return null;
    }

    public boolean tieneViajeEnCurso() {
        return getViajeEnCurso() != null;
    }

    // RF06 (diagrama de secuencia 1): retirar un vehículo de una estación.
    // RN01, RN02 y RN04: el usuario tiene que estar habilitado, sin otro viaje abierto y con saldo mínimo.
    // RN07 y RN08: el vehículo tiene que estar disponible en esa estación.
    public Viaje iniciarViaje(int numero, Vehiculo vehiculo, Estacion estacion, Tarifa tarifa, LocalDateTime inicio)
            throws RetiroNoPermitidoExcepcion, VehiculoNoDisponibleExcepcion {
        if (!estaHabilitado()) {
            throw new RetiroNoPermitidoExcepcion("El usuario está inhabilitado hasta regularizar su saldo");
        }
        if (tieneViajeEnCurso()) {
            throw new RetiroNoPermitidoExcepcion("El usuario ya tiene un viaje en curso");
        }
        double saldoMinimo = tarifa.calcularSaldoMinimo();
        if (!cuenta.tieneSaldoSuficiente(saldoMinimo)) {
            throw new RetiroNoPermitidoExcepcion("Saldo insuficiente: se necesitan al menos $" + saldoMinimo);
        }
        if (!estacion.getVehiculosDisponibles().contains(vehiculo)) {
            throw new VehiculoNoDisponibleExcepcion("El vehículo " + vehiculo.getCodigo()
                    + " no está disponible en la estación " + estacion.getCodigo());
        }
        Viaje viaje = new Viaje(numero, this, vehiculo, estacion, tarifa, inicio);
        vehiculo.marcarEnUso();
        estacion.liberarVehiculo(vehiculo, inicio);
        viajes.add(viaje);
        return viaje;
    }

    // RF08 (diagrama de secuencia 2): devolver el vehículo y pagar el viaje.
    // RN13 y RN15: si la estación está llena no se devuelve; el usuario gana 10 minutos sin cargo.
    // Devuelve el importe cobrado.
    public double finalizarViaje(Estacion estacion, List<Promocion> promociones, LocalDateTime horaFin)
            throws EstacionLlenaExcepcion {
        Viaje viaje = getViajeEnCurso();
        if (viaje == null) {
            throw new IllegalStateException("El usuario no tiene un viaje en curso");
        }
        if (estacion.estaLlena()) {
            viaje.otorgarMinutosExtra(horaFin);
            throw new EstacionLlenaExcepcion("La estación " + estacion.getCodigo()
                    + " está llena: tiene " + Viaje.MINUTOS_EXTRA_ESTACION_LLENA + " minutos sin cargo para llegar a otra");
        }
        estacion.recibirVehiculo(viaje.getVehiculo(), horaFin);
        viaje.getVehiculo().marcarDisponible();
        viaje.finalizar(estacion, horaFin);
        viaje.aplicarMejorPromocion(promociones);
        double importe = viaje.calcularImporte();
        if (importe > 0) {
            cobrar(importe, "Viaje " + viaje.getNumero(), horaFin);
        }
        return importe;
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