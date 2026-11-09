package modelo.mantenimiento;

import java.time.LocalDate;
import modelo.excepciones.OrdenIncompletaExcepcion;
import modelo.BicicletaComun;
import modelo.BicicletaElectrica;
import modelo.EstadoVehiculo;
import modelo.Operador;
import modelo.Tecnico;
import pruebas.Verificador;

public class OrdenMantenimientoPrueba {
    private static final LocalDate ALTA = LocalDate.of(2026, 1, 1);
    private static final LocalDate APERTURA = LocalDate.of(2026, 11, 9);

    public static void ejecutar(Verificador v) {
        creacion(v);
        cierre(v);
        cierreRechazado(v);
        tecnico(v);
        operador(v);
    }

    private static BicicletaComun enReparacion() {
        BicicletaComun bici = new BicicletaComun("BC-1", ALTA, 26);
        bici.enviarAReparacion();
        return bici;
    }

    private static void creacion(Verificador v) {
        Tecnico tecnico = new Tecnico("T-1", "Tomás", "1111");
        BicicletaComun bici = enReparacion();
        OrdenMantenimiento orden = new OrdenMantenimiento(bici, tecnico, APERTURA);

        v.verificarVerdadero("Orden: nace abierta", orden.estaAbierta());
        v.verificarNumero("Orden: el vehículo la conoce", 1, bici.getOrdenesMantenimiento().size());
        v.verificarNumero("Orden: el técnico tiene 1 abierta", 1, tecnico.contarOrdenesAbiertas());
        v.verificarVerdadero("Orden: el vehículo tiene orden abierta", bici.tieneOrdenAbierta());
        v.verificarExcepcion("Orden: sin técnico falla", IllegalArgumentException.class,
                () -> new OrdenMantenimiento(new BicicletaComun("BC-2", ALTA, 26), null, APERTURA));
        v.verificarExcepcion("Orden: sin vehículo falla", IllegalArgumentException.class,
                () -> new OrdenMantenimiento(null, tecnico, APERTURA));
        v.verificarExcepcion("Orden: dos órdenes abiertas del mismo vehículo fallan", IllegalStateException.class,
                () -> new OrdenMantenimiento(bici, tecnico, APERTURA));
        v.verificarNumero("Orden: la orden rechazada no se asigna al técnico", 1, tecnico.getOrdenes().size());
        v.verificarExcepcion("Orden: tarea vacía falla", IllegalArgumentException.class, () -> orden.agregarTarea(" "));
        v.verificarExcepcion("Orden: repuesto vacío falla", IllegalArgumentException.class, () -> orden.agregarRepuesto(""));
    }

