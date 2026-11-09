package VehiculoMantenimiento;

import java.time.LocalDate;

// Bicicleta común: no tiene batería (RN09). Se registra el tamaño de rodado.

public class BicicletaComun extends Vehiculo {
    private static final long serialVersionUID = 1L;
    private int rodado;

    public BicicletaComun(String codigo, LocalDate fechaAlta, int rodado) {
        super(codigo, fechaAlta);
        this.rodado = rodado;
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.BICICLETA_COMUN;
    }

    public int getRodado() {
        return rodado;
    }
}