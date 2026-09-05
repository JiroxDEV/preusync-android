# PreuSync: Crónica de una Evolución Técnica y Resiliencia en el Desarrollo
**Autor:** Daniel (JiroxDEV)
**Fecha de Inicio:** 27 de marzo de 2025
**Estado del Proyecto:** v0.99.80 (Arquitectura de Grado Industrial)

## Introducción y Propósito
El 27 de marzo de 2025 inicié el desarrollo de PreuSync con el objetivo de centralizar la dispersa información académica de los preuniversitarios en Cuba. Lo que comenzó como un tablero de horarios en Sketchware Pro, se transformó mediante un proceso de aprendizaje autodidacta y refactorizaciones profundas en un ecosistema robusto. La validación inicial del sistema provino de mi profesor de Literatura y Lengua, el Licenciado Luis Enrique Guerrero Soto, cuyo asombro ante la integración de múltiples herramientas en una sola interfaz confirmó que la arquitectura debía evolucionar hacia un estándar profesional.

## Ingeniería frente a Restricciones: El Factor Cuba
Una característica fundamental de PreuSync es la ausencia de los servicios de Google Firebase, denegados para el territorio cubano. Esta limitación me obligó a diseñar y construir desde cero componentes que el estándar de la industria suele delegar a la nube de Google:

1. **Sincronización Híbrida de Fondo:** En lugar de Firebase Cloud Messaging (FCM), desarrollé una arquitectura de tres capas para asegurar la persistencia de datos. Utilizo WorkManager para tareas periódicas, AlarmManager con `setExactAndAllowWhileIdle` para recordatorios críticos y un Foreground Service (`PersistentService`) con una notificación discreta de baja prioridad. Este sistema garantiza la ejecución de sincronización incluso ante las políticas de ahorro de energía más agresivas de fabricantes como Samsung en Android 13+.
2. **Autenticación y Seguridad:** Ante la falta de Firebase Auth, implementé mi propio sistema de gestión de identidad. Evolucionó de contraseñas en texto plano a un sistema de Hash SHA-256 con Salt único por usuario. En el cliente, desarrollé `SecurePreferencesHelper` para cifrar los tokens de sesión mediante Android Keystore (API 23+), asegurando que la identidad del usuario esté protegida a nivel de hardware.
3. **Persistencia y Cifrado de Datos:** La caché local transitó de archivos planos con ofuscación XOR manual hacia una base de datos relacional con Room. Para cumplir con altos estándares de privacidad, integré cifrado AES-GCM (Galois/Counter Mode) con derivación de claves mediante PBKDF2 y un Salt aleatorio generado por instalación, blindando la información sensible incluso en dispositivos vulnerables.

## Evolución del Backend y la Integridad de Datos
La primera infraestructura se basó en Google Apps Script y Google Sheets. Durante esta fase, enfrenté problemas de concurrencia que corrompían las tablas. Resolví esto migrando los identificadores de tipo `Integer` a `Long` (Timestamps) en la v0.70.0 para evitar colisiones. En junio de 2026, realicé la migración definitiva a una API en Node.js alojada en Render, utilizando PostgreSQL en Supabase.

Esta transición requirió una decisión de ingeniería difícil: reiniciar la base de usuarios para garantizar la integridad del nuevo esquema relacional y el uso de JSONB para datos flexibles. Implementé una lógica de "Célula como Unidad de Verdad" para el horario, donde cada registro representa una combinación de Grupo-Día-Turno, permitiendo un resaltado en tiempo real con precisión absoluta sincronizado con la base de datos.

## El Motor de Renderizado: Markwon vs. WebView
El tratamiento del contenido educativo fue uno de los mayores desafíos técnicos. Durante la v0.45.0, dediqué horas diarias a intentar personalizar la librería Markwon para renderizar fórmulas LaTeX y tablas complejas mediante Spans de Android. Tras agotar las posibilidades técnicas de `SpannableString` —que impedía implementar funciones como botones de copiar en bloques de código o bordes redondeados en tablas—, tomé la decisión pragmática de migrar a un motor basado en WebView.

Desarrollé el `MarkdownHtmlGenerator`, un procesador que transforma el Markdown en HTML/CSS personalizado, permitiendo el resaltado de sintaxis para 160 lenguajes mediante Prism4j y la visualización de fórmulas matemáticas con JLatexMath, manteniendo una calidad de publicación profesional sin sacrificar el rendimiento de la aplicación.

## Estandarización y Refactorización Masiva
Mi formación se consolidó mediante la refactorización. Utilicé el entorno DayDream para acceder a componentes Material 1.14 y el CLI de Termux para gestionar cambios masivos en el código fuente. Mediante scripts complejos en `sed` y `find`, renombré actividades monolíticas y recursos, purgué el idioma español del código interno y extraje más de 1500 líneas de lógica hacia una arquitectura de Helpers centralizados (`DialogHelper`, `FilePicker`, `AvatarHelper`).

La adopción final de Android Studio permitió implementar el patrón MVVM estricto. Todos los componentes de la interfaz ahora heredan de `BaseViewModel` y `BaseFragment`, gestionando un sistema unificado de cuatro estados (`LOADING`, `SUCCESS`, `EMPTY`, `ERROR`) y utilizando `PaginationState` para un scroll infinito fluido y libre de excepciones de tipo `IndexOutOfBoundsException`.

## Interfaz de Usuario y Accesibilidad
La identidad visual celeste de PreuSync se alinea con el color del uniforme de los estudiantes preuniversitarios en Cuba. Para garantizar la accesibilidad, desarrollé el `ImageTextColorHelper`, un algoritmo que calcula la luminancia relativa según el estándar WCAG 2.1 y aplica corrección Gamma para ajustar automáticamente el color del texto y sus sombras sobre imágenes de fondo, asegurando la legibilidad en cualquier condición de iluminación.

## Filosofía de Eficiencia y Futuro Nacional
Mantener la aplicación por debajo de los 13 MB es una meta técnica constante. En un contexto donde los datos son limitados, cada recurso debe estar optimizado; por ello, implementé un modo de ahorro de datos que escala dinámicamente las imágenes con Glide. Administro el sistema mediante comandos CURL y gestión directa de endpoints para mantener un control absoluto sobre la infraestructura.

PreuSync ha dejado de ser un proyecto escolar para convertirse en una plataforma con visión nacional. La arquitectura actual es **Multi-Tenancy**, preparada para segmentar horarios, eventos y efemérides locales según la jerarquía Provincia > Municipio > Escuela. Este sistema es mi respuesta técnica a la pregunta de un usuario que marcó el futuro del proyecto: esto ya no es solo para mi escuela, es un nexo para todos los estudiantes del país.
