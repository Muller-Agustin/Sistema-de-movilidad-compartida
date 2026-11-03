package modeloVehiculos;

import java.time.LocalDate;

public abstract class VehiculoElectrico extends Vehiculo {
    public static final int BATERIA_MINIMA = 15;

    private int nivelBateria;

    protected VehiculoElectrico(String codigo, LocalDate fechaAlta, int nivelBateria){
        super(codigo, fechaAlta);
        validarNivel(nivelBateria);
        this.nivelBateria = nivelBateria;
        if(tieneBateriaBaja()){
            setEstado(EstadoVehiculo.BATERIA_BAJA);
        }

    }
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

    public void recargarBateria() {
        this.nivelBateria = 100;
        if (getEstado() == EstadoVehiculo.BATERIA_BAJA) {
            setEstado(EstadoVehiculo.DISPONIBLE);
        }
    }

    @Override
    public boolean puedeRetirarse() {
        return super.puedeRetirarse() && !tieneBateriaBaja();
    }

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


