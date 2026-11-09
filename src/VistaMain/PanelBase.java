package VistaMain;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import GeneralPersistencia.SistemaMovilidad;

// Clase abstracta con lo que comparten todas las pantallas: colores, encabezado,
// botones, tablas y mensajes. Cada pantalla define cómo se actualiza (actualizar()).

public abstract class PanelBase extends JPanel {
    protected static final Color AZUL = new Color(31, 78, 121);
    protected static final Color AZUL_CLARO = new Color(220, 233, 245);
    protected static final Color FONDO = new Color(245, 247, 250);

    protected SistemaMovilidad sistema;

    public PanelBase(SistemaMovilidad sistema, String titulo, String subtitulo) {
        this.sistema = sistema;
        setLayout(new BorderLayout(15, 15));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel etiquetaTitulo = new JLabel(titulo);
        etiquetaTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        etiquetaTitulo.setForeground(AZUL);

        JLabel etiquetaSubtitulo = new JLabel(subtitulo);
        etiquetaSubtitulo.setForeground(Color.GRAY);

        JPanel encabezado = new JPanel(new GridLayout(2, 1, 0, 5));
        encabezado.setBackground(FONDO);
        encabezado.add(etiquetaTitulo);
        encabezado.add(etiquetaSubtitulo);
        add(encabezado, BorderLayout.NORTH);
    }

    // Vuelve a leer los datos del sistema y refresca la pantalla
    public abstract void actualizar();

    protected JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(AZUL);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    // Modelo de tabla que no se puede editar a mano
    protected DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    protected JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setBackground(AZUL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        return tabla;
    }

    // Muestra un formulario con una etiqueta por campo. Devuelve true si se apretó "Aceptar".
    protected boolean mostrarFormulario(String titulo, String[] etiquetas, JComponent[] campos) {
        JPanel formulario = new JPanel(new GridLayout(etiquetas.length, 2, 8, 8));
        for (int i = 0; i < etiquetas.length; i++) {
            formulario.add(new JLabel(etiquetas[i]));
            formulario.add(campos[i]);
        }
        int opcion = JOptionPane.showConfirmDialog(this, formulario, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return opcion == JOptionPane.OK_OPTION;
    }

    protected void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Movilidad Compartida", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Formato de dinero para las tablas: $ 1500,00
    protected String dinero(double importe) {
        return String.format("$ %.2f", importe);
    }
}