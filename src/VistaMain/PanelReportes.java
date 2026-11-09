package VistaMain;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import EstacionesTraslados.Estacion;
import GeneralPersistencia.SistemaMovilidad;
import UsuariosCuenta.Usuario;
import VehiculoMantenimiento.Vehiculo;
import ViajesTarifas.Viaje;

// Pantalla de reportes del administrador: viajes por usuario (RF25), uso por estación (RF26)
// y vehículos con más fallas (RF27).

public class PanelReportes extends PanelBase {
    private DefaultTableModel modelo;
    private JComboBox<String> comboReporte;

    public PanelReportes(SistemaMovilidad sistema) {
        super(sistema, "Reportes", "Elegí un reporte para verlo.");

        String[] reportes = {"Viajes por usuario", "Uso por estación", "Vehículos con más fallas"};
        comboReporte = new JComboBox<>(reportes);
        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selector.setBackground(FONDO);
        selector.add(new JLabel("Reporte:"));
        selector.add(comboReporte);

        modelo = crearModelo(new String[]{});
        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setBackground(FONDO);
        centro.add(selector, BorderLayout.NORTH);
        centro.add(new JScrollPane(crearTabla(modelo)), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        // Evento: al elegir otro reporte se vuelve a armar la tabla
        comboReporte.addActionListener(e -> actualizar());

        actualizar();
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        int elegido = comboReporte.getSelectedIndex();
        if (elegido == 0) {
            mostrarViajesPorUsuario();
        } else if (elegido == 1) {
            mostrarUsoPorEstacion();
        } else {
            mostrarVehiculosConMasFallas();
        }
    }

    // RF25
    private void mostrarViajesPorUsuario() {
        modelo.setColumnIdentifiers(new String[]{"Documento", "Usuario", "Cantidad de viajes", "Total pagado"});
        for (Usuario u : sistema.getUsuarios().getUsuarios()) {
            double total = 0;
            for (Viaje v : u.getViajes()) {
                total += v.getImporte();
            }
            modelo.addRow(new Object[]{u.getDocumento(), u.getNombre(), u.getViajes().size(), dinero(total)});
        }
    }

    // RF26
    private void mostrarUsoPorEstacion() {
        modelo.setColumnIdentifiers(new String[]{"Estación", "Dirección", "Viajes (retiros y devoluciones)", "Ocupación actual"});
        for (Estacion e : sistema.getEstaciones().getEstaciones()) {
            modelo.addRow(new Object[]{e.getCodigo(), e.getDireccion(),
                    sistema.getViajes().contarViajesPorEstacion(e),
                    e.getVehiculos().size() + " / " + e.getCapacidadMaxima()});
        }
    }

    // RF27: ordenado con ComparadorVehiculosPorFallas (Comparator)
    private void mostrarVehiculosConMasFallas() {
        modelo.setColumnIdentifiers(new String[]{"Vehículo", "Tipo", "Fallas reportadas", "Órdenes de mantenimiento", "Estado"});
        for (Vehiculo v : sistema.getMantenimiento().listarVehiculosConMasFallas()) {
            modelo.addRow(new Object[]{v.getCodigo(), v.getTipo(), v.getCantidadFallas(),
                    v.getOrdenesMantenimiento().size(), v.getEstado()});
        }
    }
}