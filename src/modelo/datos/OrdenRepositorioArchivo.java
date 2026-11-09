package modelo.datos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.mantenimiento.OrdenMantenimiento;

// Guarda las órdenes de mantenimiento en dos archivos:
//   ordenes.txt -> codigoVehiculo;legajoTecnico;fechaApertura;fechaCierre
//   tareas.txt  -> numeroDeOrden;tipo;texto      (tipo T = tarea, R = repuesto; la orden se cuenta desde 0)
// Necesita que ya estén cargados el personal y los vehículos.
public class OrdenRepositorioArchivo extends RepositorioArchivo<OrdenMantenimiento> {
    private static final String ORDENES = "ordenes.txt";
    private static final String TAREAS = "tareas.txt";

    private final DatosDelSistema datos;

    public OrdenRepositorioArchivo(String carpeta, DatosDelSistema datos) {
        super(carpeta);
        this.datos = datos;
    }

    @Override
    public List<OrdenMantenimiento> leerTodos() {
        Map<Integer, List<String[]>> tareasPorOrden = new HashMap<>();
        recorrer(TAREAS, c -> tareasPorOrden.computeIfAbsent(Integer.parseInt(c[0]), k -> new ArrayList<>()).add(c));

        List<OrdenMantenimiento> ordenes = new ArrayList<>();
        recorrer(ORDENES, c -> ordenes.add(crearOrden(c, tareasPorOrden.get(ordenes.size()))));
        return ordenes;
    }

    private OrdenMantenimiento crearOrden(String[] c, List<String[]> tareas) {
        OrdenMantenimiento orden = new OrdenMantenimiento(datos.obtenerVehiculo(c[0]),
                datos.obtenerTecnico(c[1]), LocalDate.parse(c[2]));
        if (tareas != null) {
            for (String[] fila : tareas) {
                if (fila[1].equals("T")) {
                    orden.agregarTarea(fila[2]);
                } else {
                    orden.agregarRepuesto(fila[2]);
                }
            }
        }
        if (!esVacio(c[3])) {
            orden.restaurarCierre(LocalDate.parse(c[3]));
        }
        return orden;
    }

    @Override
    public void guardarTodos(List<OrdenMantenimiento> ordenes) {
        List<String> lineasOrdenes = new ArrayList<>();
        List<String> lineasTareas = new ArrayList<>();
        for (int i = 0; i < ordenes.size(); i++) {
            OrdenMantenimiento o = ordenes.get(i);
            lineasOrdenes.add(unir(o.getVehiculo().getCodigo(), o.getTecnico().getLegajo(),
                    o.getFechaApertura(), o.getFechaCierre()));
            for (String tarea : o.getTareasRealizadas()) {
                lineasTareas.add(unir(i, "T", tarea));
            }
            for (String repuesto : o.getRepuestosUtilizados()) {
                lineasTareas.add(unir(i, "R", repuesto));
            }
        }
        escribir(ORDENES, lineasOrdenes);
        escribir(TAREAS, lineasTareas);
    }
}