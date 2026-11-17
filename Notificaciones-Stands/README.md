# Notificaciones-Stands

Sistema administrativo de venta de stands de la FILEY, con módulo de notificaciones y lectura de la base de datos con JDBC. Se opera por línea de comandos.

- Requisitos, criterios de aceptación y excepciones: [`docs/requisitos.md`](docs/requisitos.md)
- Diagrama de clases (paquetes del sistema): [`docs/Diagrama-de-Clases-paquetes.jpg`](docs/Diagrama-de-Clases-paquetes.jpg)
- Diagrama de clases (capa de base de datos): [`docs/Diagrama-de-Clases-bd.png`](docs/Diagrama-de-Clases-bd.png)
- Diagrama Entidad-Relación: [`docs/Diagrama ER.png`](docs/Diagrama ER.jpeg)


## Estructura

```
src/main/java/mx/filey/stands/
├── datos/            Stand, EstadoStand, Editorial, Reservacion, Pago, Repositorio, RepositorioEnMemoria
├── configuracion/    Configuracion (lee configuracion.properties)
├── negocio/          ServicioStands, ServicioReservaciones, ServicioTransferencias,
│                     CalculadoraPrecios, Reloj, RelojSimulado, NegocioException
├── notificaciones/   ServicioNotificaciones, Notificacion, TipoNotificacion, Notificador, NotificadorConsola
├── bd/               ConexionBD, InicializadorBD, StandDAO, EditorialDAO, ReservacionDAO, ErrorBaseDatos
└── app/              Main (simulación), MainBD (demo de la base de datos), DatosDemo
src/main/resources/configuracion.properties
src/main/resources/bd/schema.sql, datos.sql    Tablas y datos de ejemplo
src/test/java/...     Pruebas JUnit 5, un paquete por cada paquete de producción
bd.properties.ejemplo                          Plantilla de conexión (copiar como bd.properties)
```

## Arquitectura: MVC + capa de base de datos

| Capa | Paquete | Qué hace |
|---|---|---|
| Modelo | `datos` | Entidades `Stand`, `Editorial`, `Reservacion` y la interfaz `Repositorio<T>` |
| Controlador | `negocio` | Lógica de negocio. Recibe un `Repositorio<T>`: no sabe si los datos vienen de memoria o de la base de datos |
| Vista | `app` | Consola: `Main` (simulación) y `MainBD` (demo de la base de datos) |
| Base de datos | `bd` | Todo lo que usa JDBC: `ConexionBD`, `InicializadorBD` y un DAO por tabla (`StandDAO`, `EditorialDAO`, `ReservacionDAO`) que implementan `Repositorio<T>` |

## Requisitos para ejecutar

- JDK 17 o superior
- Maven 3.8 o superior
- Opcional: una base PostgreSQL en la nube (Neon). Sin ella, el programa usa SQLite local.

## Comandos

```bash
mvn test                       # ejecuta las 158 pruebas
mvn package                    # compila y genera target/notificaciones-stands-1.0.0.jar
java -jar target/notificaciones-stands-1.0.0.jar     # simulación (Main)
mvn compile exec:java@mainbd   # demo de la base de datos (MainBD)
```

Ejecuta los comandos desde la carpeta que contiene el `pom.xml`.

## Simulación (Main)

La consola es solo de **simulación y auditoría**: no hay altas manuales. Al iniciar, `DatosDemo` precarga 8 stands, 4 editoriales y 6 reservaciones con fechas distintas (con pago que ocupa el stand, con pago parcial, sin pago, reciente y recién creadas) y el administrador bloquea un stand (B-03). Menú:

1. Ver mapa de stands y reservaciones activas (vigencia, monto pagado y porcentaje).
2. Avanzar la fecha del sistema: avanza día por día y, en cada fecha, libera las reservas vencidas y envía los avisos de cada editorial según la vigencia de su propia reserva.
3. Revisar vencimientos y enviar avisos en la fecha actual.

La simulación trabaja con datos en memoria: lo que ocurre al avanzar los días no se guarda en la base de datos.

## Base de datos (MainBD)

`MainBD` demuestra la conexión a la base de datos y presenta las listas de las tres tablas (`stands`, `editoriales` y `reservaciones`). Si las tablas no existen, las crea y carga los datos de ejemplo; después comprueba que `ServicioStands` (negocio) lee los stands a través de `StandDAO`.

- Esquema y datos de ejemplo: [`schema.sql`](src/main/resources/bd/schema.sql) y [`datos.sql`](src/main/resources/bd/datos.sql)
- Una editorial y un stand pueden tener varias reservaciones (ver el diagrama Entidad-Relación).
- **Alcance de esta fase:** solo lectura. Los pagos no están en la base de datos, así que una reservación leída de ella llega sin pagos y no se calculan descuentos ni saldos sobre ella.

### Conexión

`ConexionBD` busca la configuración en este orden:

1. Variables de entorno `DB_URL`, `DB_USUARIO` y `DB_PASSWORD`.
2. Archivo `bd.properties` junto al `pom.xml` (copia `bd.properties.ejemplo`). **No se sube al repositorio.**
3. Si no hay nada, usa **SQLite** en el archivo local `stands.db`.

Al ejecutar `mvn compile exec:java@mainbd`, la línea `Tipo :` indica a qué base se conectó (`PostgreSQL` o `SQLite`). La contraseña nunca se imprime.

## Configuración

Edita `src/main/resources/configuracion.properties`: precio por m², vigencia de la reserva, porcentaje mínimo para ocupar, descuento por pronto pago, fechas límite y días de anticipación de los avisos.

Las credenciales de la base de datos van en `bd.properties` (o en variables de entorno), nunca en el código.
