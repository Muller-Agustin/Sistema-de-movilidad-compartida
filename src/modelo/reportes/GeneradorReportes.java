package modelo.reportes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import modelo.Tecnico;
import modelo.TipoVehiculo;
import modelo.cuentas.Usuario;
import modelo.estaciones.ComparadorEstacionesPorDistancia;
import modelo.estaciones.Coordenada;
import modelo.estaciones.Estacion;
import modelo.mantenimiento.ComparadorOrdenesPorVehiculo;
import modelo.mantenimiento.OrdenMantenimiento;
import modelo.vehiculos.ComparadorVehiculosPorFallas;
import modelo.vehiculos.Vehiculo;
import modelo.viajes.EstadoViaje;
import modelo.viajes.Viaje;

// Arma los reportes y consultas de solo lectura: RF25 a RF28 (reportes del administrador), RF29 (estaciones
// por cercanía), RF30 (historial de viajes) y RF31 y RF32 (órdenes de un técnico, abiertas y cerradas).
// No modifica nada: recibe las listas del sistema y devuelve un Reporte listo para mostrar o exportar.
public class GeneradorReportes {
    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String SEPARADOR_LISTA = " / ";

    // RF25 + S26: un renglón por usuario, del que más viajó al que menos. Cuentan todos los viajes;
    // los km solo los de viajes finalizados y el total es lo que se le cobró (sin contar ajustes).
    public Reporte viajesPorUsuario(List<Usuario> usuarios) {
        List<ResumenDeUsuario> resumenes = new ArrayList<>();
        for (Usuario u : usuarios) {
            resumenes.add(new ResumenDeUsuario(u));
        }
        Collections.sort(resumenes);

        Reporte reporte = new Reporte("Viajes por usuario",
                "Documento", "Usuario", "Viajes", "Finalizados", "Km recorridos", "Total cobrado");
        for (ResumenDeUsuario r : resumenes) {
            reporte.agregarFila(r.usuario.getDocumento(), r.usuario.getNombre(), String.valueOf(r.viajes),
                    String.valueOf(r.finalizados), numero(r.km), numero(r.cobrado));
        }
        return reporte;
    }

    // RF25 (detalle de un usuario) y RF30 (historial propio): del viaje más reciente al más antiguo
    public Reporte historialDeViajes(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        List<Viaje> viajes = new ArrayList<>(usuario.getViajes());
        Collections.reverse(viajes);

        Reporte reporte = new Reporte("Viajes de " + usuario.getNombre() + " (" + usuario.getDocumento() + ")",
                "Inicio", "Fin", "Vehículo", "Retiro", "Devolución", "Estado", "Minutos cobrables", "Km", "Promoción", "Cobrado");
        for (Viaje v : viajes) {
            String minutos = v.getFin() == null ? null : String.valueOf(v.getMinutosCobrables());
            String devolucion = v.getDevolucion() == null ? null : v.getDevolucion().getCodigo();
            String promocion = v.getPromocion() == null ? null : v.getPromocion().getNombre();
            reporte.agregarFila(fechaHora(v.getInicio()), fechaHora(v.getFin()), v.getVehiculo().getCodigo(),
                    v.getRetiro().getCodigo(), devolucion, texto(v.getEstado()), minutos,
                    numero(v.getDistanciaKm()), promocion, numero(v.getImporteCobrado()));
        }
        return reporte;
    }

    // RF26 + S27: vehículos con al menos una falla, del que más fallas tiene al que menos
    // (ComparadorVehiculosPorFallas). Los viajes son los finalizados. "maximo" corta la lista (0 = todos).
    public Reporte vehiculosConMasFallas(List<Vehiculo> vehiculos, List<Viaje> viajes, int maximo) {
        if (maximo < 0) {
            throw new IllegalArgumentException("El máximo no puede ser negativo");
        }
        List<Vehiculo> conFallas = new ArrayList<>();
        for (Vehiculo v : vehiculos) {
            if (v.getCantidadFallas() > 0) {
                conFallas.add(v);
            }
        }
        Collections.sort(conFallas, new ComparadorVehiculosPorFallas());

        Reporte reporte = new Reporte("Vehículos con más fallas",
                "Puesto", "Código", "Tipo", "Fallas", "Viajes", "Km recorridos");
        for (int i = 0; i < conFallas.size() && (maximo == 0 || i < maximo); i++) {
            Vehiculo v = conFallas.get(i);
            int finalizados = 0;
            for (Viaje viaje : viajes) {
                if (viaje.getVehiculo() == v && viaje.getEstado() == EstadoViaje.FINALIZADO) {
                    finalizados++;
                }
            }
            reporte.agregarFila(String.valueOf(i + 1), v.getCodigo(), v.getTipo().toString(),
                    String.valueOf(v.getCantidadFallas()), String.valueOf(finalizados), numero(v.getKmRecorridos()));
        }
        return reporte;
    }

