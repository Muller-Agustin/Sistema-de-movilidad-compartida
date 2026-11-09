package PersonalServicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import EstacionesTraslados.Estacion;
import EstacionesTraslados.Traslado;
import VehiculoMantenimiento.OrdenMantenimiento;
import VehiculoMantenimiento.Reportante;
import VehiculoMantenimiento.ReporteFalla;
import VehiculoMantenimiento.Vehiculo;

// Operador del servicio: reporta fallas, genera órdenes de mantenimiento (RF19)
// y redistribuye vehículos entre estaciones (RF23).

public class Operador extends Personal implements Reportante {
    private static final long serialVersionUID = 1L;

    public Operador(String legajo, String nombre, String telefono) {
        super(legajo, nombre, telefono);
    }

    // RF18: el operador también puede reportar fallas.
    @Override
    public ReporteFalla reportarFalla(Vehiculo vehiculo, String descripcion, LocalDate fecha) {
        ReporteFalla reporte = new ReporteFalla(vehiculo, this, fecha, descripcion);
        vehiculo.agregarReporte(reporte);
        return reporte;
    }

    @Override
    public String getIdentificacion() {
        return "Operador " + getNombre() + " (legajo " + getLegajo() + ")";
    }

    // RF19: genera la orden, la asigna al técnico y manda el vehículo a reparación.
    public OrdenMantenimiento generarOrden(Vehiculo vehiculo, Tecnico tecnico, LocalDate fecha) {
        OrdenMantenimiento orden = new OrdenMantenimiento(vehiculo, tecnico, fecha);
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