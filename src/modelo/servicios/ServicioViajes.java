package modelo.servicios;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.cuentas.Usuario;
import modelo.EstadoVehiculo;
import modelo.estaciones.Estacion;
import modelo.Operador;
import modelo.tarifas.CatalogoTarifas;
import modelo.tarifas.Promocion;
import modelo.tarifas.Tarifa;
import modelo.vehiculos.Vehiculo;
import modelo.viajes.EstadoViaje;
import modelo.viajes.NotaAjuste;
import modelo.viajes.Viaje;

// Coordina todo lo que pasa con un viaje: retirar, cancelar, devolver y cobrar, detectar vehículos
// perdidos y registrar ajustes. Cada regla puntual vive en la clase experta (Usuario, Viaje, etc.).
public class ServicioViajes {
    private final CalculadoraDeTarifa calculadora;
    private final CatalogoTarifas catalogo;
    private final Reloj reloj;
    private final List<Viaje> viajes;

    public ServicioViajes(CalculadoraDeTarifa calculadora, CatalogoTarifas catalogo, Reloj reloj) {
        this.calculadora = calculadora;
        this.catalogo = catalogo;
        this.reloj = reloj;
        this.viajes = new ArrayList<>();
    }

    // RF06: el usuario retira un vehículo de una estación y empieza un viaje
    public Viaje retirar(Usuario usuario, Vehiculo vehiculo, Estacion estacion) {
        if (vehiculo.getEstacion() != estacion) {
            throw new IllegalArgumentException("El vehículo " + vehiculo.getCodigo() + " no está en la estación " + estacion.getCodigo());
        }
        if (vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE) {
            throw new IllegalStateException("El vehículo no está disponible para retirarse");
        }
        LocalDateTime ahora = reloj.ahora();
        Tarifa tarifa = catalogo.tarifaVigente(vehiculo.getTipo(), ahora.toLocalDate());

        usuario.validarRetiro(tarifa.getSaldoMinimoParaRetirar());   // RN01, RN02, RN04
        vehiculo.marcarEnUso();                                       // RN07, RN08
        estacion.liberarVehiculo(vehiculo, ahora);                    // RN12

        Viaje viaje = new Viaje(usuario, vehiculo, tarifa, estacion, ahora);
        usuario.agregarViaje(viaje);
        viajes.add(viaje);
        return viaje;
    }

    // RF07 + RN18: cancelar dentro de los 2 minutos, sin cargo
    public void cancelar(Viaje viaje) {
        viaje.cancelar(reloj.ahora());
    }

    // RF08 + RF10: devuelve el vehículo, cierra el viaje y cobra. Devuelve el importe cobrado.
    public double devolver(Viaje viaje, Estacion estacion) {
        LocalDateTime ahora = reloj.ahora();
        viaje.finalizar(estacion, ahora);                             // puede lanzar EstacionLlenaExcepcion

        Promocion promocion = calculadora.elegirPromocion(viaje);
        viaje.setPromocion(promocion);
        double importe = calculadora.calcular(viaje);
        if (promocion != null) {
            promocion.registrarUso(viaje);
        }
        if (importe > 0) {
            viaje.getUsuario().cobrar(importe, "Viaje en " + viaje.getVehiculo().getCodigo(), ahora);   // RN03
        }
        viaje.registrarCobro(importe);
        return importe;
    }

    // RF13 + RN20: los viajes abiertos hace 24 horas o más se dan por perdidos y se cobra el cargo fijo
    public List<Viaje> detectarViajesPerdidos() {
        LocalDateTime ahora = reloj.ahora();
        List<Viaje> perdidos = new ArrayList<>();
        for (Viaje viaje : viajes) {
            if (viaje.superaLimitePerdida(ahora)) {
                viaje.marcarPerdido(ahora);
                double cargo = calculadora.getCargoVehiculoPerdido();
                viaje.getUsuario().cobrar(cargo, "Cargo por vehículo perdido " + viaje.getVehiculo().getCodigo(), ahora);
                viaje.registrarCobro(cargo);
                perdidos.add(viaje);
            }
        }
        return perdidos;
    }

    // RF14 + RN24 + S16: el viaje cerrado no se toca; el ajuste se guarda aparte y mueve el saldo
    public NotaAjuste registrarAjuste(Viaje viaje, Operador operador, double importe, String motivo) {
        if (viaje.getEstado() == EstadoViaje.EN_CURSO) {
            throw new IllegalStateException("Solo se puede ajustar un viaje cerrado");
        }
        LocalDateTime ahora = reloj.ahora();
        NotaAjuste nota = new NotaAjuste(operador, ahora, importe, motivo);
        viaje.agregarNotaAjuste(nota);
        viaje.getUsuario().ajustarSaldo(importe, motivo, ahora);
        return nota;
    }

    public List<Viaje> getViajes() {
        return List.copyOf(viajes);
    }

    public List<Viaje> getViajesEnCurso() {
        List<Viaje> enCurso = new ArrayList<>();
        for (Viaje v : viajes) {
            if (v.getEstado() == EstadoViaje.EN_CURSO) {
                enCurso.add(v);
            }
        }
        return enCurso;
    }
}