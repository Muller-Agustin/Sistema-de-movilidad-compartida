package modelo.datos;

import java.util.ArrayList;
import java.util.List;
import modelo.Administrador;
import modelo.Operador;
import modelo.Personal;
import modelo.Tecnico;

// personal.txt -> rol;legajo;nombre;telefono
public class PersonalRepositorioArchivo extends RepositorioArchivo<Personal> {
    private static final String ARCHIVO = "personal.txt";

    public PersonalRepositorioArchivo(String carpeta) {
        super(carpeta);
    }

    @Override
    public List<Personal> leerTodos() {
        return leer(ARCHIVO, c -> crear(c[0], c[1], c[2], textoONull(c[3])));
    }

    private Personal crear(String rol, String legajo, String nombre, String telefono) {
        switch (rol) {
            case "Operador":
                return new Operador(legajo, nombre, telefono);
            case "Tecnico":
                return new Tecnico(legajo, nombre, telefono);
            case "Administrador":
                return new Administrador(legajo, nombre, telefono);
            default:
                throw new IllegalArgumentException("Rol desconocido: " + rol);
        }
    }

    @Override
    public void guardarTodos(List<Personal> personal) {
        List<String> lineas = new ArrayList<>();
        for (Personal p : personal) {
            lineas.add(unir(p.getClass().getSimpleName(), p.getLegajo(), p.getNombre(), p.getTelefono()));
        }
        escribir(ARCHIVO, lineas);
    }
}