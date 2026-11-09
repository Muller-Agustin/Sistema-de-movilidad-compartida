package Excepciones;

public class OrdenIncompletaExcepcion extends IllegalStateException {
    public OrdenIncompletaExcepcion(String mensaje) {
        super(mensaje);
    }
}
