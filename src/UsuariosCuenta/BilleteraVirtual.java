package UsuariosCuenta;

public class BilleteraVirtual extends MedioPago {
    private final String alias;

    public BilleteraVirtual(String alias) {
        if (alias == null || alias.isBlank()) {
            throw new IllegalArgumentException("El alias es obligatorio");
        }
        this.alias = alias;
    }

    public String getAlias() { return alias; }

    @Override
    public String getDescripcion() {
        return "billetera virtual " + alias;
    }
}