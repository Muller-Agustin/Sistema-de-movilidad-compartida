package VehiculoMantenimiento;

import java.time.LocalDate;

// Clase intermedia: agrupa lo que comparten la bicicleta eléctrica y el monopatín
// (nivel de batería y la regla del 15 %), así no se repite en las dos subclases.

public abstract class VehiculoElectrico extends Vehiculo {
    private static final long serialVersionUID = 1L;
    public static final int BATERIA_MINIMA = 15;

    private int nivelBateria;

    public VehiculoElectrico(String codigo, LocalDate fechaAlta, int nivelBateria) {
        super(codigo, fechaAlta);
        this.nivelBateria = nivelBateria;
        if (tieneBateriaBaja()) {
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        }
    }

    // RN08: con menos del 15 % se considera batería baja
    public boolean tieneBateriaBaja() {
        return nivelBateria < BATERIA_MINIMA;
    }

    // RF21: al recargar queda al 100 % y, si estaba en batería baja, vuelve a estar disponible
    public void recargarBateria() {
        nivelBateria = 100;
        if (getEstado() == EstadoVehiculo.BATERIA_BAJA) {
            setEstado(EstadoVehiculo.DISPONIBLE);
        }
    }

    // RN08: además de estar disponible, necesita batería suficiente (sobrescribe a Vehiculo)
    @Override
    public boolean puedeRetirarse() {
        return super.puedeRetirarse() && !tieneBateriaBaja();
    }

    // Si vuelve de un viaje o de reparación con poca batería, queda en batería baja
    @Override
    public void marcarDisponible() {
        if (tieneBateriaBaja() && !tieneMantenimientoPendiente()) {
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        } else {
            super.marcarDisponible();
        }
    }

    @Override
    public String toString() {
        return super.toString() + " - batería " + nivelBateria + " %";
    }

    public int getNivelBateria() {
        return nivelBateria;
    }
}