    // RF27 + S27: retiros y devoluciones de cada estación, de la más usada a la menos usada.
    // Un viaje cancelado no cuenta como retiro; una devolución es un viaje finalizado en esa estación.
    public Reporte usoPorEstacion(List<Estacion> estaciones, List<Viaje> viajes) {
        List<UsoDeEstacion> usos = new ArrayList<>();
        for (Estacion e : estaciones) {
            UsoDeEstacion uso = new UsoDeEstacion(e);
            for (Viaje v : viajes) {
                if (v.getRetiro() == e && v.getEstado() != EstadoViaje.CANCELADO) {
                    uso.retiros++;
                }
                if (v.getDevolucion() == e && v.getEstado() == EstadoViaje.FINALIZADO) {
                    uso.devoluciones++;
                }
            }
            usos.add(uso);
        }
        Collections.sort(usos);

        Reporte reporte = new Reporte("Uso por estación",
                "Estación", "Dirección", "Retiros", "Devoluciones", "Total", "Ocupación actual");
        for (UsoDeEstacion uso : usos) {
            reporte.agregarFila(uso.estacion.getCodigo(), uso.estacion.getDireccion(), String.valueOf(uso.retiros),
                    String.valueOf(uso.devoluciones), String.valueOf(uso.total()), porcentaje(uso.estacion.getPorcentajeOcupacion()));
        }
        return reporte;
    }

    // RF28: todas las órdenes cerradas, por vehículo y fecha de apertura
    public Reporte reparaciones(List<OrdenMantenimiento> ordenes) {
        return armarReparaciones("Reparaciones realizadas", ordenes, null);
    }

