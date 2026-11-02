package modelo;

public class Monopatin extends VehiculoElectrico {
    public Monopatin(String codigo, EstadoVehiculo estado, int nivelBateria) {
        super(codigo, estado, nivelBateria);
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.MONOPATIN;
    }
}
