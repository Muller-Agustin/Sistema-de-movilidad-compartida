package modeloVehiculos;
import excepciones.VehiculoNoDisponibleException;
import java.time.LocalDate;

public abstract class Vehiculo {
    private String codigo;
    private LocalDate fechaAlta;
    private double kmRecorridos;
    private  EstadoVehiculo estado;

    protected Vehiculo(String codigo, LocalDate fechaAlta) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        if (fechaAlta == null) {
            throw new IllegalArgumentException("La fecha de alta es obligatoria");
        }
        this.codigo = codigo;
        this.fechaAlta = fechaAlta;
        this.kmRecorridos = 0;
        this.estado = EstadoVehiculo.DISPONIBLE;
    }
    public abstract TipoVehiculo getTipo();

    public boolean puedeRetirarse(){
        return estado==EstadoVehiculo.DISPONIBLE;
    }

    public void marcaeEnUso(){
        if(!puedeRetirarse()){
            throw new VehiculoNoDisponibleException(
                    "El vehiculo" + codigo + "no esta disponible(" + estado.getDescripcion() + ")");
        }
        setEstado(EstadoVehiculo.DISPONIBLE);
    }

    public void marcarDisponible() {
        setEstado(EstadoVehiculo.DISPONIBLE);
    }

    public void enviarAReparacion() {
        setEstado(EstadoVehiculo.EN_REPARACION);
    }

    public void darDeBaja() {
        setEstado(EstadoVehiculo.FUERA_DE_SERVICIO);
    }

    // RF08: al devolver el vehículo se suman los km del viaje.
    public void sumarKm(double km) {
        if (km < 0) {
            throw new IllegalArgumentException("Los km no pueden ser negativos");
        }
        kmRecorridos += km;
    }

    // protected: solo esta clase y sus subclases cambian el estado directamente.
    protected void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return getTipo().getDescripcion() + " " + codigo + " - " + estado.getDescripcion()
                + " - " + kmRecorridos + " km";
    }

    public String getCodigo() { return codigo; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public double getKmRecorridos() { return kmRecorridos; }
    public EstadoVehiculo getEstado() { return estado; }
}

