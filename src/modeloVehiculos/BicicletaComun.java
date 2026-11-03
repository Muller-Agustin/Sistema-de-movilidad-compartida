package modeloVehiculos;

import excepciones.VehiculoNoDisponibleException;
import modelo.BicicletaElectrica;

import java.time.LocalDate;
public class BicicletaComun extends Vehiculo {
    private int rodado;

    public BicicletaComun(String codigo, LocalDate fechaAlta, int rodado){
        super(codigo, fechaAlta);
        if (rodado<=0){
            throw new IllegalArgumentException("El rodado debe ser positivo");
        }
        this.rodado = rodado;
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.BICILETA_COMUN;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " - rodado " + rodado;
    }

    public int getRodado() { return rodado; }
}