    // RF28: las órdenes cerradas de un solo vehículo
    public Reporte reparacionesDe(List<OrdenMantenimiento> ordenes, Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo es obligatorio");
        }
        return armarReparaciones("Reparaciones de " + vehiculo.getCodigo(), ordenes, vehiculo);
    }

    private Reporte armarReparaciones(String titulo, List<OrdenMantenimiento> ordenes, Vehiculo vehiculo) {
        List<OrdenMantenimiento> cerradas = new ArrayList<>();
        for (OrdenMantenimiento o : ordenes) {
            if (!o.estaAbierta() && (vehiculo == null || o.getVehiculo() == vehiculo)) {
                cerradas.add(o);
            }
        }
        Collections.sort(cerradas, new ComparadorOrdenesPorVehiculo());

        Reporte reporte = new Reporte(titulo,
                "Vehículo", "Tipo", "Apertura", "Cierre", "Días", "Técnico", "Tareas", "Repuestos");
        for (OrdenMantenimiento o : cerradas) {
            reporte.agregarFila(o.getVehiculo().getCodigo(), o.getVehiculo().getTipo().toString(),
                    fecha(o.getFechaApertura()), fecha(o.getFechaCierre()),
                    String.valueOf(ChronoUnit.DAYS.between(o.getFechaApertura(), o.getFechaCierre())),
                    o.getTecnico().getNombre(), String.join(SEPARADOR_LISTA, o.getTareasRealizadas()),
                    String.join(SEPARADOR_LISTA, o.getRepuestosUtilizados()));
        }
        return reporte;
    }

    // RF29: estaciones que tienen al menos un vehículo disponible, de la más cercana a la más lejana
    public Reporte estacionesCercanas(List<Estacion> estaciones, Coordenada ubicacion) {
        List<Estacion> conDisponibles = new ArrayList<>();
        for (Estacion e : estaciones) {
            if (!e.getVehiculosDisponibles().isEmpty()) {
                conDisponibles.add(e);
            }
        }
        Collections.sort(conDisponibles, new ComparadorEstacionesPorDistancia(ubicacion));

        Reporte reporte = new Reporte("Estaciones con vehículos disponibles, de la más cercana a la más lejana",
                "Estación", "Dirección", "Distancia (km)", "Disponibles", "Bicis comunes", "Bicis eléctricas", "Monopatines");
        for (Estacion e : conDisponibles) {
            List<Vehiculo> disponibles = e.getVehiculosDisponibles();
            reporte.agregarFila(e.getCodigo(), e.getDireccion(), numero(e.getCoordenada().distanciaKm(ubicacion)),
                    String.valueOf(disponibles.size()),
                    String.valueOf(contarTipo(disponibles, TipoVehiculo.BICICLETA_COMUN)),
                    String.valueOf(contarTipo(disponibles, TipoVehiculo.BICICLETA_ELECTRICA)),
                    String.valueOf(contarTipo(disponibles, TipoVehiculo.MONOPATIN)));
        }
        return reporte;
    }

    // RF31 (abiertas = true) y RF32 (abiertas = false): las órdenes de un técnico
    public Reporte ordenesDeTecnico(Tecnico tecnico, boolean abiertas) {
        if (tecnico == null) {
            throw new IllegalArgumentException("El técnico es obligatorio");
        }
        List<OrdenMantenimiento> ordenes = abiertas ? tecnico.getOrdenesAbiertas() : tecnico.getOrdenesCerradas();
        Collections.sort(ordenes, new ComparadorOrdenesPorVehiculo());

        String titulo = (abiertas ? "Órdenes abiertas de " : "Órdenes cerradas de ") + tecnico.getNombre();
        Reporte reporte = new Reporte(titulo, "Vehículo", "Apertura", "Cierre", "Tareas", "Repuestos");
        for (OrdenMantenimiento o : ordenes) {
            reporte.agregarFila(o.getVehiculo().getCodigo(), fecha(o.getFechaApertura()), fecha(o.getFechaCierre()),
                    String.join(SEPARADOR_LISTA, o.getTareasRealizadas()),
                    String.join(SEPARADOR_LISTA, o.getRepuestosUtilizados()));
        }
        return reporte;
    }

    private int contarTipo(List<Vehiculo> vehiculos, TipoVehiculo tipo) {
        int cantidad = 0;
        for (Vehiculo v : vehiculos) {
            if (v.getTipo() == tipo) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // ---------- formato de los datos (decimales con coma y fechas día/mes/año) ----------

    private static String numero(double valor) {
        return String.format(ARGENTINA, "%.2f", valor);
    }

    private static String porcentaje(double valor) {
        return String.format(ARGENTINA, "%.1f%%", valor);
    }

    private static String fecha(LocalDate fecha) {
        return fecha == null ? null : fecha.format(FECHA);
    }

    private static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? null : fecha.format(FECHA_HORA);
    }

    private static String texto(EstadoViaje estado) {
        switch (estado) {
            case EN_CURSO:
                return "En curso";
            case FINALIZADO:
                return "Finalizado";
            case CANCELADO:
                return "Cancelado";
            default:
                return "Perdido";
        }
    }

    // ---------- filas intermedias que se ordenan antes de armar el reporte ----------

    // Natural: el que más viajes hizo primero; si empatan, por documento
    private static class ResumenDeUsuario implements Comparable<ResumenDeUsuario> {
        private final Usuario usuario;
        private final int viajes;
        private int finalizados;
        private double km;
        private double cobrado;

        private ResumenDeUsuario(Usuario usuario) {
            this.usuario = usuario;
            this.viajes = usuario.getViajes().size();
            for (Viaje v : usuario.getViajes()) {
                cobrado += v.getImporteCobrado();
                if (v.getEstado() == EstadoViaje.FINALIZADO) {
                    finalizados++;
                    km += v.getDistanciaKm();
                }
            }
        }

        @Override
        public int compareTo(ResumenDeUsuario otro) {
            int resultado = Integer.compare(otro.viajes, viajes);
            if (resultado == 0) {
                resultado = usuario.getDocumento().compareTo(otro.usuario.getDocumento());
            }
            return resultado;
        }
    }

    // Natural: la estación con más movimientos (retiros + devoluciones) primero; si empatan, por código
    private static class UsoDeEstacion implements Comparable<UsoDeEstacion> {
        private final Estacion estacion;
        private int retiros;
        private int devoluciones;

        private UsoDeEstacion(Estacion estacion) {
            this.estacion = estacion;
        }

        private int total() {
            return retiros + devoluciones;
        }

        @Override
        public int compareTo(UsoDeEstacion otro) {
            int resultado = Integer.compare(otro.total(), total());
            if (resultado == 0) {
                resultado = estacion.getCodigo().compareTo(otro.estacion.getCodigo());
            }
            return resultado;
        }
    }
}