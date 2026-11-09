package ViajesTarifas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import VehiculoMantenimiento.TipoVehiculo;

// RF16: guarda las tarifas definidas y elige la vigente para un tipo de vehículo y una fecha.
public class CatalogoTarifas {
    private final List<Tarifa> tarifas;

    public CatalogoTarifas() {
        this.tarifas = new ArrayList<>();
    }

    public void agregar(Tarifa tarifa) {
        if (tarifa == null) {
            throw new IllegalArgumentException("La tarifa es obligatoria");
        }
        tarifas.add(tarifa);
    }

    public List<Tarifa> getTarifas() {
        return List.copyOf(tarifas);
    }

    // S11: entre las tarifas del tipo con vigencia menor o igual a la fecha, gana la más reciente
    public Tarifa tarifaVigente(TipoVehiculo tipo, LocalDate fecha) {
        Tarifa elegida = null;
        for (Tarifa t : tarifas) {
            boolean esDelTipo = t.getTipoVehiculo() == tipo;
            boolean yaRige = !t.getFechaVigencia().isAfter(fecha);
            boolean esMasReciente = elegida == null || t.getFechaVigencia().isAfter(elegida.getFechaVigencia());
            if (esDelTipo && yaRige && esMasReciente) {
                elegida = t;
            }
        }
        if (elegida == null) {
            throw new IllegalStateException("No hay una tarifa vigente para " + tipo + " al " + fecha);
        }
        return elegida;
    }

    // RN17: valores de referencia del enunciado
    public static CatalogoTarifas conValoresDeReferencia(LocalDate vigenteDesde) {
        CatalogoTarifas catalogo = new CatalogoTarifas();
        catalogo.agregar(new Tarifa(TipoVehiculo.BICICLETA_COMUN, 200, 20, vigenteDesde));
        catalogo.agregar(new Tarifa(TipoVehiculo.BICICLETA_ELECTRICA, 300, 45, vigenteDesde));
        catalogo.agregar(new Tarifa(TipoVehiculo.MONOPATIN, 300, 50, vigenteDesde));
        return catalogo;
    }
}