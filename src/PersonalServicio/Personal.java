package PersonalServicio;

public abstract class Personal {
    private final String legajo;
    private final String nombre;
    private final String telefono;

    protected Personal() {
        this("", "", "");
    }

    protected Personal(String legajo, String nombre, String telefono) {
        this.legajo = legajo;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getLegajo() { return legajo; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
}
