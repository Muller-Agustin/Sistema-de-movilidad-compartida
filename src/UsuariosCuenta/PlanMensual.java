package UsuariosCuenta;

import java.time.LocalDate;
import java.time.YearMonth;

// RN22 y S15: 20 viajes por mes de hasta 30 minutos. El contador se reinicia cada mes.
public class PlanMensual {
    public static final double PRECIO = 8000;
    public static final int VIAJES_INCLUIDOS = 20;
    public static final int MINUTOS_MAXIMOS = 30;

    private final LocalDate fechaAlta;
    private YearMonth mesDelContador;
    private int viajesUsados;

    public PlanMensual(LocalDate fechaAlta) {
        if (fechaAlta == null) {
            throw new IllegalArgumentException("La fecha de alta es obligatoria");
        }
        this.fechaAlta = fechaAlta;
        this.mesDelContador = YearMonth.from(fechaAlta);
        this.viajesUsados = 0;
    }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public YearMonth getMesDelContador() { return mesDelContador; }

    // Viajes usados en el mes de "hoy": si cambió el mes, el contador arranca de cero.
    public int getViajesUsados(LocalDate hoy) {
        if (!YearMonth.from(hoy).equals(mesDelContador)) {
            return 0;
        }
        return viajesUsados;
    }

    public int getViajesRestantes(LocalDate hoy) {
        return VIAJES_INCLUIDOS - getViajesUsados(hoy);
    }

    public boolean tieneViajesDisponibles(LocalDate hoy) {
        return getViajesRestantes(hoy) > 0;
    }

    public void registrarViaje(LocalDate hoy) {
        if (!tieneViajesDisponibles(hoy)) {
            throw new IllegalStateException("El plan mensual no tiene viajes disponibles este mes");
        }
        if (!YearMonth.from(hoy).equals(mesDelContador)) {
            mesDelContador = YearMonth.from(hoy);
            viajesUsados = 0;
        }
        viajesUsados++;
    }
}