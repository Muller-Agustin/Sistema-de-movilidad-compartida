package modeloVehiculos;

public enum TipoVehiculo {
    BICILETA_COMUN("Bicicleta comun"),
    BICILETA_ELECTRICA("Bicicleta electrica"),
    MONOPATIN("Monopatin electrico");

    private final String descripcion;

    TipoVehiculo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
