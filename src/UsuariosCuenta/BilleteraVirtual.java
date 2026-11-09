package UsuariosCuenta;

// Medio de pago: billetera virtual, identificada por su alias.

public class BilleteraVirtual extends MedioPago {
    private static final long serialVersionUID = 1L;
    private String alias;

    public BilleteraVirtual(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String getDescripcion() {
        return "billetera virtual " + alias;
    }
}