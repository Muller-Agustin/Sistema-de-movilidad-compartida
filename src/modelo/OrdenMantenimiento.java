package modelo;

public class OrdenMantenimiento {
    private Tecnico tecnico;
    private Vehiculo vehiculo;

    public OrdenMantenimiento(Tecnico tecnico) {
        this.tecnico = tecnico;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    public void setTecnico(Tecnico tecnico) {
        this.tecnico = tecnico;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }
}
