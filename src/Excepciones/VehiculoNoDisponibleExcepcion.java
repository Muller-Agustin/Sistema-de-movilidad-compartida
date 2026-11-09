package Excepciones;

// RN07 y RN08: se lanza cuando se intenta retirar un vehículo que no está disponible
// en la estación (en uso, en reparación, con batería baja o en otra estación).

public class VehiculoNoDisponibleExcepcion extends Exception {
    public VehiculoNoDisponibleExcepcion(String mensaje) {
        super(mensaje);
    }
}