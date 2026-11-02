# Requisitos funcionales y no funcionales

**Notificaciones-Stands — Sistema administrativo de venta de stands (FILEY)**

Requisitos del módulo de notificaciones y de la gestión de stands, con sus criterios de aceptación y las excepciones operativas de cada funcionalidad. Cada criterio y cada excepción indica la prueba automatizada que lo verifica (`Clase.método`, en `src/test/java`).

## 1. Lista de requisitos funcionales y no funcionales

| ID | Épica / ámbito | Descripción del requisito | Imp. | Fact. | Puntaje | Prioridad | Estado / validación |
|---|---|---|---:|---:|---:|---|---|
| RF-1.1 | EPIC-01 | El sistema deberá registrar, para cada stand, su identificador, precio, metros cuadrados y su estado (Disponible, Reservado, Ocupado o Bloqueado). | 5 | 5 | 5.0 | Alta |  |
| RF-1.2 | EPIC-01 | El sistema deberá calcular el costo de cada stand en función de sus dimensiones y el precio por metro cuadrado configurado. | 4 | 5 | 4.4 | Alta |  |
| RF-2.1 | EPIC-02 | El sistema deberá permitir reservar un stand Disponible a nombre de una editorial, estableciendo un tiempo de vigencia. | 5 | 5 | 5.0 | Alta |  |
| RF-2.2 | EPIC-02 | El sistema deberá registrar los pagos de una editorial sobre su reservación para calcular el porcentaje del costo cubierto y definir el pase a Ocupado. | 5 | 4 | 4.6 | Alta |  |
| RF-3.1 | EPIC-03 | El sistema deberá permitir al administrador bloquear stands como negociación interna previa a la salida del sistema, pudiendo regresarlos a Disponible. | 4 | 5 | 4.4 | Alta |  |
| RF-3.2 | EPIC-03 | El sistema deberá permitir transferir un stand Bloqueado a Reservado directamente a nombre de una editorial (Opcional). | 3 | 4 | 3.4 | Media | Opcional |
| RF-4.1 | EPIC-04 | El sistema deberá aplicar el descuento por pronto pago si se realiza antes de la fecha establecida, y omitirlo después de esta. | 5 | 4 | 4.6 | Alta |  |
| RF-4.2 | EPIC-04 | El sistema deberá manejar una fecha máxima para cubrir el costo total del stand y una configuración de costos y plazos. | 5 | 5 | 5.0 | Alta |  |
| RF-5.1 | EPIC-05 | El sistema deberá notificar a las editoriales cuando su reserva esté a punto de vencer. | 5 | 4 | 4.6 | Alta |  |
| RF-5.2 | EPIC-05 | El sistema deberá notificar a las editoriales cuando el plazo de pronto pago esté a punto de vencer. | 5 | 4 | 4.6 | Alta |  |
| RF-5.3 | EPIC-05 | El sistema deberá notificar a las editoriales cuando la fecha límite para cubrir el costo esté a punto de vencer. | 5 | 4 | 4.6 | Alta |  |
| RNF-1 | TRANSVERSAL | El sistema deberá poder operarse completamente desde la línea de comandos. | 5 | 5 | 5.0 | Alta |  |
| RNF-2 | TRANSVERSAL | El sistema podrá utilizar una base de datos en memoria como H2 para pruebas (Opcional). | 4 | 5 | 4.4 | Alta | Opcional |
| RNF-3 | TRANSVERSAL | El sistema deberá organizarse en paquetes (paquete de clases de lógica de negocios, paquete de notificaciones y configuración). | 3 | 4 | 3.4 | Media | Sugerencia |

## 2. Épicas y ámbitos

| Código | Épica / ámbito | Descripción |
|---|---|---|
| EPIC-01 | Gestión del mapa de stands | Mapeo de los espacios, identificadores, dimensiones, precios y estados. |
| EPIC-02 | Reserva, pago y ocupación | Reserva con vigencia, registro de pagos y porcentaje para pasar a Ocupado. |
| EPIC-03 | Bloqueo y transferencia de stands | Operaciones del administrador previas al despliegue y transferencia opcional. |
| EPIC-04 | Costos, descuentos y plazos | Configuración de costos, plazos, fechas límite y descuento por pronto pago. |
| EPIC-05 | Notificaciones | Avisos sobre reservas, pronto pago y fechas límite por vencer. |
| TRANSVERSAL | Operación y arquitectura | Línea de comandos, organización en paquetes y almacenamiento opcional en memoria. |

## 3. Método de priorización

Se utiliza una **Matriz de Priorización Ponderada** con dos criterios:

| Criterio | Escala | Peso | Interpretación |
|---|---|---:|---|
| Importancia | 1–5 | 60% | Necesidad y valor del requisito para el proceso. |
| Factibilidad | 1–5 | 40% | Viabilidad de implementación actual. |

`Puntaje = (Importancia × 0.60) + (Factibilidad × 0.40)`

| Rango | Prioridad |
|---|---|
| 4.0 – 5.0 | Alta |
| 3.0 – 3.9 | Media |
| 1.0 – 2.9 | Baja |

**Justificación.** Prioridad alta: el núcleo del proyecto (mapa de stands, reservas con vigencia, pagos, configuración de costos y plazos y, de forma primordial, el módulo de notificaciones). Prioridad media: elementos opcionales o sugerencias (transferencia de stands y organización en paquetes).

