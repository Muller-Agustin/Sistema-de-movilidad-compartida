package modelo.servicios;

import java.time.LocalDate;
import modelo.BicicletaComun;
import modelo.BicicletaElectrica;
import modelo.EstadoVehiculo;
import modelo.Tecnico;
import modelo.estaciones.Estacion;
import modelo.mantenimiento.OrdenMantenimiento;
import modelo.vehiculos.Monopatin;
import modelo.vehiculos.Vehiculo;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class ServicioVehiculosPrueba {

    private static class Escenario {
        final RelojSimulado reloj = new RelojSimulado(ViajesDePrueba.INICIO);
        final ServicioVehiculos servicio = new ServicioVehiculos(reloj);
        final Estacion estacion = ViajesDePrueba.estacion("E1", 10);
    }

    public static void ejecutar(Verificador v) {
        altas(v);
        altasRechazadas(v);
        consultas(v);
        cambiosDeEstado(v);
        bateria(v);
    }

    // RF11
    private static void altas(Verificador v) {
        Escenario e = new Escenario();
        BicicletaComun comun = e.servicio.registrarBicicletaComun("BC-1", 26, e.estacion);
        BicicletaElectrica electrica = e.servicio.registrarBicicletaElectrica("BE-1", 80, 40, e.estacion);
        Monopatin monopatin = e.servicio.registrarMonopatin("MP-1", 90, 25, e.estacion);

        v.verificarIgual("Alta: se crea disponible", EstadoVehiculo.DISPONIBLE, comun.getEstado());
        v.verificarNumero("Alta: se crea con 0 km", 0, comun.getKmRecorridos());
        v.verificarVerdadero("Alta: la fecha de alta es la del sistema",
                LocalDate.of(2026, 11, 9).equals(comun.getFechaAlta()));
        v.verificarNumero("Alta: guarda el rodado", 26, comun.getRodado());
        v.verificarNumero("Alta: guarda la autonomía", 40, electrica.getAutonomiaKm());
        v.verificarNumero("Alta: guarda la velocidad máxima", 25, monopatin.getVelocidadMaxima());
        v.verificarNumero("Alta: guarda la batería", 90, monopatin.getNivelBateria());
        v.verificarNumero("Alta: la estación recibe los 3", 3, e.estacion.getVehiculos().size());
        v.verificarVerdadero("Alta: el vehículo queda en la estación", comun.getEstacion() == e.estacion);
        v.verificarNumero("Alta: el servicio registra los 3", 3, e.servicio.getVehiculos().size());

        BicicletaElectrica baja = e.servicio.registrarBicicletaElectrica("BE-2", 10, 40, e.estacion);
        v.verificarIgual("Alta: una eléctrica con 10% nace en batería baja", EstadoVehiculo.BATERIA_BAJA, baja.getEstado());
    }

    private static void altasRechazadas(Verificador v) {
        Escenario e = new Escenario();
        e.servicio.registrarBicicletaComun("BC-1", 26, e.estacion);
        v.verificarExcepcion("Alta: código repetido falla", IllegalArgumentException.class,
                () -> e.servicio.registrarMonopatin("BC-1", 90, 25, e.estacion));
        v.verificarNumero("Alta: el repetido no entra a la estación", 1, e.estacion.getVehiculos().size());
        v.verificarNumero("Alta: el repetido no entra al servicio", 1, e.servicio.getVehiculos().size());
        v.verificarExcepcion("Alta: sin estación falla", IllegalArgumentException.class,
                () -> e.servicio.registrarBicicletaComun("BC-2", 26, null));
        v.verificarExcepcion("Alta: rodado inválido falla", IllegalArgumentException.class,
                () -> e.servicio.registrarBicicletaComun("BC-3", 0, e.estacion));

        Estacion chica = ViajesDePrueba.estacion("CHICA", 1);
        e.servicio.registrarBicicletaComun("BC-4", 26, chica);
        v.verificarExcepcion("Alta: en una estación llena falla", IllegalStateException.class,
                () -> e.servicio.registrarBicicletaComun("BC-5", 26, chica));
        v.verificarFalso("Alta: si falla no queda registrado", e.servicio.existe("BC-5"));
    }

    // RF12: consultar
    private static void consultas(Verificador v) {
        Escenario e = new Escenario();
        e.servicio.registrarBicicletaComun("C", 26, e.estacion);
        e.servicio.registrarBicicletaComun("A", 26, e.estacion);
        Vehiculo b = e.servicio.registrarBicicletaComun("B", 26, e.estacion);
        b.enviarAReparacion();

        v.verificarIgual("Consulta: ordenados por código", "A,B,C",
                e.servicio.getVehiculos().get(0).getCodigo() + "," + e.servicio.getVehiculos().get(1).getCodigo()
                + "," + e.servicio.getVehiculos().get(2).getCodigo());
        v.verificarNumero("Consulta: 2 disponibles", 2, e.servicio.getVehiculosPorEstado(EstadoVehiculo.DISPONIBLE).size());
        v.verificarNumero("Consulta: 1 en reparación", 1, e.servicio.getVehiculosPorEstado(EstadoVehiculo.EN_REPARACION).size());
        v.verificarNumero("Consulta: 0 en uso", 0, e.servicio.getVehiculosPorEstado(EstadoVehiculo.EN_USO).size());
        v.verificarVerdadero("Consulta: obtener por código", e.servicio.obtener("B") == b);
        v.verificarExcepcion("Consulta: código inexistente falla", IllegalArgumentException.class,
                () -> e.servicio.obtener("ZZ"));
    }

    // RF12: cambiar
    private static void cambiosDeEstado(Verificador v) {
        Escenario e = new Escenario();
        Vehiculo bici = e.servicio.registrarBicicletaComun("BC-1", 26, e.estacion);

        e.servicio.cambiarEstado(bici, EstadoVehiculo.EN_REPARACION);
        v.verificarIgual("Estado: a reparación", EstadoVehiculo.EN_REPARACION, bici.getEstado());
        e.servicio.cambiarEstado(bici, EstadoVehiculo.DISPONIBLE);
        v.verificarIgual("Estado: de vuelta a disponible", EstadoVehiculo.DISPONIBLE, bici.getEstado());
        e.servicio.cambiarEstado(bici, EstadoVehiculo.FUERA_DE_SERVICIO);
        v.verificarIgual("Estado: fuera de servicio", EstadoVehiculo.FUERA_DE_SERVICIO, bici.getEstado());

        v.verificarExcepcion("Estado: 'en uso' no se asigna a mano", IllegalArgumentException.class,
                () -> e.servicio.cambiarEstado(bici, EstadoVehiculo.EN_USO));
        v.verificarExcepcion("Estado: 'batería baja' no se asigna a mano", IllegalArgumentException.class,
                () -> e.servicio.cambiarEstado(bici, EstadoVehiculo.BATERIA_BAJA));

        // con una orden abierta no se puede marcar disponible
        Vehiculo otra = e.servicio.registrarBicicletaComun("BC-2", 26, e.estacion);
        otra.enviarAReparacion();
        new OrdenMantenimiento(otra, new Tecnico("T-1", "Tomás", "1111"), LocalDate.of(2026, 11, 9));
        v.verificarExcepcion("Estado: con orden abierta no se marca disponible", IllegalStateException.class,
                () -> e.servicio.cambiarEstado(otra, EstadoVehiculo.DISPONIBLE));

        // en un viaje no se toca
        Vehiculo enViaje = e.servicio.registrarBicicletaComun("BC-3", 26, e.estacion);
        enViaje.marcarEnUso();
        v.verificarExcepcion("Estado: en uso no se cambia", IllegalStateException.class,
                () -> e.servicio.cambiarEstado(enViaje, EstadoVehiculo.EN_REPARACION));

        // RN12: sin estación no puede quedar disponible
        Vehiculo suelta = e.servicio.registrarBicicletaComun("BC-4", 26, e.estacion);
        e.estacion.liberarVehiculo(suelta);
        suelta.darDeBaja();
        v.verificarExcepcion("Estado: sin estación no se marca disponible", IllegalStateException.class,
                () -> e.servicio.cambiarEstado(suelta, EstadoVehiculo.DISPONIBLE));

        // una eléctrica con poca batería marcada como disponible queda en batería baja
        Vehiculo electrica = e.servicio.registrarMonopatin("MP-1", 90, 25, e.estacion);
        e.servicio.actualizarBateria(electrica, 10);
        e.servicio.cambiarEstado(electrica, EstadoVehiculo.EN_REPARACION);
        e.servicio.cambiarEstado(electrica, EstadoVehiculo.DISPONIBLE);
        v.verificarIgual("Estado: con 10% de batería queda en batería baja", EstadoVehiculo.BATERIA_BAJA, electrica.getEstado());
    }

    // RF21
    private static void bateria(Verificador v) {
        Escenario e = new Escenario();
        Vehiculo monopatin = e.servicio.registrarMonopatin("MP-1", 10, 25, e.estacion);
        v.verificarFalso("Recarga: con 10% no se ofrece para retiro", monopatin.puedeRetirarse());
        e.servicio.registrarRecargaBateria(monopatin);
        v.verificarIgual("Recarga: queda disponible", EstadoVehiculo.DISPONIBLE, monopatin.getEstado());
        v.verificarNumero("Recarga: queda al 100%", 100, ((Monopatin) monopatin).getNivelBateria());

        Vehiculo comun = e.servicio.registrarBicicletaComun("BC-1", 26, e.estacion);
        v.verificarExcepcion("Recarga: la bicicleta común no tiene batería (RN09)", IllegalArgumentException.class,
                () -> e.servicio.registrarRecargaBateria(comun));
        v.verificarExcepcion("Batería: no se actualiza en una bicicleta común", IllegalArgumentException.class,
                () -> e.servicio.actualizarBateria(comun, 50));

        e.servicio.actualizarBateria(monopatin, 14);
        v.verificarIgual("Batería: 14% pasa a batería baja", EstadoVehiculo.BATERIA_BAJA, monopatin.getEstado());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}