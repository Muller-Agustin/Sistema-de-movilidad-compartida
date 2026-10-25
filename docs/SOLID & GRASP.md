# TPO POO --- Punto 5: SOLID y GRASP

## Introducción

Aplicamos 4 patrones GRASP (Experto en información, Controlador,
Polimorfismo y Fabricación pura) y 3 principios SOLID (Responsabilidad
única, Abierto/Cerrado e Inversión de dependencias). Cada uno parte de
un problema concreto del diagrama de clases inicial y se refleja en el
diagrama de clases final.

## GRASP 1: Experto en información

Cada regla de negocio la resuelve la clase que tiene los datos
necesarios para resolverla.

-   **Problema:** Las reglas de retiro, devolución, tarifa y fallas
    dependen de datos repartidos en varias clases. Si una clase externa
    las verifica, tiene que pedirle a cada objeto su estado interno, y
    la misma regla termina repetida en varios lugares.
-   **Decisión:** Asignamos cada verificación a la clase que posee la
    información. Los demás objetos solo le preguntan el resultado.
-   **Clases:**
    -   `Vehiculo.puedeRetirarse()`: conoce el estado del vehículo
        (RN07).
    -   `VehiculoElectrico`: sobrescribe el método para controlar la
        batería (RN08).
    -   `Vehiculo.contarFallasRecientes()`: conoce sus reportes de falla
        (RN10).
    -   `Estacion.estaLlena()` y `Estacion.getPorcentajeOcupacion()`:
        conocen la capacidad y los vehículos guardados (RN13, RN16).
    -   `Tarifa.calcularCosto()`: conoce el desbloqueo, el precio por
        minuto y el recargo (RN17, RN19).
    -   `Cuenta.tieneSaldoSuficiente()`: conoce el saldo (RN01).
-   **Ventaja:** Cada regla vive en un solo lugar, así que un cambio en
    ella se hace en una sola clase. Baja el acoplamiento, porque nadie
    necesita acceder a los atributos privados de otra clase, y sube la
    cohesión.

## GRASP 2: Controlador

Los pedidos del usuario los recibe un controlador, que coordina a los
objetos del dominio sin ejecutar él la lógica de negocio.

-   **Problema:** En el diagrama inicial, `Usuario.iniciarViaje()` y
    `Usuario.finalizarViaje()` coordinan vehículo, estación, tarifa,
    promociones y saldo. `Usuario` queda sobrecargado, y el menú tendría
    que conocer muchas clases del dominio para atender un solo pedido.
-   **Decisión:** Creamos controladores que reciben los eventos del
    sistema desde el menú y delegan cada paso en el experto que
    corresponde. Por ejemplo, para retirar un vehículo el controlador le
    pregunta a `Usuario` si puede retirar, a `Vehiculo` si está
    disponible y a `Estacion` que lo libere, y después crea el `Viaje`.
-   **Clases:**
    -   `ControladorViajes`: retirar, cancelar y devolver vehículo.
    -   `ControladorUsuarios`: registro, medios de pago y recargas.
    -   `ControladorMantenimiento`: reportes de falla y órdenes.
    -   `ControladorEstaciones`: traslados y alertas.

    Son los que aparecen en los diagramas de secuencia de retirar y
    devolver vehículo.
-   **Ventaja:** El menú depende solo de los controladores, no del
    dominio. `Usuario` vuelve a tener solo responsabilidades propias de
    un usuario, y cada caso de uso tiene un punto de entrada claro.

## GRASP 3: Polimorfismo

Cuando el comportamiento cambia según el tipo, cada tipo implementa su
propia versión del método.

-   **Problema:** En el diagrama inicial,
    `Promocion.calcularDescuento()` decide qué hacer con un `switch`
    sobre el enum `TipoPromocion` (primer viaje, plan mensual, estación
    con baja ocupación). Cada tipo nuevo obliga a agregar un caso al
    `switch` y a modificar una clase que ya funciona.
-   **Decisión:** `Promocion` pasa a ser una clase abstracta con
    `esAplicable(v : Viaje)` y `calcularDescuento(v : Viaje)`
    abstractos. Cada promoción es una subclase que los sobrescribe. El
    enum `TipoPromocion` desaparece.
-   **Clases:**
    -   `Promocion` (abstracta).
    -   `PromoPrimerViaje`: gratis hasta 15 minutos (RN21).
    -   `PromoPlanMensual`: hasta 30 minutos (RN22).
    -   `PromoEstacionBajaOcupacion`: descuento por devolver en una
        estación con baja ocupación.
    -   `Viaje.aplicarMejorPromocion()`: recorre la lista de promociones
        vigentes y elige la de menor costo para el usuario sin preguntar
        de qué tipo es cada una (RN23, S7).
-   **Ventaja:** Se eliminan los condicionales por tipo. Cada promoción
    tiene su regla en su propia clase, y `Viaje` trata a todas por
    igual.

## GRASP 4: Fabricación pura

Para las responsabilidades técnicas que no le corresponden a ninguna
clase del dominio, creamos clases de diseño.

-   **Problema:** El sistema necesita guardar datos en archivos, armar
    reportes, exportarlos a CSV y simular el avance del tiempo. Ninguna
    clase del dominio es una buena candidata. Si `Usuario` se guardara a
    sí mismo en un archivo, o `Administrador` armara los textos de los
    reportes, mezclarían lógica de negocio con tareas técnicas.
-   **Decisión:** Creamos clases que no representan nada del mundo real,
    pero ordenan el diseño.
