package modelo.vehiculos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.BicicletaComun;
import modelo.BicicletaElectrica;
import modelo.EstadoVehiculo;
import modelo.Tecnico;
import modelo.TipoVehiculo;
import modelo.cuentas.Usuario;
import modelo.excepciones.VehiculoNoDisponibleExcepcion;
import modelo.mantenimiento.OrdenMantenimiento;
import modelo.vehiculos.Monopatin;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class VehiculoPrueba {
    private static final LocalDate ALTA = LocalDate.of(2026, 1, 1);

    public static void ejecutar(Verificador v) {
        creacion(v);
        bateria(v);
        estados(v);
        fallas(v);
        reparacionYFallas(v);
        mantenimientoPendiente(v);
        orden(v);
    }

    private static void creacion(Verificador v) {
        BicicletaComun bici = new BicicletaComun("BC-1", ALTA, 26);
        v.verificarIgual("Vehículo: se crea disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        v.verificarNumero("Vehículo: se crea con 0 km", 0, bici.getKmRecorridos());
        v.verificarIgual("Vehículo: guarda su tipo", TipoVehiculo.BICICLETA_COMUN, bici.getTipo());
        v.verificarExcepcion("Vehículo: código vacío falla", IllegalArgumentException.class,
                () -> new BicicletaComun(" ", ALTA, 26));
        v.verificarExcepcion("Vehículo: sin fecha de alta falla", IllegalArgumentException.class,
                () -> new BicicletaComun("BC-2", null, 26));
        v.verificarExcepcion("Vehículo: rodado 0 falla", IllegalArgumentException.class,
                () -> new BicicletaComun("BC-2", ALTA, 0));
        v.verificarExcepcion("Vehículo: autonomía 0 falla", IllegalArgumentException.class,
                () -> new BicicletaElectrica("BE-1", ALTA, 80, 0));
        v.verificarExcepcion("Vehículo: velocidad 0 falla", IllegalArgumentException.class,
                () -> new Monopatin("MP-1", ALTA, 80, 0));

        bici.sumarKm(12.5);
        bici.sumarKm(7.5);
        v.verificarNumero("Vehículo: los km se acumulan", 20, bici.getKmRecorridos());
        v.verificarExcepcion("Vehículo: km negativos fallan", IllegalArgumentException.class, () -> bici.sumarKm(-1));
    }

    // RN08 y RN09
    private static void bateria(Verificador v) {
        v.verificarIgual("Batería: 80% queda disponible", EstadoVehiculo.DISPONIBLE,
                new BicicletaElectrica("BE-1", ALTA, 80, 40).getEstado());
        v.verificarIgual("Batería: 15% todavía es disponible", EstadoVehiculo.DISPONIBLE,
                new Monopatin("MP-1", ALTA, 15, 25).getEstado());
        Monopatin bajo = new Monopatin("MP-2", ALTA, 14, 25);
        v.verificarIgual("Batería: 14% nace en batería baja", EstadoVehiculo.BATERIA_BAJA, bajo.getEstado());
        v.verificarFalso("Batería: con 14% no se puede retirar", bajo.puedeRetirarse());
        v.verificarExcepcion("Batería: más de 100 falla", IllegalArgumentException.class,
                () -> new Monopatin("MP-3", ALTA, 101, 25));

        BicicletaElectrica bici = new BicicletaElectrica("BE-2", ALTA, 80, 40);
        bici.actualizarBateria(10);
        v.verificarIgual("Batería: bajar a 10% la pasa a batería baja", EstadoVehiculo.BATERIA_BAJA, bici.getEstado());
        bici.actualizarBateria(50);
        v.verificarIgual("Batería: subir a 50% la vuelve disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        bici.actualizarBateria(5);
        bici.recargarBateria();
        v.verificarNumero("Batería: recargar deja 100%", 100, bici.getNivelBateria());
        v.verificarIgual("Batería: recargar la vuelve disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());

        // vuelve de un viaje con poca batería: queda en batería baja
        BicicletaElectrica enViaje = new BicicletaElectrica("BE-3", ALTA, 80, 40);
        enViaje.marcarEnUso();
        enViaje.actualizarBateria(10);
        v.verificarIgual("Batería: en uso no cambia de estado por la batería", EstadoVehiculo.EN_USO, enViaje.getEstado());
        v.verificarExcepcion("Batería: no se recarga en uso", IllegalStateException.class, () -> enViaje.recargarBateria());
        enViaje.marcarDisponible();
        v.verificarIgual("Batería: al volver con 10% queda en batería baja", EstadoVehiculo.BATERIA_BAJA, enViaje.getEstado());
    }

    private static void estados(Verificador v) {
        BicicletaComun bici = new BicicletaComun("BC-1", ALTA, 26);
        v.verificarVerdadero("Estados: disponible se puede retirar", bici.puedeRetirarse());
        bici.marcarEnUso();
        v.verificarIgual("Estados: retirar la pasa a en uso", EstadoVehiculo.EN_USO, bici.getEstado());
        v.verificarExcepcion("Estados: en uso no se retira de nuevo", VehiculoNoDisponibleExcepcion.class, () -> bici.marcarEnUso());
        bici.marcarDisponible();
        bici.enviarAReparacion();
        v.verificarIgual("Estados: enviar a reparación", EstadoVehiculo.EN_REPARACION, bici.getEstado());
        v.verificarFalso("Estados: en reparación no se retira", bici.puedeRetirarse());
        bici.darDeBaja();
        v.verificarIgual("Estados: dar de baja", EstadoVehiculo.FUERA_DE_SERVICIO, bici.getEstado());
    }

    // RN10 + S3: 3 reportes en 30 días
    private static void fallas(Verificador v) {
        Usuario usuario = ViajesDePrueba.usuario();

        BicicletaComun bici = new BicicletaComun("BC-1", ALTA, 26);
        usuario.reportarFalla(bici, "Freno flojo", LocalDate.of(2026, 11, 1));
        usuario.reportarFalla(bici, "Cadena suelta", LocalDate.of(2026, 11, 10));
        v.verificarIgual("Fallas: con 2 reportes sigue disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        v.verificarNumero("Fallas: cuenta 2 recientes", 2, bici.contarFallasRecientes(LocalDate.of(2026, 11, 10)));
        usuario.reportarFalla(bici, "Rueda pinchada", LocalDate.of(2026, 11, 20));
        v.verificarIgual("Fallas: el tercer reporte la manda a reparación", EstadoVehiculo.EN_REPARACION, bici.getEstado());
        v.verificarVerdadero("Fallas: requiere mantenimiento obligatorio", bici.requiereMantenimientoObligatorio(LocalDate.of(2026, 11, 20)));

        // el reporte más viejo cae fuera de la ventana: 1/9 está a más de 30 días del 10/11
        BicicletaComun fuera = new BicicletaComun("BC-2", ALTA, 26);
        usuario.reportarFalla(fuera, "Vieja", LocalDate.of(2026, 9, 1));
        usuario.reportarFalla(fuera, "Media", LocalDate.of(2026, 10, 15));
        usuario.reportarFalla(fuera, "Nueva", LocalDate.of(2026, 11, 10));
        v.verificarIgual("Fallas: un reporte fuera de los 30 días no cuenta", EstadoVehiculo.DISPONIBLE, fuera.getEstado());
        v.verificarNumero("Fallas: el total histórico sigue siendo 3", 3, fuera.getCantidadFallas());

        // al 10/11 la ventana empieza el 11/10: ese día todavía cuenta, el 10/10 ya no
        BicicletaComun borde = new BicicletaComun("BC-3", ALTA, 26);
        usuario.reportarFalla(borde, "A", LocalDate.of(2026, 10, 11));
        usuario.reportarFalla(borde, "B", LocalDate.of(2026, 10, 20));
        usuario.reportarFalla(borde, "C", LocalDate.of(2026, 11, 10));
        v.verificarIgual("Fallas: a exactamente 30 días todavía cuenta", EstadoVehiculo.EN_REPARACION, borde.getEstado());
        BicicletaComun pasado = new BicicletaComun("BC-4", ALTA, 26);
        usuario.reportarFalla(pasado, "A", LocalDate.of(2026, 10, 10));
        usuario.reportarFalla(pasado, "B", LocalDate.of(2026, 10, 20));
        usuario.reportarFalla(pasado, "C", LocalDate.of(2026, 11, 10));
        v.verificarIgual("Fallas: a 31 días ya no cuenta", EstadoVehiculo.DISPONIBLE, pasado.getEstado());

        v.verificarExcepcion("Fallas: sin descripción falla", IllegalArgumentException.class,
                () -> usuario.reportarFalla(bici, " ", LocalDate.of(2026, 11, 21)));
    }

    // S20: después de una reparación, los reportes anteriores ya están atendidos
    private static void reparacionYFallas(Verificador v) {
        Usuario usuario = ViajesDePrueba.usuario();
        Tecnico tecnico = new Tecnico("T-1", "Tomás", "1111");
        BicicletaComun bici = new BicicletaComun("BC-1", ALTA, 26);
        usuario.reportarFalla(bici, "A", LocalDate.of(2026, 11, 1));
        usuario.reportarFalla(bici, "B", LocalDate.of(2026, 11, 2));
        usuario.reportarFalla(bici, "C", LocalDate.of(2026, 11, 3));
        OrdenMantenimiento orden = new OrdenMantenimiento(bici, tecnico, LocalDate.of(2026, 11, 3));
        orden.agregarTarea("Ajuste general");
        orden.cerrar(LocalDate.of(2026, 11, 4));

        v.verificarIgual("Reparada: vuelve a estar disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        v.verificarVerdadero("Reparada: guarda la fecha de la última reparación",
                LocalDate.of(2026, 11, 4).equals(bici.getFechaUltimaReparacion()));
        usuario.reportarFalla(bici, "D", LocalDate.of(2026, 11, 5));
        v.verificarNumero("Reparada: solo cuenta el reporte posterior a la reparación", 1,
                bici.contarFallasRecientes(LocalDate.of(2026, 11, 5)));
        v.verificarIgual("Reparada: un reporte nuevo no la manda otra vez a reparación", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        v.verificarNumero("Reparada: el historial total tiene 4", 4, bici.getCantidadFallas());
        v.verificarVerdadero("Reparada: sin órdenes nunca reparadas la fecha es null",
                new BicicletaComun("BC-9", ALTA, 26).getFechaUltimaReparacion() == null);
    }

    // S20: la falla llega durante un viaje
    private static void mantenimientoPendiente(Verificador v) {
        Usuario usuario = ViajesDePrueba.usuario();
        BicicletaComun bici = new BicicletaComun("BC-1", ALTA, 26);
        bici.marcarEnUso();
        usuario.reportarFalla(bici, "A", LocalDate.of(2026, 11, 1));
        usuario.reportarFalla(bici, "B", LocalDate.of(2026, 11, 1));
        usuario.reportarFalla(bici, "C", LocalDate.of(2026, 11, 1));
        v.verificarIgual("Pendiente: con 3 fallas en pleno viaje sigue en uso", EstadoVehiculo.EN_USO, bici.getEstado());
        v.verificarVerdadero("Pendiente: queda mantenimiento pendiente", bici.tieneMantenimientoPendiente());
        bici.marcarDisponible();
        v.verificarIgual("Pendiente: al devolverla pasa a reparación", EstadoVehiculo.EN_REPARACION, bici.getEstado());
        v.verificarFalso("Pendiente: ya no queda pendiente", bici.tieneMantenimientoPendiente());

        BicicletaElectrica electrica = new BicicletaElectrica("BE-1", ALTA, 80, 40);
        electrica.marcarEnUso();
        electrica.enviarAReparacion();
        electrica.actualizarBateria(5);
        electrica.marcarDisponible();
        v.verificarIgual("Pendiente: la reparación gana a la batería baja", EstadoVehiculo.EN_REPARACION, electrica.getEstado());
    }

    // orden natural por código y orden por cantidad de fallas (RF26)
    private static void orden(Verificador v) {
        Usuario usuario = ViajesDePrueba.usuario();
        BicicletaComun a = new BicicletaComun("A1", ALTA, 26);
        BicicletaComun b = new BicicletaComun("B1", ALTA, 26);
        BicicletaComun c = new BicicletaComun("C1", ALTA, 26);
        usuario.reportarFalla(a, "x", LocalDate.of(2026, 11, 1));
        usuario.reportarFalla(a, "y", LocalDate.of(2026, 11, 2));
        usuario.reportarFalla(c, "x", LocalDate.of(2026, 11, 1));
        usuario.reportarFalla(c, "y", LocalDate.of(2026, 11, 2));
        usuario.reportarFalla(b, "x", LocalDate.of(2026, 11, 1));

        List<Vehiculo> lista = new ArrayList<>(List.of(c, b, a));
        Collections.sort(lista);
        v.verificarIgual("Orden: el orden natural es por código", "A1,B1,C1",
                lista.get(0).getCodigo() + "," + lista.get(1).getCodigo() + "," + lista.get(2).getCodigo());
        Collections.sort(lista, new ComparadorVehiculosPorFallas());
        v.verificarIgual("Orden: por fallas de mayor a menor, y por código si empatan", "A1,C1,B1",
                lista.get(0).getCodigo() + "," + lista.get(1).getCodigo() + "," + lista.get(2).getCodigo());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}