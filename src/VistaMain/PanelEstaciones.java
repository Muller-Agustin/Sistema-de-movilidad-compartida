package VistaMain;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import EstacionesTraslados.AlertaRedistribucion;
import EstacionesTraslados.Coordenada;
import EstacionesTraslados.Estacion;
import EstacionesTraslados.Traslado;
import Excepciones.EstacionLlenaExcepcion;
import GeneralPersistencia.SistemaMovilidad;
import PersonalServicio.Operador;
import VehiculoMantenimiento.Vehiculo;

// Pantalla de estaciones: alta (RF22), alertas de redistribución (RN16) y traslados (RF23).

public class PanelEstaciones extends PanelBase {
    private DefaultTableModel modeloEstaciones;
    private DefaultTableModel modeloAlertas;

    public PanelEstaciones(SistemaMovilidad sistema) {
        super(sistema, "Estaciones", "Ocupación de cada estación, alertas y traslados de vehículos.");

        modeloEstaciones = crearModelo(new String[]{"Código", "Dirección", "Vehículos", "Capacidad", "Ocupación", "Disponibles para retirar"});
        modeloAlertas = crearModelo(new String[]{"Fecha", "Estación", "Alerta"});

        JPanel tablas = new JPanel(new GridLayout(2, 1, 0, 10));
        tablas.setBackground(FONDO);
        tablas.add(new JScrollPane(crearTabla(modeloEstaciones)));
        JScrollPane scrollAlertas = new JScrollPane(crearTabla(modeloAlertas));
        scrollAlertas.setBorder(BorderFactory.createTitledBorder("Alertas de redistribución (menos del 20 % o más del 90 %)"));
        tablas.add(scrollAlertas);
        add(tablas, BorderLayout.CENTER);

        JButton botonNueva = crearBoton("+ Nueva estación");
        JButton botonTrasladar = crearBoton("Trasladar vehículos");
        JButton botonTraslados = crearBoton("Ver traslados");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        acciones.setBackground(FONDO);
        acciones.add(botonNueva);
        acciones.add(botonTrasladar);
        acciones.add(botonTraslados);
        add(acciones, BorderLayout.SOUTH);

        botonNueva.addActionListener(e -> registrarEstacion());
        botonTrasladar.addActionListener(e -> trasladar());
        botonTraslados.addActionListener(e -> verTraslados());

        actualizar();
    }

    @Override
    public void actualizar() {
        modeloEstaciones.setRowCount(0);
        for (Estacion e : sistema.getEstaciones().getEstaciones()) {
            modeloEstaciones.addRow(new Object[]{e.getCodigo(), e.getDireccion(), e.getVehiculos().size(),
                    e.getCapacidadMaxima(), String.format("%.0f %%", e.getPorcentajeOcupacion()),
                    e.getVehiculosDisponibles().size()});
        }
        modeloAlertas.setRowCount(0);
        for (AlertaRedistribucion a : sistema.getEstaciones().listarAlertas()) {
            modeloAlertas.addRow(new Object[]{a.getFecha().toLocalDate(), a.getEstacion().getCodigo(), a.getTipo()});
        }
    }

