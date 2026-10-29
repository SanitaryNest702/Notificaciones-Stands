# Requisitos funcionales y no funcionales

**Sistema administrativo de venta de stands — FILEY**

Documento adaptado a partir de los requerimientos del proyecto escolar[cite: 1, 2]. Reúne los requisitos funcionales y no funcionales del sistema enfocados en el módulo de notificaciones, su épica o ámbito, descripción, prioridad y estado[cite: 1, 2].

## 1. Lista de requisitos funcionales y no funcionales

| ID | Épica / ámbito | Descripción del requisito | Imp. | Fact. | Puntaje | Prioridad | Estado / validación |
|---|---|---|---:|---:|---:|---|---|
| RF-1.1 | EPIC-01 | El sistema deberá registrar, para cada stand, su identificador, precio, metros cuadrados y su estado (Disponible, Reservado, Ocupado o Bloqueado)[cite: 1, 2]. | 5 | 5 | 5.0 | Alta | |
| RF-1.2 | EPIC-01 | El sistema deberá calcular el costo de cada stand en función de sus dimensiones y el precio por metro cuadrado configurado[cite: 1, 2]. | 4 | 5 | 4.4 | Alta | |
| RF-2.1 | EPIC-02 | El sistema deberá permitir reservar un stand Disponible a nombre de una editorial, estableciendo un tiempo de vigencia[cite: 1, 2]. | 5 | 5 | 5.0 | Alta | |
| RF-2.2 | EPIC-02 | El sistema deberá registrar los pagos de una editorial sobre su reservación para calcular el porcentaje del costo cubierto y definir el pase a Ocupado[cite: 1, 2]. | 5 | 4 | 4.6 | Alta | |
| RF-3.1 | EPIC-03 | El sistema deberá permitir al administrador bloquear stands como negociación interna previa a la salida del sistema, pudiendo regresarlos a Disponible[cite: 1, 2]. | 4 | 5 | 4.4 | Alta | |
| RF-3.2 | EPIC-03 | El sistema deberá permitir transferir un stand Bloqueado a Reservado directamente a nombre de una editorial (Opcional)[cite: 1, 2]. | 3 | 4 | 3.4 | Media | Opcional[cite: 2] |
| RF-4.1 | EPIC-04 | El sistema deberá aplicar el descuento por pronto pago si se realiza antes de la fecha establecida, y omitirlo después de esta[cite: 1, 2]. | 5 | 4 | 4.6 | Alta | |
| RF-4.2 | EPIC-04 | El sistema deberá manejar una fecha máxima para cubrir el costo total del stand y una configuración de costos y plazos[cite: 1, 2]. | 5 | 5 | 5.0 | Alta | |
| RF-5.1 | EPIC-05 | El sistema deberá notificar a las editoriales cuando su reserva esté a punto de vencer[cite: 1, 2]. | 5 | 4 | 4.6 | Alta | |
| RF-5.2 | EPIC-05 | El sistema deberá notificar a las editoriales cuando el plazo de pronto pago esté a punto de vencer[cite: 1, 2]. | 5 | 4 | 4.6 | Alta | |
| RF-5.3 | EPIC-05 | El sistema deberá notificar a las editoriales cuando la fecha límite para cubrir el costo esté a punto de vencer[cite: 1, 2]. | 5 | 4 | 4.6 | Alta | |
| RNF-1 | TRANSVERSAL | El sistema deberá poder operarse completamente desde la línea de comandos[cite: 1, 2]. | 5 | 5 | 5.0 | Alta | |
| RNF-2 | TRANSVERSAL | El sistema podrá utilizar una base de datos en memoria como H2 para pruebas (Opcional)[cite: 1, 2]. | 4 | 5 | 4.4 | Alta | Opcional[cite: 2] |
| RNF-3 | TRANSVERSAL | El sistema deberá organizarse en paquetes (paquete de clases de lógica de negocios, paquete de notificaciones y configuración)[cite: 1, 2]. | 3 | 4 | 3.4 | Media | Sugerencia[cite: 2] |

## Épicas y ámbitos

| Código | Épica / ámbito | Descripción |
|---|---|---|
| EPIC-01 | Gestión del mapa de stands | Agrupa los requisitos relacionados con el mapeo de los espacios, identificadores, dimensiones, precios y estados[cite: 1, 2]. |
| EPIC-02 | Reserva, pago y ocupación | Agrupa los requisitos de reserva con vigencia, registro de pagos y porcentaje para pasar a ocupado[cite: 1, 2]. |
| EPIC-03 | Bloqueo y transferencia de stands | Agrupa los apartados del administrador previos al despliegue y transferencia opcional[cite: 1, 2]. |
| EPIC-04 | Costos, descuentos y plazos | Agrupa la configuración de costos, plazos, fechas límite y descuentos por pronto pago[cite: 1, 2]. |
| EPIC-05 | Notificaciones | Agrupa las notificaciones sobre reservas, pronto pago y fechas límite por vencer[cite: 1, 2]. |
| TRANSVERSAL | Operación y arquitectura | Agrupa la ejecución por línea de comandos, organización en paquetes y almacenamiento opcional en memoria[cite: 1, 2]. |

## 2. Método de priorización

Se utiliza una **Matriz de Priorización Ponderada** con dos criterios[cite: 1]:

| Criterio | Escala | Peso | Interpretación |
|---|---|---|---|
| Importancia | 1–5 | 60% | Necesidad y valor del requisito para el proceso[cite: 1]. |
| Factibilidad | 1–5 | 40% | Viabilidad de implementación actual[cite: 1]. |

**Puntaje:** 
`Puntaje = (Importancia × 0.60) + (Factibilidad × 0.40)`[cite: 1]

## 3. Justificación de la priorización

- **Prioridad alta:** Concentra el núcleo del proyecto solicitado, destacando el mapa de stands, las reservas con vigencia, el registro de pagos, la configuración de costos y plazos, y de forma primordial el módulo de notificaciones[cite: 1, 2].
- **Prioridad media:** Corresponde a elementos opcionales o sugerencias de empaquetado y transferencias de bloques[cite: 1, 2].

## 4. Reglas de negocio: transiciones de estado de un stand

| Desde | Hacia | Quién / cuándo |
|---|---|---|
| Disponible | Reservado | Una editorial reserva el stand con vigencia[cite: 1, 2] |
| Disponible | Bloqueado | El administrador bloquea el stand antes de la salida del sistema[cite: 1, 2] |
| Bloqueado | Disponible | El administrador libera el bloqueo[cite: 1, 2] |
| Bloqueado | Reservado | El administrador transfiere el stand a una editorial (Opcional)[cite: 1, 2] |
| Reservado | Ocupado | Los pagos cubren el porcentaje mínimo configurado[cite: 1, 2] |
| Reservado | Disponible | La reserva vence sin cubrir el porcentaje mínimo[cite: 1, 2] |
| Ocupado | — | Estado final[cite: 1] |