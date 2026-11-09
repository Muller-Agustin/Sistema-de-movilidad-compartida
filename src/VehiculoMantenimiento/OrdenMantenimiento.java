package VehiculoMantenimiento;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Excepciones.OrdenIncompletaExcepcion;
import PersonalServicio.Tecnico;

// RF19 y RF20: orden de mantenimiento de un vehículo, asignada a un técnico.
// Mientras no tiene fecha de cierre, está abierta.

public class OrdenMantenimiento implements Serializable {
    private static final long serialVersionUID = 1L;
    private Vehiculo vehiculo;
    private Tecnico tecnico;
    private LocalDate fechaApertura;
    private LocalDate fechaCierre;
    private List<String> tareasRealizadas;
    private List<String> repuestosUtilizados;

    public OrdenMantenimiento(Vehiculo vehiculo, Tecnico tecnico, LocalDate fechaApertura) {
        this.vehiculo = vehiculo;
        this.tecnico = tecnico;
        this.fechaApertura = fechaApertura;
        this.tareasRealizadas = new ArrayList<>();
        this.repuestosUtilizados = new ArrayList<>();
        // Se registra en los dos lados para que la relación quede completa
        vehiculo.agregarOrden(this);
        tecnico.asignarOrden(this);
    }

    // RN11: no se cierra sin técnico, fecha de cierre y tareas realizadas.
    // S20: tampoco si el vehículo todavía no llegó a reparación (seguía en un viaje).
    public void cerrar(LocalDate fecha, List<String> tareas, List<String> repuestos) throws OrdenIncompletaExcepcion {
        if (!estaAbierta()) {
            throw new OrdenIncompletaExcepcion("La orden ya está cerrada");
        }
        if (tecnico == null) {
            throw new OrdenIncompletaExcepcion("La orden no tiene técnico asignado");
        }
        if (fecha == null || fecha.isBefore(fechaApertura)) {
            throw new OrdenIncompletaExcepcion("La fecha de cierre no es válida");
        }
        if (tareas.isEmpty()) {
            throw new OrdenIncompletaExcepcion("No se registraron tareas realizadas");
        }
        if (vehiculo.getEstado() != EstadoVehiculo.EN_REPARACION) {
            throw new OrdenIncompletaExcepcion("El vehículo " + vehiculo.getCodigo() + " todavía no está en reparación");
        }
        tareasRealizadas.addAll(tareas);
        repuestosUtilizados.addAll(repuestos);
        fechaCierre = fecha;
        // El vehículo reparado vuelve a estar disponible (o en batería baja si es eléctrico)
        vehiculo.marcarDisponible();
    }

    public boolean estaAbierta() {
        return fechaCierre == null;
    }

    @Override
    public String toString() {
        String estado;
        if (estaAbierta()) {
            estado = "abierta";
        } else {
            estado = "cerrada el " + fechaCierre;
        }
        return "Orden de " + vehiculo.getCodigo() + " - técnico " + tecnico.getNombre()
                + " - apertura " + fechaApertura + " - " + estado;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public List<String> getTareasRealizadas() {
        return tareasRealizadas;
    }

    public List<String> getRepuestosUtilizados() {
        return repuestosUtilizados;
    }
}