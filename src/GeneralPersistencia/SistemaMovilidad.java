package GeneralPersistencia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import EstacionesTraslados.ControladorEstaciones;
import PersonalServicio.Operador;
import UsuariosCuenta.ControladorUsuarios;
import VehiculoMantenimiento.ControladorMantenimiento;
import ViajesTarifas.ControladorViajes;

// Reúne todo el sistema: los cuatro controladores y los operadores.
// Es el único objeto que GestorArchivos guarda y carga. Al guardar una sola raíz, los objetos
// compartidos (por ejemplo, el mismo Vehiculo en una Estacion y en un Viaje) siguen siendo
// el mismo objeto después de cargar.

public class SistemaMovilidad implements Serializable {
    private static final long serialVersionUID = 1L;
    private ControladorUsuarios usuarios;
    private ControladorMantenimiento mantenimiento;
    private ControladorEstaciones estaciones;
    private ControladorViajes viajes;
    private List<Operador> operadores;

    public SistemaMovilidad() {
        this.usuarios = new ControladorUsuarios();
        this.mantenimiento = new ControladorMantenimiento();
        this.estaciones = new ControladorEstaciones();
        this.viajes = new ControladorViajes();
        this.operadores = new ArrayList<>();
    }

    public void registrarOperador(Operador operador) {
        operadores.add(operador);
    }

    // Devuelve null si no existe
    public Operador buscarOperador(String legajo) {
        for (Operador o : operadores) {
            if (o.getLegajo().equals(legajo)) {
                return o;
            }
        }
        return null;
    }

    public ControladorUsuarios getUsuarios() {
        return usuarios;
    }

    public ControladorMantenimiento getMantenimiento() {
        return mantenimiento;
    }

    public ControladorEstaciones getEstaciones() {
        return estaciones;
    }

    public ControladorViajes getViajes() {
        return viajes;
    }

    public List<Operador> getOperadores() {
        return operadores;
    }
}