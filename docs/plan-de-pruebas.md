# Plan de pruebas y resultados

Pruebas del sistema de movilidad compartida en dos niveles:

1. **Pruebas unitarias:** verifican las reglas de negocio sobre las clases del dominio, con horarios fijos para que el resultado sea siempre el mismo. Están en `test/PruebasUnitarias.java` y se ejecutan con `Pruebas.bat`.
2. **Pruebas funcionales:** verifican los casos de uso principales desde la interfaz Swing, partiendo de los datos de prueba (carpeta `datos` borrada antes de empezar).

## 1. Entorno

| Parámetro | Valor |
|---|---|
| Sistema | Movilidad Compartida – Propuesta C |
| Fecha de ejecución | Noviembre 2026 |
| Lenguaje | Java (JDK 17+) |
| Pruebas unitarias | Clase `PruebasUnitarias` con `main` |
| Interfaz | Java Swing (`VistaMain.Main`) |
| Persistencia | `datos/sistema.dat` (serialización) |

## 2. Pruebas unitarias

Tarifa usada en las pruebas: bicicleta común, $200 de desbloqueo + $20 por minuto.

| ID | Prueba | Regla | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|:---:|
| TU-01 | `pruebaTarifa` | RN17, RN19 | 30 min = $800; 130 min = 200 + 20×120 + 30×10 = $2.900 | $800 y $2.900 | APROBADO |
| TU-02 | `pruebaSaldoMinimo` | RN01 | Con $799 se rechaza el retiro y el vehículo sigue en la estación | `RetiroNoPermitidoExcepcion`: "se necesitan al menos $800" | APROBADO |
| TU-03 | `pruebaViajeEnCurso` | RN02 | El segundo retiro con un viaje abierto se rechaza | `RetiroNoPermitidoExcepcion` | APROBADO |
| TU-04 | `pruebaBateriaBaja` | RN08 | Eléctrica al 10 % en `BATERIA_BAJA`, no se retira; al recargar vuelve a `DISPONIBLE` | Como se esperaba | APROBADO |
| TU-05 | `pruebaPrimerViaje` | RN21 | Primer viaje de 20 min = 600 − 500 = $100; el segundo, $600 | $100 y $600 | APROBADO |
| TU-06 | `pruebaPlanMensual` | RN22, RN23 | Viaje de 40 min con plan = 1.000 − 800 = $200; se elige el plan; 1 viaje usado | $200, plan elegido, 1 viaje | APROBADO |
| TU-07 | `pruebaSaldoNegativo` | RN03 | Saldo −$500 → inhabilitado; al recargar a $0 → habilitado | Como se esperaba | APROBADO |
| TU-08 | `pruebaEstacionLlena` | RN13, RN15 | Estación llena → excepción; devolución a los 38 min = 30 min cobrados = $800 | `EstacionLlenaExcepcion` y $800 | APROBADO |
| TU-09 | `pruebaCancelacion` | RN18 | A los 3 min no se cancela; a 1 min sí, sin cargo | Como se esperaba | APROBADO |
| TU-10 | `pruebaTresFallas` | RN10 | 2 fallas → disponible; 3 fallas → `EN_REPARACION` y orden automática | Como se esperaba | APROBADO |
| TU-11 | `pruebaCierreOrden` | RN11 | Sin tareas no se cierra; con tareas se cierra y el vehículo vuelve a `DISPONIBLE` | `OrdenIncompletaExcepcion` y luego cierre correcto | APROBADO |
| TU-12 | `pruebaAlertaRedistribucion` | RN16 | 10 % → `VACIA`; 100 % → `SATURADA`; no se repiten | 2 alertas: `VACIA` y `SATURADA` | APROBADO |
| TU-13 | `pruebaOrdenamiento` | Comparable / Comparator | Por código: B1, B2, B3. Por fallas: B2, B3, B1 | Como se esperaba | APROBADO |
| TU-14 | `pruebaPersistencia` | Entrada/salida | Lo guardado se recupera y las relaciones se mantienen | Usuario con saldo y vehículo en su estación | APROBADO |

> **Resultado:** 14 pruebas, 35 verificaciones, **35 aprobadas (100 %)**, 0 fallas.

## 3. Pruebas funcionales (interfaz Swing)

Antes de empezar se borra la carpeta `datos` para partir de los datos de prueba descriptos en el README.

### CU-01: Registrar usuario (RF01)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-01 | Usuarios → Nuevo usuario: "Pedro Gil", "40111222", "pedro@mail.com" | Aparece en la tabla con saldo $0, habilitado | | |
| PF-02 | Nuevo usuario con documento 30111222 | Error: "Ya existe un usuario con el documento 30111222" | | |
| PF-03 | Nuevo usuario con email "pedro" (sin @) | Error: "Completá el nombre, el documento y un email válido." | | |

### CU-02: Medios de pago y recarga (RF02, RF03, RF04)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-04 | Elegir a Sofía → Recargar saldo | Error: "El usuario no tiene medios de pago." | | |
| PF-05 | Sofía → Agregar medio de pago: tarjeta "123" | Error: "Ingresá los últimos 4 dígitos de la tarjeta." | | |
| PF-06 | Sofía → Agregar billetera "sofi.mp" → Recargar $2.000 | Saldo de Sofía: $2.000 | | |
| PF-07 | Recargar con importe "abc" | Error: "El importe tiene que ser un número." | | |
| PF-08 | Ana → Ver movimientos | Una recarga de $5.000 con tarjeta terminada en 4321 | | |

