package modelo.personal;

public abstract class Personal {
    private String legajo;
    private String nombre;
    private String telefono;

    protected Personal(String legajo, String nombre, String telefono) {
        if (legajo == null || legajo.isBlank()) {
            throw new IllegalArgumentException("El legajo es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.legajo = legajo;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public abstract String getRol();

    @Override
    public String toString() {
        return getRol() + " " + nombre + " (legajo " + legajo + ")";
    }

    public String getLegajo() { return legajo; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
}

