package modelo.servicios;

import java.time.LocalDateTime;

// Indica qué hora es. Con esta interfaz el sistema puede usar la hora real o una hora simulada (RF29).
public interface Reloj {
    LocalDateTime ahora();
}