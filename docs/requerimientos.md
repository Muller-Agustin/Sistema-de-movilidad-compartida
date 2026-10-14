# Requerimientos

## Actores

- **Usuario:** Se registra, recarga saldo, retira y devuelve los vehículos, reporta fallas.
- **Operador del sistema:** Redistribuye los vehículos, genera órdenes de mantenimiento, ajusta los viajes.
- **Técnico de mantenimiento:** Ejecuta y cierra órdenes de mantenimiento.
- **Administrador:** Define las tarifas y promociones, emite reportes.

## Requerimientos funcionales (RF)

- **RF01:** Registrar usuario con nombre, dni, mail. Se crea con saldo 0
- **RF02:** Agregar un medio de pago
- **RF03:** Recargar saldo usando un medio de pago registrado
- **RF04:** Consultar saldo y el historial de movimientos
- **RF05:** Suscribirse al plan mensual
- **RF06:** Retirar vehículo disponible, iniciando un viaje
- **RF07:** Cancelar el retiro de un vehículo
- **RF08:** Devolver vehículo, finalizando un viaje y actualizando los kilómetros del vehículo
- **RF09:** Registrar coordenadas y hora durante el transcurso de un viaje
- **RF10:** Calcular tarifa al finalizar un viaje según el vehículo, el tiempo de uso y las promociones vigentes descontando del saldo del usuario
- **RF11:** Registrar un vehículo en una estación con tipo, código, fecha de alta y sus datos propios según el tipo (rodado, batería, autonomía, velocidad máxima). Se crea disponible y con kilómetros 0
- **RF12:** Consultar y cambiar estado de un vehículo
- **RF13:** Detectar viajes abiertos por más de 24 horas y reportar el vehículo como perdido
- **RF14:** Registrar una nota de ajuste sobre un viaje cerrado
- **RF15:** Registrar una estación con código, dirección, coordenadas y capacidad
- **RF16:** Definir tarifa para cada tipo de vehículo con cargo de desbloqueo, precio por minuto y fecha de vigencia
- **RF17:** Definir promociones con nombre, periodo de vigencia y condiciones
- **RF18:** Reportar una falla de un vehículo con fecha y descripción
- **RF19:** Generar una orden de mantenimiento y asignarla a un técnico
- **RF20:** Cerrar una orden de mantenimiento indicando fecha, técnico, tareas y repuestos usados
- **RF21:** Registrar la recarga de batería de un vehículo eléctrico
- **RF22:** Generar alerta de redistribución indicando estación vacía (menos del 20%) o saturada (más del 90%)
- **RF23:** Registrar una redistribución de vehículo con origen, destino, vehículos movidos, fecha y operador responsable
- **RF24:** Dar de alta personal del sistema con nombre, rol, legajo y teléfono
- **RF25:** Emitir reporte de viajes por usuario
- **RF26:** Emitir reporte de vehículos con más fallas, con sus viajes y kilómetros recorridos
- **RF27:** Emitir reporte de uso por estación (retiros y devoluciones)
- **RF28:** Emitir reporte de reparaciones según vehículo y orden de mantenimiento cerrada
- **RF29:** Consultar estaciones con vehículos disponibles ordenadas por cercanía
- **RF30:** Consultar el historial de viajes propio
- **RF31:** Consultar las órdenes de mantenimiento abiertas asignadas a un técnico
- **RF32:** Consultar las órdenes de mantenimiento cerradas de un técnico
- **RF29:** Simular el avance del tiempo del sistema para verificar reglas temporales
- **RF30:** Aplicar un descuento al devolver un vehículo en una estación con baja ocupación
- **RF31:** Asignar automáticamente una orden de mantenimiento al técnico con menos órdenes abiertas
- **RF32:** Exportar los reportes a un archivo CSV

## Reglas de negocio (RN)

### Usuarios y Saldos

- **RN01:** Un usuario solo puede retirar un vehículo si su saldo alcanza el mínimo establecido, equivalente a 30 minutos de uso del tipo de vehículo elegido.
- **RN02:** Un usuario no puede retirar su vehículo si tiene un viaje en curso.
- **RN03:** Si al cerrar un viaje el saldo queda negativo, el usuario pasa a estado inhabilitado hasta regularizar su saldo.
- **RN04:** Un usuario inhabilitado no puede retirar vehículos.

### Vehículo

