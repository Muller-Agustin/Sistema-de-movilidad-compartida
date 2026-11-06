package modelo.vehiculos;

// RN05: tipos de vehículo. Tarifa usa este enum para saber a qué tipo corresponde.
public enum TipoVehiculo {
    BICICLETA_COMUN("Bicicleta común"),
    BICICLETA_ELECTRICA("Bicicleta eléctrica"),
    MONOPATIN("Monopatín eléctrico");

    private final String descripcion;

    TipoVehiculo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}