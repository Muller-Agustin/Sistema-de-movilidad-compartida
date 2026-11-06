package modelo;

import modelo.personal.Operador;

public class NotaAjuste {
    private Operador operador;

    public NotaAjuste(Operador operador) {
        this.operador = operador;
    }

    public Operador getOperador() {
        return operador;
    }

    public void setOperador(Operador operador) {
        this.operador = operador;
    }
}
