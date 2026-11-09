# Movilidad Compartida 🚲
## Integrantes

| Nombre | Usuario GitHub |
|--------|----------------|
| Müller, Agustín | Muller-Agustin |
| Evans Gonzales, Franco | FrancoEvans |
| Cozzi Miranda, Franco | Francozzii |

## Propuesta C

**Nombre:** Sistema de movilidad compartida (bicicletas y monopatines)

## Descripción del sistema

Aplicación de escritorio para gestionar un servicio municipal de bicicletas y monopatines eléctricos compartidos, distribuidos en estaciones de la ciudad. Está desarrollada en Java con interfaz gráfica Swing: desde un menú lateral se accede a usuarios, viajes, vehículos y mantenimiento, estaciones y reportes.

Los usuarios tienen una cuenta con saldo que recargan con tarjeta o billetera virtual. Al retirar un vehículo se inicia un viaje y al devolverlo se calcula la tarifa según el tipo de vehículo, los minutos de uso y la promoción más favorable (primer viaje gratis o plan mensual). Los datos se conservan entre ejecuciones en un archivo local.

## Funcionalidades

- **Inicio:** indicadores de usuarios, vehículos disponibles, estaciones, viajes en curso, órdenes abiertas y alertas.
- **Usuarios:** alta de usuarios, medios de pago (tarjeta o billetera virtual), recarga de saldo, suscripción al plan mensual y consulta de movimientos.
- **Viajes:** retiro y devolución de vehículos, cancelación dentro de los 2 minutos, registro de puntos del recorrido, notas de ajuste sobre viajes cerrados y detección de vehículos perdidos (más de 24 horas).
- **Vehículos y mantenimiento:** alta de bicicletas comunes, bicicletas eléctricas y monopatines; cambio de estado; recarga de batería; reporte de fallas (con orden automática al llegar a 3 fallas en 30 días); generación y cierre de órdenes de mantenimiento.
- **Estaciones:** alta de estaciones, ocupación, alertas de redistribución (menos del 20 % o más del 90 %) y traslados de vehículos entre estaciones.
- **Reportes:** viajes por usuario, uso por estación y vehículos con más fallas.

Es un prototipo académico: no usa base de datos y los pagos se simulan.

## Requisitos

- **JDK 17 o superior** (se necesita el JDK para compilar; con el JRE solo no alcanza).
- Sistema operativo con entorno gráfico (la interfaz es Swing).
- No usa bibliotecas externas.

Para comprobar la instalación:

```text
java -version
javac -version
```

## Cómo ejecutar

### Windows (doble clic)

- **`Ejecutar.bat`**: compila el proyecto y abre la aplicación.
- **`Pruebas.bat`**: compila el proyecto y ejecuta las pruebas unitarias.

### Desde una terminal

Desde la carpeta raíz del proyecto (la que contiene `src` y este README).

**Windows (PowerShell):**

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 -d out (Get-ChildItem -Path src -Recurse -Filter *.java).FullName
java -cp out VistaMain.Main
```

```bash
javac -encoding UTF-8 -d out $(find src test -name '*.java')
java -cp out PruebasUnitarias
```

### Desde un IDE

1. Abrir la carpeta raíz del proyecto.
2. Marcar `src` como carpeta de código fuente (en IntelliJ: clic derecho → *Mark Directory as → Sources Root*).
3. Configurar un JDK 17 o superior.
4. Ejecutar `src/VistaMain/Main.java`.

## Datos de prueba

La primera vez que se ejecuta (o si se borra la carpeta `datos`), el sistema carga estos datos:

| Dato | Detalle | Sirve para probar |
|------|---------|-------------------|
| Ana López (30111222) | $5.000 de saldo y tarjeta terminada en 4321 | Retiro, devolución y primer viaje gratis |
| Juan Pérez (28999888) | $300 de saldo | Retiro rechazado por saldo insuficiente (mínimo $800) |
| Sofía Ruiz (35444555) | Sin saldo ni medios de pago | Recarga sin medio de pago |
| E1 Plaza de Mayo | Capacidad 10 | Estación con lugar |
| E2 Plaza Italia | Capacidad 8 | Estación con lugar |
| E3 Plaza Francia | Capacidad 3, llena | Devolución en estación llena (10 minutos sin cargo) |
| BE02 | Bicicleta eléctrica con 10 % de batería | No se ofrece para retiro (batería baja) |
| Técnicos | Carlos Gómez (T001), Lucía Fernández (T002) | Órdenes de mantenimiento |
| Operador | Martín Díaz (O001) | Fallas, órdenes, traslados y ajustes |

Tarifas (valores de referencia del enunciado): bicicleta común $200 + $20/min, bicicleta eléctrica $300 + $45/min, monopatín $300 + $50/min. Plan mensual: $8.000.

## Datos guardados

- La información se guarda en `datos/sistema.dat` (formato binario, serialización de Java) al cerrar la ventana principal.
- La ruta es relativa a la carpeta desde la que se ejecuta el programa.
- Si el archivo no existe o no se puede leer, el sistema empieza con los datos de prueba.

## Estructura del repositorio

- `src/VistaMain`: punto de entrada (`Main`) y pantallas Swing.
- `src/UsuariosCuenta`: usuarios, cuentas, movimientos, medios de pago y plan mensual.
- `src/VehiculoMantenimiento`: vehículos, fallas y órdenes de mantenimiento.
- `src/EstacionesTraslados`: estaciones, alertas y traslados.
- `src/ViajesTarifas`: viajes, tarifas y promociones.
- `src/PersonalServicio`: operadores, técnicos y administrador.
- `src/GeneralPersistencia`: `SistemaMovilidad` (raíz del sistema) y `GestorArchivos` (guardado y carga).
- `src/Excepciones`: excepciones de las reglas de negocio.
- `test/PruebasUnitarias.java`: pruebas unitarias.
- `docs/`: enunciado, requerimientos, plan de pruebas, cambios de diseño e informe final.
- `docs/uml/`: diagramas (archivo fuente `.puml` e imagen exportada).