## 4. Reglas de negocio

### Transiciones de estado de un stand

| Desde | Hacia | Quién / cuándo |
|---|---|---|
| Disponible | Reservado | Una editorial reserva el stand con vigencia |
| Disponible | Bloqueado | El administrador bloquea el stand antes de la salida del sistema |
| Bloqueado | Disponible | El administrador libera el bloqueo |
| Bloqueado | Reservado | El administrador transfiere el stand a una editorial (Opcional) |
| Reservado | Ocupado | Los pagos cubren el porcentaje mínimo configurado |
| Reservado | Disponible | La reserva vence sin cubrir el porcentaje mínimo |
| Ocupado | — | Estado final |

Cualquier otra transición se rechaza. Se verifica la tabla completa (las 16 combinaciones) en `EstadoStandTest.soloSePermitenLasTransicionesDeLasReglasDeNegocio`.

### Otras reglas

- **Vigencia:** el último día de vigencia todavía se puede pagar; la reserva se libera a partir del día siguiente.
- **Descuento:** lo define la fecha del **primer pago** y se conserva en los pagos posteriores.
- **Paso a Ocupado:** se compara el monto pagado exacto contra el porcentaje mínimo; no se usa el porcentaje redondeado a 2 decimales.
- **Ventana de aviso:** se avisa cuando faltan entre 0 y N días (N configurable, ambos extremos incluidos); un aviso se envía una sola vez por reservación y tipo.

### Parámetros de referencia para los ejemplos

Los ejemplos numéricos usan la configuración de las pruebas (`EscenarioPruebas`). Si cambia la configuración, cambian las cifras, no el criterio.

| Parámetro | Valor |
|---|---|
| Precio por m² | $1,000.00 |
| Vigencia de la reserva | 10 días |
| Porcentaje mínimo para ocupar | 30% |
| Descuento por pronto pago | 10% |
| Fecha límite de pronto pago | 2027-01-31 |
| Fecha máxima de pago | 2027-02-15 |
| Días de aviso | 5 |
| Fecha inicial del sistema | 2027-01-01 |
| Stands | S-1 y S-2, de 10 m² ($10,000.00) |
| Editoriales | E-1 y E-2 |

## 5. Criterios de aceptación y excepciones por requisito

Un requisito se considera cumplido cuando **todos** sus criterios pasan y **todas** sus excepciones se comportan como se indica. Las excepciones de negocio lanzan `NegocioException`; los datos inválidos en entidades y configuración lanzan `IllegalArgumentException`.

### RF-1.1 Registro de stands (identificador, precio, metros cuadrados y estado)

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-1.1-a | **Dado** que se registra el stand S-3 de 12 m², **cuando** se consulta, **entonces** muestra su identificador, sus metros cuadrados, su precio y el estado Disponible. | `ServicioStandsTest.unStandNuevoNaceDisponible`<br>`StandTest.unStandNuevoGuardaSusDatosYNaceDisponible` |
| CA-RF-1.1-b | **Dado** que existen los stands S-1, S-2 y S-3, **cuando** se consulta el mapa, **entonces** se listan en el orden en que se dieron de alta. | `ServicioStandsTest.elMapaListaLosStandsEnElOrdenDeAlta` |
| CA-RF-1.1-c | **Dado** un stand Disponible, **cuando** pasa a Reservado y después a Ocupado (transiciones permitidas), **entonces** su estado cambia en cada paso. | `StandTest.unaTransicionValidaCambiaElEstado` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-1.1-a | Se registra un stand con un identificador que ya existe → se rechaza con `NegocioException` y el mapa conserva sus stands. | `ServicioStandsTest.noSePermitenIdentificadoresRepetidos` |
| EX-RF-1.1-b | Se registra un stand con identificador vacío o nulo → se rechaza. | `ServicioStandsTest.noSePermiteUnIdentificadorVacioONulo`<br>`StandTest.rechazaUnIdentificadorVacioONulo` |
| EX-RF-1.1-c | Se registra un stand con 0 o menos metros cuadrados → se rechaza. | `ServicioStandsTest.noSePermitenMetrosCuadradosEnCeroONegativos`<br>`StandTest.rechazaMetrosCuadradosEnCeroONegativos` |
| EX-RF-1.1-d | Se consulta un stand que no existe → `NegocioException` que menciona el identificador. | `ServicioStandsTest.consultarUnStandInexistenteFalla` |
| EX-RF-1.1-e | Se intenta una transición de estado no permitida (por ejemplo Ocupado a Bloqueado) → se rechaza y el estado no cambia. | `StandTest.unaTransicionInvalidaLanzaExcepcionYConservaElEstado`<br>`ServicioStandsTest.unStandOcupadoNoPuedeCambiarDeEstado`<br>`EstadoStandTest.soloSePermitenLasTransicionesDeLasReglasDeNegocio` |

