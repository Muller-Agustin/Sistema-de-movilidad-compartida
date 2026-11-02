package modelo;

public abstract class VehiculoElectrico extends Vehiculo {
    private int nivelBateria;

    protected VehiculoElectrico(String codigo, EstadoVehiculo estado, int nivelBateria) {
        super(codigo, estado);
        this.nivelBateria = nivelBateria;
    }

    public int getNivelBateria() {
        return nivelBateria;
    }

    public void setNivelBateria(int nivelBateria) {
        this.nivelBateria = nivelBateria;
    }

    public boolean tieneBateriaBaja() {
        return false;
    }

    @Override
    public boolean puedeRetirarse() {
        return false;
    }
}
