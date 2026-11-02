package modelo;

public class BicicletaComun extends Vehiculo {
    public BicicletaComun(String codigo, EstadoVehiculo estado) {
        super(codigo, estado);
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.BICICLETA_COMUN;
    }
}