### RF-1.2 Cálculo del costo del stand según dimensiones y precio por metro cuadrado

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-1.2-a | **Dado** un precio de $1,000.00 por m², **cuando** se registra un stand de 12 m², **entonces** su precio es $12,000.00. | `CalculadoraPreciosTest.elPrecioDependeDeLosMetrosCuadrados`<br>`ServicioStandsTest.elPrecioDelStandSeCalculaConSusDimensiones` |
| CA-RF-1.2-b | **Dado** un precio de $1,000.00 por m², **cuando** se calcula el precio de 13.5 m², **entonces** el resultado es $13,500.00. | `CalculadoraPreciosTest.elPrecioDependeDeLosMetrosCuadrados` |
| CA-RF-1.2-c | **Dado** un resultado con más de 2 decimales (0.123456 m²), **cuando** se calcula el precio, **entonces** se redondea a 2 decimales ($123.46). | `CalculadoraPreciosTest.elPrecioSeRedondeaADosDecimales` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-1.2-a | El precio por m² configurado es 0, negativo o nulo → la configuración se rechaza con `IllegalArgumentException`. | `ConfiguracionTest.rechazaUnPrecioPorMetroNoPositivo` |
| EX-RF-1.2-b | Se crea un stand con precio nulo o menor o igual a cero → se rechaza con `IllegalArgumentException`. | `StandTest.rechazaUnPrecioNuloONoPositivo` |

### RF-2.1 Reserva de un stand Disponible con tiempo de vigencia

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-2.1-a | **Dado** que S-1 está Disponible y la fecha es 2027-01-01, **cuando** E-1 reserva S-1, **entonces** S-1 queda Reservado y la vigencia es 2027-01-11 (10 días). | `ServicioReservacionesTest.reservarCambiaElEstadoYFijaLaVigencia`<br>`ReservacionTest.naceActivaYSinPagos` |
| CA-RF-2.1-b | **Dado** dos reservaciones consecutivas, **cuando** se crean, **entonces** reciben los folios R-1 y R-2. | `ServicioReservacionesTest.cadaReservacionRecibeUnFolioConsecutivo` |
| CA-RF-2.1-c | **Dado** una reservación sin pagos cuya vigencia terminó el 2027-01-11, **cuando** la fecha es 2027-01-12 y se liberan las reservas vencidas, **entonces** la reservación se cancela y el stand vuelve a Disponible. | `ServicioReservacionesTest.unaReservaVencidaLiberaElStand`<br>`ReservacionTest.cancelarLaDejaInactiva` |
| CA-RF-2.1-d | **Dado** que la fecha es el último día de vigencia (2027-01-11), **cuando** se liberan las reservas vencidas, **entonces** no se libera nada. | `ServicioReservacionesTest.elUltimoDiaDeVigenciaLaReservaNoSeLibera` |
| CA-RF-2.1-e | **Dado** una reserva con un pago menor al mínimo, **cuando** vence, **entonces** se libera el stand y el historial de pagos se conserva. | `ServicioReservacionesTest.unaReservaConPagoInsuficienteSeLiberaYConservaSuHistorial` |
| CA-RF-2.1-f | **Dado** un stand Ocupado, **cuando** pasan 30 días y se liberan las reservas vencidas, **entonces** sigue Ocupado. | `ServicioReservacionesTest.unStandOcupadoNoSeLiberaAunquePaseLaVigencia` |
| CA-RF-2.1-g | **Dado** un stand liberado por vencimiento, **cuando** otra editorial lo reserva, **entonces** se crea una reservación nueva con su propia vigencia. | `ServicioReservacionesTest.unStandLiberadoPuedeReservarloOtraEditorial` |
| CA-RF-2.1-h | **Dado** una reservación cancelada y otra vigente, **cuando** se listan las activas, **entonces** solo aparece la vigente. | `ServicioReservacionesTest.listarActivasOmiteLasReservacionesCanceladas` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-2.1-a | El stand ya está Reservado → `NegocioException`. | `ServicioReservacionesTest.noSePuedeReservarUnStandQueYaEstaReservado` |
| EX-RF-2.1-b | El stand está Ocupado → `NegocioException`. | `ServicioReservacionesTest.noSePuedeReservarUnStandOcupado` |
| EX-RF-2.1-c | El stand está Bloqueado → `NegocioException` (solo se transfiere, no se reserva directo). | `ServicioTransferenciasTest.unStandBloqueadoNoPuedeReservarseDirectamente` |
| EX-RF-2.1-d | El stand no existe → `NegocioException`. | `ServicioReservacionesTest.reservarUnStandInexistenteFalla` |
| EX-RF-2.1-e | La editorial no existe → `NegocioException`, el stand sigue Disponible y no se crea reservación. | `ServicioReservacionesTest.reservarConUnaEditorialInexistenteFallaYElStandSigueDisponible` |
| EX-RF-2.1-f | Se consulta una reservación inexistente → `NegocioException`. | `ServicioReservacionesTest.obtenerUnaReservacionInexistenteFalla` |
| EX-RF-2.1-g | Se liberan dos veces seguidas las reservas vencidas → la segunda vez no libera nada. | `ServicioReservacionesTest.liberarDosVecesNoLiberaNadaLaSegundaVez` |
| EX-RF-2.1-h | Se crea una reservación sin stand o sin editorial, o con vigencia anterior a la fecha de reserva → se rechaza con `IllegalArgumentException`. | `ReservacionTest.rechazaDatosInvalidosAlCrearse` |

