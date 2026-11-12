# Especificación de Requerimientos de Software (SRS) — Dot

**Proyecto:** Dot — Aplicación móvil de seguimiento menstrual contextual  
**Programa:** Licenciatura en Ingeniería en Software (Desarrollo de Aplicaciones Nativas)  
**Equipo:** DOT TEAM  

---

## 1. Requisitos Funcionales (RF)

Los requisitos funcionales definen el comportamiento interno del sistema, el procesamiento de datos clínicos de la usuaria y los flujos de interacción dentro de la aplicación.

### RF-01: Cuestionario Inicial (Onboarding)
- **Descripción:** Al iniciar la aplicación por primera vez, el sistema debe presentar un flujo guiado de configuración inicial para construir la línea base fisiológica de la usuaria.
- **Datos capturados:**
  - Fecha de inicio del último periodo menstrual.
  - Duración promedio del sangrado (en días).
  - Duración promedio del ciclo completo (en días).
  - Tipo de regularidad percibida (regular, irregular o desconocida).
  - Uso de métodos anticonceptivos habituales (hormonales, de barrera, DIU, ninguno, etc.).
- **Criterio de aceptación:** El sistema valida la coherencia de los rangos ingresados, persiste el perfil base localmente y genera la primera proyección del ciclo sin requerir conexión de red.

### RF-02: Registro Diario de Seguimiento
- **Descripción:** El sistema debe permitir a la usuaria registrar eventos físicos, síntomas y actividad sexual asociados a una fecha específica del calendario.
- **Datos capturados:**
  - Presencia y nivel de flujo menstrual (ligero, moderado, abundante, manchado/spotting).
  - Síntomas físicos y anímicos predeterminados.
  - Registro de relaciones sexuales e indicación del uso de métodos de protección.
- **Criterio de aceptación:** Los registros diarios se almacenan de forma transaccional en la base de datos local y actualizan en tiempo real el estado visual del calendario.

### RF-03: Módulo de Anticoncepción de Emergencia
- **Descripción:** El sistema debe contar con un módulo especializado para registrar la ingesta de anticoncepción hormonal de emergencia (pastilla del día siguiente) e integrarla como variable crítica de alteración del ciclo.
- **Datos capturados:**
  - Fecha y hora aproximada de la toma.
  - Tipo de compuesto activo (ej. Levonorgestrel o Acetato de ulipristal, o selección asistida).
  - Existencia de protección previa en el acto asociado.
  - Registro de relaciones sexuales posteriores a la toma dentro del mismo ciclo.
- **Criterio de aceptación:** Al guardar el evento, el sistema dispara automáticamente una solicitud de actualización al motor de predicción (RF-04) y activa las reglas de acompañamiento contextual (RF-05).

### RF-04: Motor de Predicción y Recálculo Contextual
- **Descripción:** El sistema debe calcular las fases estimadas del ciclo y la ventana probable del próximo sangrado, ajustando dinámicamente sus proyecciones cuando se detecten variaciones fisiológicas o eventos externos.
- **Reglas de negocio:**
  - Ajustar los márgenes de predicción e incertidumbre tras el registro de anticoncepción de emergencia, considerando el impacto hormonal según la fase del ciclo en que fue administrada.
  - Adaptar el cálculo histórico en perfiles con ciclos irregulares, utilizando rangos de probabilidad en lugar de fechas fijas inflexibles.
- **Criterio de aceptación:** Cualquier registro que altere los parámetros del ciclo recalcula de inmediato las fechas estimadas en la capa de dominio y refleja el nuevo intervalo en la interfaz.

### RF-05: Sistema de Mensajería Contextual y Empática
- **Descripción:** El sistema debe informar el estado del ciclo mediante mensajes claros, objetivos y libres de lenguaje alarmista, contextualizando los retrasos o alteraciones explicables.
- **Reglas de negocio:**
  - Evitar la emisión de alertas sugerentes de embarazo o notificaciones de "retraso crítico" cuando exista un registro reciente de anticoncepción de emergencia o historial de irregularidad documentada.
  - Explicar los efectos secundarios esperados (adelanto o retraso del sangrado, manchado intermenstrual, cambios en el flujo) derivados de factores registrados por la usuaria.
- **Criterio de aceptación:** Ante una desviación en la fecha esperada del periodo con causa registrada, la interfaz sustituye el contador estándar de retraso por una tarjeta informativa contextualizada.

### RF-06: Módulo Educativo Gratuito
- **Descripción:** El sistema debe integrar una sección de consulta con contenido informativo verificado sobre salud menstrual, fases del ciclo, métodos anticonceptivos y efectos de la anticoncepción de emergencia.
- **Criterio de aceptación:** Todo el catálogo de artículos y guías debe ser accesible de manera local (offline) y sin bloqueos por suscripción.

