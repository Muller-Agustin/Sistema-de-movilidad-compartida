package ViajesTarifas;

// Estados de un viaje. Solo el viaje EN_CURSO puede cambiar; los demás están cerrados (RN24).

public enum EstadoViaje {
    EN_CURSO,
    FINALIZADO,
    CANCELADO,
    PERDIDO
}