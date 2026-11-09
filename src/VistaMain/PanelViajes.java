package VistaMain;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import EstacionesTraslados.Coordenada;
import EstacionesTraslados.Estacion;
import Excepciones.EstacionLlenaExcepcion;
import Excepciones.RetiroNoPermitidoExcepcion;
import Excepciones.VehiculoNoDisponibleExcepcion;
import GeneralPersistencia.SistemaMovilidad;
import PersonalServicio.Operador;
import UsuariosCuenta.Usuario;
import VehiculoMantenimiento.Vehiculo;
import ViajesTarifas.EstadoViaje;
import ViajesTarifas.Viaje;

// Pantalla de viajes: retirar (RF06, diagrama de secuencia 1), devolver (RF08, diagrama 2),
// cancelar (RF07), registrar el recorrido, ajustes (RF14) y vehículos perdidos (RF13).

public class PanelViajes extends PanelBase {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelViajes(SistemaMovilidad sistema) {
        super(sistema, "Viajes", "Retirá y devolvé vehículos. Seleccioná un viaje para cancelarlo o ajustarlo.");

        modelo = crearModelo(new String[]{"N°", "Usuario", "Vehículo", "Retiro", "Devolución",
                "Inicio", "Fin", "Minutos", "Promoción", "Importe", "Estado"});
        tabla = crearTabla(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton botonRetirar = crearBoton("Retirar vehículo");
        JButton botonDevolver = crearBoton("Devolver vehículo");
        JButton botonPunto = crearBoton("Registrar punto");
        JButton botonCancelar = crearBoton("Cancelar viaje");
        JButton botonAjuste = crearBoton("Nota de ajuste");
        JButton botonPerdidos = crearBoton("Detectar perdidos");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        acciones.setBackground(FONDO);
        acciones.add(botonRetirar);
        acciones.add(botonDevolver);
        acciones.add(botonPunto);
        acciones.add(botonCancelar);
        acciones.add(botonAjuste);
        acciones.add(botonPerdidos);
        add(acciones, BorderLayout.SOUTH);

        botonRetirar.addActionListener(e -> retirar());
        botonDevolver.addActionListener(e -> devolver());
        botonPunto.addActionListener(e -> registrarPunto());
        botonCancelar.addActionListener(e -> cancelar());
        botonAjuste.addActionListener(e -> registrarAjuste());
        botonPerdidos.addActionListener(e -> detectarPerdidos());

        actualizar();
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        for (Viaje v : sistema.getViajes().getViajes()) {
            String devolucion = "-";
            String fin = "-";
            String minutos = "-";
            if (v.getDevolucion() != null) {
                devolucion = v.getDevolucion().getCodigo();
            }
            if (v.getFin() != null) {
                fin = v.getFin().format(FORMATO);
            }
            if (v.getEstado() == EstadoViaje.FINALIZADO) {
                minutos = String.valueOf(v.getDuracionMinutos());
            }
            String promocion = "-";
            if (v.getPromocion() != null) {
                promocion = v.getPromocion().getNombre();
            }
            modelo.addRow(new Object[]{v.getNumero(), v.getUsuario().getNombre(), v.getVehiculo().getCodigo(),
                    v.getRetiro().getCodigo(), devolucion, v.getInicio().format(FORMATO), fin, minutos,
                    promocion, dinero(v.getImporte()), v.getEstado()});
        }
    }