### RF-2.2 Registro de pagos, porcentaje cubierto y paso a Ocupado

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-2.2-a | **Dado** una reservación con costo de $9,000.00, **cuando** la editorial paga $2,000.00, **entonces** el porcentaje cubierto es 22.22% y el stand sigue Reservado. | `ServicioReservacionesTest.conElPorcentajeMinimoElStandPasaAOcupado` |
| CA-RF-2.2-b | **Dado** que ya pagó $2,000.00, **cuando** paga $700.00 más (30%), **entonces** el stand pasa a Ocupado. | `ServicioReservacionesTest.conElPorcentajeMinimoElStandPasaAOcupado` |
| CA-RF-2.2-c | **Dado** el mínimo de 30%, **cuando** paga exactamente $2,700.00 de $9,000.00, **entonces** el stand pasa a Ocupado. | `ServicioReservacionesTest.exactamenteElPorcentajeMinimoYaOcupaElStand` |
| CA-RF-2.2-d | **Dado** el mínimo de 30%, **cuando** paga $2,699.99 (29.9999%), **entonces** el stand sigue Reservado: la comparación usa montos exactos, no el porcentaje redondeado. | `ServicioReservacionesTest.unCentavoPorDebajoDelMinimoNoOcupaElStand`<br>`ReservacionTest.haCubiertoAlMenosComparaMontosExactosSinRedondear` |
| CA-RF-2.2-e | **Dado** una reservación, **cuando** paga el costo completo de una vez, **entonces** queda pagada al 100% y el stand pasa a Ocupado. | `ServicioReservacionesTest.sePuedePagarElCostoCompletoDeUnaVez`<br>`ReservacionTest.estaPagadaCompletaAlCubrirElCosto` |
| CA-RF-2.2-f | **Dado** varios pagos en fechas distintas, **cuando** se consulta la reservación, **entonces** el historial conserva cada monto con su fecha y el total se acumula. | `ServicioReservacionesTest.elHistorialGuardaCadaPagoConSuFecha`<br>`ReservacionTest.losPagosSeAcumulanYSeConservanEnElHistorial`<br>`PagoTest.guardaElMontoYLaFecha` |
| CA-RF-2.2-g | **Dado** que es el último día de vigencia, **cuando** la editorial paga, **entonces** el pago se acepta. | `ServicioReservacionesTest.elUltimoDiaDeVigenciaTodaviaSePuedePagar` |
| CA-RF-2.2-h | **Dado** un stand Ocupado cuya vigencia ya pasó, **cuando** la editorial paga el saldo, **entonces** el pago se acepta. | `ServicioReservacionesTest.unStandOcupadoSigueRecibiendoPagosAunqueYaPasoLaVigencia` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-2.2-a | El pago es mayor al saldo pendiente (por ejemplo $99,999) → `NegocioException`; el monto pagado no cambia y no se fija el descuento. | `ServicioReservacionesTest.noSePermiteUnPagoMayorAlSaldo` |
| EX-RF-2.2-b | El pago excede el saldo por un centavo → se rechaza; el saldo exacto sí se acepta. | `ServicioReservacionesTest.unCentavoMasQueElSaldoSeRechaza` |
| EX-RF-2.2-c | El pago es $0, negativo o nulo → `NegocioException`; el monto pagado no cambia. | `ServicioReservacionesTest.noSePermiteUnPagoEnCeroNegativoNiNulo`<br>`PagoTest.rechazaMontosNulosEnCeroONegativos`<br>`PagoTest.rechazaUnaFechaNula`<br>`ReservacionTest.rechazaUnPagoNulo` |
| EX-RF-2.2-d | La reservación no existe → `NegocioException`. | `ServicioReservacionesTest.pagarUnaReservacionInexistenteFalla` |
| EX-RF-2.2-e | La reserva ya venció (Reservado y fecha posterior a la vigencia) → `NegocioException`. | `ServicioReservacionesTest.noSePuedePagarUnaReservaVencida` |
| EX-RF-2.2-f | La reservación fue cancelada → `NegocioException` («ya no esta activa»). | `ServicioReservacionesTest.noSePuedePagarUnaReservacionCancelada` |
| EX-RF-2.2-g | Se intenta modificar desde fuera el historial de pagos de una reservación → se rechaza (la lista es de solo lectura). | `ReservacionTest.elHistorialEsDeSoloLectura` |

### RF-3.1 Bloqueo de stands por el administrador y regreso a Disponible

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-3.1-a | **Dado** que S-1 está Disponible, **cuando** el administrador lo bloquea, **entonces** queda Bloqueado. | `ServicioStandsTest.bloquearCambiaElEstadoABloqueado` |
| CA-RF-3.1-b | **Dado** que S-1 está Bloqueado, **cuando** el administrador libera el bloqueo, **entonces** vuelve a Disponible. | `ServicioStandsTest.elAdministradorPuedeLiberarUnBloqueo` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-3.1-a | Bloquear un stand que ya está Bloqueado → `NegocioException`. | `ServicioStandsTest.noSePuedeBloquearUnStandYaBloqueado` |
| EX-RF-3.1-b | Bloquear un stand Reservado → `NegocioException` y sigue Reservado. | `ServicioStandsTest.noSePuedeBloquearUnStandReservado` |
| EX-RF-3.1-c | Bloquear un stand Ocupado → `NegocioException` y sigue Ocupado. | `ServicioStandsTest.unStandOcupadoNoPuedeCambiarDeEstado` |
| EX-RF-3.1-d | Bloquear un stand inexistente → `NegocioException`. | `ServicioStandsTest.bloquearUnStandInexistenteFalla` |
| EX-RF-3.1-e | Liberar el bloqueo de un stand que no está Bloqueado → `NegocioException`. | `ServicioStandsTest.noSePuedeLiberarUnStandQueNoEstaBloqueado` |