    // RF22
    private void registrarEstacion() {
        JTextField campoCodigo = new JTextField();
        JTextField campoDireccion = new JTextField();
        JTextField campoCapacidad = new JTextField();
        JTextField campoLatitud = new JTextField();
        JTextField campoLongitud = new JTextField();
        boolean aceptado = mostrarFormulario("Nueva estación",
                new String[]{"Código:", "Dirección:", "Capacidad máxima:", "Latitud:", "Longitud:"},
                new JComponent[]{campoCodigo, campoDireccion, campoCapacidad, campoLatitud, campoLongitud});
        if (!aceptado) {
            return;
        }
        String codigo = campoCodigo.getText().trim();
        if (codigo.isEmpty()) {
            mostrarError("El código es obligatorio.");
            return;
        }
        try {
            int capacidad = Integer.parseInt(campoCapacidad.getText().trim());
            double latitud = Double.parseDouble(campoLatitud.getText().trim());
            double longitud = Double.parseDouble(campoLongitud.getText().trim());
            if (capacidad <= 0) {
                mostrarError("La capacidad tiene que ser mayor a cero.");
                return;
            }
            sistema.getEstaciones().registrarEstacion(new Estacion(codigo, campoDireccion.getText().trim(),
                    capacidad, new Coordenada(latitud, longitud)));
            actualizar();
        } catch (NumberFormatException ex) {
            mostrarError("La capacidad, la latitud y la longitud tienen que ser números.");
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // Carga en la lista los vehículos que están en la estación de origen
    private void cargarVehiculos(DefaultListModel<Vehiculo> modeloLista, Estacion origen) {
        modeloLista.clear();
        if (origen != null) {
            for (Vehiculo v : origen.getVehiculos()) {
                modeloLista.addElement(v);
            }
        }
    }

    // RF23: el operador elige origen, destino y uno o más vehículos (Ctrl + clic)
    private void trasladar() {
        JComboBox<Operador> comboOperador = new JComboBox<>();
        for (Operador o : sistema.getOperadores()) {
            comboOperador.addItem(o);
        }
        JComboBox<Estacion> comboOrigen = new JComboBox<>();
        JComboBox<Estacion> comboDestino = new JComboBox<>();
        for (Estacion e : sistema.getEstaciones().getEstaciones()) {
            comboOrigen.addItem(e);
            comboDestino.addItem(e);
        }
        DefaultListModel<Vehiculo> modeloLista = new DefaultListModel<>();
        JList<Vehiculo> listaVehiculos = new JList<>(modeloLista);
        listaVehiculos.setVisibleRowCount(6);
        cargarVehiculos(modeloLista, (Estacion) comboOrigen.getSelectedItem());

        // Evento: al cambiar el origen se actualiza la lista de vehículos
        comboOrigen.addActionListener(e -> cargarVehiculos(modeloLista, (Estacion) comboOrigen.getSelectedItem()));

        boolean aceptado = mostrarFormulario("Trasladar vehículos",
                new String[]{"Operador:", "Origen:", "Destino:", "Vehículos (Ctrl + clic para varios):"},
                new JComponent[]{comboOperador, comboOrigen, comboDestino, new JScrollPane(listaVehiculos)});
        if (!aceptado) {
            return;
        }
        Estacion origen = (Estacion) comboOrigen.getSelectedItem();
        Estacion destino = (Estacion) comboDestino.getSelectedItem();
        List<Vehiculo> vehiculos = listaVehiculos.getSelectedValuesList();
        if (origen == destino) {
            mostrarError("El origen y el destino tienen que ser distintos.");
            return;
        }
        if (vehiculos.isEmpty()) {
            mostrarError("Elegí al menos un vehículo.");
            return;
        }
        try {
            Traslado traslado = sistema.getEstaciones().redistribuir((Operador) comboOperador.getSelectedItem(),
                    origen, destino, vehiculos);
            mostrarMensaje("Traslado registrado: " + traslado.getVehiculos().size() + " vehículo(s) de "
                    + origen.getCodigo() + " a " + destino.getCodigo() + ".");
            actualizar();
        } catch (EstacionLlenaExcepcion | IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void verTraslados() {
        DefaultTableModel modeloTraslados = crearModelo(new String[]{"Fecha", "Origen", "Destino", "Vehículos", "Operador"});
        for (Traslado t : sistema.getEstaciones().getTraslados()) {
            StringBuilder codigos = new StringBuilder();
            for (Vehiculo v : t.getVehiculos()) {
                codigos.append(v.getCodigo()).append(" ");
            }
            modeloTraslados.addRow(new Object[]{t.getFecha().toLocalDate(), t.getOrigen().getCodigo(),
                    t.getDestino().getCodigo(), codigos.toString().trim(), t.getOperador().getNombre()});
        }
        JScrollPane scroll = new JScrollPane(crearTabla(modeloTraslados));
        scroll.setPreferredSize(new Dimension(600, 250));
        JOptionPane.showMessageDialog(this, scroll, "Traslados realizados", JOptionPane.PLAIN_MESSAGE);
    }
}