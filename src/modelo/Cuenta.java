package modelo;

import java.util.ArrayList;
import java.util.List;

public class Cuenta {
    private double saldo;
    private List<Movimiento> movimientos;
    private List<MedioPago> mediosPago;

    public Cuenta(double saldo) {
        this.saldo = saldo;
        this.movimientos = new ArrayList<>();
        this.mediosPago = new ArrayList<>();
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public List<Movimiento> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<Movimiento> movimientos) {
        this.movimientos = movimientos;
    }

    public List<MedioPago> getMediosPago() {
        return mediosPago;
    }

    public void setMediosPago(List<MedioPago> mediosPago) {
        this.mediosPago = mediosPago;
    }

    public void recargar(double importe, MedioPago medio) {
    }

    public void debitar(double importe, String concepto) {
    }

    public boolean tieneSaldoSuficiente(double minimo) {
        return false;
    }
}