### RF-3.2 Transferencia de un stand Bloqueado a Reservado (opcional)

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-3.2-a | **Dado** que S-1 está Bloqueado, **cuando** el administrador lo transfiere a E-2, **entonces** queda Reservado a nombre de E-2. | `ServicioTransferenciasTest.unStandBloqueadoSeTransfiereComoReservadoALaEditorial` |
| CA-RF-3.2-b | **Dado** una transferencia hecha el 2027-01-05, **cuando** se crea la reservación, **entonces** su vigencia (10 días) cuenta desde esa fecha: 2027-01-15. | `ServicioTransferenciasTest.laTransferenciaFijaLaVigenciaDesdeElDiaDeLaTransferencia` |
| CA-RF-3.2-c | **Dado** un stand recién transferido, **cuando** la editorial paga el mínimo, **entonces** pasa a Ocupado. | `ServicioTransferenciasTest.despuesDeTransferirLaEditorialPuedePagarYOcuparElStand` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-3.2-a | El stand no está Bloqueado (está Disponible) → `NegocioException`. | `ServicioTransferenciasTest.soloSeTransfierenStandsBloqueados` |
| EX-RF-3.2-b | El stand está Reservado u Ocupado → `NegocioException`. | `ServicioTransferenciasTest.noSeTransfiereUnStandReservadoNiOcupado` |
| EX-RF-3.2-c | El stand no existe → `NegocioException`. | `ServicioTransferenciasTest.transferirUnStandInexistenteFalla` |
| EX-RF-3.2-d | La editorial no existe → `NegocioException`; el stand sigue Bloqueado y no se crea reservación. | `ServicioTransferenciasTest.transferirAUnaEditorialInexistenteFallaYElStandSigueBloqueado` |

### RF-4.1 Descuento por pronto pago

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-4.1-a | **Dado** un descuento de 10% con límite el 2027-01-31, **cuando** el primer pago es el 2027-01-10, **entonces** el costo aplicable de un stand de $10,000.00 es $9,000.00. | `CalculadoraPreciosTest.conDescuentoSiSePagaAntesDeLaFechaLimite`<br>`ServicioReservacionesTest.elPrimerPagoATiempoAplicaDescuento`<br>`ReservacionTest.asegurarElDescuentoCambiaElCostoAplicable` |
| CA-RF-4.1-b | **Dado** que el primer pago es el 2027-01-31 (último día), **cuando** se registra, **entonces** todavía aplica el descuento. | `CalculadoraPreciosTest.elUltimoDiaTodaviaTieneDescuento`<br>`ServicioReservacionesTest.elUltimoDiaDelProntoPagoTodaviaAplicaDescuento`<br>`CalculadoraPreciosTest.unPagoMuyAnteriorTambienTieneDescuento` |
| CA-RF-4.1-c | **Dado** que el primer pago es el 2027-02-01, **cuando** se registra, **entonces** no hay descuento y el costo es $10,000.00. | `CalculadoraPreciosTest.sinDescuentoDespuesDeLaFechaLimite`<br>`ServicioReservacionesTest.elPrimerPagoTardeNoAplicaDescuento` |
| CA-RF-4.1-d | **Dado** que el primer pago fue a tiempo, **cuando** un pago posterior se hace sin descuento vigente, **entonces** el costo sigue siendo $9,000.00. | `ServicioReservacionesTest.elDescuentoAseguradoEnElPrimerPagoSeConservaEnLosSiguientes` |
| CA-RF-4.1-e | **Dado** una reservación sin descuento, **cuando** se paga, **entonces** el máximo a pagar es el precio completo ($10,000.00). | `ServicioReservacionesTest.sinDescuentoElMontoMaximoAPagarEsElPrecioCompleto` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-4.1-a | Un pago rechazado (mayor al saldo, cero o negativo) no fija el descuento. | `ServicioReservacionesTest.noSePermiteUnPagoMayorAlSaldo`<br>`ServicioReservacionesTest.noSePermiteUnPagoEnCeroNegativoNiNulo` |
| EX-RF-4.1-b | El porcentaje de descuento configurado es negativo o de 100% o más → la configuración se rechaza. | `ConfiguracionTest.rechazaUnDescuentoFueraDeRango` |

