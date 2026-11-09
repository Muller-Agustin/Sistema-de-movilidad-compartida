package ViajesTarifas;

import java.time.LocalDate;

import UsuariosCuenta.PlanMensual;

// RN22 y S6: el plan mensual cubre hasta 30 minutos de cada viaje (desbloqueo incluido),
// con un máximo de 20 viajes por mes. Los minutos que pasan de 30 se cobran a la tarifa normal.

public class PromoPlanMensual extends Promocion {
    private static final long serialVersionUID = 1L;

    public PromoPlanMensual(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        super(nombre, fechaInicio, fechaFin);
    }

    @Override
    public boolean esAplicable(Viaje viaje) {
        PlanMensual plan = viaje.getUsuario().getPlanMensual();
        return plan != null && plan.tieneViajesDisponibles(viaje.getInicio().toLocalDate());
    }

    @Override
    public double calcularDescuento(Viaje viaje) {
        int minutosCubiertos = Math.min(viaje.getDuracionMinutos(), PlanMensual.MINUTOS_MAXIMOS);
        return viaje.getTarifa().calcularCosto(minutosCubiertos);
    }

    // Si el plan fue el elegido, el viaje consume uno de los 20 del mes
    @Override
    public void registrarUso(Viaje viaje) {
        viaje.getUsuario().getPlanMensual().registrarViaje(viaje.getInicio().toLocalDate());
    }
}