package modelo;

import java.util.ArrayList;
import java.util.List;

public class Traslado {
    private List<Vehiculo> vehiculos;

    public Traslado() {
        this.vehiculos = new ArrayList<>();
    }

    public List<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public void setVehiculos(List<Vehiculo> vehiculos) {
        this.vehiculos = vehiculos;
    }
}