### RF-4.2 Fecha máxima de pago y configuración de costos y plazos

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-4.2-a | **Dado** el archivo `configuracion.properties` incluido, **cuando** se carga, **entonces** se obtienen precio, vigencia, % mínimo, descuento, fechas y días de aviso. | `ConfiguracionTest.cargaLosParametrosDelArchivoIncluidoEnElPrograma` |
| CA-RF-4.2-b | **Dado** un archivo de configuración externo válido, **cuando** se carga, **entonces** se usan sus valores. | `ConfiguracionTest.cargaLosParametrosDeUnArchivoExterno` |
| CA-RF-4.2-c | **Dado** que la fecha de pronto pago es igual a la fecha máxima, **cuando** se crea la configuración, **entonces** se acepta. | `ConfiguracionTest.aceptaUnaFechaDeProntoPagoIgualALaFechaMaxima` |
| CA-RF-4.2-d | **Dado** que se inicia el programa, **cuando** se precargan los datos de demostración, **entonces** se informan el pronto pago, el pago máximo y los días de aviso vigentes. | `MainTest.alIniciarSePrecargaLaDemostracion`<br>`ConfiguracionTest.toStringMuestraTodosLosParametros` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-4.2-a | La fecha de pronto pago es posterior a la fecha máxima → `IllegalArgumentException`. | `ConfiguracionTest.rechazaUnaFechaDeProntoPagoPosteriorALaFechaMaxima` |
| EX-RF-4.2-b | La vigencia de la reserva es menor a 1 día → se rechaza. | `ConfiguracionTest.rechazaUnaVigenciaMenorAUnDia` |
| EX-RF-4.2-c | El porcentaje mínimo es 0 o mayor a 100 → se rechaza. | `ConfiguracionTest.rechazaUnPorcentajeMinimoFueraDeRango` |
| EX-RF-4.2-d | Los días de aviso son negativos → se rechaza (0 sí es válido). | `ConfiguracionTest.rechazaDiasDeAvisoNegativosPeroAceptaCero` |
| EX-RF-4.2-e | Falta una de las fechas → se rechaza. | `ConfiguracionTest.rechazaFechasNulas` |
| EX-RF-4.2-f | Al archivo le falta un parámetro → el error indica cuál falta. | `ConfiguracionTest.unArchivoSinUnParametroIndicaCualFalta` |
| EX-RF-4.2-g | El archivo tiene un valor mal formado (fecha o número) → se rechaza. | `ConfiguracionTest.unArchivoConUnValorMalFormadoLoRechaza` |
| EX-RF-4.2-h | El archivo no existe → `UncheckedIOException`. | `ConfiguracionTest.unArchivoInexistenteLanzaUnErrorDeLectura` |

### RF-5.1 Notificación de reserva por vencer

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-5.1-a | **Dado** una reserva con vigencia hasta 2027-01-11, **cuando** la fecha es 2027-01-07 y se revisan los vencimientos, **entonces** se envía un aviso `RESERVA_POR_VENCER` a la editorial indicando 4 días restantes. | `ServicioNotificacionesTest.avisaCuandoLaReservaEstaPorVencer` |
| CA-RF-5.1-b | **Dado** 5 días de anticipación, **cuando** faltan 6 días **entonces** no se avisa, y cuando faltan 5 **entonces** sí (límite inclusivo). | `ServicioNotificacionesTest.elLimiteDeLaVentanaDeAvisoEsInclusivo` |
| CA-RF-5.1-c | **Dado** que es el día del vencimiento, **cuando** se revisa, **entonces** se avisa con 0 días restantes. | `ServicioNotificacionesTest.avisaElMismoDiaDelVencimiento` |
| CA-RF-5.1-d | **Dado** que faltan más días que la anticipación configurada, **cuando** se revisa, **entonces** no se envía nada. | `ServicioNotificacionesTest.noAvisaSiTodaviaFaltaMuchoParaVencer` |
| CA-RF-5.1-e | **Dado** que la anticipación configurada es 0 días, **cuando** se revisa, **entonces** solo se avisa el mismo día del vencimiento. | `ServicioNotificacionesTest.conCeroDiasDeAvisoSoloAvisaElMismoDia` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-5.1-a | La reserva ya venció → no se avisa. | `ServicioNotificacionesTest.noAvisaDeUnaReservaQueYaVencio` |
| EX-RF-5.1-b | El stand ya está Ocupado → no se avisa de la reserva. | `ServicioNotificacionesTest.noAvisaDeLaReservaSiElStandYaEstaOcupado` |
| EX-RF-5.1-c | La reservación fue cancelada → no se envía ningún aviso. | `ServicioNotificacionesTest.noAvisaDeUnaReservacionCancelada` |

### RF-5.2 Notificación de plazo de pronto pago por vencer

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-5.2-a | **Dado** que el pronto pago vence el 2027-01-31, **cuando** la fecha es 2027-01-28 y no hay pagos, **entonces** se envía un aviso `PRONTO_PAGO_POR_VENCER` con 3 días restantes. | `ServicioNotificacionesTest.avisaCuandoElProntoPagoEstaPorVencer` |
| CA-RF-5.2-b | **Dado** 5 días de anticipación, **cuando** faltan 6 días **entonces** no se avisa, y cuando faltan 5 **entonces** sí. | `ServicioNotificacionesTest.elLimiteDeLaVentanaDelProntoPagoEsInclusivo` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-5.2-a | La editorial ya hizo un pago → no se avisa del pronto pago. | `ServicioNotificacionesTest.noAvisaDelProntoPagoSiLaEditorialYaPago` |
| EX-RF-5.2-b | El plazo de pronto pago ya venció → no se avisa. | `ServicioNotificacionesTest.noAvisaDelProntoPagoDespuesDeQueVencio` |

### RF-5.3 Notificación de fecha límite para cubrir el costo por vencer

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-5.3-a | **Dado** que la fecha máxima es 2027-02-15 y la editorial tiene un pago parcial, **cuando** la fecha es 2027-02-12, **entonces** se envía un aviso `FECHA_LIMITE_POR_VENCER` con 3 días restantes. | `ServicioNotificacionesTest.avisaCuandoLaFechaLimiteDePagoEstaPorVencer` |
| CA-RF-5.3-b | **Dado** una reserva sin pagos, **cuando** faltan 5 días para la fecha máxima, **entonces** también se envía el aviso. | `ServicioNotificacionesTest.laFechaLimiteDePagoTambienAvisaAUnaReservaSinPagos` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-5.3-a | El stand ya está pagado por completo → no se avisa. | `ServicioNotificacionesTest.noAvisaSiElStandYaEstaPagadoPorCompleto` |
| EX-RF-5.3-b | La fecha máxima ya pasó → no se avisa. | `ServicioNotificacionesTest.noAvisaDeLaFechaLimiteDespuesDeQueVencio` |

