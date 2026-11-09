package VehiculoMantenimiento;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import EstacionesTraslados.Estacion;
import Excepciones.EstacionLlenaExcepcion;
import Excepciones.OrdenIncompletaExcepcion;
import PersonalServicio.Operador;
import PersonalServicio.Tecnico;

// Controlador (GRASP) del módulo de vehículos y mantenimiento: recibe los pedidos de la
// interfaz y los delega en las clases del dominio. Las reglas puntuales (3 fallas en 30 días,
// no cerrar una orden sin tareas) viven en Vehiculo y OrdenMantenimiento.

public class ControladorMantenimiento implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Vehiculo> vehiculos;
    private List<Tecnico> tecnicos;
    private List<OrdenMantenimiento> ordenes;

    public ControladorMantenimiento() {
        this.vehiculos = new ArrayList<>();
        this.tecnicos = new ArrayList<>();
        this.ordenes = new ArrayList<>();
    }

    // Vehículos

    // RF11: recibe cualquier tipo de vehículo (LSP: una BicicletaComun o un Monopatin
    // se usan igual que un Vehiculo). Falla si el código se repite o la estación está llena.
    public void registrarVehiculo(Vehiculo vehiculo, Estacion estacion) throws EstacionLlenaExcepcion {
        if (buscarVehiculo(vehiculo.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe un vehículo con el código " + vehiculo.getCodigo());
        }
        estacion.recibirVehiculo(vehiculo, LocalDateTime.now());
        vehiculos.add(vehiculo);
    }

    // Devuelve null si no existe
    public Vehiculo buscarVehiculo(String codigo) {
        for (Vehiculo v : vehiculos) {
            if (v.getCodigo().equals(codigo)) {
                return v;
            }
        }
        return null;
    }

    // Ordenados por código (orden natural de Vehiculo)
    public List<Vehiculo> listarVehiculos() {
        List<Vehiculo> lista = new ArrayList<>(vehiculos);
        Collections.sort(lista);
        return lista;
    }

    // RF12: vehículos que están en un estado
    public List<Vehiculo> listarVehiculosPorEstado(EstadoVehiculo estado) {
        List<Vehiculo> resultado = new ArrayList<>();
        for (Vehiculo v : listarVehiculos()) {
            if (v.getEstado() == estado) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    // RF27: reporte de vehículos con más fallas (orden personalizado con Comparator)
    public List<Vehiculo> listarVehiculosConMasFallas() {
        List<Vehiculo> lista = new ArrayList<>(vehiculos);
        lista.sort(new ComparadorVehiculosPorFallas());
        return lista;
    }

    // RF12: cambio manual de estado. "En uso" y "Batería baja" no se asignan a mano:
    // el primero lo pone el retiro de un viaje y el segundo depende de la batería (RN08).
    public void cambiarEstado(Vehiculo vehiculo, EstadoVehiculo nuevoEstado) {
        if (vehiculo.getEstado() == EstadoVehiculo.EN_USO) {
            throw new IllegalStateException("El vehículo " + vehiculo.getCodigo() + " está en un viaje");
        }
        if (nuevoEstado == EstadoVehiculo.DISPONIBLE) {
            if (vehiculo.tieneOrdenAbierta()) {
                throw new IllegalStateException("El vehículo " + vehiculo.getCodigo() + " tiene una orden de mantenimiento abierta");
            }
            vehiculo.marcarDisponible();
        } else if (nuevoEstado == EstadoVehiculo.EN_REPARACION) {
            vehiculo.enviarAReparacion();
        } else if (nuevoEstado == EstadoVehiculo.FUERA_DE_SERVICIO) {
            vehiculo.darDeBaja();
        } else {
            throw new IllegalArgumentException("El estado " + nuevoEstado + " no se asigna manualmente");
        }
    }

    // RF21 y RN09: la bicicleta común no tiene batería
    public void recargarBateria(Vehiculo vehiculo) {
        if (!(vehiculo instanceof VehiculoElectrico)) {
            throw new IllegalArgumentException("El vehículo " + vehiculo.getCodigo() + " no tiene batería");
        }
        if (vehiculo.getEstado() == EstadoVehiculo.EN_USO) {
            throw new IllegalStateException("No se puede recargar un vehículo que está en uso");
        }
        ((VehiculoElectrico) vehiculo).recargarBateria();
    }

    // Técnicos y órdenes

    public void registrarTecnico(Tecnico tecnico) {
        tecnicos.add(tecnico);
    }

    // RF18 y RN10: si con este reporte el vehículo llega a 3 fallas en 30 días, se abre
    // una orden automática para el técnico con menos órdenes abiertas.
    public ReporteFalla reportarFalla(Reportante autor, Vehiculo vehiculo, String descripcion) {
        LocalDate hoy = LocalDate.now();
        ReporteFalla reporte = autor.reportarFalla(vehiculo, descripcion, hoy);
        if (vehiculo.requiereMantenimientoObligatorio(hoy) && !vehiculo.tieneOrdenAbierta() && !tecnicos.isEmpty()) {
            OrdenMantenimiento orden = new OrdenMantenimiento(vehiculo, tecnicoConMenosOrdenes(), hoy);
            ordenes.add(orden);
        }
        return reporte;
    }

    // RF19: el operador genera la orden y elige el técnico
    public OrdenMantenimiento generarOrden(Operador operador, Vehiculo vehiculo, Tecnico tecnico) {
        if (vehiculo.tieneOrdenAbierta()) {
            throw new IllegalStateException("El vehículo " + vehiculo.getCodigo() + " ya tiene una orden abierta");
        }
        OrdenMantenimiento orden = operador.generarOrden(vehiculo, tecnico, LocalDate.now());
        ordenes.add(orden);
        return orden;
    }

    // Si dos técnicos tienen la misma cantidad, gana el que se registró primero
    public Tecnico tecnicoConMenosOrdenes() {
        Tecnico elegido = tecnicos.get(0);
        for (Tecnico t : tecnicos) {
            if (t.contarOrdenesAbiertas() < elegido.contarOrdenesAbiertas()) {
                elegido = t;
            }
        }
        return elegido;
    }

    // RF20 y RN11: el técnico carga tareas y repuestos y cierra la orden con la fecha de hoy
    public void cerrarOrden(OrdenMantenimiento orden, List<String> tareas, List<String> repuestos)
            throws OrdenIncompletaExcepcion {
        orden.cerrar(LocalDate.now(), tareas, repuestos);
    }

    public List<OrdenMantenimiento> listarOrdenesAbiertas() {
        List<OrdenMantenimiento> abiertas = new ArrayList<>();
        for (OrdenMantenimiento orden : ordenes) {
            if (orden.estaAbierta()) {
                abiertas.add(orden);
            }
        }
        return abiertas;
    }

    public List<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public List<Tecnico> getTecnicos() {
        return tecnicos;
    }

    public List<OrdenMantenimiento> getOrdenes() {
        return ordenes;
    }
}