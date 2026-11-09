package PersonalServicio;

import java.io.Serializable;

// Personal del servicio. Es abstracta: puede ser operador, técnico o administrador.

public abstract class Personal implements Serializable {
    private static final long serialVersionUID = 1L;
    private String legajo;
    private String nombre;
    private String telefono;

    public Personal(String legajo, String nombre, String telefono) {
        this.legajo = legajo;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getLegajo() {
        return legajo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return nombre + " (legajo " + legajo + ")";
    }
}