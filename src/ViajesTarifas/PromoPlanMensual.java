package ViajesTarifas;

import java.time.LocalDate;

import UsuariosCuenta.PlanMensual;

// RN22 + S6: el plan mensual cubre hasta 30 minutos de cada viaje (desbloqueo incluido),
// con un máximo de 20 viajes por mes. Los minutos que pasan de 30 se cobran a la tarifa normal.
public class PromoPlanMensual extends Promocion {

    public PromoPlanMensual(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        super(nombre, fechaInicio, fechaFin);
    }

    @Override
    public boolean esAplicable(Viaje v) {
        PlanMensual plan = v.getUsuario().getPlanMensual();
        LocalDate fecha = v.getInicio().toLocalDate();
        return v.getEstado() == EstadoViaje.FINALIZADO
                && estaVigente(fecha)
                && plan != null
                && plan.tieneViajesDisponibles(fecha);
    }

    @Override
    public double calcularDescuento(Viaje v) {
        if (!esAplicable(v)) {
            return 0.0;
        }
        int minutosCubiertos = Math.min(v.getMinutosCobrables(), PlanMensual.MINUTOS_MAXIMOS);
        return v.getTarifa().calcularCosto(minutosCubiertos);
    }

    // si el plan fue el elegido, el viaje consume uno de los 20 del mes
    @Override
    public void registrarUso(Viaje v) {
        v.getUsuario().getPlanMensual().registrarViaje(v.getInicio().toLocalDate());
    }
}