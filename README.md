# RecordatoriosContextuales — Actividad 2

Proyecto actualizado a partir de la Actividad 1. Kotlin + Jetpack Compose + Material 3 + Navigation Compose + ViewModel + StateFlow.

## Abrir
1. Descomprimir y abrir la carpeta en Android Studio.
2. Esperar **Gradle Sync** (requiere Internet para descargar dependencias nuevas).
3. Elegir el emulador Pixel 6 API 35 y pulsar **Run app**.

## Pantallas
- Listado: tarjetas pulsables y botón **+ Nuevo**.
- Creación: título, descripción, categoría, prioridad y botones Guardar/Cancelar.
- Detalle: tocar una tarjeta para abrirla; se transporta únicamente el ID.

## Evidencias
- Listado: seleccionar **Datos**.
- Vacío: seleccionar **Vacío** y capturar el botón **Crear recordatorio**.
- Error: seleccionar **Error** y capturar **Reintentar**. Para volver a datos, tocar **Datos**.
- Creación: pulsar **+ Nuevo**, completar formulario y capturar.
- Detalle: abrir un recordatorio de la lista.
- Estructura: expandir el paquete en el panel Android de Android Studio.

## Consideraciones
- Los recordatorios nuevos se almacenan **solo en memoria**; al reiniciar el proceso se restauran los cinco datos de muestra.
- Los botones Datos/Vacío/Error simulan estados para evidencias, no borran datos.
- No se integran aún avisos reales ni ubicación; se conservan como datos opcionales del modelo.
- Esta entrega no fue compilada ni ejecutada en el entorno de generación. Validar con Gradle Sync y Run en Android Studio.
