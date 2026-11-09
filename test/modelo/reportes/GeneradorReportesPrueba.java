package modelo.reportes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.BicicletaComun;
import modelo.BicicletaElectrica;
import modelo.Operador;
import modelo.Tecnico;
import modelo.cuentas.Tarjeta;
import modelo.cuentas.Usuario;
import modelo.estaciones.ComparadorEstacionesPorDistancia;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;
import modelo.mantenimiento.OrdenMantenimiento;
import modelo.servicios.CalculadoraDeTarifa;
import modelo.servicios.RelojSimulado;
import modelo.servicios.ServicioMantenimiento;
import modelo.servicios.ServicioVehiculos;
import modelo.servicios.ServicioViajes;
import modelo.tarifas.CatalogoTarifas;
import modelo.vehiculos.Monopatin;
import modelo.vehiculos.Vehiculo;
import modelo.viajes.Viaje;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class GeneradorReportesPrueba {

    // Un sistema chico con historia, armado con los servicios:
    //
    //   Estaciones: A (0,0) cap. 10 | B (0,1) cap. 10 | C (0,2) cap. 4        (1 grado de longitud = 111,19 km)
    //   Viajes (bicicleta común: 200 + 20/min, monopatín: 300 + 50/min):
    //     1. Ana  BC-1 A->B 20 min = 600     2. Ana  MP-1 B->A 10 min = 800     3. Beto BC-1 B->C 5 min = 300
    //     4. Beto BE-1 sale de B y cancela al minuto = 0                        5. Beto BC-2 A->A 3 min = 260
    //   Fallas: BC-1 dos veces (Ana) y MP-1 una vez (Beto)
    //   Órdenes: MP-1 con Tomás (9/11 al 11/11, cerrada) | BC-1 con Tania (11/11 al 12/11, cerrada) | BE-1 con Tania (12/11, abierta)
    private static class Escenario {
        final Estacion a = new Estacion("A", "Calle A 100", 10, new Coordenada(0, 0));
        final Estacion b = new Estacion("B", "Calle B 200", 10, new Coordenada(0, 1));
        final Estacion c = new Estacion("C", "Calle C 300", 4, new Coordenada(0, 2));
        final List<Estacion> estaciones = new ArrayList<>();
        final Usuario ana = new Usuario("Ana", "12345678", "ana@mail.com", LocalDate.of(2026, 11, 1));
        final Usuario beto = new Usuario("Beto", "87654321", "beto@mail.com", LocalDate.of(2026, 11, 1));
        final Usuario cami = new Usuario("Cami", "11111111", "cami@mail.com", LocalDate.of(2026, 11, 1));
        final List<Usuario> usuarios = new ArrayList<>();
        final Operador operador = new Operador("L-1", "Luis", "1133334444");
        final Tecnico tomas = new Tecnico("T-1", "Tomás", "1155556666");
        final Tecnico tania = new Tecnico("T-2", "Tania", "1177778888");
        final RelojSimulado reloj = new RelojSimulado(ViajesDePrueba.INICIO);
        final ServicioVehiculos servicioVehiculos = new ServicioVehiculos(reloj);
        final ServicioViajes servicioViajes;
        final ServicioMantenimiento mantenimiento = new ServicioMantenimiento(reloj);
        final BicicletaComun bc1;
        final BicicletaComun bc2;
        final BicicletaElectrica be1;
        final Monopatin mp1;
        final OrdenMantenimiento ordenMp1;
        final OrdenMantenimiento ordenBc1;
        final OrdenMantenimiento ordenBe1;

        Escenario() {
            estaciones.add(a);
            estaciones.add(b);
            estaciones.add(c);
            usuarios.add(ana);
            usuarios.add(beto);
            usuarios.add(cami);
            mantenimiento.agregarTecnico(tomas);
            mantenimiento.agregarTecnico(tania);
            servicioViajes = new ServicioViajes(new CalculadoraDeTarifa(new ArrayList<>()),
                    CatalogoTarifas.conValoresDeReferencia(LocalDate.of(2026, 1, 1)), reloj);

            bc1 = servicioVehiculos.registrarBicicletaComun("BC-1", 26, a);
            bc2 = servicioVehiculos.registrarBicicletaComun("BC-2", 26, a);
            be1 = servicioVehiculos.registrarBicicletaElectrica("BE-1", 80, 40, b);
            mp1 = servicioVehiculos.registrarMonopatin("MP-1", 90, 25, b);

            for (Usuario u : List.of(ana, beto)) {
                Tarjeta tarjeta = new Tarjeta(u.getNombre(), "1234");
                u.getCuenta().agregarMedioPago(tarjeta);
                u.recargarSaldo(10000, tarjeta, reloj.ahora());
            }

            Viaje v1 = servicioViajes.retirar(ana, bc1, a);
            reloj.avanzarMinutos(20);
            servicioViajes.devolver(v1, b);
            Viaje v2 = servicioViajes.retirar(ana, mp1, b);
            reloj.avanzarMinutos(10);
            servicioViajes.devolver(v2, a);
            Viaje v3 = servicioViajes.retirar(beto, bc1, b);
            reloj.avanzarMinutos(5);
            servicioViajes.devolver(v3, c);
            Viaje v4 = servicioViajes.retirar(beto, be1, b);
            reloj.avanzarMinutos(1);
            servicioViajes.cancelar(v4);
            Viaje v5 = servicioViajes.retirar(beto, bc2, a);
            reloj.avanzarMinutos(3);
            servicioViajes.devolver(v5, a);

            mantenimiento.reportarFalla(ana, bc1, "Freno");
            mantenimiento.reportarFalla(ana, bc1, "Luz");
            mantenimiento.reportarFalla(beto, mp1, "Rueda floja");

            ordenMp1 = mantenimiento.generarOrden(operador, mp1, tomas);
            reloj.avanzarHoras(48);
            mantenimiento.cerrarOrden(tomas, ordenMp1, List.of("Cambio de rueda"), List.of("Rueda trasera"));
            ordenBc1 = mantenimiento.generarOrden(operador, bc1, tania);
            reloj.avanzarHoras(24);
            mantenimiento.cerrarOrden(tania, ordenBc1, List.of("Ajuste de frenos", "Cambio de cadena"), List.of("Cadena"));
            ordenBe1 = mantenimiento.generarOrden(operador, be1, tania);
        }
    }

    public static void ejecutar(Verificador v) {
        Escenario e = new Escenario();
        viajesPorUsuario(v, e);
        historialDeViajes(v, e);
        vehiculosConMasFallas(v, e);
        usoPorEstacion(v, e);
        reparaciones(v, e);
        estacionesCercanas(v, e);
        ordenesDeTecnico(v, e);
        comparadores(v, e);
        sinDatos(v);
    }

    private static List<String> fila(String... celdas) {
        return List.of(celdas);
    }

    private static void viajesPorUsuario(Verificador v, Escenario e) {
        Reporte r = new GeneradorReportes().viajesPorUsuario(e.usuarios);
        v.verificarIgual("Viajes por usuario: título", "Viajes por usuario", r.getTitulo());
        v.verificarNumero("Viajes por usuario: un renglón por usuario", 3, r.cantidadFilas());
        // Beto hizo 3 viajes (2 finalizados + 1 cancelado): 111,19 km y 300 + 0 + 260 = 560
        v.verificarIgual("Viajes por usuario: primero el que más viajó (Beto)",
                fila("87654321", "Beto", "3", "2", "111,19", "560,00"), r.getFilas().get(0));
        // Ana: 2 viajes finalizados, 2 x 111,19 = 222,39 km y 600 + 800 = 1400
        v.verificarIgual("Viajes por usuario: Ana",
                fila("12345678", "Ana", "2", "2", "222,39", "1400,00"), r.getFilas().get(1));
        v.verificarIgual("Viajes por usuario: el que no viajó queda último, en cero",
                fila("11111111", "Cami", "0", "0", "0,00", "0,00"), r.getFilas().get(2));

        // si empatan en cantidad de viajes se ordena por documento
        Usuario zoe = new Usuario("Zoe", "22222222", "zoe@mail.com", LocalDate.of(2026, 11, 1));
        Usuario yan = new Usuario("Yan", "10000000", "yan@mail.com", LocalDate.of(2026, 11, 1));
        Reporte empate = new GeneradorReportes().viajesPorUsuario(new ArrayList<>(List.of(zoe, yan)));
        v.verificarIgual("Viajes por usuario: con igual cantidad desempata el documento (10000000 antes que 22222222)",
                "Yan", empate.getFilas().get(0).get(1));
    }

    private static void historialDeViajes(Verificador v, Escenario e) {
        GeneradorReportes generador = new GeneradorReportes();
        Reporte ana = generador.historialDeViajes(e.ana);
        v.verificarIgual("Historial: título con nombre y documento", "Viajes de Ana (12345678)", ana.getTitulo());
        v.verificarNumero("Historial de Ana: 2 viajes", 2, ana.cantidadFilas());
        v.verificarIgual("Historial de Ana: primero el más reciente (monopatín)",
                fila("09/11/2026 10:20", "09/11/2026 10:30", "MP-1", "B", "A", "Finalizado", "10", "111,19", "-", "800,00"),
                ana.getFilas().get(0));
        v.verificarIgual("Historial de Ana: después la bicicleta",
                fila("09/11/2026 10:00", "09/11/2026 10:20", "BC-1", "A", "B", "Finalizado", "20", "111,19", "-", "600,00"),
                ana.getFilas().get(1));

        Reporte beto = generador.historialDeViajes(e.beto);
        v.verificarNumero("Historial de Beto: 3 viajes", 3, beto.cantidadFilas());
        v.verificarIgual("Historial de Beto: el viaje cancelado figura sin devolución ni cobro",
                fila("09/11/2026 10:35", "09/11/2026 10:36", "BE-1", "B", "-", "Cancelado", "1", "0,00", "-", "0,00"),
                beto.getFilas().get(1));
        v.verificarIgual("Historial de Beto: el último es el de BC-2 en la misma estación",
                fila("09/11/2026 10:36", "09/11/2026 10:39", "BC-2", "A", "A", "Finalizado", "3", "0,00", "-", "260,00"),
                beto.getFilas().get(0));

        v.verificarNumero("Historial: un usuario sin viajes no tiene filas", 0, generador.historialDeViajes(e.cami).cantidadFilas());
        v.verificarExcepcion("Historial: el usuario es obligatorio", IllegalArgumentException.class,
                () -> generador.historialDeViajes(null));
    }

    private static void vehiculosConMasFallas(Verificador v, Escenario e) {
        GeneradorReportes generador = new GeneradorReportes();
        List<Vehiculo> vehiculos = e.servicioVehiculos.getVehiculos();
        Reporte r = generador.vehiculosConMasFallas(vehiculos, e.servicioViajes.getViajes(), 0);
        v.verificarNumero("Más fallas: solo aparecen los vehículos con fallas (2 de 4)", 2, r.cantidadFilas());
        // BC-1: 2 fallas, 2 viajes finalizados (1 y 3) y 222,39 km
        v.verificarIgual("Más fallas: primero BC-1",
                fila("1", "BC-1", "Bicicleta común", "2", "2", "222,39"), r.getFilas().get(0));
        v.verificarIgual("Más fallas: después MP-1",
                fila("2", "MP-1", "Monopatín eléctrico", "1", "1", "111,19"), r.getFilas().get(1));

        Reporte primero = generador.vehiculosConMasFallas(vehiculos, e.servicioViajes.getViajes(), 1);
        v.verificarNumero("Más fallas: con máximo 1 solo queda el primero", 1, primero.cantidadFilas());
        v.verificarExcepcion("Más fallas: el máximo no puede ser negativo", IllegalArgumentException.class,
                () -> generador.vehiculosConMasFallas(vehiculos, e.servicioViajes.getViajes(), -1));
    }

    private static void usoPorEstacion(Verificador v, Escenario e) {
        Reporte r = new GeneradorReportes().usoPorEstacion(e.estaciones, e.servicioViajes.getViajes());
        v.verificarNumero("Uso por estación: 3 estaciones", 3, r.cantidadFilas());
        // A: retiros 1 y 5, devoluciones 2 y 5 = 4 | ocupación: BC-2 y MP-1 = 2 de 10
        v.verificarIgual("Uso por estación: A es la más usada",
                fila("A", "Calle A 100", "2", "2", "4", "20,0%"), r.getFilas().get(0));
        // B: retiros 2 y 3 (el 4 se canceló), devolución 1 = 3 | ocupación: BE-1 = 1 de 10
        v.verificarIgual("Uso por estación: B, el viaje cancelado no cuenta como retiro",
                fila("B", "Calle B 200", "2", "1", "3", "10,0%"), r.getFilas().get(1));
        // C: solo la devolución 3 | ocupación: BC-1 = 1 de 4
        v.verificarIgual("Uso por estación: C",
                fila("C", "Calle C 300", "0", "1", "1", "25,0%"), r.getFilas().get(2));
    }

    private static void reparaciones(Verificador v, Escenario e) {
        GeneradorReportes generador = new GeneradorReportes();
        List<OrdenMantenimiento> ordenes = e.mantenimiento.getOrdenes();
        Reporte r = generador.reparaciones(ordenes);
        v.verificarNumero("Reparaciones: 2 cerradas (la de BE-1 sigue abierta y no aparece)", 2, r.cantidadFilas());
        v.verificarIgual("Reparaciones: primero BC-1 (ordenado por vehículo)",
                fila("BC-1", "Bicicleta común", "11/11/2026", "12/11/2026", "1", "Tania",
                        "Ajuste de frenos / Cambio de cadena", "Cadena"), r.getFilas().get(0));
        v.verificarIgual("Reparaciones: después MP-1, 2 días",
                fila("MP-1", "Monopatín eléctrico", "09/11/2026", "11/11/2026", "2", "Tomás",
                        "Cambio de rueda", "Rueda trasera"), r.getFilas().get(1));

        Reporte deMp1 = generador.reparacionesDe(ordenes, e.mp1);
        v.verificarIgual("Reparaciones de un vehículo: título", "Reparaciones de MP-1", deMp1.getTitulo());
        v.verificarNumero("Reparaciones de un vehículo: solo las suyas", 1, deMp1.cantidadFilas());
        v.verificarNumero("Reparaciones de un vehículo: una orden abierta no cuenta", 0,
                generador.reparacionesDe(ordenes, e.be1).cantidadFilas());
        v.verificarNumero("Reparaciones de un vehículo sin órdenes", 0,
                generador.reparacionesDe(ordenes, e.bc2).cantidadFilas());
        v.verificarExcepcion("Reparaciones: el vehículo es obligatorio", IllegalArgumentException.class,
                () -> generador.reparacionesDe(ordenes, null));
    }

    private static void estacionesCercanas(Verificador v, Escenario e) {
        GeneradorReportes generador = new GeneradorReportes();
        // desde (0; 1,9): C queda a 0,1 grado (11,12 km), B a 0,9 grado y A a 1,9 grados (211,27 km)
        Reporte r = generador.estacionesCercanas(e.estaciones, new Coordenada(0, 1.9));
        v.verificarNumero("Cercanas: B no aparece porque su único vehículo (BE-1) está en reparación", 2, r.cantidadFilas());
        v.verificarIgual("Cercanas: primero C, con BC-1 disponible",
                fila("C", "Calle C 300", "11,12", "1", "1", "0", "0"), r.getFilas().get(0));
        v.verificarIgual("Cercanas: después A, con BC-2 y MP-1",
                fila("A", "Calle A 100", "211,27", "2", "1", "0", "1"), r.getFilas().get(1));

        // desde (0; 0,1) el orden se invierte
        Reporte otra = generador.estacionesCercanas(e.estaciones, new Coordenada(0, 0.1));
        v.verificarIgual("Cercanas: cambiando la ubicación cambia el orden", "A", otra.getFilas().get(0).get(0));

        v.verificarExcepcion("Cercanas: la ubicación es obligatoria", IllegalArgumentException.class,
                () -> generador.estacionesCercanas(e.estaciones, null));
    }

    private static void ordenesDeTecnico(Verificador v, Escenario e) {
        GeneradorReportes generador = new GeneradorReportes();
        Reporte abiertas = generador.ordenesDeTecnico(e.tania, true);
        v.verificarIgual("Órdenes abiertas: título", "Órdenes abiertas de Tania", abiertas.getTitulo());
        v.verificarNumero("Órdenes abiertas de Tania: 1", 1, abiertas.cantidadFilas());
        v.verificarIgual("Órdenes abiertas de Tania: BE-1 sin cierre ni tareas",
                fila("BE-1", "12/11/2026", "-", "-", "-"), abiertas.getFilas().get(0));

        Reporte cerradas = generador.ordenesDeTecnico(e.tania, false);
        v.verificarIgual("Órdenes cerradas: título", "Órdenes cerradas de Tania", cerradas.getTitulo());
        v.verificarIgual("Órdenes cerradas de Tania: BC-1",
                fila("BC-1", "11/11/2026", "12/11/2026", "Ajuste de frenos / Cambio de cadena", "Cadena"),
                cerradas.getFilas().get(0));

        v.verificarNumero("Órdenes abiertas de Tomás: ninguna", 0, generador.ordenesDeTecnico(e.tomas, true).cantidadFilas());
        v.verificarIgual("Órdenes abiertas de Tomás: avisa que no hay datos",
                "Órdenes abiertas de Tomás\n(sin datos)\n", generador.ordenesDeTecnico(e.tomas, true).aTexto());
        v.verificarIgual("Órdenes cerradas de Tomás: MP-1", "MP-1", generador.ordenesDeTecnico(e.tomas, false).getFilas().get(0).get(0));
        v.verificarExcepcion("Órdenes de técnico: el técnico es obligatorio", IllegalArgumentException.class,
                () -> generador.ordenesDeTecnico(null, true));
    }

    private static void comparadores(Verificador v, Escenario e) {
        // dos estaciones en el mismo lugar: desempata el código
        Estacion x1 = ViajesDePrueba.estacion("X1", 5, 0, 5);
        Estacion x2 = ViajesDePrueba.estacion("X2", 5, 0, 5);
        ComparadorEstacionesPorDistancia comparador = new ComparadorEstacionesPorDistancia(new Coordenada(0, 0));
        v.verificarVerdadero("Comparador de distancia: igual distancia, X1 antes que X2", comparador.compare(x1, x2) < 0);
        v.verificarVerdadero("Comparador de distancia: A (en la ubicación) antes que B", comparador.compare(e.a, e.b) < 0);
        v.verificarVerdadero("Comparador de distancia: C después de B", comparador.compare(e.c, e.b) > 0);
        v.verificarNumero("Comparador de distancia: una estación contra sí misma", 0, comparador.compare(e.a, e.a));
        v.verificarExcepcion("Comparador de distancia: la ubicación es obligatoria", IllegalArgumentException.class,
                () -> new ComparadorEstacionesPorDistancia(null));

        List<Estacion> ordenadas = new ArrayList<>(List.of(e.c, e.a, e.b));
        Collections.sort(ordenadas, new ComparadorEstacionesPorDistancia(new Coordenada(0, 2)));
        v.verificarIgual("Comparador de distancia: ordenando desde C queda C, B, A", "C,B,A",
                ordenadas.get(0).getCodigo() + "," + ordenadas.get(1).getCodigo() + "," + ordenadas.get(2).getCodigo());
    }

    private static void sinDatos(Verificador v) {
        GeneradorReportes generador = new GeneradorReportes();
        List<Usuario> usuarios = new ArrayList<>();
        List<Estacion> estaciones = new ArrayList<>();
        List<Vehiculo> vehiculos = new ArrayList<>();
        List<Viaje> viajes = new ArrayList<>();
        List<OrdenMantenimiento> ordenes = new ArrayList<>();
        v.verificarNumero("Sin datos: viajes por usuario vacío", 0, generador.viajesPorUsuario(usuarios).cantidadFilas());
        v.verificarNumero("Sin datos: vehículos con fallas vacío", 0, generador.vehiculosConMasFallas(vehiculos, viajes, 0).cantidadFilas());
        v.verificarNumero("Sin datos: uso por estación vacío", 0, generador.usoPorEstacion(estaciones, viajes).cantidadFilas());
        v.verificarNumero("Sin datos: reparaciones vacío", 0, generador.reparaciones(ordenes).cantidadFilas());
        v.verificarNumero("Sin datos: estaciones cercanas vacío", 0,
                generador.estacionesCercanas(estaciones, new Coordenada(0, 0)).cantidadFilas());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}