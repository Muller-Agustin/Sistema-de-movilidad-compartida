package VistaMain;

import java.time.LocalDate;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import EstacionesTraslados.Coordenada;
import EstacionesTraslados.Estacion;
import Excepciones.EstacionLlenaExcepcion;
import GeneralPersistencia.GestorArchivos;
import GeneralPersistencia.SistemaMovilidad;
import PersonalServicio.Operador;
import PersonalServicio.Tecnico;
import UsuariosCuenta.Tarjeta;
import UsuariosCuenta.Usuario;
import VehiculoMantenimiento.BicicletaComun;
import VehiculoMantenimiento.BicicletaElectrica;
import VehiculoMantenimiento.Monopatin;
import VehiculoMantenimiento.TipoVehiculo;
import ViajesTarifas.PromoPlanMensual;
import ViajesTarifas.PromoPrimerViaje;
import ViajesTarifas.Tarifa;

// Punto de entrada: carga los datos guardados (o crea datos de prueba la primera vez)
// y abre la ventana principal.

public class Main {
    private static final String ARCHIVO_DATOS = "datos/sistema.dat";

    public static void main(String[] args) {
        GestorArchivos gestor = new GestorArchivos(ARCHIVO_DATOS);
        SistemaMovilidad sistema = gestor.cargar();

        // Primera ejecución: el sistema está vacío y se cargan los datos de prueba
        if (sistema.getEstaciones().getEstaciones().isEmpty()) {
            try {
                cargarDatosIniciales(sistema);
            } catch (EstacionLlenaExcepcion e) {
                JOptionPane.showMessageDialog(null, "Error en los datos de prueba: " + e.getMessage());
            }
        }

        // La interfaz gráfica se abre en el hilo de Swing
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(sistema, gestor).setVisible(true));
    }

    private static void cargarDatosIniciales(SistemaMovilidad sistema) throws EstacionLlenaExcepcion {
        LocalDate hoy = LocalDate.now();

        // Tarifas (valores de referencia del enunciado, RN17)
        sistema.getViajes().agregarTarifa(new Tarifa(TipoVehiculo.BICICLETA_COMUN, 200, 20, hoy.minusYears(1)));
        sistema.getViajes().agregarTarifa(new Tarifa(TipoVehiculo.BICICLETA_ELECTRICA, 300, 45, hoy.minusYears(1)));
        sistema.getViajes().agregarTarifa(new Tarifa(TipoVehiculo.MONOPATIN, 300, 50, hoy.minusYears(1)));

        // Promociones (RN21 y RN22)
        sistema.getViajes().agregarPromocion(new PromoPrimerViaje("Primer viaje gratis", hoy.minusYears(1), hoy.plusYears(1)));
        sistema.getViajes().agregarPromocion(new PromoPlanMensual("Plan mensual", hoy.minusYears(1), hoy.plusYears(1)));

        // Estaciones (la E3 es chica para poder probar la estación llena)
        Estacion centro = new Estacion("E1", "Plaza de Mayo", 10, new Coordenada(-34.6083, -58.3712));
        Estacion palermo = new Estacion("E2", "Plaza Italia", 8, new Coordenada(-34.5811, -58.4210));
        Estacion recoleta = new Estacion("E3", "Plaza Francia", 3, new Coordenada(-34.5875, -58.3934));
        sistema.getEstaciones().registrarEstacion(centro);
        sistema.getEstaciones().registrarEstacion(palermo);
        sistema.getEstaciones().registrarEstacion(recoleta);

        // Vehículos
        sistema.getMantenimiento().registrarVehiculo(new BicicletaComun("BC01", hoy, 26), centro);
        sistema.getMantenimiento().registrarVehiculo(new BicicletaComun("BC02", hoy, 28), centro);
        sistema.getMantenimiento().registrarVehiculo(new BicicletaComun("BC03", hoy, 26), palermo);
        sistema.getMantenimiento().registrarVehiculo(new BicicletaElectrica("BE01", hoy, 90, 60), centro);
        sistema.getMantenimiento().registrarVehiculo(new BicicletaElectrica("BE02", hoy, 10, 60), palermo);  // batería baja
        sistema.getMantenimiento().registrarVehiculo(new Monopatin("MO01", hoy, 80, 25), centro);
        sistema.getMantenimiento().registrarVehiculo(new Monopatin("MO02", hoy, 70, 25), recoleta);
        sistema.getMantenimiento().registrarVehiculo(new BicicletaComun("BC04", hoy, 26), recoleta);
        sistema.getMantenimiento().registrarVehiculo(new BicicletaComun("BC05", hoy, 26), recoleta);  // E3 queda llena

        // Personal
        sistema.getMantenimiento().registrarTecnico(new Tecnico("T001", "Carlos Gómez", "1144445555"));
        sistema.getMantenimiento().registrarTecnico(new Tecnico("T002", "Lucía Fernández", "1155556666"));
        sistema.registrarOperador(new Operador("O001", "Martín Díaz", "1166667777"));

        // Usuarios
        Usuario ana = new Usuario("Ana López", "30111222", "ana@mail.com", hoy);
        Tarjeta tarjetaAna = new Tarjeta("Ana López", "4321");
        sistema.getUsuarios().registrarUsuario(ana);
        sistema.getUsuarios().agregarMedioPago(ana, tarjetaAna);
        sistema.getUsuarios().recargarSaldo(ana, 5000, tarjetaAna);

        Usuario juan = new Usuario("Juan Pérez", "28999888", "juan@mail.com", hoy);
        Tarjeta tarjetaJuan = new Tarjeta("Juan Pérez", "1111");
        sistema.getUsuarios().registrarUsuario(juan);
        sistema.getUsuarios().agregarMedioPago(juan, tarjetaJuan);
        sistema.getUsuarios().recargarSaldo(juan, 300, tarjetaJuan);   // no le alcanza para retirar

        sistema.getUsuarios().registrarUsuario(new Usuario("Sofía Ruiz", "35444555", "sofia@mail.com", hoy));
    }
}