    // Devuelve el viaje de la fila elegida, o null (y avisa) si no se eligió ninguna
    private Viaje viajeSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            mostrarError("Seleccioná un viaje de la tabla.");
            return null;
        }
        return sistema.getViajes().getViajes().get(fila);
    }

    // Combo con todas las estaciones
    private JComboBox<Estacion> crearComboEstaciones() {
        JComboBox<Estacion> combo = new JComboBox<>();
        for (Estacion e : sistema.getEstaciones().getEstaciones()) {
            combo.addItem(e);
        }
        return combo;
    }

    // Carga en el combo de vehículos los que se pueden retirar en la estación elegida
    private void cargarVehiculosDisponibles(JComboBox<Vehiculo> comboVehiculos, Estacion estacion) {
        comboVehiculos.removeAllItems();
        if (estacion != null) {
            for (Vehiculo v : estacion.getVehiculosDisponibles()) {
                comboVehiculos.addItem(v);
            }
        }
    }

    // RF06: el usuario elige la estación y, según la estación, el vehículo
    private void retirar() {
        JComboBox<Usuario> comboUsuarios = new JComboBox<>();
        for (Usuario u : sistema.getUsuarios().getUsuarios()) {
            comboUsuarios.addItem(u);
        }
        JComboBox<Estacion> comboEstaciones = crearComboEstaciones();
        JComboBox<Vehiculo> comboVehiculos = new JComboBox<>();
        cargarVehiculosDisponibles(comboVehiculos, (Estacion) comboEstaciones.getSelectedItem());

        // Evento: al cambiar la estación se actualiza la lista de vehículos
        comboEstaciones.addActionListener(e ->
                cargarVehiculosDisponibles(comboVehiculos, (Estacion) comboEstaciones.getSelectedItem()));

        boolean aceptado = mostrarFormulario("Retirar vehículo",
                new String[]{"Usuario:", "Estación:", "Vehículo:"},
                new JComponent[]{comboUsuarios, comboEstaciones, comboVehiculos});
        if (!aceptado) {
            return;
        }
        Usuario usuario = (Usuario) comboUsuarios.getSelectedItem();
        Estacion estacion = (Estacion) comboEstaciones.getSelectedItem();
        Vehiculo vehiculo = (Vehiculo) comboVehiculos.getSelectedItem();
        if (usuario == null || vehiculo == null) {
            mostrarError("Elegí un usuario y un vehículo disponible.");
            return;
        }
        try {
            Viaje viaje = sistema.getViajes().retirar(usuario, vehiculo, estacion);
            mostrarMensaje("Retiro confirmado: viaje " + viaje.getNumero() + " iniciado.");
            actualizar();
        } catch (RetiroNoPermitidoExcepcion | VehiculoNoDisponibleExcepcion ex) {
            mostrarError("Retiro rechazado: " + ex.getMessage());
        }
    }

    // RF08: el usuario con un viaje en curso elige dónde devolver
    private void devolver() {
        JComboBox<Usuario> comboUsuarios = new JComboBox<>();
        for (Usuario u : sistema.getUsuarios().getUsuarios()) {
            if (u.tieneViajeEnCurso()) {
                comboUsuarios.addItem(u);
            }
        }
        if (comboUsuarios.getItemCount() == 0) {
            mostrarError("No hay usuarios con un viaje en curso.");
            return;
        }
        JComboBox<Estacion> comboEstaciones = crearComboEstaciones();
        boolean aceptado = mostrarFormulario("Devolver vehículo",
                new String[]{"Usuario:", "Estación de devolución:"},
                new JComponent[]{comboUsuarios, comboEstaciones});
        if (!aceptado) {
            return;
        }
        Usuario usuario = (Usuario) comboUsuarios.getSelectedItem();
        Estacion estacion = (Estacion) comboEstaciones.getSelectedItem();
        try {
            double importe = sistema.getViajes().devolver(usuario, estacion);
            String mensaje = "Devolución confirmada. Importe debitado: " + dinero(importe);
            if (!usuario.estaHabilitado()) {
                mensaje += "\nEl saldo quedó negativo: el usuario queda inhabilitado hasta regularizarlo.";
            }
            mostrarMensaje(mensaje);
            actualizar();
        } catch (EstacionLlenaExcepcion ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void registrarPunto() {
        Viaje viaje = viajeSeleccionado();
        if (viaje == null) {
            return;
        }
        JTextField campoLatitud = new JTextField();
        JTextField campoLongitud = new JTextField();
        boolean aceptado = mostrarFormulario("Punto del recorrido del viaje " + viaje.getNumero(),
                new String[]{"Latitud:", "Longitud:"},
                new JComponent[]{campoLatitud, campoLongitud});
        if (!aceptado) {
            return;
        }
        try {
            double latitud = Double.parseDouble(campoLatitud.getText().trim());
            double longitud = Double.parseDouble(campoLongitud.getText().trim());
            sistema.getViajes().registrarPunto(viaje, new Coordenada(latitud, longitud));
            mostrarMensaje("Punto registrado. El viaje tiene " + viaje.getPuntosRecorrido().size() + " puntos.");
        } catch (NumberFormatException ex) {
            mostrarError("La latitud y la longitud tienen que ser números (ejemplo: -34.60).");
        } catch (IllegalStateException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // RF07 y RN18
    private void cancelar() {
        Viaje viaje = viajeSeleccionado();
        if (viaje == null) {
            return;
        }
        if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
            mostrarError("Solo se puede cancelar un viaje en curso.");
            return;
        }
        try {
            sistema.getViajes().cancelar(viaje);
            mostrarMensaje("Viaje " + viaje.getNumero() + " cancelado sin cargo.");
            actualizar();
        } catch (EstacionLlenaExcepcion | IllegalStateException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // RF14 y RN24: el viaje cerrado no se modifica; se registra una nota de ajuste
    private void registrarAjuste() {
        Viaje viaje = viajeSeleccionado();
        if (viaje == null) {
            return;
        }
        JComboBox<Operador> comboOperadores = new JComboBox<>();
        for (Operador o : sistema.getOperadores()) {
            comboOperadores.addItem(o);
        }
        JTextField campoImporte = new JTextField();
        JTextField campoMotivo = new JTextField();
        boolean aceptado = mostrarFormulario("Nota de ajuste del viaje " + viaje.getNumero(),
                new String[]{"Operador:", "Importe (+ a favor del usuario, - en contra):", "Motivo:"},
                new JComponent[]{comboOperadores, campoImporte, campoMotivo});
        if (!aceptado) {
            return;
        }
        try {
            double importe = Double.parseDouble(campoImporte.getText().trim());
            String motivo = campoMotivo.getText().trim();
            if (importe == 0 || motivo.isEmpty()) {
                mostrarError("El importe no puede ser cero y el motivo es obligatorio.");
                return;
            }
            sistema.getViajes().registrarAjuste(viaje, (Operador) comboOperadores.getSelectedItem(), importe, motivo);
            mostrarMensaje("Nota de ajuste registrada. Nuevo saldo de " + viaje.getUsuario().getNombre()
                    + ": " + dinero(viaje.getUsuario().getCuenta().getSaldo()));
        } catch (NumberFormatException ex) {
            mostrarError("El importe tiene que ser un número.");
        } catch (IllegalStateException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // RF13 y RN20
    private void detectarPerdidos() {
        int cantidad = sistema.getViajes().detectarViajesPerdidos().size();
        if (cantidad == 0) {
            mostrarMensaje("No hay viajes abiertos hace más de 24 horas.");
        } else {
            mostrarMensaje(cantidad + " viaje(s) marcados como perdidos. Se cobró el cargo fijo.");
        }
        actualizar();
    }
}