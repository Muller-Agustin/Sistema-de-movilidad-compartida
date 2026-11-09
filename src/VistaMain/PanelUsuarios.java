package VistaMain;

import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import Excepciones.SaldoInsuficienteExcepcion;
import GeneralPersistencia.SistemaMovilidad;
import UsuariosCuenta.BilleteraVirtual;
import UsuariosCuenta.MedioPago;
import UsuariosCuenta.Movimiento;
import UsuariosCuenta.Tarjeta;
import UsuariosCuenta.Usuario;

// Pantalla de usuarios: alta (RF01), medios de pago (RF02), recargas (RF03),
// movimientos de la cuenta (RF04) y suscripción al plan mensual (RF05).

public class PanelUsuarios extends PanelBase {
    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelUsuarios(SistemaMovilidad sistema) {
        super(sistema, "Usuarios", "Registrá usuarios, cargá medios de pago y recargá saldo.");

        modelo = crearModelo(new String[]{"Documento", "Nombre", "Email", "Saldo", "Plan mensual", "Habilitado", "Viajes"});
        tabla = crearTabla(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton botonNuevo = crearBoton("+ Nuevo usuario");
        JButton botonMedio = crearBoton("Agregar medio de pago");
        JButton botonRecargar = crearBoton("Recargar saldo");
        JButton botonPlan = crearBoton("Suscribir al plan");
        JButton botonMovimientos = crearBoton("Ver movimientos");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        acciones.setBackground(FONDO);
        acciones.add(botonNuevo);
        acciones.add(botonMedio);
        acciones.add(botonRecargar);
        acciones.add(botonPlan);
        acciones.add(botonMovimientos);
        add(acciones, BorderLayout.SOUTH);

        // Eventos de los botones
        botonNuevo.addActionListener(e -> registrarUsuario());
        botonMedio.addActionListener(e -> agregarMedioPago());
        botonRecargar.addActionListener(e -> recargarSaldo());
        botonPlan.addActionListener(e -> suscribirPlan());
        botonMovimientos.addActionListener(e -> verMovimientos());

        actualizar();
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        for (Usuario u : sistema.getUsuarios().getUsuarios()) {
            String plan;
            if (u.getPlanMensual() == null) {
                plan = "No";
            } else {
                plan = "Sí (" + u.getPlanMensual().getViajesUsados() + " viajes usados)";
            }
            String habilitado;
            if (u.estaHabilitado()) {
                habilitado = "Sí";
            } else {
                habilitado = "No (saldo negativo)";
            }
            modelo.addRow(new Object[]{u.getDocumento(), u.getNombre(), u.getEmail(),
                    dinero(u.getCuenta().getSaldo()), plan, habilitado, u.getViajes().size()});
        }
    }

    // Devuelve el usuario de la fila elegida, o null (y avisa) si no se eligió ninguna
    private Usuario usuarioSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            mostrarError("Seleccioná un usuario de la tabla.");
            return null;
        }
        String documento = (String) modelo.getValueAt(fila, 0);
        return sistema.getUsuarios().buscarUsuario(documento);
    }

    private void registrarUsuario() {
        JTextField campoNombre = new JTextField();
        JTextField campoDocumento = new JTextField();
        JTextField campoEmail = new JTextField();
        boolean aceptado = mostrarFormulario("Nuevo usuario",
                new String[]{"Nombre:", "Documento:", "Email:"},
                new JComponent[]{campoNombre, campoDocumento, campoEmail});
        if (!aceptado) {
            return;
        }
        String nombre = campoNombre.getText().trim();
        String documento = campoDocumento.getText().trim();
        String email = campoEmail.getText().trim();
        if (nombre.isEmpty() || documento.isEmpty() || !email.contains("@")) {
            mostrarError("Completá el nombre, el documento y un email válido.");
            return;
        }
        try {
            sistema.getUsuarios().registrarUsuario(new Usuario(nombre, documento, email, LocalDate.now()));
            actualizar();
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void agregarMedioPago() {
        Usuario usuario = usuarioSeleccionado();
        if (usuario == null) {
            return;
        }
        String[] tipos = {"Tarjeta", "Billetera virtual"};
        JComboBox<String> comboTipo = new JComboBox<>(tipos);
        JTextField campoDato = new JTextField();
        boolean aceptado = mostrarFormulario("Medio de pago de " + usuario.getNombre(),
                new String[]{"Tipo:", "Últimos 4 dígitos o alias:"},
                new JComponent[]{comboTipo, campoDato});
        if (!aceptado) {
            return;
        }
        String dato = campoDato.getText().trim();
        MedioPago medio;
        if (comboTipo.getSelectedIndex() == 0) {
            if (dato.length() != 4) {
                mostrarError("Ingresá los últimos 4 dígitos de la tarjeta.");
                return;
            }
            medio = new Tarjeta(usuario.getNombre(), dato);
        } else {
            if (dato.isEmpty()) {
                mostrarError("Ingresá el alias de la billetera.");
                return;
            }
            medio = new BilleteraVirtual(dato);
        }
        sistema.getUsuarios().agregarMedioPago(usuario, medio);
        mostrarMensaje("Se agregó " + medio.getDescripcion() + ".");
    }

    private void recargarSaldo() {
        Usuario usuario = usuarioSeleccionado();
        if (usuario == null) {
            return;
        }
        if (usuario.getCuenta().getMediosPago().isEmpty()) {
            mostrarError("El usuario no tiene medios de pago. Agregá uno primero.");
            return;
        }
        JComboBox<MedioPago> comboMedio = new JComboBox<>();
        for (MedioPago m : usuario.getCuenta().getMediosPago()) {
            comboMedio.addItem(m);
        }
        comboMedio.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean elegido, boolean conFoco) {
                super.getListCellRendererComponent(lista, valor, indice, elegido, conFoco);
                setText(((MedioPago) valor).getDescripcion());
                return this;
            }
        });
        JTextField campoImporte = new JTextField();
        boolean aceptado = mostrarFormulario("Recargar saldo de " + usuario.getNombre(),
                new String[]{"Medio de pago:", "Importe:"},
                new JComponent[]{comboMedio, campoImporte});
        if (!aceptado) {
            return;
        }
        try {
            double importe = Double.parseDouble(campoImporte.getText().trim());
            sistema.getUsuarios().recargarSaldo(usuario, importe, (MedioPago) comboMedio.getSelectedItem());
            actualizar();
        } catch (NumberFormatException ex) {
            mostrarError("El importe tiene que ser un número.");
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void suscribirPlan() {
        Usuario usuario = usuarioSeleccionado();
        if (usuario == null) {
            return;
        }
        try {
            sistema.getUsuarios().suscribirPlan(usuario);
            mostrarMensaje(usuario.getNombre() + " quedó suscripto al plan mensual.");
            actualizar();
        } catch (SaldoInsuficienteExcepcion | IllegalStateException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void verMovimientos() {
        Usuario usuario = usuarioSeleccionado();
        if (usuario == null) {
            return;
        }
        DefaultTableModel modeloMovimientos = crearModelo(new String[]{"Fecha", "Tipo", "Importe", "Concepto"});
        for (Movimiento m : usuario.getCuenta().getMovimientos()) {
            modeloMovimientos.addRow(new Object[]{m.getFecha().toLocalDate(), m.getTipo(),
                    dinero(m.getEfectoSobreSaldo()), m.getConcepto()});
        }
        JScrollPane scroll = new JScrollPane(crearTabla(modeloMovimientos));
        scroll.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(this, scroll, "Movimientos de " + usuario.getNombre(), JOptionPane.PLAIN_MESSAGE);
    }
}