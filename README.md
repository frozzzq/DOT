# **Dot** — *Consigue un balance entre tu vida y tu periodo*

**Proyecto Integrador** · Licenciatura en Ingeniería en Software (Desarrollo de Aplicaciones Nativas)

---

## Descripción y Propósito

Las aplicaciones actuales de seguimiento menstrual en el mercado (como Flo o Clue) presentan limitaciones críticas para las usuarias: restringen funciones e información esencial de salud detrás de modelos de suscripción (*paywalls*), operan bajo políticas cuestionables de privacidad y comercialización de datos sensibles, y dependen de calendarios predictivos rígidos. Cuando existen ciclos irregulares o intervienen factores hormonales externos —como la toma de anticoncepción de emergencia—, estos algoritmos estáticos fallan y emiten alertas de retraso descontextualizadas que generan ansiedad innecesaria.

**Dot** nace como una alternativa **100% gratuita, empática y contextual**. Su propósito es ayudar a las usuarias a comprender qué ocurre fisiológicamente en cada etapa de su ciclo menstrual mediante un motor de cálculo adaptable y comunicación clara basada en evidencia, eliminando alertas alarmistas y garantizando el control total sobre la privacidad de su información médica.

---

## Pilares Técnicos y Arquitectura

El proyecto está diseñado bajo estándares modernos de ingeniería de software para dispositivos móviles, priorizando el rendimiento local, la mantenibilidad y la confidencialidad de los datos:

- **Desarrollo Android Nativo:** Construido íntegramente en **Kotlin**, aprovechando corrutinas (`Coroutines`) y flujos reactivos (`StateFlow` / `Flow`) para el manejo asíncrono. Interfaz de usuario declarativa implementada con **Jetpack Compose** bajo lineamientos de Material Design 3.
- **Clean Architecture + MVVM:** Separación estricta de responsabilidades en capas (`Presentation`, `Domain` y `Data`). El núcleo de negocio y las reglas de predicción residen en la capa de dominio sin dependencias del framework, facilitando las pruebas unitarias y la escalabilidad.
- **Persistencia Local *Offline-First*:** Almacenamiento primario en el dispositivo mediante **Room (SQLite)**. Todas las funciones de registro, consulta de historial y cálculo de proyecciones operan sin requerir conexión a internet.
- **Privacidad *Zero-Knowledge* y Cifrado Local:** Los datos sensibles de salud se cifran directamente en el dispositivo cliente utilizando **AES-256-GCM**, con llaves criptográficas generadas y resguardadas por hardware a través de **Android Keystore** antes de cualquier persistencia externa.
- **Sincronización Segura en AWS:** Respaldo opcional en la nube sobre infraestructura de **Amazon Web Services (AWS)**. Gracias al enfoque *Zero-Knowledge*, los paquetes viajan y se almacenan cifrados de extremo a extremo, impidiendo que terceros o el propio servidor puedan leer la información clínica de la usuaria.

---

## Estructura del Proyecto

```text
DOT/
├── app/                  # Código fuente de la aplicación nativa Android (Data, Domain, UI)
├── docs/                 # Documentación técnica y artefactos de ingeniería de software
│   └── REQUERIMIENTOS.md # Especificación formal de Requisitos Funcionales y No Funcionales
└── README.md             # Documentación general del repositorio
```

Para consultar el detalle de las reglas de negocio, módulos del sistema y atributos de calidad, revisa la [Especificación de Requerimientos](docs/REQUERIMIENTOS.md).

---

## Equipo de Desarrollo — DOT TEAM

- **Camila Miguely Macías Hernández** — *Líder de Proyecto*
- **Josué Vega Gallardo** — *Desarrollador de Software*
- **Carlos Ignacio Verduzco Gastélum** — *Desarrollador de Software*
- **Elías Romero Valdez Gómez** — *Desarrollador de Software*
