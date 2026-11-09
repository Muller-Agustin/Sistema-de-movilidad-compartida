package VehiculoMantenimiento;

import java.time.LocalDate;

// Monopatín eléctrico: tiene batería (heredada) y una velocidad máxima.

public class Monopatin extends VehiculoElectrico {
    private static final long serialVersionUID = 1L;
    private double velocidadMaxima;

    public Monopatin(String codigo, LocalDate fechaAlta, int nivelBateria, double velocidadMaxima) {
        super(codigo, fechaAlta, nivelBateria);
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