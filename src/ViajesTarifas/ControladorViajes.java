package ViajesTarifas;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import EstacionesTraslados.Coordenada;
import EstacionesTraslados.Estacion;
import Excepciones.EstacionLlenaExcepcion;
import Excepciones.RetiroNoPermitidoExcepcion;
import Excepciones.VehiculoNoDisponibleExcepcion;
import PersonalServicio.Operador;
import UsuariosCuenta.Usuario;
import VehiculoMantenimiento.TipoVehiculo;
import VehiculoMantenimiento.Vehiculo;

// Controlador (GRASP) del módulo de viajes: guarda las tarifas, las promociones y todos los
// viajes, y recibe los pedidos de la interfaz. Las reglas viven en Usuario y Viaje (Experto).

public class ControladorViajes implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Tarifa> tarifas;
    private List<Promocion> promociones;
    private List<Viaje> viajes;
    private int proximoNumeroViaje;

    public ControladorViajes() {
        this.tarifas = new ArrayList<>();
        this.promociones = new ArrayList<>();
        this.viajes = new ArrayList<>();
        this.proximoNumeroViaje = 1;
    }

    // Tarifas y promociones

    // RF16
    public void agregarTarifa(Tarifa tarifa) {
        tarifas.add(tarifa);
    }

    // S11: entre las tarifas del tipo que ya rigen en esa fecha, gana la más reciente
    public Tarifa buscarTarifaVigente(TipoVehiculo tipo, LocalDate fecha) {
        Tarifa elegida = null;
        for (Tarifa t : tarifas) {
            boolean esDelTipo = t.getTipoVehiculo() == tipo;
            boolean yaRige = !t.getFechaVigencia().isAfter(fecha);
            if (esDelTipo && yaRige && (elegida == null || t.getFechaVigencia().isAfter(elegida.getFechaVigencia()))) {
                elegida = t;
            }
        }
        if (elegida == null) {
            throw new IllegalStateException("No hay una tarifa vigente para " + tipo);
        }
        return elegida;
    }

    // RF17
    public void agregarPromocion(Promocion promocion) {
        promociones.add(promocion);
    }

    // Viajes

    // RF06: busca la tarifa vigente y le pide al usuario que inicie el viaje
    public Viaje retirar(Usuario usuario, Vehiculo vehiculo, Estacion estacion)
            throws RetiroNoPermitidoExcepcion, VehiculoNoDisponibleExcepcion {
        LocalDateTime ahora = LocalDateTime.now();
        Tarifa tarifa = buscarTarifaVigente(vehiculo.getTipo(), ahora.toLocalDate());
        Viaje viaje = usuario.iniciarViaje(proximoNumeroViaje, vehiculo, estacion, tarifa, ahora);
        proximoNumeroViaje++;
        viajes.add(viaje);
        return viaje;
    }

    // RF08: devuelve el vehículo y cobra. Devuelve el importe cobrado.
    public double devolver(Usuario usuario, Estacion estacion) throws EstacionLlenaExcepcion {
        return usuario.finalizarViaje(estacion, promociones, LocalDateTime.now());
    }

    // RF06: mientras el viaje está en curso se registran los puntos del recorrido
    public void registrarPunto(Viaje viaje, Coordenada coordenada) {
        if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
            throw new IllegalStateException("Solo se registran puntos en un viaje en curso");
        }
        viaje.registrarPunto(coordenada, LocalDateTime.now());
    }

    // RF07 y RN18: cancelar dentro de los 2 minutos, sin cargo
    public void cancelar(Viaje viaje) throws EstacionLlenaExcepcion {
        viaje.cancelar(LocalDateTime.now());
    }

    // RF13 y RN20: los viajes abiertos hace 24 horas o más se dan por perdidos y se cobra el cargo fijo
    public List<Viaje> detectarViajesPerdidos() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Viaje> perdidos = new ArrayList<>();
        for (Viaje viaje : viajes) {
            if (viaje.superaLimitePerdida(ahora)) {
                viaje.marcarPerdido(ahora);
                viaje.getUsuario().cobrar(viaje.getImporte(), "Vehículo perdido " + viaje.getVehiculo().getCodigo(), ahora);
                perdidos.add(viaje);
            }
        }
        return perdidos;
    }

    // RF14 y RN24: el viaje cerrado no se modifica; el ajuste queda aparte y mueve el saldo
    public NotaAjuste registrarAjuste(Viaje viaje, Operador operador, double importe, String motivo) {
        if (viaje.getEstado() == EstadoViaje.EN_CURSO) {
            throw new IllegalStateException("Solo se puede ajustar un viaje cerrado");
        }
        LocalDateTime ahora = LocalDateTime.now();
        NotaAjuste nota = new NotaAjuste(operador, ahora, importe, motivo);
        viaje.agregarNotaAjuste(nota);
        viaje.getUsuario().ajustarSaldo(importe, motivo, ahora);
        return nota;
    }

    // RF26: reporte de uso por estación (viajes que empezaron o terminaron en ella)
    public int contarViajesPorEstacion(Estacion estacion) {
        int cantidad = 0;
        for (Viaje viaje : viajes) {
            if (viaje.getRetiro() == estacion || viaje.getDevolucion() == estacion) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public List<Viaje> listarViajesEnCurso() {
        List<Viaje> enCurso = new ArrayList<>();
        for (Viaje viaje : viajes) {
            if (viaje.getEstado() == EstadoViaje.EN_CURSO) {
                enCurso.add(viaje);
            }
        }
        return enCurso;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public List<Promocion> getPromociones() {
        return promociones;
    }

    public List<Viaje> getViajes() {
        return viajes;
    }
}