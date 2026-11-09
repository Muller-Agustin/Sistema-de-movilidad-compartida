package modelo.mantenimiento;

import modelo.excepciones.OrdenIncompletaExcepcion;
import modelo.personal.Tecnico;
import modelo.vehiculos.Vehiculo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdenMantenimiento {
    private Vehiculo vehiculo;
    private Tecnico tecnico;
    private LocalDate fechaApertura;
    private LocalDate fechaCierre;
    private List<String> tareasRealizadas;
    private List<String> repuestosUtilizados;

    public OrdenMantenimiento(Vehiculo vehiculo, Tecnico tecnico, LocalDate fechaApertura) {
        if (vehiculo == null || tecnico == null || fechaApertura == null) {
            throw new IllegalArgumentException("El vehículo, el técnico y la fecha de apertura son obligatorios");
        }
        this.vehiculo = vehiculo;
        this.tecnico = tecnico;
        this.fechaApertura = fechaApertura;
        this.tareasRealizadas = new ArrayList<>();
        this.repuestosUtilizados = new ArrayList<>();
        // Se registra en los dos extremos para que la relación quede consistente.
        vehiculo.agregarOrden(this);
        tecnico.asignarOrden(this);
    }

    public void agregarTarea(String tarea) {
        if (!estaAbierta()) {
            throw new IllegalStateException("La orden ya está cerrada");
        }
        if (tarea == null || tarea.isBlank()) {
            throw new IllegalArgumentException("La tarea no puede estar vacía");
        }
        tareasRealizadas.add(tarea);
    }

    public void agregarRepuesto(String repuesto) {
        if (!estaAbierta()) {
            throw new IllegalStateException("La orden ya está cerrada");
        }
        if (repuesto == null || repuesto.isBlank()) {
            throw new IllegalArgumentException("El repuesto no puede estar vacío");
        }
        repuestosUtilizados.add(repuesto);
    }

    // RN11: no se cierra sin técnico, fecha de cierre y tareas realizadas.
    public void cerrar(LocalDate fecha) {
        if (!estaAbierta()) {
            throw new OrdenIncompletaExcepcion("La orden ya está cerrada");
        }
        if (tecnico == null) {
            throw new OrdenIncompletaExcepcion("La orden no tiene técnico asignado");
        }
        if (fecha == null) {
            throw new OrdenIncompletaExcepcion("Falta la fecha de cierre");
        }
        if (fecha.isBefore(fechaApertura)) {
            throw new OrdenIncompletaExcepcion("La fecha de cierre es anterior a la de apertura");
        }
        if (tareasRealizadas.isEmpty()) {
            throw new OrdenIncompletaExcepcion("No se registraron tareas realizadas");
        }
        this.fechaCierre = fecha;
        // El vehículo reparado vuelve a estar disponible (o en batería baja si es eléctrico).
        vehiculo.marcarDisponible();
    }

    public boolean estaAbierta() {
        return fechaCierre == null;
    }

    @Override
    public String toString() {
        return "Orden de " + vehiculo.getCodigo() + " - técnico " + tecnico.getNombre()
                + " - apertura " + fechaApertura
                + (estaAbierta() ? " - abierta" : " - cerrada el " + fechaCierre);
    }

    public Vehiculo getVehiculo() { return vehiculo; }
    public Tecnico getTecnico() { return tecnico; }
    public LocalDate getFechaApertura() { return fechaApertura; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public List<String> getTareasRealizadas() { return new ArrayList<>(tareasRealizadas); }
    public List<String> getRepuestosUtilizados() { return new ArrayList<>(repuestosUtilizados); }
}
