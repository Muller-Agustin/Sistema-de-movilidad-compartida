package UsuariosCuenta;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;

// RN22: el plan mensual incluye 20 viajes por mes de hasta 30 minutos.
// El contador de viajes vuelve a cero cuando cambia el mes.

public class PlanMensual implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double PRECIO = 8000;
    public static final int VIAJES_INCLUIDOS = 20;
    public static final int MINUTOS_MAXIMOS = 30;

    private LocalDate fechaAlta;
    private YearMonth mesActual;
    private int viajesUsados;

    public PlanMensual(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
        this.mesActual = YearMonth.from(fechaAlta);
        this.viajesUsados = 0;
    }

    public boolean tieneViajesDisponibles(LocalDate hoy) {
        reiniciarSiCambioElMes(hoy);
        return viajesUsados < VIAJES_INCLUIDOS;
    }

    public void registrarViaje(LocalDate hoy) {
        reiniciarSiCambioElMes(hoy);
        viajesUsados++;
    }

    private void reiniciarSiCambioElMes(LocalDate hoy) {
        if (!YearMonth.from(hoy).equals(mesActual)) {
            mesActual = YearMonth.from(hoy);
            viajesUsados = 0;
        }
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public int getViajesUsados() {
        return viajesUsados;
    }
}