package modelo.personal;

// Define tarifas y promociones y pide los reportes. La lógica de los reportes
// está en GeneradorReportes (responsabilidad única).
public class Administrador extends Personal {
    public Administrador(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
    }

    @Override
    public String getRol() {
        return "Administrador";
    }
}