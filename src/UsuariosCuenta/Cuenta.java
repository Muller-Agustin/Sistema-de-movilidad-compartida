package UsuariosCuenta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {
    private double saldo;
    private final List<Movimiento> movimientos;
    private final List<MedioPago> mediosPago;

    public Cuenta(double saldo) {
        this.saldo = saldo;
        this.movimientos = new ArrayList<>();
        this.mediosPago = new ArrayList<>();
    }

    public double getSaldo() { return saldo; }
    public List<Movimiento> getMovimientos() { return List.copyOf(movimientos); }
    public List<MedioPago> getMediosPago() { return List.copyOf(mediosPago); }

    // RF02: agrega un medio de pago si todavía no está registrado
    public void agregarMedioPago(MedioPago medio) {
        if (medio == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio");
        }
        if (!mediosPago.contains(medio)) {
            mediosPago.add(medio);
        }
    }

    // RF03: solo se puede recargar con un medio de pago ya registrado
    public void recargar(double importe, MedioPago medio, LocalDateTime fecha) {
        if (!mediosPago.contains(medio)) {
            throw new IllegalArgumentException("El medio de pago no está registrado en la cuenta");
        }
        registrar(new Recarga(fecha, importe, medio));
    }

    // El saldo puede quedar negativo (RN03): el estado del usuario se decide en Usuario
    public void debitar(double importe, String concepto, LocalDateTime fecha) {
        registrar(new Debito(fecha, importe, concepto));
    }

    // S16: corrección con crédito (importe positivo) o débito (importe negativo)
    public void ajustar(double importe, String motivo, LocalDateTime fecha) {
        registrar(new Ajuste(fecha, importe, motivo));
    }

    public boolean tieneSaldoSuficiente(double minimo) {
        return saldo >= minimo;
    }

    // el saldo solo cambia a través de un movimiento, así el historial siempre explica el saldo
    private void registrar(Movimiento movimiento) {
        saldo += movimiento.getEfectoSobreSaldo();
        movimientos.add(movimiento);
    }
}