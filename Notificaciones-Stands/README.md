# Notificaciones-Stands

Sistema administrativo de venta de stands de la FILEY, con módulo de notificaciones. Se opera por línea de comandos.

- Requisitos, criterios de aceptación y excepciones: [`docs/requisitos.md`](docs/requisitos.md)

## Estructura

```
src/main/java/mx/filey/stands/
├── datos/            Stand, EstadoStand, Editorial, Reservacion, Pago, Repositorio, RepositorioEnMemoria
├── configuracion/    Configuracion (lee configuracion.properties)
├── negocio/          ServicioStands, ServicioReservaciones, ServicioTransferencias,
│                     CalculadoraPrecios, Reloj, RelojSimulado, NegocioException
├── notificaciones/   ServicioNotificaciones, Notificacion, TipoNotificacion, Notificador, NotificadorConsola
└── app/              Main (menú de consola), DatosDemo
src/main/resources/configuracion.properties
src/test/java/...     Pruebas JUnit 5, un paquete por cada paquete de producción
```

## Requisitos para ejecutar

- JDK 17 o superior
- Maven 3.8 o superior

## Comandos

```bash
mvn test                      # ejecuta las 149 pruebas
mvn package                   # compila y genera target/notificaciones-stands-1.0.0.jar
java -jar target/notificaciones-stands-1.0.0.jar
```

La consola es solo de **simulación y auditoría**: no hay altas manuales. Al iniciar, `DatosDemo` precarga 8 stands, 3 editoriales y 5 reservaciones con fechas distintas (sin pago y por vencer, con pago parcial, con pago que ocupa el stand, reciente y recién creada) y bloquea un stand. Menú:

1. Ver mapa de stands y reservaciones activas (vigencia, monto pagado y porcentaje).
2. Avanzar la fecha del sistema: avanza día por día y, en cada fecha, libera las reservas vencidas y envía los avisos de cada editorial según la vigencia de su propia reserva.
3. Revisar vencimientos y enviar avisos en la fecha actual.

## Configuración

Edita `src/main/resources/configuracion.properties`: precio por m², vigencia de la reserva, porcentaje mínimo para ocupar, descuento por pronto pago, fechas límite y días de anticipación de los avisos.