### RF-5.x Reglas comunes del módulo de notificaciones (aplican a RF-5.1, RF-5.2 y RF-5.3)

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RF-5.x-a | **Dado** que ya se envió un aviso, **cuando** se vuelve a revisar (el mismo día o uno posterior), **entonces** no se repite. | `ServicioNotificacionesTest.noRepiteElMismoAviso`<br>`ServicioNotificacionesTest.unNuevoDiaTampocoRepiteUnAvisoYaEnviado` |
| CA-RF-5.x-b | **Dado** que coinciden dos vencimientos cercanos, **cuando** se revisa, **entonces** se envían ambos avisos en una sola revisión (reserva primero, pronto pago después). | `ServicioNotificacionesTest.puedeEnviarDosTiposDeAvisoALaVez` |
| CA-RF-5.x-c | **Dado** reservaciones de dos editoriales, **cuando** se revisa, **entonces** cada una recibe únicamente sus propios avisos. | `ServicioNotificacionesTest.cadaEditorialRecibeSusPropiosAvisos` |
| CA-RF-5.x-d | **Dado** un canal de entrega configurado, **cuando** se genera un aviso, **entonces** se entrega por ese canal (la lógica solo conoce la interfaz `Notificador`). | `ServicioNotificacionesTest.cadaAvisoSeEntregaAlCanalConfigurado` |
| CA-RF-5.x-e | **Dado** el canal de consola, **cuando** se envía un aviso, **entonces** se imprime con el prefijo `[AVISO]`. | `NotificadorConsolaTest.imprimeElAvisoEnLaLineaDeComandos` |
| CA-RF-5.x-f | **Dado** un aviso, **cuando** se lee su mensaje, **entonces** incluye editorial, correo, stand, fecha de vencimiento y días restantes; su clave combina reservación y tipo. | `NotificacionTest.elMensajeIdentificaAEditorialStandFechaYDias`<br>`NotificacionTest.laClaveCombinaReservacionYTipo`<br>`NotificacionTest.tiposDistintosTienenClavesDistintas`<br>`NotificacionTest.cadaTipoTieneUnaDescripcion` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RF-5.x-a | El canal de entrega falla → el aviso NO se da por enviado, se contabiliza como fallo y se reintenta en la siguiente revisión. | `ServicioNotificacionesTest.siElCanalFallaElAvisoNoSeDaPorEnviadoYSeReintenta` |
| EX-RF-5.x-b | El canal falla solo para una editorial → los avisos de las demás editoriales se entregan igual. | `ServicioNotificacionesTest.laFallaConUnaEditorialNoBloqueaLosAvisosDeLasDemas` |

### RNF-1 Operación completa desde la línea de comandos

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RNF-1-a | **Dado** que se inicia el programa, **cuando** el usuario elige 0, **entonces** el menú muestra todas las opciones y el programa termina con «Hasta luego.». | `MainTest.elMenuMuestraLasOpcionesYSeCierraConCero` |
| CA-RNF-1-b | **Dado** el menú, **cuando** se consulta el mapa, **entonces** se listan los 8 stands de la demostración con su estado. | `MainTest.elMapaMuestraLosOchoStandsDeLaDemostracion` |
| CA-RNF-1-c | **Dado** el menú, **cuando** se consultan las reservaciones, **entonces** se muestran las precargadas con vigencia, monto pagado y estado. | `MainTest.laOpcionUnoListaLasReservacionesPrecargadasConSusDetalles` |
| CA-RNF-1-d | **Dado** el menú, **cuando** se revisa, **entonces** no ofrece opciones de creación ni edición de entidades. | `MainTest.elMenuNoOfreceOpcionesDeCreacionNiEdicion` |
| CA-RNF-1-e | **Dado** las reservas precargadas, **cuando** se avanza la fecha 20 días, **entonces** se envían los avisos y se informa, sin otra acción, que las reservas sin ocupar vencieron y sus stands volvieron a Disponible, cada una con su propia vigencia. | `MainTest.alAvanzarLosDiasSeAvisaYSeLiberanAutomaticamenteLasReservasSinPago`<br>`MainTest.cadaReservaVenceConSuPropiaVigencia`<br>`MainTest.unStandOcupadoNoSeLiberaAlAvanzar` |
| CA-RNF-1-f | **Dado** que no hay novedades, **cuando** se revisan los vencimientos, **entonces** se muestra «No hay avisos nuevos.». | `MainTest.sinNovedadesLaRevisionLoDice`<br>`MainTest.laPrimeraRevisionAvisaDeLaReservaProximaAVencer` |
| CA-RNF-1-g | **Dado** el reloj simulado, **cuando** se avanzan días (incluso cambiando de mes), **entonces** la fecha del sistema se actualiza. | `RelojSimuladoTest.avanzarSumaLosDiasIncluyendoElCambioDeMes`<br>`RelojSimuladoTest.arrancaEnLaFechaIndicada`<br>`RelojSimuladoTest.sinParametrosArrancaHoy`<br>`RelojSimuladoTest.avanzarCeroDiasNoCambiaLaFecha` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RNF-1-c | Se ingresa una cantidad de días que no es número → «Dato numerico invalido.». | `MainTest.unNumeroDeDiasQueNoEsNumeroSeRechazaConUnMensaje` |
| EX-RNF-1-d | Se ingresan días negativos al simular → «Dato invalido: …»; el reloj no retrocede. | `MainTest.avanzarUnNumeroNegativoDeDiasSeRechaza`<br>`RelojSimuladoTest.noSePuedeRetrocederElReloj` |
| EX-RNF-1-e | Se elige una opción inexistente → «Opcion no valida.». | `MainTest.unaOpcionInexistenteSeIgnoraConUnAviso` |