### RF-07: Historial y Estadísticas de Ciclos
- **Descripción:** El sistema debe recopilar y presentar un resumen histórico de los ciclos previos de la usuaria para facilitar el autoconocimiento y la consulta médica.
- **Datos visualizados:**
  - Listado cronológico de ciclos pasados con fechas de inicio y fin.
  - Promedio y variabilidad de duración del ciclo y duración del sangrado.
  - Frecuencia de síntomas e incidencia de eventos hormonales extraordinarios (como tomas de emergencia).
- **Criterio de aceptación:** Las métricas se calculan dinámicamente a partir de los registros confirmados en la base de datos local, excluyendo o señalizando ciclos atípicos alterados por anticoncepción de emergencia para no sesgar el promedio habitual.

### RF-08: Edición y Corrección de Registros
- **Descripción:** El sistema debe permitir a la usuaria modificar o eliminar registros de fechas anteriores (inicio/fin de sangrado, síntomas, relaciones o tomas de emergencia) en caso de error de captura u omisión.
- **Criterio de aceptación:** Al editar o eliminar un registro pasado, el sistema revalida la integridad de la línea temporal y ejecuta nuevamente el motor de predicción (RF-04) y el cálculo de estadísticas (RF-07).

---

## 2. Requisitos No Funcionales (RNF)

Los requisitos no funcionales establecen las restricciones de arquitectura, seguridad, rendimiento y atributos de calidad del sistema.

### RNF-01: Privacidad y Seguridad de Datos (Enfoque *Zero-Knowledge*)
- **Especificación:** Toda la información sensible de salud debe cifrarse localmente en el dispositivo antes de ser enviada a cualquier servicio externo de respaldo.
- **Implementación técnica:** Uso del algoritmo **AES-256-GCM** gestionado mediante **Android Keystore** para el resguardo seguro de llaves en hardware. La sincronización en **AWS** almacena únicamente bloques cifrados (*ciphertext*). Queda estrictamente prohibida la recopilación de datos con fines publicitarios, analítica invasiva, venta o monetización a terceros.

### RNF-02: Modelo 100% Gratuito sin *Paywalls*
- **Especificación:** La totalidad de las funcionalidades del sistema —incluyendo el motor de recálculo contextual, el módulo de anticoncepción de emergencia, las estadísticas históricas y el contenido educativo— debe estar disponible para todas las usuarias sin costos ocultos, suscripciones premium ni anuncios intrusivos.

### RNF-03: Usabilidad y Diseño Empático Libre de Ansiedad
- **Especificación:** La interfaz de usuario (UI) y la experiencia de usuario (UX) deben diseñarse bajo principios de comunicación calmada y accesible.
- **Implementación técnica:** Evitar el uso de códigos de color de peligro (rojos intensos de alerta) o iconografía de advertencia ante retrasos menstruales explicables. La navegación en **Jetpack Compose** debe ser intuitiva, permitiendo registrar eventos diarios en un máximo de 3 interacciones.

### RNF-04: Rendimiento y Operación *Offline-First*
- **Especificación:** La aplicación debe ser completamente funcional sin conexión a internet. La red se utilizará exclusivamente para la sincronización asíncrona opcional de respaldos cifrados en AWS.
- **Métrica de calidad:** Las operaciones de lectura/escritura local y el recálculo del ciclo menstrual deben ejecutarse en hilos secundarios (`Dispatchers.IO` / `Dispatchers.Default`) con un tiempo de respuesta en interfaz inferior a 200 ms, garantizando una tasa de refresco fluida sin bloqueos del hilo principal (UI Thread).

### RNF-05: Compatibilidad y Arquitectura Nativa en Android
- **Especificación:** El sistema debe desarrollarse de forma 100% nativa para la plataforma Android utilizando **Kotlin** y **Jetpack Compose**, estructurado bajo **Clean Architecture + MVVM**.
- **Alcance técnico:** Soporte para dispositivos con **Android 8.0 (API nivel 26)** o superior, asegurando compatibilidad con las APIs criptográficas modernas de Android Keystore y adaptabilidad a distintas densidades de pantalla.

### RNF-06: Integridad y Persistencia de Datos Local
- **Especificación:** El almacenamiento en el dispositivo debe garantizar las propiedades ACID (Atomicidad, Consistencia, Aislamiento y Durabilidad) mediante **Room Persistence Library** sobre **SQLite**.
- **Implementación técnica:** Definición estricta de esquemas relacionales, claves foráneas y migraciones controladas para prevenir la corrupción o pérdida de registros históricos ante cierres inesperados de la aplicación o actualizaciones de versión.
