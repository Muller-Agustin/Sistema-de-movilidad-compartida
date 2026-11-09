# Informe Final – Etapa 2

**Propuesta C:** Sistema de movilidad compartida

**Integrantes:** Müller, Agustín · Evans Gonzales, Franco · Cozzi Miranda, Franco

## Cambios respecto del diseño original

El detalle completo está en [`cambios-diseno.md`](cambios-diseno.md).

- La persistencia pasó de un repositorio genérico por clase a guardar un único objeto raíz (`SistemaMovilidad`) con `GestorArchivos`.
- El cálculo de la tarifa quedó en `Viaje` y `Tarifa`, siguiendo el diagrama de secuencia de la devolución, y se eliminó `CalculadoraDeTarifa`.
- Se agregaron `SistemaMovilidad` y las pantallas Swing (`VistaMain`).
- Las excepciones de reglas de negocio pasaron a ser verificadas (`extends Exception`).

## Dificultades encontradas

- **Exceso de complejidad en la primera versión del código:** El primer borrador de la Etapa 2 tenía 71 clases y 25 archivos de prueba. Se reorganizó por bloques hasta llegar a 58 clases, incluida la interfaz, más una clase de pruebas.
- **Correspondencia entre los diagramas y el código:** El código inicial ubicaba la lógica de retiro y devolución en clases de servicio, mientras que los diagramas de secuencia la ubicaban en `Usuario` y `Viaje`. Se reescribió para seguir los diagramas.
- **Excepciones .** Al cambiar las excepciones a `extends Exception`, hubo que declarar `throws` en toda la cadena de llamadas hasta la interfaz.
- **Persistencia de objetos relacionados:** Guardar cada lista por separado duplicaba objetos al cargar; se resolvió guardando una sola raíz.


## Distribución de tareas

| Integrante | Etapa 1 | Etapa 2 |
|---|---|---|
| Müller, Agustín | Diagramas de secuencia | Vistas en Swing |
| Evans Gonzales, Franco | Casos de Uso |  |
| Cozzi Miranda, Franco | Diagramas de Clase | Modelado de Vehiculos |

## Reflexión del grupo
Este trabajo nos sirvió para comprender como se trabaja en grupo, en un proyecto grande. Aprendimos a dividir las tareas para poder ser más eficientes. Aprendimos a usar Git y Github de manera fluida. Esto nos sirvio para poder desarrollar partes separadas del trabajo para luego unir todo.
Nos gusto como grupo que la consigna sea un caso realista.