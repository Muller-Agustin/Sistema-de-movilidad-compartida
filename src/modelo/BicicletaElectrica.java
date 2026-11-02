package modelo;

public class BicicletaElectrica extends VehiculoElectrico {
    public BicicletaElectrica(String codigo, EstadoVehiculo estado, int nivelBateria) {
        super(codigo, estado, nivelBateria);
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.BICICLETA_ELECTRICA;
    }
}
