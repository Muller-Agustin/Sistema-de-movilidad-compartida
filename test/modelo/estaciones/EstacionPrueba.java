package modelo.estaciones;

import java.time.LocalDateTime;
import java.lang.reflect.Method;
import modelo.BicicletaComun;
import modelo.EstadoVehiculo;
import pruebas.Verificador;
import pruebas.ViajesDePrueba;

public class EstacionPrueba {
    private static final LocalDateTime AHORA = ViajesDePrueba.INICIO;

    public static void ejecutar(Verificador v) {
        datosInvalidos(v);
        capacidadYOcupacion(v);
        vehiculosDisponibles(v);
        alertasDeRedistribucion(v);
    }

    private static void datosInvalidos(Verificador v) {
        v.verificarExcepcion("Estación: capacidad 0 falla", IllegalArgumentException.class,
                () -> new Estacion("E1", "Calle 1", 0, new Coordenada(0, 0)));
        v.verificarExcepcion("Estación: sin código falla", IllegalArgumentException.class,
                () -> new Estacion(" ", "Calle 1", 5, new Coordenada(0, 0)));
    }

    private static void capacidadYOcupacion(Verificador v) {
        Estacion estacion = ViajesDePrueba.estacion("E1", 10);
        for (int i = 0; i < 3; i++) {
            estacion.recibirVehiculo(ViajesDePrueba.bicicleta(), AHORA);
        }
        v.verificarNumero("Estación: 3 de 10 = 30% de ocupación", 30, estacion.getPorcentajeOcupacion());
        v.verificarFalso("Estación: con lugar no está llena", estacion.estaLlena());

        // RN13: no se puede devolver en una estación llena
        Estacion chica = ViajesDePrueba.estacion("E2", 2);
        chica.recibirVehiculo(ViajesDePrueba.bicicleta(), AHORA);
        chica.recibirVehiculo(ViajesDePrueba.bicicleta(), AHORA);
        v.verificarVerdadero("Estación: 2 de 2 está llena", chica.estaLlena());
        v.verificarExcepcion("Estación: no recibe un vehículo si está llena", IllegalStateException.class,
                () -> chica.recibirVehiculo(ViajesDePrueba.bicicleta(), AHORA));

        v.verificarExcepcion("Estación: no libera un vehículo que no tiene", IllegalArgumentException.class,
                () -> estacion.liberarVehiculo(ViajesDePrueba.bicicleta(), AHORA));
    }

    private static void vehiculosDisponibles(Verificador v) {
        Estacion estacion = ViajesDePrueba.estacion("E1", 10);
        BicicletaComun buena = ViajesDePrueba.bicicleta();
        BicicletaComun rota = ViajesDePrueba.bicicleta();
        estacion.recibirVehiculo(buena, AHORA);
        estacion.recibirVehiculo(rota, AHORA);
        ponerEnReparacion(rota);

        v.verificarNumero("Estación: 2 vehículos guardados", 2, estacion.getVehiculos().size());
        v.verificarNumero("Estación: solo 1 disponible (el otro está en reparación)", 1,
                estacion.getVehiculosDisponibles().size());
    }

    private static void ponerEnReparacion(BicicletaComun bicicleta) {
        try {
            Class<?> tipo = bicicleta.getClass();
            while (tipo != null) {
                try {
                    Method metodo = tipo.getDeclaredMethod("setEstado", EstadoVehiculo.class);
                    metodo.setAccessible(true);
                    metodo.invoke(bicicleta, EstadoVehiculo.EN_REPARACION);
                    return;
                } catch (NoSuchMethodException e) {
                    tipo = tipo.getSuperclass();
                }
            }
            throw new AssertionError("No se encontró el método setEstado");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("No se pudo cambiar el estado del vehículo", e);
        }
    }

    // RN16 + S9: una alerta por cada vez que la estación entra en zona (menos de 20% o más de 90%)
    private static void alertasDeRedistribucion(Verificador v) {
        Estacion estacion = ViajesDePrueba.estacion("E1", 10);
        BicicletaComun[] bicis = new BicicletaComun[10];
        for (int i = 0; i < 10; i++) {
            bicis[i] = ViajesDePrueba.bicicleta();
        }

        estacion.recibirVehiculo(bicis[0], AHORA);   // 10%: entra en zona "vacía"
        v.verificarNumero("Alertas: con 1 de 10 hay 1 alerta", 1, estacion.getAlertasRedistribucion().size());
        v.verificarIgual("Alertas: la primera es de estación vacía", TipoAlerta.VACIA,
                estacion.getAlertasRedistribucion().get(0).getTipo());

        estacion.recibirVehiculo(bicis[1], AHORA);   // 20%: ya salió de la zona
        v.verificarNumero("Alertas: con 2 de 10 (20%) no hay alerta nueva", 1, estacion.getAlertasRedistribucion().size());

        for (int i = 2; i < 9; i++) {                // hasta 9 de 10 (90%): zona normal
            estacion.recibirVehiculo(bicis[i], AHORA);
        }
        v.verificarNumero("Alertas: con 9 de 10 (90%) no hay alerta nueva", 1, estacion.getAlertasRedistribucion().size());

        estacion.recibirVehiculo(bicis[9], AHORA);   // 100%: entra en zona "saturada"
        v.verificarNumero("Alertas: con 10 de 10 hay 2 alertas", 2, estacion.getAlertasRedistribucion().size());
        v.verificarIgual("Alertas: la segunda es de estación saturada", TipoAlerta.SATURADA,
                estacion.getAlertasRedistribucion().get(1).getTipo());

        for (int i = 9; i >= 1; i--) {               // se retiran hasta que queda 1 (10%)
            estacion.liberarVehiculo(bicis[i], AHORA);
        }
        v.verificarNumero("Alertas: al volver a quedar con 1 de 10 hay 3 alertas", 3, estacion.getAlertasRedistribucion().size());

        estacion.liberarVehiculo(bicis[0], AHORA);   // 0%: sigue en la misma zona, no repite
        v.verificarNumero("Alertas: vaciar del todo no repite la alerta", 3, estacion.getAlertasRedistribucion().size());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}