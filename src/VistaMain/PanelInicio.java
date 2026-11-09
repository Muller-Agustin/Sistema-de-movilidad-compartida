package VistaMain;

import java.awt.*;
import javax.swing.*;

import GeneralPersistencia.SistemaMovilidad;
import VehiculoMantenimiento.EstadoVehiculo;

// Pantalla de bienvenida con indicadores del sistema.

public class PanelInicio extends PanelBase {
    private JLabel valorUsuarios;
    private JLabel valorDisponibles;
    private JLabel valorEstaciones;
    private JLabel valorViajesEnCurso;
    private JLabel valorOrdenesAbiertas;
    private JLabel valorAlertas;

    public PanelInicio(SistemaMovilidad sistema) {
        super(sistema, "Bienvenido", "Sistema de movilidad compartida: bicicletas y monopatines");

        valorUsuarios = new JLabel();
        valorDisponibles = new JLabel();
        valorEstaciones = new JLabel();
        valorViajesEnCurso = new JLabel();
        valorOrdenesAbiertas = new JLabel();
        valorAlertas = new JLabel();

        JPanel tarjetas = new JPanel(new GridLayout(2, 3, 20, 20));
        tarjetas.setBackground(FONDO);
        tarjetas.add(crearTarjeta("Usuarios registrados", valorUsuarios));
        tarjetas.add(crearTarjeta("Vehículos disponibles", valorDisponibles));
        tarjetas.add(crearTarjeta("Estaciones", valorEstaciones));
        tarjetas.add(crearTarjeta("Viajes en curso", valorViajesEnCurso));
        tarjetas.add(crearTarjeta("Órdenes de mantenimiento abiertas", valorOrdenesAbiertas));
        tarjetas.add(crearTarjeta("Alertas de redistribución", valorAlertas));
        add(tarjetas, BorderLayout.CENTER);

        actualizar();
    }

    private JPanel crearTarjeta(String titulo, JLabel valor) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL_CLARO, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setForeground(Color.GRAY);
        valor.setFont(new Font("SansSerif", Font.BOLD, 36));
        valor.setForeground(AZUL);

        tarjeta.add(etiqueta, BorderLayout.NORTH);
        tarjeta.add(valor, BorderLayout.CENTER);
        return tarjeta;
    }

    @Override
    public void actualizar() {
        valorUsuarios.setText(String.valueOf(sistema.getUsuarios().getUsuarios().size()));
        valorDisponibles.setText(String.valueOf(
                sistema.getMantenimiento().listarVehiculosPorEstado(EstadoVehiculo.DISPONIBLE).size()));
        valorEstaciones.setText(String.valueOf(sistema.getEstaciones().getEstaciones().size()));
        valorViajesEnCurso.setText(String.valueOf(sistema.getViajes().listarViajesEnCurso().size()));
        valorOrdenesAbiertas.setText(String.valueOf(sistema.getMantenimiento().listarOrdenesAbiertas().size()));
        valorAlertas.setText(String.valueOf(sistema.getEstaciones().listarAlertas().size()));
    }
}