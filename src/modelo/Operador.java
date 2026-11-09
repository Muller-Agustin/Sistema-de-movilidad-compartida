package modelo;

import modelo.estaciones.Estacion;
import modelo.estaciones.Traslado;
import modelo.mantenimiento.OrdenMantenimiento;
import modelo.Tecnico;
import modelo.vehiculos.Vehiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Operador extends Personal implements Reportante {
    public Operador(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
    }

    // RF18: el operador también puede reportar fallas.
    @Override
    public ReporteFalla reportarFalla(Vehiculo vehiculo, String descripcion, LocalDate fecha) {
        ReporteFalla reporte = new ReporteFalla(vehiculo, this, fecha, descripcion);
        try {
            vehiculo.getClass().getMethod("agregarReporte", reporte.getClass()).invoke(vehiculo, reporte);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo agregar el reporte de falla", e);
        }
        return reporte;
    }

    @Override
    public String getIdentificacion() {
        return "Operador " + getNombre() + " (legajo " + getLegajo() + ")";
    }

    // RF19: genera la orden, la asigna al técnico y manda el vehículo a reparación.
    public OrdenMantenimiento generarOrden(Vehiculo vehiculo, Tecnico tecnico, LocalDate fecha) {
        OrdenMantenimiento orden;
        try {
            orden = OrdenMantenimiento.class
                    .getConstructor(Vehiculo.class, Tecnico.class, LocalDate.class)
                    .newInstance(vehiculo, tecnico, fecha);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo generar la orden de mantenimiento", e);
        }
        vehiculo.enviarAReparacion();
        return orden;
    }

    // RF23: mueve vehículos de una estación a otra. Primero se controla todo, y recién
    // después se mueve, así un traslado inválido no deja vehículos a medio camino.
    public Traslado redistribuir(Estacion origen, Estacion destino, List<Vehiculo> vehiculos, LocalDateTime fecha) {
        Traslado traslado = new Traslado(origen, destino, vehiculos, fecha, this);
        for (Vehiculo v : vehiculos) {
            if (!origen.getVehiculos().contains(v)) {
                throw new IllegalArgumentException("El vehículo " + v.getCodigo() + " no está en la estación de origen");
            }
        }
        int lugaresLibres = destino.getCapacidadMaxima() - destino.getVehiculos().size();
        if (vehiculos.size() > lugaresLibres) {
            throw new IllegalArgumentException("La estación de destino solo tiene " + lugaresLibres + " lugares libres");
        }
        for (Vehiculo v : vehiculos) {
            origen.liberarVehiculo(v, fecha);
            destino.recibirVehiculo(v, fecha);
        }
        return traslado;
    }
}