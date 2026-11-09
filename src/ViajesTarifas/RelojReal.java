package ViajesTarifas;

import java.time.LocalDateTime;

public class RelojReal implements Reloj {

    @Override
    public LocalDateTime ahora() {
        return LocalDateTime.now();
    }
}