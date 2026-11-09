package UsuariosCuenta;

import java.io.Serializable;

// RF02: medio con el que el usuario recarga saldo. El pago se simula, no hay integración real.

public abstract class MedioPago implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract String getDescripcion();
}