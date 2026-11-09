package modelo.datos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.Operador;
import modelo.Personal;
import modelo.Tecnico;
import modelo.TipoVehiculo;
import modelo.cuentas.Usuario;
import modelo.estaciones.Estacion;
import modelo.mantenimiento.OrdenMantenimiento;
import modelo.excepciones.PersistenciaExcepcion;
import modelo.tarifas.CatalogoTarifas;
import modelo.tarifas.Promocion;
import modelo.tarifas.Tarifa;
import modelo.vehiculos.Vehiculo;
import modelo.viajes.Viaje;

// Todo lo que el sistema guarda entre ejecuciones. Los métodos "obtener" sirven para unir los datos
// cuando se cargan los archivos (un viaje apunta a un usuario por su documento, a un vehículo por su código, etc.).
public class DatosDelSistema {
    private CatalogoTarifas tarifas;
    private List<Promocion> promociones;
    private List<Personal> personal;
    private List<Usuario> usuarios;
    private List<Estacion> estaciones;
    private List<Vehiculo> vehiculos;
    private List<OrdenMantenimiento> ordenes;
    private List<Viaje> viajes;

    public DatosDelSistema() {
        this.tarifas = new CatalogoTarifas();
        this.promociones = new ArrayList<>();
        this.personal = new ArrayList<>();
        this.usuarios = new ArrayList<>();
        this.estaciones = new ArrayList<>();
        this.vehiculos = new ArrayList<>();
        this.ordenes = new ArrayList<>();
        this.viajes = new ArrayList<>();
    }

    // Las listas se devuelven tal cual (no copias): es el lugar donde el sistema agrega y quita datos.
    public CatalogoTarifas getTarifas() { return tarifas; }
    public List<Promocion> getPromociones() { return promociones; }
    public List<Personal> getPersonal() { return personal; }
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Estacion> getEstaciones() { return estaciones; }
    public List<Vehiculo> getVehiculos() { return vehiculos; }
    public List<OrdenMantenimiento> getOrdenes() { return ordenes; }
    public List<Viaje> getViajes() { return viajes; }

    public void setTarifas(CatalogoTarifas tarifas) { this.tarifas = tarifas; }
    public void setPromociones(List<Promocion> promociones) { this.promociones = new ArrayList<>(promociones); }
    public void setPersonal(List<Personal> personal) { this.personal = new ArrayList<>(personal); }
    public void setUsuarios(List<Usuario> usuarios) { this.usuarios = new ArrayList<>(usuarios); }
    public void setEstaciones(List<Estacion> estaciones) { this.estaciones = new ArrayList<>(estaciones); }
    public void setVehiculos(List<Vehiculo> vehiculos) { this.vehiculos = new ArrayList<>(vehiculos); }
    public void setOrdenes(List<OrdenMantenimiento> ordenes) { this.ordenes = new ArrayList<>(ordenes); }
    public void setViajes(List<Viaje> viajes) { this.viajes = new ArrayList<>(viajes); }

    public Usuario obtenerUsuario(String documento) {
        for (Usuario u : usuarios) {
            if (u.getDocumento().equals(documento)) {
                return u;
            }
        }
        throw new PersistenciaExcepcion("No existe el usuario con documento " + documento);
    }

    public Personal obtenerPersonal(String legajo) {
        for (Personal p : personal) {
            if (p.getLegajo().equals(legajo)) {
                return p;
            }
        }
        throw new PersistenciaExcepcion("No existe el personal con legajo " + legajo);
    }

    public Operador obtenerOperador(String legajo) {
        Personal p = obtenerPersonal(legajo);
        if (!(p instanceof Operador)) {
            throw new PersistenciaExcepcion("El legajo " + legajo + " no es de un operador");
        }
        return (Operador) p;
    }

    public Tecnico obtenerTecnico(String legajo) {
        Personal p = obtenerPersonal(legajo);
        if (!(p instanceof Tecnico)) {
            throw new PersistenciaExcepcion("El legajo " + legajo + " no es de un técnico");
        }
        return (Tecnico) p;
    }

    public Estacion obtenerEstacion(String codigo) {
        for (Estacion e : estaciones) {
            if (e.getCodigo().equals(codigo)) {
                return e;
            }
        }
        throw new PersistenciaExcepcion("No existe la estación " + codigo);
    }

    public Vehiculo obtenerVehiculo(String codigo) {
        for (Vehiculo v : vehiculos) {
            if (v.getCodigo().equals(codigo)) {
                return v;
            }
        }
        throw new PersistenciaExcepcion("No existe el vehículo " + codigo);
    }

    public Promocion obtenerPromocion(String nombre) {
        for (Promocion p : promociones) {
            if (p.getNombre().equals(nombre)) {
                return p;
            }
        }
        throw new PersistenciaExcepcion("No existe la promoción " + nombre);
    }

    public Tarifa obtenerTarifa(TipoVehiculo tipo, LocalDate vigencia) {
        for (Tarifa t : tarifas.getTarifas()) {
            if (t.getTipoVehiculo() == tipo && t.getFechaVigencia().equals(vigencia)) {
                return t;
            }
        }
        throw new PersistenciaExcepcion("No existe la tarifa de " + tipo + " con vigencia " + vigencia);
    }
}