-   **Clases:**
    -   `RepositorioUsuarios`, `RepositorioVehiculos`,
        `RepositorioEstaciones` y `RepositorioViajes`: guardan y cargan
        los datos entre ejecuciones.
    -   `GeneradorReportes`: arma los reportes de viajes por usuario,
        uso por estación, vehículos con más fallas y reparaciones.
    -   `ExportadorCSV`: escribe esos reportes en archivos CSV.
    -   `Reloj`: da la fecha y hora del sistema y permite simular su
        avance para probar las reglas temporales (2 minutos, 2 horas, 24
        horas, 30 días).
-   **Ventaja:** Las clases del dominio quedan enfocadas en las reglas
    de negocio. La persistencia, los reportes y el tiempo se pueden
    cambiar o probar por separado, sin tocar el dominio.

## SOLID 1: Responsabilidad única (S)

Cada clase tiene un solo motivo para cambiar.

-   **Problema:** En el diagrama inicial, `Administrador` define
    tarifas, crea promociones y además arma cuatro reportes con su
    formato. Cambiar el formato de un reporte, o agregar uno nuevo,
    obliga a modificar la misma clase que gestiona tarifas y
    promociones. Algo parecido pasa con `Usuario`, que además de sus
    datos coordina el ciclo completo de un viaje.
-   **Decisión:** Separamos las responsabilidades. `Administrador`
    conserva solo lo que le corresponde como personal: definir tarifas y
    promociones. Los reportes pasan a `GeneradorReportes` y su
    exportación a `ExportadorCSV`. La coordinación del viaje pasa a
    `ControladorViajes`.
-   **Clases:** `Administrador`, `GeneradorReportes`, `ExportadorCSV`,
    `Usuario` y `ControladorViajes`.
-   **Ventaja:** Cada cambio afecta a una sola clase. Por ejemplo,
    agregar el reporte de reparaciones (RF28) solo toca
    `GeneradorReportes`. Las clases quedan más chicas y más fáciles de
    entender y de probar.

## SOLID 2: Abierto/Cerrado (O)

El diseño está abierto a extensiones y cerrado a modificaciones.

-   **Problema:** Con el `switch` sobre `TipoPromocion` del diagrama
    inicial, agregar una promoción nueva (por ejemplo, un descuento por
    viaje nocturno) obliga a modificar el enum y
    `Promocion.calcularDescuento()`, que ya estaban probados. Cada
    cambio puede romper las promociones existentes.
-   **Decisión:** Usamos la jerarquía de promociones descrita en
    Polimorfismo. Para agregar una promoción se crea una subclase nueva
    de `Promocion`, sin tocar las existentes ni `Viaje`.
-   **Clases:** `Promocion`, `PromoPrimerViaje`, `PromoPlanMensual`,
    `PromoEstacionBajaOcupacion` y `Viaje`.
-   **Ventaja:** El sistema crece agregando clases, no editando código
    que ya funciona. Las pruebas de las promociones existentes siguen
    siendo válidas cuando se agrega una nueva. Es el cambio principal
    entre el diagrama inicial y el final.

## SOLID 3: Inversión de dependencias (D)

Las clases de alto nivel dependen de abstracciones (interfaces), no de
clases concretas.

-   **Problema:** Si `ControladorViajes` usara directamente una clase
    que lee y escribe archivos de texto, quedaría atado a ese formato.
    Cambiar cómo se guardan los datos obligaría a modificar el
    controlador, y para probarlo con pruebas unitarias habría que crear
    archivos reales.
-   **Decisión:** Definimos interfaces para lo que puede variar. Los
    controladores reciben esas interfaces en su constructor y no conocen
    la implementación concreta.
-   **Clases:**
    -   Las interfaces `RepositorioUsuarios`, `RepositorioVehiculos`,
        `RepositorioEstaciones` y `RepositorioViajes`, implementadas por
        `RepositorioUsuariosArchivo`, `RepositorioVehiculosArchivo`,
        etc.
    -   `ControladorViajes` depende de esas interfaces y de la
        abstracción `Promocion`, no de una promoción concreta.
-   **Ventaja:** Se puede cambiar el formato de guardado sin tocar la
    lógica. En las pruebas unitarias se usan repositorios en memoria,
    así que se prueban las reglas de negocio sin depender de archivos.

## Resumen

Los principios se apoyan entre sí: Polimorfismo es la herramienta con la
que cumplimos Abierto/Cerrado, y Fabricación pura es la que permite
aplicar Responsabilidad única e Inversión de dependencias.

  -----------------------------------------------------------------------------------
  Principio         Tipo              Problema en el    Clases en el diagrama final
                                      diagrama inicial  
  ----------------- ----------------- ----------------- -----------------------------
  Experto en        GRASP             Reglas que        `Vehiculo`,
  información                         dependen de datos `VehiculoElectrico`,
                                      de varias clases  `Estacion`, `Tarifa`,
                                                        `Cuenta`

  Controlador       GRASP             `Usuario`         `ControladorViajes`,
                                      coordina todo el  `ControladorUsuarios`,
                                      viaje             `ControladorMantenimiento`,
                                                        `ControladorEstaciones`

  Polimorfismo      GRASP             `switch` sobre    `Promocion` y sus subclases
                                      `TipoPromocion`   

  Fabricación pura  GRASP             Persistencia,     Repositorios,
                                      reportes y tiempo `GeneradorReportes`,
                                      sin dueño         `ExportadorCSV`, `Reloj`

  Responsabilidad   SOLID             `Administrador`   `Administrador`,
  única                               gestiona y además `GeneradorReportes`
                                      arma reportes     

  Abierto/Cerrado   SOLID             Una promoción     Jerarquía de `Promocion`
                                      nueva obliga a    
                                      editar código     
                                      existente         

  Inversión de      SOLID             Controladores     Interfaces `RepositorioXxx` y
  dependencias                        atados a archivos sus implementaciones
                                      concretos         
  -----------------------------------------------------------------------------------
