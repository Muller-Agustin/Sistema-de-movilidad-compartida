package ViajesTarifas;

import java.time.LocalDate;

// RN21 + S6: el primer viaje del usuario es gratis hasta 15 minutos (desbloqueo incluido).
// Los minutos que pasan de 15 se cobran a la tarifa normal.
public class PromoPrimerViaje extends Promocion {
    public static final int MINUTOS_GRATIS = 15;

    public PromoPrimerViaje(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        super(nombre, fechaInicio, fechaFin);
    }

    @Override
    public boolean esAplicable(Viaje v) {
        return v.getEstado() == EstadoViaje.FINALIZADO
                && estaVigente(v.getInicio().toLocalDate())
                && !tuvoViajesAnteriores(v);
    }

    // true si el usuario ya había terminado otro viaje antes de este
    private boolean tuvoViajesAnteriores(Viaje v) {
        for (Viaje otro : v.getUsuario().getViajes()) {
            if (otro != v
                    && otro.getEstado() == EstadoViaje.FINALIZADO
                    && otro.getInicio().isBefore(v.getInicio())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double calcularDescuento(Viaje v) {
        if (!esAplicable(v)) {
            return 0.0;
        }
        int minutosGratis = Math.min(v.getMinutosCobrables(), MINUTOS_GRATIS);
        return v.getTarifa().calcularCosto(minutosGratis);
    }
}