package pruebas;

import datos.ExportadorCSVPrueba;
import datos.PersistenciaPrueba;
import modelo.estaciones.CoordenadaPrueba;
import modelo.estaciones.EstacionPrueba;
import modelo.mantenimiento.OrdenMantenimientoPrueba;
import modelo.personal.OperadorPrueba;
import modelo.reportes.GeneradorReportesPrueba;
import modelo.reportes.ReportePrueba;
import modelo.servicios.CalculadoraDeTarifaPrueba;
import modelo.servicios.RelojPrueba;
import modelo.servicios.ServicioMantenimientoPrueba;
import modelo.servicios.ServicioVehiculosPrueba;
import modelo.servicios.ServicioViajesPrueba;
import modelo.tarifas.CatalogoTarifasPrueba;
import modelo.tarifas.PromocionPrueba;
import modelo.tarifas.TarifaPrueba;
import modelo.vehiculos.VehiculoPrueba;
import modelo.viajes.ViajePrueba;

// Ejecuta todas las pruebas del proyecto. Se agrega una línea por cada clase de prueba nueva.
public class EjecutarPruebas {
    public static void main(String[] args) {
        Verificador v = new Verificador();

        TarifaPrueba.ejecutar(v);
        CatalogoTarifasPrueba.ejecutar(v);
        CoordenadaPrueba.ejecutar(v);
        EstacionPrueba.ejecutar(v);
        ViajePrueba.ejecutar(v);
        PromocionPrueba.ejecutar(v);
        CalculadoraDeTarifaPrueba.ejecutar(v);
        RelojPrueba.ejecutar(v);
        OperadorPrueba.ejecutar(v);
        ServicioViajesPrueba.ejecutar(v);
        VehiculoPrueba.ejecutar(v);
        OrdenMantenimientoPrueba.ejecutar(v);
        ServicioVehiculosPrueba.ejecutar(v);
        ServicioMantenimientoPrueba.ejecutar(v);
        PersistenciaPrueba.ejecutar(v);
        ReportePrueba.ejecutar(v);
        ExportadorCSVPrueba.ejecutar(v);
        GeneradorReportesPrueba.ejecutar(v);

        v.resumen();
    }
}