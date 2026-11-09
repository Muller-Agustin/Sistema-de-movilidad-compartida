package modelo.servicios;

import java.time.LocalDateTime;
import pruebas.Verificador;

public class RelojPrueba {

    public static void ejecutar(Verificador v) {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 9, 10, 0);
        RelojSimulado reloj = new RelojSimulado(inicio);

        v.verificarIgual("Reloj: arranca en la hora indicada", inicio, reloj.ahora());
        reloj.avanzarMinutos(90);
        v.verificarIgual("Reloj: avanzar 90 minutos", LocalDateTime.of(2026, 11, 9, 11, 30), reloj.ahora());
        reloj.avanzarHoras(24);
        v.verificarIgual("Reloj: avanzar 24 horas", LocalDateTime.of(2026, 11, 10, 11, 30), reloj.ahora());
        v.verificarExcepcion("Reloj: no se puede retroceder", IllegalArgumentException.class,
                () -> reloj.avanzarMinutos(-5));
        v.verificarExcepcion("Reloj: sin fecha inicial falla", IllegalArgumentException.class,
                () -> new RelojSimulado(null));

        Reloj real = new RelojReal();
        v.verificarVerdadero("Reloj real: devuelve una hora", real.ahora() != null);
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}