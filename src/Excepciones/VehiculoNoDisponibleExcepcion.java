package Excepciones;

public class VehiculoNoDisponibleExcepcion extends IllegalStateException {
    public VehiculoNoDisponibleExcepcion(String mensaje) {
        super(mensaje);
    }
}
