package ViajesTarifas;

import java.time.LocalDate;

// RN21 y S6: el primer viaje del usuario es gratis hasta 15 minutos (desbloqueo incluido).
// Los minutos que pasan de 15 se cobran a la tarifa normal.

public class PromoPrimerViaje extends Promocion {
    private static final long serialVersionUID = 1L;
    public static final int MINUTOS_GRATIS = 15;

    public PromoPrimerViaje(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        super(nombre, fechaInicio, fechaFin);
    }

    // Aplica si el usuario no terminó ningún otro viaje antes de este
    @Override
    public boolean esAplicable(Viaje viaje) {
        for (Viaje otro : viaje.getUsuario().getViajes()) {
            if (otro != viaje && otro.getEstado() == EstadoViaje.FINALIZADO) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double calcularDescuento(Viaje viaje) {
        int minutosGratis = Math.min(viaje.getDuracionMinutos(), MINUTOS_GRATIS);
        return viaje.getTarifa().calcularCosto(minutosGratis);
    }
}