### RNF-2 Almacenamiento en memoria (H2 opcional)

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RNF-2-a | **Dado** el repositorio en memoria, **cuando** se guarda y se busca por identificador, **entonces** se recupera el mismo objeto. | `RepositorioEnMemoriaTest.guardaYRecuperaPorIdentificador` |
| CA-RNF-2-b | **Dado** varios elementos guardados, **cuando** se listan, **entonces** conservan el orden de inserción. | `RepositorioEnMemoriaTest.listarConservaElOrdenDeInsercion` |
| CA-RNF-2-c | **Dado** un elemento guardado con un identificador existente, **cuando** se vuelve a guardar, **entonces** reemplaza al anterior. | `RepositorioEnMemoriaTest.guardarConElMismoIdReemplazaElElemento` |

**Excepciones operativas**

| ID | Situación → comportamiento esperado | Prueba |
|---|---|---|
| EX-RNF-2-a | Se busca un identificador inexistente → se devuelve vacío (`Optional`), sin error. | `RepositorioEnMemoriaTest.buscarUnIdInexistenteDevuelveVacio` |
| EX-RNF-2-b | Se modifica la lista devuelta por `listar()` → el repositorio no se altera. | `RepositorioEnMemoriaTest.modificarLaListaDevueltaNoAfectaAlRepositorio` |

### RNF-3 Organización en paquetes

**Criterios de aceptación**

| ID | Criterio | Prueba |
|---|---|---|
| CA-RNF-3-a | **Dado** el código fuente, **cuando** se revisan los paquetes, **entonces** la lógica de negocio está en `negocio`, los avisos en `notificaciones`, los parámetros en `configuracion`, las entidades en `datos` y el menú en `app`. | `EstructuraPaquetesTest.lasClasesDeNegocioEstanEnElPaqueteNegocio`<br>`EstructuraPaquetesTest.lasClasesDeNotificacionesEstanEnSuPaquete`<br>`EstructuraPaquetesTest.laConfiguracionTieneSuPaquete`<br>`EstructuraPaquetesTest.lasEntidadesEstanEnElPaqueteDatos`<br>`EstructuraPaquetesTest.elPuntoDeEntradaEstaEnElPaqueteApp` |

## 6. Trazabilidad requisito → implementación

| Requisito | Clases principales | Criterios | Excepciones |
|---|---|---:|---:|
| RF-1.1 | `datos.Stand`, `datos.EstadoStand`, `negocio.ServicioStands`, `datos.Repositorio` | 3 | 5 |
| RF-1.2 | `negocio.CalculadoraPrecios`, `negocio.ServicioStands` | 3 | 2 |
| RF-2.1 | `negocio.ServicioReservaciones`, `datos.Reservacion`, `negocio.Reloj` | 8 | 8 |
| RF-2.2 | `negocio.ServicioReservaciones`, `datos.Reservacion`, `datos.Pago` | 8 | 7 |
| RF-3.1 | `negocio.ServicioStands` | 2 | 5 |
| RF-3.2 | `negocio.ServicioTransferencias` | 3 | 4 |
| RF-4.1 | `negocio.CalculadoraPrecios`, `negocio.ServicioReservaciones` | 5 | 2 |
| RF-4.2 | `configuracion.Configuracion`, `configuracion.properties` | 4 | 8 |
| RF-5.1 | `notificaciones.ServicioNotificaciones`, `notificaciones.Notificacion`, `notificaciones.TipoNotificacion` | 5 | 3 |
| RF-5.2 | `notificaciones.ServicioNotificaciones`, `notificaciones.Notificacion`, `notificaciones.TipoNotificacion` | 2 | 2 |
| RF-5.3 | `notificaciones.ServicioNotificaciones`, `notificaciones.Notificacion`, `notificaciones.TipoNotificacion` | 2 | 2 |
| RF-5.x | `notificaciones.Notificador`, `notificaciones.NotificadorConsola`, `notificaciones.ServicioNotificaciones` | 6 | 2 |
| RNF-1 | `app.Main`, `negocio.RelojSimulado` | 7 | 5 |
| RNF-2 | `datos.RepositorioEnMemoria`, `datos.Repositorio` (se puede sustituir por una implementación H2) | 3 | 2 |
| RNF-3 | Paquetes `datos`, `configuracion`, `negocio`, `notificaciones`, `app` | 1 | 0 |

**Cobertura:** 149 pruebas automatizadas; cada una está asociada al menos a un criterio o excepción.

**Nota sobre RNF-2:** el repositorio en memoria cumple el requisito de almacenamiento para pruebas. Cambiarlo por H2 solo requeriría otra implementación de la interfaz `Repositorio`; no se incluyó porque es opcional.
