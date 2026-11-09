package modelo.servicios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.BicicletaComun;
import modelo.BicicletaElectrica;
import modelo.EstadoVehiculo;
import modelo.estaciones.Estacion;
import modelo.vehiculos.Monopatin;
import modelo.vehiculos.Vehiculo;
import modelo.vehiculos.VehiculoElectrico;

// Lleva el registro de todos los vehículos del sistema: los da de alta en una estación (RF11),
// permite consultarlos y cambiarles el estado (RF12) y registra la recarga de batería (RF21).
public class ServicioVehiculos {
    private final Reloj reloj;
    private final List<Vehiculo> vehiculos;

    public ServicioVehiculos(Reloj reloj) {
        this.reloj = reloj;
        this.vehiculos = new ArrayList<>();
    }

    // RF11: la fecha de alta es la del reloj del sistema
    public BicicletaComun registrarBicicletaComun(String codigo, int rodado, Estacion estacion) {
        BicicletaComun bicicleta = new BicicletaComun(codigo, reloj.ahora().toLocalDate(), rodado);
        registrar(bicicleta, estacion);
        return bicicleta;
    }

    public BicicletaElectrica registrarBicicletaElectrica(String codigo, int nivelBateria, double autonomiaKm, Estacion estacion) {
        BicicletaElectrica bicicleta = new BicicletaElectrica(codigo, reloj.ahora().toLocalDate(), nivelBateria, autonomiaKm);
        registrar(bicicleta, estacion);
        return bicicleta;
    }

    public Monopatin registrarMonopatin(String codigo, int nivelBateria, double velocidadMaxima, Estacion estacion) {
        Monopatin monopatin = new Monopatin(codigo, reloj.ahora().toLocalDate(), nivelBateria, velocidadMaxima);
        registrar(monopatin, estacion);
        return monopatin;
    }

    // Primero se controla todo y recién después se guarda, así un alta fallida no deja nada a medias.
    private void registrar(Vehiculo vehiculo, Estacion estacion) {
        if (estacion == null) {
            throw new IllegalArgumentException("La estación es obligatoria");
        }
        if (existe(vehiculo.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un vehículo con el código " + vehiculo.getCodigo());
        }
        estacion.recibirVehiculo(vehiculo, reloj.ahora());   // falla si la estación está llena
        vehiculos.add(vehiculo);
    }

    public boolean existe(String codigo) {
        for (Vehiculo v : vehiculos) {
            if (v.getCodigo().equals(codigo)) {
                return true;
            }
        }
        return false;
    }

    public Vehiculo obtener(String codigo) {
        for (Vehiculo v : vehiculos) {
            if (v.getCodigo().equals(codigo)) {
                return v;
            }
        }
        throw new IllegalArgumentException("No existe un vehículo con el código " + codigo);
    }

    // ordenados por código (orden natural de Vehiculo)
    public List<Vehiculo> getVehiculos() {
        List<Vehiculo> copia = new ArrayList<>(vehiculos);
        Collections.sort(copia);
        return copia;
    }

    // RF12: consultar los vehículos que están en un estado
    public List<Vehiculo> getVehiculosPorEstado(EstadoVehiculo estado) {
        List<Vehiculo> resultado = new ArrayList<>();
        for (Vehiculo v : getVehiculos()) {
            if (v.getEstado() == estado) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    // RF12: cambio manual de estado. "En uso" y "Batería baja" no se asignan a mano: el primero lo
    // pone el retiro de un viaje y el segundo depende del nivel de batería (RN08).
    public void cambiarEstado(Vehiculo vehiculo, EstadoVehiculo nuevoEstado) {
        if (vehiculo.getEstado() == EstadoVehiculo.EN_USO) {
            throw new IllegalStateException("El vehículo " + vehiculo.getCodigo() + " está en un viaje");
        }
        switch (nuevoEstado) {
            case DISPONIBLE:
                if (vehiculo.getEstacion() == null) {   // RN12
                    throw new IllegalStateException("El vehículo " + vehiculo.getCodigo() + " no está guardado en una estación");
                }
                if (vehiculo.tieneOrdenAbierta()) {
                    throw new IllegalStateException("El vehículo " + vehiculo.getCodigo() + " tiene una orden de mantenimiento abierta");
                }
                vehiculo.marcarDisponible();
                break;
            case EN_REPARACION:
                vehiculo.enviarAReparacion();
                break;
            case FUERA_DE_SERVICIO:
                vehiculo.darDeBaja();
                break;
            default:
                throw new IllegalArgumentException("El estado \"" + nuevoEstado + "\" no se asigna manualmente");
        }
    }

    // RF21 + RN09: la bicicleta común no tiene batería
    public void registrarRecargaBateria(Vehiculo vehiculo) {
        if (!(vehiculo instanceof VehiculoElectrico)) {
            throw new IllegalArgumentException("El vehículo " + vehiculo.getCodigo() + " no tiene batería");
        }
        ((VehiculoElectrico) vehiculo).recargarBateria();
    }

    // Se usa para informar la lectura de batería (S21)
    public void actualizarBateria(Vehiculo vehiculo, int nivel) {
        if (!(vehiculo instanceof VehiculoElectrico)) {
            throw new IllegalArgumentException("El vehículo " + vehiculo.getCodigo() + " no tiene batería");
        }
        ((VehiculoElectrico) vehiculo).actualizarBateria(nivel);
    }
}