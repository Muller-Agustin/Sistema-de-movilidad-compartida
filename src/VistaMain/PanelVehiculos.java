package VistaMain;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import EstacionesTraslados.Estacion;
import Excepciones.EstacionLlenaExcepcion;
import Excepciones.OrdenIncompletaExcepcion;
import GeneralPersistencia.SistemaMovilidad;
import PersonalServicio.Operador;
import PersonalServicio.Tecnico;
import UsuariosCuenta.Usuario;
import VehiculoMantenimiento.BicicletaComun;
import VehiculoMantenimiento.BicicletaElectrica;
import VehiculoMantenimiento.EstadoVehiculo;
import VehiculoMantenimiento.Monopatin;
import VehiculoMantenimiento.OrdenMantenimiento;
import VehiculoMantenimiento.Reportante;
import VehiculoMantenimiento.Vehiculo;
import VehiculoMantenimiento.VehiculoElectrico;

// Pantalla de vehículos y mantenimiento: alta (RF11), cambio de estado (RF12), recarga de
// batería (RF21), reporte de fallas (RF18) y órdenes de mantenimiento (RF19 y RF20).

public class PanelVehiculos extends PanelBase {
    private DefaultTableModel modeloVehiculos;
    private JTable tablaVehiculos;
    private DefaultTableModel modeloOrdenes;
    private JTable tablaOrdenes;

    public PanelVehiculos(SistemaMovilidad sistema) {
        super(sistema, "Vehículos y mantenimiento", "Seleccioná un vehículo para cambiarle el estado, recargarlo o reportar una falla.");

        modeloVehiculos = crearModelo(new String[]{"Código", "Tipo", "Estado", "Estación", "Km", "Batería", "Fallas"});
        tablaVehiculos = crearTabla(modeloVehiculos);
        modeloOrdenes = crearModelo(new String[]{"Vehículo", "Técnico", "Apertura", "Cierre", "Tareas", "Repuestos"});
        tablaOrdenes = crearTabla(modeloOrdenes);

        JPanel tablas = new JPanel(new GridLayout(2, 1, 0, 10));
        tablas.setBackground(FONDO);
        tablas.add(new JScrollPane(tablaVehiculos));
        JScrollPane scrollOrdenes = new JScrollPane(tablaOrdenes);
        scrollOrdenes.setBorder(BorderFactory.createTitledBorder("Órdenes de mantenimiento"));
        tablas.add(scrollOrdenes);
        add(tablas, BorderLayout.CENTER);

        JButton botonNuevo = crearBoton("+ Nuevo vehículo");
        JButton botonEstado = crearBoton("Cambiar estado");
        JButton botonBateria = crearBoton("Recargar batería");
        JButton botonFalla = crearBoton("Reportar falla");
        JButton botonOrden = crearBoton("Generar orden");
        JButton botonCerrar = crearBoton("Cerrar orden");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        acciones.setBackground(FONDO);
        acciones.add(botonNuevo);
        acciones.add(botonEstado);
        acciones.add(botonBateria);
        acciones.add(botonFalla);
        acciones.add(botonOrden);
        acciones.add(botonCerrar);
        add(acciones, BorderLayout.SOUTH);

        botonNuevo.addActionListener(e -> registrarVehiculo());
        botonEstado.addActionListener(e -> cambiarEstado());
        botonBateria.addActionListener(e -> recargarBateria());
        botonFalla.addActionListener(e -> reportarFalla());
        botonOrden.addActionListener(e -> generarOrden());
        botonCerrar.addActionListener(e -> cerrarOrden());

        actualizar();
    }

    @Override
    public void actualizar() {
        modeloVehiculos.setRowCount(0);
        for (Vehiculo v : sistema.getMantenimiento().listarVehiculos()) {
            String estacion = "-";
            if (v.getEstacion() != null) {
                estacion = v.getEstacion().getCodigo();
            }
            String bateria = "No tiene";
            if (v instanceof VehiculoElectrico) {
                bateria = ((VehiculoElectrico) v).getNivelBateria() + " %";
            }
            modeloVehiculos.addRow(new Object[]{v.getCodigo(), v.getTipo(), v.getEstado(), estacion,
                    String.format("%.1f", v.getKmRecorridos()), bateria, v.getCantidadFallas()});
        }

        modeloOrdenes.setRowCount(0);
        for (OrdenMantenimiento o : sistema.getMantenimiento().getOrdenes()) {
            String cierre = "Abierta";
            if (!o.estaAbierta()) {
                cierre = o.getFechaCierre().toString();
            }
            modeloOrdenes.addRow(new Object[]{o.getVehiculo().getCodigo(), o.getTecnico().getNombre(),
                    o.getFechaApertura(), cierre, String.join(", ", o.getTareasRealizadas()),
                    String.join(", ", o.getRepuestosUtilizados())});
        }
    }

