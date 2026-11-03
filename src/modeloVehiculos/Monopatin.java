package modeloVehiculos;
import java.time.LocalDate;

public class Monopatin extends VehiculoElectrico {
    private double velocidadMaxima;

    public Monopatin(String codigo, LocalDate fechaAlta, int nivelBateria, double velocidadMaxima) {
        super(codigo, fechaAlta, nivelBateria);
        if (velocidadMaxima <= 0) {
            throw new IllegalArgumentException("La velocidad máxima debe ser positiva");
        }
        this.velocidadMaxima = velocidadMaxima;
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.MONOPATIN;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " - velocidad máx. " + velocidadMaxima + " km/h";
    }

    public double getVelocidadMaxima() { return velocidadMaxima; }
}