package VehiculoMantenimiento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Excepciones.OrdenIncompletaExcepcion;
import PersonalServicio.Operador;
import PersonalServicio.Tecnico;
import ViajesTarifas.Reloj;

// Coordina el mantenimiento: recibe los reportes de falla, genera las órdenes (a mano o automáticas),
// las asigna al técnico con menos órdenes abiertas y las cierra. Las reglas puntuales
// (3 fallas en 30 días, no cerrar sin tareas) viven en Vehiculo y OrdenMantenimiento.
public class ServicioMantenimiento {
    private final Reloj reloj;
    private final List<Tecnico> tecnicos;
    private final List<OrdenMantenimiento> ordenes;

    public ServicioMantenimiento(Reloj reloj) {
        this.reloj = reloj;
        this.tecnicos = new ArrayList<>();
        this.ordenes = new ArrayList<>();
    }

    public void agregarTecnico(Tecnico tecnico) {
        if (tecnico == null) {
            throw new IllegalArgumentException("El técnico es obligatorio");
        }
        for (Tecnico t : tecnicos) {
            if (t.getLegajo().equals(tecnico.getLegajo())) {
                throw new IllegalArgumentException("Ya existe un técnico con el legajo " + tecnico.getLegajo());
            }
        }
        tecnicos.add(tecnico);
    }

    // RF18 + RN10 + S3: si con este reporte el vehículo llega a 3 fallas en 30 días, se genera
    // una orden automática. Si todavía no hay técnicos cargados, el vehículo igual queda en
    // reparación y el operador genera la orden más tarde.
    public ReporteFalla reportarFalla(Reportante autor, Vehiculo vehiculo, String descripcion) {
        LocalDate hoy = reloj.ahora().toLocalDate();
        ReporteFalla reporte = autor.reportarFalla(vehiculo, descripcion, hoy);
        if (!vehiculo.getReportesFalla().contains(reporte)) {
            vehiculo.agregarReporte(reporte);
        }
        if (vehiculo.requiereMantenimientoObligatorio(hoy) && !vehiculo.tieneOrdenAbierta() && !tecnicos.isEmpty()) {
            abrirOrden(vehiculo, tecnicoConMenosOrdenes(), hoy);
        }
        return reporte;
    }

    // RF19: el operador elige el técnico
    public OrdenMantenimiento generarOrden(Operador operador, Vehiculo vehiculo, Tecnico tecnico) {
        if (!tecnicos.contains(tecnico)) {
            throw new IllegalArgumentException("El técnico no está registrado en el sistema");
        }
        OrdenMantenimiento orden = operador.generarOrden(vehiculo, tecnico, reloj.ahora().toLocalDate());
        ordenes.add(orden);
        return orden;
    }

    // RF31: el sistema elige al técnico con menos órdenes abiertas
    public OrdenMantenimiento generarOrdenAutomatica(Operador operador, Vehiculo vehiculo) {
        return generarOrden(operador, vehiculo, tecnicoConMenosOrdenes());
    }

    // Si dos técnicos tienen la misma cantidad, gana el que se registró primero.
    public Tecnico tecnicoConMenosOrdenes() {
        if (tecnicos.isEmpty()) {
            throw new IllegalStateException("No hay técnicos registrados");
        }
        Tecnico elegido = tecnicos.get(0);
        for (Tecnico t : tecnicos) {
            if (t.contarOrdenesAbiertas() < elegido.contarOrdenesAbiertas()) {
                elegido = t;
            }
        }
        return elegido;
    }

    private OrdenMantenimiento abrirOrden(Vehiculo vehiculo, Tecnico tecnico, LocalDate fecha) {
        OrdenMantenimiento orden = new OrdenMantenimiento(vehiculo, tecnico, fecha);
        vehiculo.enviarAReparacion();
        ordenes.add(orden);
        return orden;
    }

    // RF20 + RN11: el técnico carga tareas y repuestos y cierra la orden con la fecha del sistema
    public void cerrarOrden(Tecnico tecnico, OrdenMantenimiento orden, List<String> tareas, List<String> repuestos) {
        if (orden.getTecnico() != tecnico) {
            throw new IllegalArgumentException("La orden no está asignada a este técnico");
        }
        // se controla antes de cargar nada, así un cierre fallido no deja datos a medias
        if (orden.estaAbierta()) {
            if (tareas.isEmpty() && orden.getTareasRealizadas().isEmpty()) {
                throw new OrdenIncompletaExcepcion("No se registraron tareas realizadas");
            }
            orden.validarVehiculoEnReparacion();
        }
        for (String tarea : tareas) {
            orden.agregarTarea(tarea);
        }
        for (String repuesto : repuestos) {
            orden.agregarRepuesto(repuesto);
        }
        orden.cerrar(reloj.ahora().toLocalDate());
    }

    public List<Tecnico> getTecnicos() {
        return List.copyOf(tecnicos);
    }

    public List<OrdenMantenimiento> getOrdenes() {
        return List.copyOf(ordenes);
    }

    public List<OrdenMantenimiento> getOrdenesAbiertas() {
        List<OrdenMantenimiento> abiertas = new ArrayList<>();
        for (OrdenMantenimiento orden : ordenes) {
            if (orden.estaAbierta()) {
                abiertas.add(orden);
            }
        }
        return abiertas;
    }
}