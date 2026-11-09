package pruebas;

import java.time.LocalDate;
import modelo.cuentas.Usuario;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;

// Prueba mínima del Bloque 0: verifica que el proyecto compila y se puede instanciar.
public class BaseProyectoPrueba {

    public static void ejecutar(Verificador v) {
        Usuario usuario = new Usuario("Ana", "12345678", "ana@mail.com", LocalDate.of(2026, 11, 1));
        v.verificarVerdadero("Base: el usuario nuevo tiene cuenta", usuario.getCuenta() != null);
        v.verificarIgual("Base: el usuario guarda su nombre", "Ana", usuario.getNombre());

        Estacion estacion = new Estacion("E01", "Calle 1", 10, new Coordenada(0, 0));
        v.verificarNumero("Base: una estación nueva está vacía", 0, estacion.getVehiculos().size());
        v.verificarNumero("Base: la estación guarda su capacidad", 10, estacion.getCapacidadMaxima());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}