    // RF20
    private static void cierre(Verificador v) {
        Tecnico tecnico = new Tecnico("T-1", "Tomás", "1111");
        BicicletaComun bici = enReparacion();
        OrdenMantenimiento orden = new OrdenMantenimiento(bici, tecnico, APERTURA);
        orden.agregarTarea("Cambio de cámara");
        orden.agregarTarea("Ajuste de frenos");
        orden.agregarRepuesto("Cámara 26");
        tecnico.cerrarOrden(orden, LocalDate.of(2026, 11, 10));

        v.verificarFalso("Cierre: la orden queda cerrada", orden.estaAbierta());
        v.verificarVerdadero("Cierre: guarda la fecha", LocalDate.of(2026, 11, 10).equals(orden.getFechaCierre()));
        v.verificarNumero("Cierre: guarda 2 tareas", 2, orden.getTareasRealizadas().size());
        v.verificarNumero("Cierre: guarda 1 repuesto", 1, orden.getRepuestosUtilizados().size());
        v.verificarIgual("Cierre: el vehículo vuelve a estar disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        v.verificarNumero("Cierre: el técnico ya no tiene abiertas", 0, tecnico.contarOrdenesAbiertas());
        v.verificarNumero("Cierre: el técnico tiene 1 cerrada", 1, tecnico.getOrdenesCerradas().size());
        v.verificarFalso("Cierre: el vehículo ya no tiene orden abierta", bici.tieneOrdenAbierta());
        v.verificarExcepcion("Cierre: no se cierra dos veces", OrdenIncompletaExcepcion.class,
                () -> orden.cerrar(LocalDate.of(2026, 11, 11)));
        v.verificarExcepcion("Cierre: no se agregan tareas a una orden cerrada", IllegalStateException.class,
                () -> orden.agregarTarea("Otra"));

        // una eléctrica con poca batería no vuelve disponible: queda en batería baja (RN08)
        BicicletaElectrica electrica = new BicicletaElectrica("BE-1", ALTA, 10, 40);
        electrica.enviarAReparacion();
        OrdenMantenimiento ordenElectrica = new OrdenMantenimiento(electrica, tecnico, APERTURA);
        ordenElectrica.agregarTarea("Revisión del motor");
        ordenElectrica.cerrar(APERTURA);
        v.verificarIgual("Cierre: la eléctrica con 10% queda en batería baja", EstadoVehiculo.BATERIA_BAJA, electrica.getEstado());

        // una vez cerrada se puede abrir otra orden para el mismo vehículo
        bici.enviarAReparacion();
        OrdenMantenimiento segunda = new OrdenMantenimiento(bici, tecnico, LocalDate.of(2026, 11, 20));
        v.verificarNumero("Cierre: el vehículo acumula 2 órdenes", 2, bici.getOrdenesMantenimiento().size());
        v.verificarVerdadero("Cierre: la segunda orden está abierta", segunda.estaAbierta());
    }

    // RN11
    private static void cierreRechazado(Verificador v) {
        Tecnico tecnico = new Tecnico("T-1", "Tomás", "1111");

        OrdenMantenimiento sinTareas = new OrdenMantenimiento(enReparacion(), tecnico, APERTURA);
        v.verificarExcepcion("RN11: sin tareas no se cierra", OrdenIncompletaExcepcion.class,
                () -> sinTareas.cerrar(APERTURA));
        v.verificarVerdadero("RN11: si falla la orden sigue abierta", sinTareas.estaAbierta());

        OrdenMantenimiento sinFecha = new OrdenMantenimiento(enReparacion(), tecnico, APERTURA);
        sinFecha.agregarTarea("Algo");
        v.verificarExcepcion("RN11: sin fecha no se cierra", OrdenIncompletaExcepcion.class, () -> sinFecha.cerrar(null));
        v.verificarExcepcion("RN11: con fecha anterior a la apertura no se cierra", OrdenIncompletaExcepcion.class,
                () -> sinFecha.cerrar(APERTURA.minusDays(1)));

        // S20: el vehículo todavía no llegó a reparación (sigue en un viaje)
        BicicletaComun enViaje = new BicicletaComun("BC-5", ALTA, 26);
        enViaje.marcarEnUso();
        enViaje.enviarAReparacion();
        OrdenMantenimiento pendiente = new OrdenMantenimiento(enViaje, tecnico, APERTURA);
        pendiente.agregarTarea("Algo");
        v.verificarIgual("S20: el vehículo en viaje sigue en uso", EstadoVehiculo.EN_USO, enViaje.getEstado());
        v.verificarExcepcion("S20: no se cierra mientras el vehículo no esté en reparación", IllegalStateException.class,
                () -> pendiente.cerrar(APERTURA));
        enViaje.marcarDisponible();
        pendiente.cerrar(APERTURA);
        v.verificarIgual("S20: ya devuelto y reparado queda disponible", EstadoVehiculo.DISPONIBLE, enViaje.getEstado());
    }

    // RF20, RF31 y RF32: el técnico solo cierra sus órdenes
    private static void tecnico(Verificador v) {
        Tecnico ana = new Tecnico("T-1", "Ana", "1111");
        Tecnico beto = new Tecnico("T-2", "Beto", "2222");
        OrdenMantenimiento deAna = new OrdenMantenimiento(enReparacion(), ana, APERTURA);
        deAna.agregarTarea("Algo");

        v.verificarExcepcion("Técnico: no cierra la orden de otro", IllegalArgumentException.class,
                () -> beto.cerrarOrden(deAna, APERTURA));
        v.verificarNumero("Técnico: Ana tiene 1 orden abierta", 1, ana.getOrdenesAbiertas().size());
        v.verificarNumero("Técnico: Beto tiene 0 órdenes abiertas", 0, beto.getOrdenesAbiertas().size());
        ana.cerrarOrden(deAna, APERTURA);
        v.verificarNumero("Técnico: Ana tiene 1 orden cerrada", 1, ana.getOrdenesCerradas().size());
        v.verificarNumero("Técnico: Ana ya no tiene abiertas", 0, ana.getOrdenesAbiertas().size());
    }

    // RF19: el operador genera la orden y el vehículo pasa a reparación
    private static void operador(Verificador v) {
        Operador operador = new Operador("L-1", "Luis", "3333");
        Tecnico tecnico = new Tecnico("T-1", "Tomás", "1111");
        BicicletaComun bici = new BicicletaComun("BC-1", ALTA, 26);
        OrdenMantenimiento orden = operador.generarOrden(bici, tecnico, APERTURA);

        v.verificarIgual("Operador: la orden manda el vehículo a reparación", EstadoVehiculo.EN_REPARACION, bici.getEstado());
        v.verificarVerdadero("Operador: la orden queda asignada al técnico", orden.getTecnico() == tecnico);
        v.verificarExcepcion("Operador: no genera una segunda orden abierta", IllegalStateException.class,
                () -> operador.generarOrden(bici, tecnico, APERTURA));
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}