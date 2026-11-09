package VehiculoMantenimiento;

import java.time.LocalDate;

public class BicicletaComun extends Vehiculo {
    private final int rodado;

    public BicicletaComun(String codigo, LocalDate fechaAlta, int rodado) {
        super(codigo, fechaAlta);
        if (rodado <= 0) {
            throw new IllegalArgumentException("El rodado debe ser mayor a cero");
        }
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
