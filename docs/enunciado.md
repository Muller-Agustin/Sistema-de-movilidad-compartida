# Propuesta C: Sistema de movilidad compartida (bicicletas y monopatines)

## Contexto

Un municipio lanza un servicio de bicicletas y monopatines eléctricos compartidos, distribuidos en estaciones de la ciudad.

## Actores

- Usuario
- Operador del sistema
- Técnico de mantenimiento
- Administrador

## Casos de uso mínimos

- Registrar usuarios y gestionar su saldo o medio de pago.
- Retirar y devolver un vehículo en una estación, registrando el inicio, el fin y el recorrido del viaje.
- Calcular la tarifa según el tipo de vehículo, el tiempo de uso y las promociones vigentes.
- Gestionar el estado de los vehículos (disponible, en uso, en reparación, batería baja) y generar órdenes de mantenimiento.
- Redistribuir vehículos entre estaciones cuando una queda vacía o saturada.
- Emitir reportes (viajes por usuario, uso por estación, vehículos con más fallas).

## Reglas de negocio

> Las reglas son de cumplimiento obligatorio y los valores numéricos son de referencia: el grupo puede ajustarlos si deja el supuesto documentado.

### Usuarios y saldo

- Un usuario solo puede retirar un vehículo si su saldo alcanza el mínimo establecido (por ejemplo, el equivalente a 30 minutos de uso) y si no tiene otro viaje abierto.
- Si al terminar un viaje el saldo queda negativo, el usuario queda inhabilitado hasta regularizarlo.

### Vehículos

- Hay tres tipos: bicicleta común, bicicleta eléctrica y monopatín eléctrico. Los estados son disponible, en uso, en reparación, batería baja y fuera de servicio.
- Solo un vehículo disponible puede retirarse. Los eléctricos con batería menor al 15 % pasan a batería baja y no se ofrecen. Las bicicletas comunes no tienen batería.
- Un vehículo con 3 reportes de falla en 30 días pasa a mantenimiento obligatorio.
- Una orden de mantenimiento no se cierra sin registrar el técnico, la fecha y las tareas realizadas.

### Estaciones

- Cada estación tiene una capacidad fija, y no se puede devolver un vehículo en una estación llena ni fuera de una estación.
- Si el usuario encuentra la estación llena, dispone de 10 minutos adicionales sin cargo para llegar a otra cercana.
- Una estación con menos del 20 % o más del 90 % de ocupación genera una alerta de redistribución para el operador.

### Viajes y tarifas

- La tarifa es un cargo de desbloqueo más un precio por minuto según el tipo de vehículo. Valores de referencia:

| Tipo de vehículo | Desbloqueo | Precio por minuto |
|---|---|---|
| Bicicleta común | $200 | $20 |
| Bicicleta eléctrica | $300 | $45 |
| Monopatín eléctrico | $300 | $50 |

- Un retiro cancelado dentro de los 2 minutos no tiene cargo.
- El tiempo máximo de un viaje es de 2 horas. Pasado ese límite, el minuto se cobra con un recargo del 50 %. Si el vehículo no se devuelve en 24 horas, se lo reporta como perdido y se aplica un cargo fijo.
- Promociones: el primer viaje de cada usuario es gratis hasta 15 minutos, y el plan mensual incluye 20 viajes de hasta 30 minutos. No se acumulan: se aplica la más favorable para el usuario.
- Un viaje cerrado no se modifica. Cualquier corrección la hace el operador con una nota de ajuste.

## Enunciado para el modelado

Un municipio pone en marcha un servicio público de bicicletas y monopatines compartidos para trasladarse por la ciudad. El servicio funciona con estaciones, que son puntos fijos donde los vehículos se retiran y se devuelven. Cada estación tiene un código, una dirección, sus coordenadas y una capacidad máxima de vehículos.

Los vehículos pueden ser de tres tipos: bicicletas comunes, bicicletas eléctricas y monopatines eléctricos. De todos se registra un código, la fecha de alta, los kilómetros recorridos y su estado. Las bicicletas comunes tienen además el tamaño de rodado; las bicicletas eléctricas tienen un nivel de batería y una autonomía; los monopatines eléctricos tienen un nivel de batería y una velocidad máxima. Un vehículo está guardado en una estación o bien en uso en un viaje.

Las personas que quieren usar el servicio se registran como usuarios con su nombre, documento, correo electrónico y fecha de alta. Cada usuario tiene una cuenta con un saldo, que se recarga con medios de pago asociados (tarjeta o billetera virtual) y se debita con cada viaje. Los movimientos de la cuenta, tanto las recargas como los débitos, quedan registrados con su fecha e importe. Un usuario puede suscribirse además a un plan mensual.

Para usar un vehículo, el usuario lo retira en una estación y, al terminar, lo devuelve en la misma o en otra. Cada viaje registra el usuario, el vehículo, la estación de retiro, la estación de devolución, el momento de inicio y de fin, la distancia recorrida y el estado del viaje. Mientras el viaje está en curso se registran los puntos del recorrido, cada uno con sus coordenadas y la hora.

El precio del viaje depende de una tarifa. Para cada tipo de vehículo existe una tarifa vigente desde una fecha, con un cargo de desbloqueo y un precio por minuto. Además, el municipio define promociones, que tienen un nombre, un período de vigencia y condiciones de aplicación, y que pueden reducir o anular el costo de un viaje.

Tanto los usuarios como los operadores pueden reportar una falla de un vehículo, indicando una descripción y la fecha. A partir de los reportes, el operador genera órdenes de mantenimiento asignadas a un técnico. De cada orden se registra la fecha de apertura, las tareas realizadas, los repuestos utilizados y la fecha de cierre. Un vehículo puede tener muchas órdenes a lo largo del tiempo.

El personal del servicio se compone de operadores, técnicos y un administrador, y de todos se conoce el nombre, el legajo y el teléfono. Los operadores se encargan de redistribuir vehículos entre estaciones: cada traslado indica el origen, el destino, los vehículos movidos y el operador responsable.

El administrador debe poder emitir reportes de viajes por usuario, de uso por estación y de vehículos con más fallas. Todo dato necesario que no figure en este enunciado debe completarse como un supuesto y quedar documentado.