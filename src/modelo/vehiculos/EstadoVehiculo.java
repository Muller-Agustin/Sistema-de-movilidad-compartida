package modelo.vehiculos;

// RN06: estados posibles de un vehículo.
// S2: FUERA_DE_SERVICIO se usa también para vehículos perdidos.
public enum EstadoVehiculo {
    DISPONIBLE("Disponible"),
    EN_USO("En uso"),
    EN_REPARACION("En reparación"),
    BATERIA_BAJA("Batería baja"),
    FUERA_DE_SERVICIO("Fuera de servicio");

    private final String descripcion;

    EstadoVehiculo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
