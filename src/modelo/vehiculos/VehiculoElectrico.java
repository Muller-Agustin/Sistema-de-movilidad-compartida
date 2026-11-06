package modelo.vehiculos;

import java.time.LocalDate;

// Clase intermedia: agrupa lo que comparten la bicicleta eléctrica y el monopatín
// (nivel de batería y la regla del 15 %), así no se repite en las dos subclases.
public abstract class VehiculoElectrico extends Vehiculo {
    public static final int BATERIA_MINIMA = 15;

    private int nivelBateria;

    protected VehiculoElectrico(String codigo, LocalDate fechaAlta, int nivelBateria) {
        super(codigo, fechaAlta);
        validarNivel(nivelBateria);
        this.nivelBateria = nivelBateria;
        if (tieneBateriaBaja()) {
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        }
    }

    // RN08: con menos del 15 % se considera batería baja.
    public boolean tieneBateriaBaja() {
        return nivelBateria < BATERIA_MINIMA;
    }

    public void actualizarBateria(int nivel) {
        validarNivel(nivel);
        this.nivelBateria = nivel;
        if (tieneBateriaBaja() && getEstado() == EstadoVehiculo.DISPONIBLE) {
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        }
    }

    // RF21: al recargar queda al 100 % y, si estaba en batería baja, vuelve a estar disponible.
    public void recargarBateria() {
        this.nivelBateria = 100;
        if (getEstado() == EstadoVehiculo.BATERIA_BAJA) {
            setEstado(EstadoVehiculo.DISPONIBLE);
        }
    }

    // RN08: además de estar disponible, necesita batería suficiente.
    @Override
    public boolean puedeRetirarse() {
        return super.puedeRetirarse() && !tieneBateriaBaja();
    }

    // Si vuelve de un viaje o de reparación con poca batería, queda en batería baja.
    @Override
    public void marcarDisponible() {
        if (tieneBateriaBaja()) {
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        } else {
            super.marcarDisponible();
        }
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " - batería " + nivelBateria + " %";
    }

    private void validarNivel(int nivel) {
        if (nivel < 0 || nivel > 100) {
            throw new IllegalArgumentException("El nivel de batería debe estar entre 0 y 100");
        }
    }

    public int getNivelBateria() { return nivelBateria; }
}
