package UsuariosCuenta;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Cuenta del usuario: saldo, medios de pago y el historial de movimientos.
// El saldo solo cambia a través de un movimiento, así el historial siempre explica el saldo.

public class Cuenta implements Serializable {
    private static final long serialVersionUID = 1L;
    private double saldo;
    private List<Movimiento> movimientos;
    private List<MedioPago> mediosPago;

    public Cuenta() {
        this.saldo = 0;
        this.movimientos = new ArrayList<>();
        this.mediosPago = new ArrayList<>();
    }

    // RF02
    public void agregarMedioPago(MedioPago medio) {
        mediosPago.add(medio);
    }

    // RF03: solo se puede recargar con un medio de pago ya registrado
    public void recargar(double importe, MedioPago medio, LocalDateTime fecha) {
        if (!mediosPago.contains(medio)) {
            throw new IllegalArgumentException("El medio de pago no está registrado en la cuenta");
        }
        registrar(new Recarga(fecha, importe, medio));
    }

    // RN03: el saldo puede quedar negativo; qué pasa con el usuario lo decide Usuario
    public void debitar(double importe, String concepto, LocalDateTime fecha) {
        registrar(new Debito(fecha, importe, concepto));
    }

    // RN24: ajuste del operador
    public void ajustar(double importe, String motivo, LocalDateTime fecha) {
        registrar(new Ajuste(fecha, importe, motivo));
    }

    public boolean tieneSaldoSuficiente(double minimo) {
        return saldo >= minimo;
    }

    private void registrar(Movimiento movimiento) {
        saldo += movimiento.getEfectoSobreSaldo();
        movimientos.add(movimiento);
    }

    public double getSaldo() {
        return saldo;
    }

    public List<Movimiento> getMovimientos() {
        return movimientos;
    }

    public List<MedioPago> getMediosPago() {
        return mediosPago;
    }
}