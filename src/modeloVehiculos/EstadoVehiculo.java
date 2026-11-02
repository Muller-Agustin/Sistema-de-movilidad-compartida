package modeloVehiculos;

public enum EstadoVehiculo {
    DISPONIBLE("Disponible"),
    EN_USO("En uso"),
    EN_REPARACION("En reparacion"),
    BATERIA_BAJA("Bateria baja"),
    FUERA_DE_SERVICIO("Fuera de servicio");

    private final String descripcion;

    EstadoVehiculo(String descripcion){
        this.descripcion = descripcion;
    }

    public String getDescripcion(){
        return this.descripcion;
    }
}
