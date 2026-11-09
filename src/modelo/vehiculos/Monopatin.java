package modelo.vehiculos;

import java.time.LocalDate;
import modelo.TipoVehiculo;

public class Monopatin extends VehiculoElectrico {
    private final double velocidadMaxima;

    public Monopatin(String codigo, LocalDate fechaAlta, int nivelBateria, double velocidadMaxima) {
        super(codigo, fechaAlta, nivelBateria);
        if (velocidadMaxima <= 0) {
            throw new IllegalArgumentException("La velocidad máxima debe ser mayor a cero");
        }
        this.velocidadMaxima = velocidadMaxima;
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.MONOPATIN;
    }

    public double getVelocidadMaxima() {
        return velocidadMaxima;
    }
}
