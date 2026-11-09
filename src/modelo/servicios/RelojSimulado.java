package modelo.servicios;

import java.time.LocalDateTime;

// RF29: reloj que solo avanza cuando se lo pedimos, para verificar reglas que dependen del tiempo.
public class RelojSimulado implements Reloj {
    private LocalDateTime actual;

    public RelojSimulado(LocalDateTime inicio) {
        if (inicio == null) {
            throw new IllegalArgumentException("La fecha inicial es obligatoria");
        }
        this.actual = inicio;
    }

    @Override
    public LocalDateTime ahora() {
        return actual;
    }

    public void avanzarMinutos(long minutos) {
        if (minutos < 0) {
            throw new IllegalArgumentException("No se puede retroceder el reloj");
        }
        actual = actual.plusMinutes(minutos);
    }

    public void avanzarHoras(long horas) {
        avanzarMinutos(horas * 60);
    }
}