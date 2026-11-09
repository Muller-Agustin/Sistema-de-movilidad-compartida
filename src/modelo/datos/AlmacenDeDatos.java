package modelo.datos;

import modelo.tarifas.Tarifa;

// Guarda y carga todos los datos del sistema en una carpeta de archivos de texto.
// Los archivos se cargan en un orden fijo, porque cada uno apunta a datos de los anteriores:
// tarifas, promociones, personal, usuarios, estaciones, vehículos, órdenes y viajes.
public class AlmacenDeDatos {
    public static final String CARPETA_POR_DEFECTO = "datos";

    private final String carpeta;

    public AlmacenDeDatos() {
        this(CARPETA_POR_DEFECTO);
    }

    public AlmacenDeDatos(String carpeta) {
        this.carpeta = carpeta;
    }

    public String getCarpeta() {
        return carpeta;
    }

    public DatosDelSistema cargar() {
        DatosDelSistema datos = new DatosDelSistema();
        for (Tarifa tarifa : new TarifaRepositorioArchivo(carpeta).leerTodos()) {
            datos.getTarifas().agregar(tarifa);
        }
        datos.setPromociones(new PromocionRepositorioArchivo(carpeta).leerTodos());
        datos.setPersonal(new PersonalRepositorioArchivo(carpeta).leerTodos());
        datos.setUsuarios(new UsuarioRepositorioArchivo(carpeta).leerTodos());
        datos.setEstaciones(new EstacionRepositorioArchivo(carpeta).leerTodos());
        datos.setVehiculos(new VehiculoRepositorioArchivo(carpeta, datos).leerTodos());
        datos.setOrdenes(new OrdenRepositorioArchivo(carpeta, datos).leerTodos());
        datos.setViajes(new ViajeRepositorioArchivo(carpeta, datos).leerTodos());
        return datos;
    }

    public void guardar(DatosDelSistema datos) {
        new TarifaRepositorioArchivo(carpeta).guardarTodos(datos.getTarifas().getTarifas());
        new PromocionRepositorioArchivo(carpeta).guardarTodos(datos.getPromociones());
        new PersonalRepositorioArchivo(carpeta).guardarTodos(datos.getPersonal());
        new UsuarioRepositorioArchivo(carpeta).guardarTodos(datos.getUsuarios());
        new EstacionRepositorioArchivo(carpeta).guardarTodos(datos.getEstaciones());
        new VehiculoRepositorioArchivo(carpeta, datos).guardarTodos(datos.getVehiculos());
        new OrdenRepositorioArchivo(carpeta, datos).guardarTodos(datos.getOrdenes());
        new ViajeRepositorioArchivo(carpeta, datos).guardarTodos(datos.getViajes());
    }
}