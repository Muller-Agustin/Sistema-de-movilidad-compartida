package Excepciones;

// RN11: se lanza cuando se intenta cerrar una orden de mantenimiento sin los datos obligatorios.

public class OrdenIncompletaExcepcion extends Exception {
    public OrdenIncompletaExcepcion(String mensaje) {
        super(mensaje);
    }
}