### CU-03: Plan mensual (RF05)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-09 | Juan → Suscribir al plan | Error: "Se necesitan $8000.0 para suscribirse al plan" | | |
| PF-10 | Recargar $10.000 a Sofía → Suscribir al plan | Plan: "Sí (0 viajes usados)", saldo $4.000 | | |

### CU-04: Retirar vehículo (RF06, diagrama de secuencia 1)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-11 | Viajes → Retirar: Juan, E1, BC01 | Error: "Retiro rechazado: Saldo insuficiente: se necesitan al menos $800.0" (RN01) | | |
| PF-12 | Retirar: cambiar la estación a E2 | La lista de vehículos se actualiza; BE02 no aparece (batería baja, RN08) | | |
| PF-13 | Retirar: Ana, E1, BC01 | "Retiro confirmado: viaje 1 iniciado"; viaje `EN_CURSO`; en Vehículos, BC01 `EN_USO` y sin estación | | |
| PF-14 | Retirar: Ana, E1, BC02 | Error: "El usuario ya tiene un viaje en curso" (RN02) | | |

### CU-05: Devolver vehículo (RF08, RF10, diagrama de secuencia 2)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-15 | Devolver: Ana en E3 | Error: "La estación E3 está llena: tiene 10 minutos sin cargo para llegar a otra" (RN13, RN15) | | |
| PF-16 | Devolver: Ana en E2 (antes de 15 minutos) | "Devolución confirmada. Importe debitado: $ 0,00"; promoción "Primer viaje gratis" (RN21) | | |
| PF-17 | Retirar con Sofía (plan) y devolver | Promoción "Plan mensual"; en Usuarios: "Sí (1 viajes usados)" (RN22) | | |

### CU-06: Cancelar retiro (RF07)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-18 | Ana retira BC02 y antes de 2 minutos: seleccionar viaje → Cancelar | "Viaje cancelado sin cargo"; BC02 vuelve a E1 (RN18) | | |
| PF-19 | Seleccionar un viaje `FINALIZADO` → Cancelar | Error: "Solo se puede cancelar un viaje en curso." | | |

### CU-07: Nota de ajuste (RF14)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-20 | Viaje 1 → Nota de ajuste: Martín, +500, "Cobro duplicado" | Saldo de Ana aumenta $500; el viaje no cambia (RN24) | | |
| PF-21 | Nota de ajuste con importe 0 | Error: "El importe no puede ser cero y el motivo es obligatorio." | | |

### CU-08: Vehículos (RF11, RF12, RF21)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-22 | Nuevo vehículo: bicicleta común "BC01" | Error: "Ya existe un vehículo con el código BC01" | | |
| PF-23 | Nuevo vehículo: monopatín "MO03", E3, 80, 25 | Error: "La estación E3 está llena" | | |
| PF-24 | Nuevo vehículo: bicicleta eléctrica "BE03", E1, 50, 60 | Aparece `DISPONIBLE` en E1 con 50 % de batería | | |
| PF-25 | Seleccionar BC03 → Recargar batería | Error: "El vehículo BC03 no tiene batería" (RN09) | | |
| PF-26 | Seleccionar BE02 → Recargar batería | Pasa de `BATERIA_BAJA` a `DISPONIBLE`, 100 % | | |
| PF-27 | Seleccionar BC03 → Cambiar estado → `FUERA_DE_SERVICIO` | Estado `FUERA_DE_SERVICIO` | | |

### CU-09: Mantenimiento (RF18, RF19, RF20)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-28 | BC02 → Reportar falla 3 veces (Ana, Martín, Ana) | A la tercera: `EN_REPARACION` y orden abierta para Carlos Gómez (RN10) | | |
| PF-29 | Seleccionar la orden → Cerrar sin tareas | Error: "No se puede cerrar la orden: No se registraron tareas realizadas" (RN11) | | |
| PF-30 | Cerrar la orden con tareas "cambio de freno" | BC02 vuelve a `DISPONIBLE`; la orden muestra la fecha de cierre | | |
| PF-31 | MO02 → Generar orden: Martín, Lucía | MO02 `EN_REPARACION`, orden abierta para Lucía Fernández | | |

### CU-10: Estaciones y traslados (RF15, RF22, RF23)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-32 | Estaciones → Nueva estación con código E1 | Error: "Ya existe una estación con el código E1" | | |
| PF-33 | Trasladar: origen y destino E1 | Error: "El origen y el destino tienen que ser distintos." | | |
| PF-34 | Trasladar: E1 → E3 (llena), un vehículo | Error: "La estación E3 solo tiene 0 lugares libres" | | |
| PF-35 | Trasladar: E3 → E1, BC05 | Traslado registrado; E3 baja a 2/3; aparece en "Ver traslados" | | |
| PF-36 | Ver la tabla de alertas | Alertas `VACIA` (E1, E2) y `SATURADA` (E3) (RN16) | | |

### CU-11: Reportes (RF25, RF26, RF27)

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-37 | Reportes → Viajes por usuario | Cantidad de viajes y total pagado por cada usuario | | |
| PF-38 | Reportes → Vehículos con más fallas | BC02 primero (3 fallas); los empatados, por código | | |
| PF-39 | Reportes → Uso por estación | Cantidad de retiros y devoluciones por estación | | |

### CU-12: Persistencia

| Caso | Acción | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|:---:|
| PF-40 | Cerrar la ventana y volver a ejecutar | Se conservan usuarios, saldos, viajes, órdenes y traslados | | |