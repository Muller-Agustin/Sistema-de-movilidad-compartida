package modelo.cuentas;

// RF02: medio con el que el usuario recarga saldo. S13: se simula, no hay integración real.
public abstract class MedioPago {

    protected MedioPago() {
    }

    public abstract String getDescripcion();
}