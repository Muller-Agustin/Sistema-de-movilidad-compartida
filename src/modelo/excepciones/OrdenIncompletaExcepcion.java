package modelo.excepciones;


// RN11: se lanza cuando se intenta cerrar una orden sin técnico, fecha o tareas.
public class OrdenIncompletaExcepcion extends RuntimeException {
    public OrdenIncompletaExcepcion(String mensaje) {
        super(mensaje);
    }
}