    // Devuelve el vehículo de la fila elegida, o null (y avisa) si no se eligió ninguna
    private Vehiculo vehiculoSeleccionado() {
        int fila = tablaVehiculos.getSelectedRow();
        if (fila == -1) {
            mostrarError("Seleccioná un vehículo de la tabla.");
            return null;
        }
        String codigo = (String) modeloVehiculos.getValueAt(fila, 0);
        return sistema.getMantenimiento().buscarVehiculo(codigo);
    }

    // RF11: según el tipo elegido se crea una subclase distinta de Vehiculo
    private void registrarVehiculo() {
        String[] tipos = {"Bicicleta común", "Bicicleta eléctrica", "Monopatín"};
        JComboBox<String> comboTipo = new JComboBox<>(tipos);
        JTextField campoCodigo = new JTextField();
        JComboBox<Estacion> comboEstacion = new JComboBox<>();
        for (Estacion e : sistema.getEstaciones().getEstaciones()) {
            comboEstacion.addItem(e);
        }
        JTextField campoDato1 = new JTextField();
        JTextField campoDato2 = new JTextField();
        boolean aceptado = mostrarFormulario("Nuevo vehículo",
                new String[]{"Tipo:", "Código:", "Estación:",
                        "Rodado (común) o batería % (eléctricos):", "Autonomía km o velocidad máx. (eléctricos):"},
                new JComponent[]{comboTipo, campoCodigo, comboEstacion, campoDato1, campoDato2});
        if (!aceptado) {
            return;
        }
        String codigo = campoCodigo.getText().trim();
        if (codigo.isEmpty() || comboEstacion.getSelectedItem() == null) {
            mostrarError("El código y la estación son obligatorios.");
            return;
        }
        try {
            int dato1 = Integer.parseInt(campoDato1.getText().trim());
            Vehiculo vehiculo;
            if (comboTipo.getSelectedIndex() == 0) {
                vehiculo = new BicicletaComun(codigo, LocalDate.now(), dato1);
            } else {
                if (dato1 < 0 || dato1 > 100) {
                    mostrarError("La batería tiene que estar entre 0 y 100.");
                    return;
                }
                double dato2 = Double.parseDouble(campoDato2.getText().trim());
                if (comboTipo.getSelectedIndex() == 1) {
                    vehiculo = new BicicletaElectrica(codigo, LocalDate.now(), dato1, dato2);
                } else {
                    vehiculo = new Monopatin(codigo, LocalDate.now(), dato1, dato2);
                }
            }
            sistema.getMantenimiento().registrarVehiculo(vehiculo, (Estacion) comboEstacion.getSelectedItem());
            actualizar();
        } catch (NumberFormatException ex) {
            mostrarError("Los datos del vehículo tienen que ser números.");
        } catch (EstacionLlenaExcepcion | IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // RF12
    private void cambiarEstado() {
        Vehiculo vehiculo = vehiculoSeleccionado();
        if (vehiculo == null) {
            return;
        }
        EstadoVehiculo[] estados = {EstadoVehiculo.DISPONIBLE, EstadoVehiculo.EN_REPARACION, EstadoVehiculo.FUERA_DE_SERVICIO};
        JComboBox<EstadoVehiculo> comboEstado = new JComboBox<>(estados);
        boolean aceptado = mostrarFormulario("Cambiar estado de " + vehiculo.getCodigo(),
                new String[]{"Nuevo estado:"}, new JComponent[]{comboEstado});
        if (!aceptado) {
            return;
        }
        try {
            sistema.getMantenimiento().cambiarEstado(vehiculo, (EstadoVehiculo) comboEstado.getSelectedItem());
            actualizar();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // RF21
    private void recargarBateria() {
        Vehiculo vehiculo = vehiculoSeleccionado();
        if (vehiculo == null) {
            return;
        }
        try {
            sistema.getMantenimiento().recargarBateria(vehiculo);
            mostrarMensaje("Batería de " + vehiculo.getCodigo() + " recargada al 100 %.");
            actualizar();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // RF18: reporta un usuario o un operador (los dos son Reportante)
    private void reportarFalla() {
        Vehiculo vehiculo = vehiculoSeleccionado();
        if (vehiculo == null) {
            return;
        }
        List<Reportante> reportantes = new ArrayList<>();
        for (Usuario u : sistema.getUsuarios().getUsuarios()) {
            reportantes.add(u);
        }
        for (Operador o : sistema.getOperadores()) {
            reportantes.add(o);
        }
        JComboBox<Reportante> comboReportante = new JComboBox<>();
        for (Reportante r : reportantes) {
            comboReportante.addItem(r);
        }
        JTextField campoDescripcion = new JTextField();
        boolean aceptado = mostrarFormulario("Reportar falla de " + vehiculo.getCodigo(),
                new String[]{"Quién reporta:", "Descripción:"},
                new JComponent[]{comboReportante, campoDescripcion});
        if (!aceptado) {
            return;
        }
        String descripcion = campoDescripcion.getText().trim();
        if (descripcion.isEmpty()) {
            mostrarError("La descripción es obligatoria.");
            return;
        }
        sistema.getMantenimiento().reportarFalla((Reportante) comboReportante.getSelectedItem(), vehiculo, descripcion);
        String mensaje = "Falla registrada.";
        if (vehiculo.tieneOrdenAbierta()) {
            mensaje += "\nEl vehículo tiene una orden de mantenimiento abierta.";
        }
        mostrarMensaje(mensaje);
        actualizar();
    }

    // RF19: el operador genera la orden y elige el técnico
    private void generarOrden() {
        Vehiculo vehiculo = vehiculoSeleccionado();
        if (vehiculo == null) {
            return;
        }
        JComboBox<Operador> comboOperador = new JComboBox<>();
        for (Operador o : sistema.getOperadores()) {
            comboOperador.addItem(o);
        }
        JComboBox<Tecnico> comboTecnico = new JComboBox<>();
        for (Tecnico t : sistema.getMantenimiento().getTecnicos()) {
            comboTecnico.addItem(t);
        }
        boolean aceptado = mostrarFormulario("Orden de mantenimiento para " + vehiculo.getCodigo(),
                new String[]{"Operador:", "Técnico:"}, new JComponent[]{comboOperador, comboTecnico});
        if (!aceptado) {
            return;
        }
        try {
            sistema.getMantenimiento().generarOrden((Operador) comboOperador.getSelectedItem(), vehiculo,
                    (Tecnico) comboTecnico.getSelectedItem());
            actualizar();
        } catch (IllegalStateException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // RF20 y RN11: el técnico carga las tareas y los repuestos (separados por coma)
    private void cerrarOrden() {
        int fila = tablaOrdenes.getSelectedRow();
        if (fila == -1) {
            mostrarError("Seleccioná una orden de la tabla de órdenes.");
            return;
        }
        OrdenMantenimiento orden = sistema.getMantenimiento().getOrdenes().get(fila);
        JTextField campoTareas = new JTextField();
        JTextField campoRepuestos = new JTextField();
        boolean aceptado = mostrarFormulario("Cerrar orden de " + orden.getVehiculo().getCodigo(),
                new String[]{"Tareas realizadas (separadas por coma):", "Repuestos usados (separados por coma):"},
                new JComponent[]{campoTareas, campoRepuestos});
        if (!aceptado) {
            return;
        }
        try {
            sistema.getMantenimiento().cerrarOrden(orden, separar(campoTareas.getText()), separar(campoRepuestos.getText()));
            mostrarMensaje("Orden cerrada. El vehículo " + orden.getVehiculo().getCodigo() + " quedó " + orden.getVehiculo().getEstado() + ".");
            actualizar();
        } catch (OrdenIncompletaExcepcion ex) {
            mostrarError("No se puede cerrar la orden: " + ex.getMessage());
        }
    }

    // "cambio de freno, ajuste" -> ["cambio de freno", "ajuste"]
    private List<String> separar(String texto) {
        List<String> lista = new ArrayList<>();
        for (String parte : texto.split(",")) {
            if (!parte.trim().isEmpty()) {
                lista.add(parte.trim());
            }
        }
        return lista;
    }
}