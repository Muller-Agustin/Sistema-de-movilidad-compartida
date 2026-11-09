package VistaMain;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.*;

import GeneralPersistencia.GestorArchivos;
import GeneralPersistencia.SistemaMovilidad;

// Ventana principal: menú lateral a la izquierda y, a la derecha, la pantalla elegida.
// CardLayout permite tener todas las pantallas cargadas y mostrar una a la vez.

public class VentanaPrincipal extends JFrame {
    private SistemaMovilidad sistema;
    private GestorArchivos gestor;
    private CardLayout administradorPantallas;
    private JPanel panelContenido;
    private JPanel menu;

    public VentanaPrincipal(SistemaMovilidad sistema, GestorArchivos gestor) {
        this.sistema = sistema;
        this.gestor = gestor;

        setTitle("Movilidad Compartida | Bicicletas y monopatines");
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        administradorPantallas = new CardLayout();
        panelContenido = new JPanel(administradorPantallas);
        add(crearMenuLateral(), BorderLayout.WEST);
        add(panelContenido, BorderLayout.CENTER);

        // Cada pantalla se agrega al menú y al panel central
        agregarPantalla("Inicio", new PanelInicio(sistema));
        agregarPantalla("Usuarios", new PanelUsuarios(sistema));
        agregarPantalla("Viajes", new PanelViajes(sistema));
        agregarPantalla("Vehículos", new PanelVehiculos(sistema));
        agregarPantalla("Estaciones", new PanelEstaciones(sistema));
        agregarPantalla("Reportes", new PanelReportes(sistema));

        // Al cerrar la ventana se guardan los datos
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarSistema();
            }
        });
    }

    private JPanel crearMenuLateral() {
        menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(220, 0));
        menu.setBackground(PanelBase.AZUL);
        menu.setBorder(BorderFactory.createEmptyBorder(25, 15, 20, 15));

        JLabel titulo = new JLabel("MOVILIDAD");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("Bicicletas y monopatines");
        subtitulo.setForeground(PanelBase.AZUL_CLARO);

        menu.add(titulo);
        menu.add(Box.createVerticalStrut(5));
        menu.add(subtitulo);
        menu.add(Box.createVerticalStrut(30));
        return menu;
    }

    // Agrega la pantalla al panel central y un botón en el menú que la muestra
    private void agregarPantalla(String nombre, PanelBase pantalla) {
        panelContenido.add(pantalla, nombre);

        JButton boton = new JButton(nombre);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(PanelBase.AZUL);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 10));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(e -> {
            pantalla.actualizar();
            administradorPantallas.show(panelContenido, nombre);
        });

        menu.add(boton);
        menu.add(Box.createVerticalStrut(6));
    }

    // Guarda los datos antes de cerrar. Si no se pudo guardar, avisa y no cierra.
    private void cerrarSistema() {
        try {
            gestor.guardar(sistema);
            dispose();
            System.exit(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudieron guardar los datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}