- **RN05:** Existen tres tipos de vehículo: Bicicleta común, Bicicleta eléctrica y Monopatín eléctrico.
- **RN06:** Un vehículo puede estar en uno de los estados: "Disponible", "En uso", "En reparación", "Batería baja", "Fuera de servicio".
- **RN07:** Solo puede retirarse un vehículo en estado disponible.
- **RN08:** Un vehículo eléctrico con batería menor al 15% pasa a estado batería baja y no se ofrece para retiro.
- **RN09:** Las bicicletas comunes no tienen batería, por lo que RN08 no se les aplica.
- **RN10:** Un vehículo que acumula 3 reportes de falla en los últimos 30 días pasa a mantenimiento obligatorio (Estado: "En reparación").
- **RN11:** Una orden de mantenimiento no puede cerrarse sin registrar técnico, fecha de cierre y tareas realizadas.
- **RN12:** Un vehículo está guardado en una estación o en uso en un viaje, nunca en ambos ni en ninguno.

### Estaciones

- **RN13:** Cada estación tiene una capacidad máxima fija. No se puede devolver un vehículo en una estación llena.
- **RN14:** No se puede devolver un vehículo fuera de una estación.
- **RN15:** Si el usuario encuentra la estación llena, dispone de 10 minutos adicionales sin cargos para llegar a otra estación.
- **RN16:** Una estación con ocupación menor al 20% o mayor al 90% genera una alerta de redistribución para el operador.

### Viajes y tarifas

- **RN17:** La tarifa es el cargo de desbloqueo más el precio por minuto según el tipo de vehículo:

  | Tipo de vehículo    | Desbloqueo | Precio por minuto |
  |---------------------|------------|-------------------|
  | Bicicleta común     | $200       | $20/min           |
  | Bicicleta eléctrica | $300       | $45/min           |
  | Monopatín           | $300       | $50/min           |

- **RN18:** Un retiro cancelado dentro de los 2 minutos no tiene cargo.
- **RN19:** El tiempo máximo de un viaje es de 2 horas. Cada minuto excedente se cobra con un recargo del 50%.
- **RN20:** Si un vehículo no se devuelve en 24 horas, se lo reporta como perdido y se aplica un cargo fijo al usuario.
- **RN21:** El primer viaje de cada usuario es gratis hasta 15 minutos.
- **RN22:** El plan mensual incluye 20 viajes de hasta 30 minutos cada uno.
- **RN23:** Las promociones no se acumulan: se aplica la más favorable para el usuario.
- **RN24:** Un viaje cerrado no se modifica. Cualquier corrección la realiza un operador mediante una nota de ajuste.

## Supuestos (S)

| ID  | Ambigüedad | Supuesto  |
|-----|------------|-------------------|
| S1  | "Saldo mínimo equivalente a 30 minutos" | Desbloqueo + 30 min al precio del tipo de vehículo que se quiere retirar |
| S2  | Hay 4 estados en los casos de uso y 5 en las reglas | Se usan los 5. "Fuera de servicio" se usa para vehículos perdidos |
| S3  | "3 reportes en 30 días" | Ventana de 30 días hacia atrás desde el último reporte. Al llegar a 3, se genera una orden automática y pasa a "En reparación" |
| S4  | No se define el cargo fijo por vehículo perdido | Valor de referencia $20.000 |
| S5  | Recargo del 50% pasado el límite de las 2h | El recargo es sobre los minutos que superan las 2 horas |
| S6  | Minutos excedentes en una promoción | Primer viaje: gratis hasta 15 min incluyendo el desbloqueo, el excedente se cobra a tarifa normal. Plan mensual: igual, hasta 30 min |
| S7  | Cómo se aplica "la más favorable" | Se calcula el costo con cada promoción vigente y se aplica la de menor costo |
| S8  | Cómo se descuentan los 10 min de estación llena | Desde el intento fallido de devolución, ese tiempo no se cobra |
| S9  | Cómo se mide la ocupación para la alerta | Vehículos guardados ÷ capacidad, evaluada en cada retiro, devolución y traslado |
| S10 | Redondeo del cobro | Minutos redondeados hacia arriba |
| S11 | Selección de tarifa ante varias opciones | La fecha de vigencia más reciente que sea menor o igual al inicio del viaje |
| S12 | Rehabilitación de usuario inhabilitado | Al recargar y dejar el saldo en cero o más |
| S13 | Recargas con tarjeta o billetera virtual | Se simulan, se asumen aprobadas, sin integración real |
| S14 | Autenticación | No hay autenticación, el actor se elige en el menú de la aplicación |
| S15 | Precio y reinicio del plan mensual | Precio de referencia $8.000. El contador de 20 viajes se reinicia cada mes |
| S16 | Cómo se corrige un viaje cerrado | La nota de ajuste genera un movimiento de cuenta (crédito o débito), sin tocar el viaje original |
