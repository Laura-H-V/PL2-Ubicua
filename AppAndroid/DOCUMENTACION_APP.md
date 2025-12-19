# 📱 Documentación Aplicación Android - Estación Meteorológica

## 📋 Índice
1. [Descripción General](#descripción-general)
2. [MainActivity - Pantalla Principal](#mainactivity---pantalla-principal)
3. [Monitoreo en Tiempo Real](#monitoreo-en-tiempo-real)
4. [Consulta de Datos Históricos](#consulta-de-datos-históricos)
5. [Pantalla de Detalles en Grande](#pantalla-de-detalles-en-grande-datadetailsactivity)
6. [Visualización de Gráficas](#visualización-de-gráficas)
7. [Sistema de Notificaciones Push](#sistema-de-notificaciones-push)
8. [Historial de Alertas](#historial-de-alertas)
9. [Tecnologías Utilizadas](#tecnologías-utilizadas)

---

## 📖 Descripción General

La aplicación Android desarrollada permite monitorear y gestionar en tiempo real los datos procedentes de la **Estación Meteorológica ST_1657**. Esta estación está equipada con un microcontrolador ESP32 y múltiples sensores ambientales que miden temperatura, humedad, radiación ultravioleta, niveles de ruido y calidad del aire.

El sistema completo funciona mediante una arquitectura IoT donde los sensores publican sus lecturas a través del protocolo MQTT hacia un broker Mosquitto. Paralelamente, estos datos se almacenan en una base de datos PostgreSQL, la cual es accesible mediante una API REST desarrollada en Jakarta EE y desplegada en un servidor Tomcat. La aplicación Android actúa como cliente consumidor de ambos servicios, permitiendo visualizar datos en tiempo real (MQTT) y consultar el historial almacenado (REST API).

### Funcionalidades Principales
- **Monitoreo en tiempo real:** Conexión directa al broker MQTT para recibir lecturas de sensores al instante
- **Consulta de datos históricos:** Acceso a la base de datos mediante API REST con filtros por fecha única o rango de fechas
- **Visualización ampliada de datos:** Pantalla dedicada para mostrar mediciones en formato grande y legible con código de colores
- **Visualización de gráficas interactivas:** Representación gráfica de la evolución temporal de los datos y cálculo de promedios
- **Sistema de notificaciones push:** Servicio en segundo plano que escucha alertas y notifica al usuario cuando se superan umbrales críticos
- **Historial de alertas:** Almacenamiento local persistente de todas las notificaciones recibidas con posibilidad de consulta posterior
- **Modo claro/oscuro con preferencia guardada:** Conmutador visible en la pantalla principal que recuerda la elección del usuario y aplica el tema antes de renderizar la UI

---

## 🏠 MainActivity - Pantalla Principal

La MainActivity es el punto de entrada de la aplicación y funciona como un hub central de navegación. Al abrir la aplicación, el usuario se encuentra con una pantalla limpia y organizada que presenta cuatro botones principales, cada uno con un color distintivo para facilitar la identificación visual.

### Diseño de la Interfaz

La pantalla muestra en la parte superior el título "Estación Meteorológica" junto con el identificador "Estación ST_1657", proporcionando contexto inmediato al usuario sobre qué sistema está monitoreando. Los botones están organizados verticalmente en el centro de la pantalla:

- **Botón verde "Monitoreo en Tiempo Real":** Abre la pantalla de visualización de datos en vivo
- **Botón azul "Datos Históricos":** Permite consultar mediciones pasadas almacenadas en la base de datos
- **Botón naranja "Gráficas":** Muestra visualizaciones interactivas de los datos
- **Botón púrpura "Historial de Alertas":** Accede al registro de notificaciones recibidas

En la parte inferior se muestra un pequeño texto con la versión de la aplicación.

### Modo claro/oscuro

En la esquina superior derecha hay un pequeño texto "Modo oscuro" junto a un switch compacto. Al activarlo se aplica el tema oscuro mediante `AppCompatDelegate`, y la preferencia queda guardada en `SharedPreferences` para que el tema se cargue antes de dibujar la UI en siguientes aperturas.

### Funcionalidad de Inicio Automático

Una de las características más importantes de la MainActivity es que, al momento de abrirse, inicia automáticamente el servicio de monitoreo de alertas en segundo plano. Este servicio se mantiene activo incluso si el usuario cierra la aplicación, garantizando que se continúen recibiendo notificaciones de alertas críticas.

### Gestión de Permisos

La aplicación solicita automáticamente los permisos necesarios para funcionar correctamente. En particular, para dispositivos con Android 13 o superior, se solicita explícitamente el permiso para mostrar notificaciones. Este proceso es transparente para el usuario y se realiza mediante un diálogo estándar del sistema operativo que aparece la primera vez que se ejecuta la aplicación.

La navegación entre pantallas se implementa de forma fluida, donde al pulsar cualquier botón, el usuario es llevado inmediatamente a la funcionalidad correspondiente sin retrasos perceptibles.

---

## 📊 Monitoreo en Tiempo Real

La funcionalidad de monitoreo en tiempo real es una de las características más destacadas de la aplicación, permitiendo ver las lecturas de los sensores conforme se generan, sin necesidad de actualizar manualmente la pantalla.

### Tecnología MQTT y Comunicación en Tiempo Real

Esta funcionalidad utiliza el protocolo MQTT (Message Queue Telemetry Transport), un estándar de comunicación ligero y eficiente especialmente diseñado para dispositivos IoT. La aplicación se conecta a un broker Mosquitto que actúa como intermediario central de mensajes. Los sensores del ESP32 publican sus lecturas en un topic específico llamado "sensors/ST_1657/weather_station/WS_USE_1657", y la aplicación se suscribe a este mismo topic para recibir las actualizaciones.

La conexión se establece utilizando la librería Eclipse Paho MQTT, que proporciona todas las herramientas necesarias para manejar la comunicación. Para el emulador de Android, la dirección del broker es 10.0.2.2:1883 (que representa el localhost del ordenador host), mientras que para dispositivos físicos se utiliza la dirección IP real del servidor en la red local.

### Interfaz de Usuario

La pantalla de monitoreo presenta seis áreas de texto claramente diferenciadas con emojis identificativos:

- 🌡️ **Temperatura:** Muestra el valor en grados Celsius
- 💧 **Humedad:** Porcentaje de humedad relativa del ambiente
- ☀️ **Radiación UV:** Índice de radiación ultravioleta
- 🔊 **Ruido:** Nivel de ruido ambiental en decibelios
- 💨 **Calidad del Aire:** Concentración de partículas en partes por millón
- 🕒 **Última actualización:** Timestamp de la última lectura recibida

### Funcionamiento Interno

Al abrir esta pantalla, la aplicación inmediatamente inicia un proceso de conexión al broker MQTT en un hilo separado para no bloquear la interfaz de usuario. Una vez establecida la conexión (que incluye autenticación con usuario y contraseña "ubicua"), la aplicación se suscribe al topic de sensores con calidad de servicio QoS 1, garantizando que cada mensaje se entregue al menos una vez.

Cuando llega un nuevo mensaje desde los sensores, este viene en formato JSON con todos los valores de las mediciones. La aplicación parsea automáticamente este JSON utilizando la librería Gson, extrae cada valor individual y actualiza inmediatamente los campos de texto en la interfaz. Esta actualización ocurre en el hilo principal de la UI para garantizar que los cambios sean visibles instantáneamente.

El sistema está configurado para reconectar automáticamente si se pierde la conexión con el broker, lo que proporciona robustez ante problemas temporales de red. Cuando el usuario sale de esta pantalla, la conexión MQTT se cierra correctamente para liberar recursos.

### Ventajas de este Enfoque

Este sistema permite ver los cambios ambientales en tiempo real, lo cual es crucial para aplicaciones de monitoreo meteorológico donde las condiciones pueden cambiar rápidamente. La arquitectura publish-subscribe de MQTT hace que múltiples dispositivos puedan recibir los mismos datos simultáneamente sin duplicar el trabajo de los sensores.

---

## 📈 Consulta de Datos Históricos

Esta funcionalidad permite acceder a todas las mediciones que han sido almacenadas en la base de datos PostgreSQL a lo largo del tiempo. A diferencia del monitoreo en tiempo real que solo muestra los datos actuales, aquí el usuario puede buscar información de días anteriores específicos.

### Interfaz de Búsqueda

La pantalla presenta un diseño simple e intuitivo. En la parte superior hay un campo de texto donde el usuario puede ingresar una fecha en formato DD-MM-YYYY (por ejemplo, 18-12-2025). Debajo de este campo se encuentra un botón "Consultar" que ejecuta la búsqueda. Los resultados aparecen en un área desplazable que ocupa el resto de la pantalla.

### Comunicación con el Servidor

La aplicación utiliza la librería Retrofit para realizar llamadas HTTP a la API REST del servidor. Cuando el usuario introduce una fecha y pulsa consultar, se envía una petición GET al endpoint del servidor con la fecha como parámetro. El servidor procesa esta petición, busca en la base de datos PostgreSQL todas las mediciones que coincidan con esa fecha y devuelve los resultados en formato JSON.

La configuración de Retrofit se implementa siguiendo el patrón Singleton, lo que significa que solo existe una instancia del cliente HTTP en toda la aplicación. Esto optimiza el uso de recursos y mantiene la coherencia en todas las comunicaciones con el servidor. Para el emulador, la URL base es http://10.0.2.2:8080/, mientras que para dispositivos físicos se utiliza la dirección IP real del servidor.

### Modelo de Datos

Cada medición recuperada contiene información completa: un identificador único, la marca temporal exacta de cuando se tomó la lectura, y los valores de todos los sensores (temperatura, humedad, radiación UV, ruido y calidad del aire). La librería Gson se encarga automáticamente de convertir el JSON recibido del servidor en objetos Java utilizables por la aplicación.

### Visualización de Resultados

Una vez recibidos los datos del servidor, la aplicación los presenta de forma clara y organizada. Para cada medición se crea dinámicamente un bloque de texto que incluye:

- La fecha y hora exacta de la medición
- Temperatura y humedad en la primera línea con sus respectivos emojis
- Radiación UV y nivel de ruido en una segunda línea
- Calidad del aire en una tercera línea
- Una línea separadora visual entre mediciones

Si no hay datos disponibles para la fecha consultada, la aplicación muestra un mensaje amigable informando al usuario. Del mismo modo, si hay problemas de conexión con el servidor, se notifica al usuario mediante un mensaje toast en la parte inferior de la pantalla.

### Validaciones y Manejo de Errores

La aplicación verifica que el campo de fecha no esté vacío antes de intentar realizar la consulta. Si el usuario intenta buscar sin introducir una fecha, aparece un mensaje solicitando que ingrese la información requerida. Toda la comunicación con el servidor se realiza de forma asíncrona, lo que significa que la interfaz permanece responsive y el usuario puede seguir interactuando con la aplicación mientras se espera la respuesta del servidor.

Este diseño asíncrono es fundamental para proporcionar una buena experiencia de usuario, especialmente en conexiones más lentas donde la respuesta del servidor podría tardar varios segundos.

### Selector de Tipo de Búsqueda - Fecha Única vs Rango

La interfaz de consulta histórica ha sido mejorada con un **selector de modo de búsqueda** mediante RadioButtons que permite elegir entre dos opciones:

**📅 Fecha Única (Opción por defecto):** Al seleccionar esta opción, aparece un único campo de texto donde el usuario introduce una fecha específica en formato DD-MM-YYYY. Este modo es ideal para consultas puntuales de un día concreto.

**📆 Rango de Fechas:** Al activar este modo, la interfaz muestra dos campos de texto: "Desde" y "Hasta". El usuario debe rellenar ambos campos para definir el período completo que desea consultar. Si solo se rellena el campo "Desde", el sistema automáticamente consultará únicamente ese día.

El cambio entre modos es instantáneo - los campos de entrada se muestran y ocultan dinámicamente según la selección, manteniendo la interfaz limpia y enfocada en las opciones relevantes.

### Pantalla de Detalles en Grande (DataDetailsActivity)

Cuando el usuario pulsa el botón "Consultar", la aplicación **abre automáticamente una nueva Activity** que presenta los resultados en un formato grande y optimizado para lectura. Esta pantalla de detalles tiene las siguientes características:

**Encabezado Informativo:**
- Título "📊 Resultados de la Consulta" en tamaño HeadlineMedium
- Contador de registros: "Total: X registros" en color secundario

**Tarjetas de Datos Ampliadas:**

Cada medición se presenta en una tarjeta (MaterialCardView) blanca con sombra de 4dp y esquinas redondeadas de 24dp. El contenido dentro de cada tarjeta incluye:

**Contraste en temas claros y oscuros:** Los títulos de cada línea usan texto negro explícito sobre la tarjeta blanca para mantener legibilidad también en modo oscuro; los valores mantienen su código de color por sensor.

**Fecha y Hora Formateada:**
- Ubicada en la parte superior con emoji 📅
- Formato limpio: `2025-12-02 07:00:00` (se eliminan la T y la Z del formato ISO)
- Color azul oscuro (`holo_blue_dark`)
- Tamaño TitleLarge para máxima legibilidad
- Margen inferior de 32dp para separarla de los datos

**Datos de Sensores en Líneas Separadas:**

Cada sensor se presenta en su propia línea horizontal con un diseño de dos columnas:
- Columna izquierda: Emoji + nombre del sensor (ej: "🌡️ Temperatura")
- Columna derecha: Valor con unidad, alineado a la derecha y en negrita

Código de colores por sensor:
- 🌡️ Temperatura → Rojo (`holo_red_light`)
- 💧 Humedad → Azul (`holo_blue_light`)
- ☀️ Radiación UV → Naranja (`holo_orange_light`)
- 🔊 Ruido → Púrpura (`holo_purple`)
- 💨 Calidad del Aire → Verde (`holo_green_light`)

**Espaciado Generoso:**
- Padding de 48dp en todo el contenido de la tarjeta
- 24dp de separación entre cada línea de dato
- 32dp de margen entre tarjetas

Este diseño espacioso y con colores diferenciados hace que los datos sean perfectamente legibles incluso a distancia, ideal para presentaciones o cuando se necesita mostrar información a varias personas simultáneamente.

**Desplazamiento Vertical:**

La pantalla utiliza un NestedScrollView que permite desplazarse suavemente a través de todas las mediciones, sin límites en la cantidad de resultados que se pueden mostrar.

**Formato de Timestamp Mejorado:**

El sistema automáticamente procesa las fechas que vienen del servidor en formato ISO 8601 (`2025-12-02T07:00:00Z`) y las transforma a un formato más legible eliminando la "T" y la "Z": `2025-12-02 07:00:00`. Esta transformación se realiza mediante el método `replace()` aplicado al timestamp antes de mostrarlo.

### Flujo Completo de Consulta

1. Usuario selecciona "Fecha única" o "Rango de fechas"
2. Introduce la(s) fecha(s) en formato DD-MM-YYYY
3. Opcionalmente activa filtros adicionales (Máx/Mín de un sensor)
4. Pulsa "Consultar"
5. La aplicación muestra mensaje "⏳ Consultando..." en el tvEstado
6. Se realiza petición HTTP al servidor vía Retrofit
7. Al recibir respuesta exitosa:
   - Se procesa la lista de mediciones
   - Se aplican filtros si hay alguno seleccionado
   - Se abre DataDetailsActivity con los datos
   - Se muestra mensaje "X registros encontrados"
8. En la nueva pantalla, el usuario puede desplazarse por todas las mediciones en formato grande

---

## 📉 Visualización de Gráficas

La funcionalidad de gráficas proporciona una forma visual e intuitiva de analizar los datos meteorológicos, permitiendo identificar tendencias, patrones y anomalías que serían difíciles de detectar mirando solo números.

### Librería de Gráficas MPAndroidChart

Para implementar esta funcionalidad se utiliza MPAndroidChart, una de las bibliotecas más populares y completas para crear gráficas interactivas en Android. Esta librería permite crear gráficas profesionales con capacidades de zoom, desplazamiento y animaciones fluidas.

### Opciones de Filtrado

La pantalla de gráficas ofrece flexibilidad en la consulta de datos mediante dos modos de filtrado:

**Modo Fecha Única:** El usuario puede seleccionar este modo para ver los datos de un día específico. Al activar esta opción, aparece un campo de texto donde se introduce la fecha en formato DD-MM-YYYY.

**Modo Rango de Fechas:** Esta opción permite analizar datos de múltiples días consecutivos. Al seleccionar este modo, aparecen dos campos de texto: uno para la fecha de inicio y otro para la fecha de fin. El sistema automáticamente recuperará todas las mediciones comprendidas entre ambas fechas.

El cambio entre estos dos modos es inmediato y se realiza mediante botones de radio (radio buttons), donde solo uno puede estar seleccionado a la vez. La interfaz se adapta dinámicamente mostrando u ocultando los campos de fecha relevantes según la selección del usuario.

### Tipos de Gráficas Disponibles

La aplicación ofrece dos visualizaciones complementarias:

**Gráfica de Evolución Temporal (LineChart):**

Esta gráfica muestra cómo han variado la temperatura y la humedad a lo largo del tiempo. Utiliza un sistema de líneas donde el eje horizontal representa el tiempo (cada punto es una medición) y el eje vertical muestra los valores medidos.

La temperatura se representa con una línea roja, mientras que la humedad utiliza una línea azul, facilitando su diferenciación visual. Cada punto en la línea es clickeable, permitiendo al usuario ver el valor exacto en ese momento. Para mantener la legibilidad, si hay más de 20 mediciones, la aplicación automáticamente limita la visualización a las primeras 20, evitando que la gráfica se vuelva demasiado densa y difícil de interpretar.

Las gráficas de línea son especialmente útiles para identificar tendencias: ¿está subiendo la temperatura?, ¿hay ciclos diarios claros?, ¿existen picos anómalos? Todas estas preguntas se responden fácilmente con un vistazo a la gráfica.

**Gráfica de Promedios (BarChart):**

Este tipo de visualización utiliza barras verticales para mostrar el valor promedio de cada sensor durante el período consultado. Cada barra representa un sensor diferente, identificado claramente en el eje horizontal:

- Temperatura en grados Celsius
- Humedad en porcentaje
- Radiación UV (multiplicada por 10 para mejor visualización)
- Nivel de ruido (dividido por 10 para escalar adecuadamente)
- Calidad del aire (dividida por 100 para ajustar la escala)

Los valores se escalan para que todas las barras sean visualmente comparables, ya que de otro modo algunos sensores con rangos muy diferentes dominarían la gráfica. Las barras utilizan colores del esquema Material Design, proporcionando un aspecto moderno y profesional.

Esta gráfica es ideal para obtener un resumen rápido de las condiciones generales durante un período: ¿cuál fue la temperatura media?, ¿los niveles de ruido fueron normalmente altos o bajos?

### Proceso de Generación de Gráficas

Cuando el usuario selecciona qué gráfica quiere ver, la aplicación realiza los siguientes pasos:

1. **Validación:** Verifica que se hayan introducido las fechas necesarias según el modo seleccionado

2. **Consulta de Datos:** Si es fecha única, hace una sola petición al servidor. Si es rango de fechas, hace múltiples peticiones (una por cada día en el rango) y combina todos los resultados

3. **Procesamiento:** En el caso de la gráfica de barras, calcula los promedios de todos los valores recibidos

4. **Renderizado:** Construye la gráfica con los datos procesados y la muestra en pantalla

5. **Interactividad:** Habilita funciones táctiles como zoom con pellizco (pinch) y desplazamiento horizontal

### Experiencia de Usuario

Un detalle importante del diseño es que las gráficas se muestran dentro de tarjetas (CardView) que inicialmente están ocultas. Solo cuando el usuario pulsa uno de los botones de gráfica, la tarjeta correspondiente se hace visible con una animación suave. Si el usuario decide cambiar de tipo de gráfica, la anterior se oculta y aparece la nueva, manteniendo la interfaz limpia y enfocada en una visualización a la vez.

Los campos de fecha están vacíos por defecto, dando al usuario total control sobre qué período desea analizar sin imponer fechas predeterminadas que podrían no ser relevantes para su caso de uso.

---

## 🔔 Sistema de Notificaciones Push

El sistema de notificaciones es una de las funcionalidades más avanzadas de la aplicación, permitiendo recibir alertas automáticas incluso cuando la aplicación no está abierta. Este sistema funciona mediante un servicio en segundo plano que monitorea constantemente las condiciones meteorológicas.

### Concepto de Foreground Service

A diferencia de una aplicación normal que se detiene cuando el usuario la cierra, este sistema utiliza un servicio en primer plano (foreground service). Este tipo de servicio muestra una notificación persistente que indica al usuario que el servicio está activo, cumpliendo con los requisitos de transparencia de Android. La notificación persistente dice "Monitoreo de Estación Meteorológica - Escuchando alertas en segundo plano..." y garantiza que el sistema operativo no mate el proceso para ahorrar batería.

### Inicio Automático

El servicio se inicia automáticamente cuando el usuario abre la MainActivity por primera vez. Esto significa que el usuario no necesita hacer nada especial para activar el monitoreo de alertas; simplemente al abrir la app, ya está protegido y recibirá notificaciones de cualquier condición peligrosa.

### Conexión MQTT en Segundo Plano

El servicio establece su propia conexión independiente con el broker MQTT, suscribiéndose al topic de alertas "alertas/ST_1657/weather_station/WS_USE_1657". Esta conexión se mantiene activa las 24 horas, escuchando cualquier mensaje que llegue. Si por alguna razón se pierde la conexión (por ejemplo, si el WiFi se desconecta momentáneamente), el sistema espera 5 segundos y automáticamente intenta reconectar, garantizando la continuidad del monitoreo.

### Umbrales de Alerta Configurados

El sistema verifica constantemente cinco condiciones críticas:

**Temperatura Máxima (35°C):** Si la temperatura supera los 35 grados Celsius, se considera peligroso y se genera una alerta de "Temperatura Alta".

**Humedad Mínima (20%):** Una humedad por debajo del 20% indica condiciones extremadamente secas que pueden ser problemáticas, generando una alerta de "Humedad Baja".

**Radiación UV Máxima (10):** Un índice UV superior a 10 representa niveles extremos de radiación que pueden causar daños en la piel en minutos, activando una alerta de "Radiación UV Alta".

**Ruido Máximo (75 dB):** Niveles de ruido superiores a 75 decibelios durante períodos prolongados pueden ser dañinos para la audición, generando una alerta de "Ruido Excesivo".

**Calidad del Aire (350 ppm):** Concentraciones de partículas superiores a 350 partes por millón indican mala calidad del aire, activando una alerta de "Calidad de Aire Mala".

### Procesamiento de Mensajes

Cada vez que llega un mensaje MQTT al servicio, este parsea el JSON para extraer todos los valores de sensores. Luego, compara cada valor contra su umbral correspondiente. Si alguno supera el límite establecido, el sistema activa inmediatamente el proceso de generación de alerta.

### Generación de Notificaciones

Cuando se detecta una condición de alerta, ocurren tres cosas simultáneamente:

**Almacenamiento:** La alerta se guarda en el historial local de la aplicación usando SharedPreferences, permitiendo que el usuario pueda consultarla más tarde incluso sin conexión a internet.

**Notificación Visual:** Aparece una notificación en el panel de notificaciones de Android con un título descriptivo (por ejemplo "🌡️ Temperatura Alta") y el mensaje detallado con el valor exacto y el límite superado.

**Retroalimentación Táctil y Sonora:** El dispositivo vibra con un patrón específico (vibración-pausa-vibración-pausa-vibración) y reproduce el sonido de notificación predeterminado del sistema, asegurando que el usuario note la alerta incluso si el dispositivo está en modo silencioso pero con vibración activada.

### Canales de Notificación

Para dispositivos con Android 8 o superior, las notificaciones se organizan en dos canales separados:

**Canal de Alertas:** Tiene prioridad alta, lo que significa que las notificaciones aparecerán en la pantalla incluso si el usuario está usando otra aplicación. Incluye vibración y sonido habilitados.

**Canal del Servicio:** Tiene prioridad baja para no molestar al usuario. Solo muestra la notificación persistente que indica que el servicio está activo, sin sonido ni vibración.

Esta separación permite que el usuario, si lo desea, pueda configurar en los ajustes del sistema cómo quiere recibir cada tipo de notificación, manteniendo el control sobre su experiencia.

### Interacción con las Notificaciones

Cuando el usuario pulsa sobre una notificación de alerta, la aplicación se abre automáticamente en la pantalla de Historial de Alertas, permitiendo ver el contexto completo de todas las alertas recibidas. Las notificaciones son auto-cancelables, lo que significa que desaparecen del panel de notificaciones una vez que el usuario las ha visto.

### Persistencia del Servicio

El servicio está configurado con la bandera START_STICKY, lo que instruye al sistema operativo Android a reiniciar el servicio si por alguna razón necesita matarlo para liberar memoria. Esto garantiza la máxima disponibilidad del sistema de monitoreo, aunque en condiciones extremas de falta de memoria podría haber interrupciones temporales.

---

## 📋 Historial de Alertas

Esta funcionalidad proporciona un registro completo y persistente de todas las alertas que ha recibido el usuario, permitiendo revisar el historial incluso días o semanas después de que ocurrieron los eventos.

### Almacenamiento Persistente Local

A diferencia de las notificaciones normales de Android que desaparecen cuando se descartan, este sistema guarda cada alerta en la memoria interna del dispositivo utilizando SharedPreferences. SharedPreferences es un sistema de almacenamiento clave-valor que Android proporciona para guardar datos de forma permanente.

Cada alerta se almacena con cuatro campos principales:

- **Título:** El encabezado descriptivo con emoji (por ejemplo "🌡️ Temperatura Alta")
- **Mensaje:** La descripción detallada del problema con los valores exactos
- **Tipo:** Una categoría que identifica qué sensor causó la alerta (TEMPERATURA, HUMEDAD, UV, RUIDO, AIRE)
- **Timestamp:** La fecha y hora exacta en milisegundos desde 1970, permitiendo ordenar cronológicamente

El sistema serializa estas alertas en formato JSON utilizando la librería Gson y las guarda todas juntas en una única entrada de SharedPreferences. Esta solución es eficiente y permite guardar estructuras de datos complejas como si fueran texto simple.

### Límite de Almacenamiento

Para evitar que el almacenamiento crezca indefinidamente y consuma espacio excesivo, el sistema mantiene un máximo de 100 alertas. Cuando se alcanza este límite, las alertas más antiguas se eliminan automáticamente para dar espacio a las nuevas. Este enfoque FIFO (First In, First Out) garantiza que siempre se conservan las alertas más recientes y relevantes.

### Interfaz de Usuario

La pantalla de historial presenta un diseño limpio y organizado:

En la parte superior, un título con el emoji de portapapeles indica claramente la función de la pantalla. Inmediatamente debajo hay un botón rojo "Borrar Todas las Alertas" que permite limpiar completamente el historial si el usuario lo desea.

### Visualización con RecyclerView

El listado de alertas utiliza un componente RecyclerView, que es la forma más eficiente de mostrar listas largas en Android. A diferencia de crear todos los elementos de una vez, RecyclerView solo crea los elementos visibles en pantalla más algunos adicionales en buffer, reciclando las vistas conforme el usuario se desplaza. Esto hace que la interfaz sea fluida incluso con 100 alertas.

Cada alerta se muestra dentro de una tarjeta (CardView) con sombra sutil que la separa visualmente de las demás. La tarjeta contiene:

**Encabezado:** Una línea superior con el título de la alerta a la izquierda en negrita, y un pequeño badge circular a la derecha mostrando el tipo de alerta con color codificado.

**Mensaje:** El cuerpo principal de la tarjeta muestra el mensaje detallado explicando qué umbral se superó y por cuánto.

**Pie de tarjeta:** En la esquina inferior derecha, en texto pequeño gris, aparece la fecha y hora formateada legiblemente (por ejemplo "18/12/2025 14:30:45").

### Código de Colores

Para facilitar la identificación rápida del tipo de problema, cada alerta usa un color específico en su badge:

- **TEMPERATURA:** Naranja (#FF5722) - Asociado al calor
- **HUMEDAD:** Azul (#2196F3) - Asociado al agua
- **UV:** Amarillo (#FFEB3B) - Asociado al sol
- **RUIDO:** Púrpura (#9C27B0) - Color distintivo para destacar
- **AIRE:** Gris (#607D8B) - Asociado a la niebla y contaminación

Esta codificación cromática permite al usuario escanear visualmente el historial y rápidamente identificar, por ejemplo, si ha habido muchas alertas de temperatura versus alertas de ruido.

### Orden Cronológico

Las alertas se muestran en orden cronológico inverso, es decir, las más recientes aparecen primero en la parte superior de la lista. Esto es intuitivo porque normalmente el usuario está más interesado en lo que ha ocurrido recientemente.

### Funcionalidad de Borrado

El botón "Borrar Todas las Alertas" está protegido con un diálogo de confirmación. Cuando el usuario lo pulsa, aparece una ventana emergente preguntando "¿Estás seguro de que quieres borrar todas las alertas?" con dos opciones: "Sí" y "No". Esto evita borrados accidentales que podrían resultar frustrantes.

Si el usuario confirma, todas las alertas se eliminan de SharedPreferences y la lista se actualiza inmediatamente mostrando el mensaje "No hay alertas en el historial" con el botón de borrado deshabilitado.

### Estado Vacío

Cuando no hay alertas en el historial (ya sea porque es la primera vez que se abre la app, o porque se acaban de borrar), el RecyclerView se oculta y en su lugar aparece un mensaje centrado en texto gris que indica "No hay alertas en el historial". Esto proporciona retroalimentación clara al usuario sobre por qué la pantalla está vacía.

### Ventajas de este Diseño

Este enfoque de almacenamiento local tiene varias ventajas importantes:

**Disponibilidad offline:** El usuario puede consultar el historial incluso sin conexión a internet, ya que los datos están en el dispositivo.

**Privacidad:** Las alertas no se envían a ningún servidor externo, manteniéndose completamente en el dispositivo del usuario.

**Rendimiento:** La consulta es instantánea ya que no requiere llamadas de red.

**Persistencia:** Los datos sobreviven al cierre de la aplicación e incluso al reinicio del dispositivo.

---

## 🛠️ Tecnologías Utilizadas

La aplicación se construye sobre un conjunto cuidadosamente seleccionado de tecnologías y librerías modernas que proporcionan funcionalidades robustas y bien mantenidas.

### Material Design

La interfaz utiliza los componentes de Material Design, el sistema de diseño de Google para Android. Esto proporciona una experiencia de usuario consistente y familiar, con botones, tarjetas y otros elementos que siguen las guías de diseño actuales de Android. Material Design no solo hace que la aplicación se vea moderna, sino que también garantiza accesibilidad y usabilidad en diferentes tamaños de pantalla.

### Retrofit para Comunicación REST

Retrofit es la librería líder en Android para realizar llamadas HTTP a APIs REST. Simplifica enormemente el proceso de hacer peticiones de red, manejar respuestas y convertir JSON a objetos Java. En lugar de escribir todo el código de bajo nivel para manejar conexiones HTTP, Retrofit permite definir las APIs como interfaces Java simples, y la librería se encarga del resto automáticamente.

La integración con Gson permite que los datos JSON recibidos del servidor se conviertan automáticamente en objetos Medicion sin necesidad de parseo manual. Esto reduce dramáticamente la posibilidad de errores y hace el código mucho más limpio y mantenible.

### Gson para Procesamiento JSON

Gson es una librería de Google para serializar y deserializar objetos Java a y desde JSON. Se utiliza en múltiples lugares de la aplicación: para parsear las respuestas de la API REST, para procesar los mensajes MQTT que llegan en tiempo real, y para guardar y recuperar alertas de SharedPreferences. Su capacidad de manejar automáticamente la conversión entre nombres de campos Java (por ejemplo "radiacionUv") y nombres JSON (por ejemplo "radiacion_uv") mediante anotaciones facilita enormemente el trabajo con APIs externas.

### Eclipse Paho MQTT

Eclipse Paho es la implementación de referencia del protocolo MQTT para múltiples plataformas. Esta librería proporciona un cliente MQTT completo con funcionalidades como reconexión automática, gestión de suscripciones, manejo de calidad de servicio (QoS) y persistencia de mensajes. 

La versión para Android incluye un servicio específicamente diseñado para trabajar con el ciclo de vida de aplicaciones móviles, manejando correctamente situaciones como cuando la aplicación pasa a segundo plano o cuando el dispositivo entra en modo de ahorro de energía.

### MPAndroidChart

MPAndroidChart es una de las bibliotecas de gráficas más completas y populares para Android, con más de 30,000 estrellas en GitHub. Soporta más de ocho tipos diferentes de gráficas, todas con capacidades interactivas como zoom, desplazamiento, animaciones y personalización extensiva de colores, estilos y formatos.

Lo que hace especial a MPAndroidChart es que está optimizada para el rendimiento en dispositivos móviles, pudiendo manejar miles de puntos de datos sin lag perceptible. Las animaciones son fluidas y la interacción táctil responde instantáneamente, proporcionando una experiencia de usuario premium.

### CardView y RecyclerView

CardView proporciona un contenedor con esquinas redondeadas y sombras que sigue los principios de Material Design. Se usa para presentar cada alerta en el historial de forma visualmente atractiva y separada.

RecyclerView es el componente moderno de Android para mostrar listas y cuadrículas de datos. Reemplaza al antiguo ListView con un diseño mucho más eficiente que recicla las vistas que salen de la pantalla en lugar de destruirlas y crearlas de nuevo. Esto hace que las listas con muchos elementos sean fluidas y consuman poca memoria.

### Compatibilidad y Versiones

La aplicación está diseñada para funcionar en Android 7.0 (API 24) o superior, lo que cubre aproximadamente el 95% de los dispositivos Android activos a finales de 2025. Al mismo tiempo, está compilada contra Android 14 (API 34), lo que permite utilizar las últimas características del sistema operativo cuando están disponibles.

El código está escrito en Java 8, utilizando características modernas como expresiones lambda, referencias a métodos y la API de streams cuando es apropiado, haciendo el código más conciso y legible.

### Configuración de Dependencias

Todas las librerías se gestionan mediante Gradle, el sistema de construcción estándar para Android. Las dependencias se especifican con versiones específicas para garantizar reproducibilidad: si la aplicación funciona en un entorno, funcionará exactamente igual en otro.

Los repositorios incluyen Google Maven (para componentes oficiales de Android), Maven Central (el repositorio principal de librerías Java), JitPack (para MPAndroidChart) y el repositorio de Eclipse (para Paho MQTT). Esta configuración multi-repositorio garantiza acceso a todas las librerías necesarias.

### Permisos del Sistema

La aplicación requiere varios permisos del sistema Android para funcionar:

**INTERNET y ACCESS_NETWORK_STATE:** Necesarios para cualquier comunicación de red, ya sea MQTT o REST API.

**WAKE_LOCK:** Permite mantener la CPU del dispositivo activa cuando llegan mensajes MQTT, asegurando que no se pierdan alertas críticas porque el dispositivo esté en suspensión profunda.

**VIBRATE:** Permite activar el motor de vibración del dispositivo cuando se recibe una alerta.

**FOREGROUND_SERVICE:** Requerido para ejecutar el servicio de monitoreo en primer plano de forma continua.

**POST_NOTIFICATIONS:** Introducido en Android 13, este permiso debe solicitarse explícitamente al usuario para poder mostrar notificaciones. La aplicación lo solicita automáticamente la primera vez que se abre.

La aplicación también utiliza `usesCleartextTraffic="true"` en el manifiesto, lo cual permite conexiones HTTP no cifradas. Esto es necesario porque el servidor de desarrollo usa HTTP en lugar de HTTPS. En un entorno de producción, esto debería cambiarse a HTTPS para mayor seguridad.

---

## 📊 Resumen Final

La aplicación Android desarrollada para la Estación Meteorológica ST_1657 representa una solución completa e integrada para el monitoreo de condiciones ambientales. Combina múltiples tecnologías modernas (MQTT, REST, gráficas interactivas, notificaciones push) en una interfaz de usuario coherente y fácil de usar.

Las seis pantallas principales (MainActivity, Monitoreo en Tiempo Real, Consulta de Datos Históricos, Detalles en Grande, Gráficas y Historial de Alertas) cubren todos los casos de uso relevantes: desde la visualización instantánea de datos actuales hasta el análisis de tendencias históricas con presentación ampliada, pasando por un sistema de alertas proactivo que mantiene al usuario informado de condiciones peligrosas incluso cuando no está usando activamente la aplicación.

El diseño técnico sigue las mejores prácticas de desarrollo Android, utilizando patrones establecidos como Singleton para clientes HTTP, Repository para almacenamiento, y Service para operaciones en segundo plano. La arquitectura está preparada para escalar y mantener a futuro.

---

**Versión:** 1.0  
**Fecha:** 19 de Diciembre de 2025  
**Estación:** ST_1657 - Weather Station USE_1657  
**Plataforma:** Android 7.0+ (API 24-34)  
**Tecnologías Principales:** MQTT, REST API, MPAndroidChart, Material Design
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

---

## 🔐 Permisos y Configuración

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permisos requeridos -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:usesCleartextTraffic="true"
        android:theme="@style/Theme.MaterialComponents.Light.DarkActionBar">
        
        <!-- Activities -->
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <activity android:name=".RealtimeMonitoringActivity" />
        <activity android:name=".HistoricDataActivity" />
        <activity android:name=".ChartsActivity" />
        <activity android:name=".AlertHistoryActivity" />
        
        <!-- Servicios -->
        <service 
            android:name=".MqttBackgroundService"
            android:foregroundServiceType="dataSync"
            android:exported="false" />
        
        <service android:name="org.eclipse.paho.android.service.MqttService" />
    </application>
</manifest>
```

### Descripción de Permisos

| Permiso | Propósito |
|---------|-----------|
| `INTERNET` | Conexiones HTTP (REST) y MQTT |
| `ACCESS_NETWORK_STATE` | Verificar disponibilidad de red |
| `WAKE_LOCK` | Mantener CPU activa para MQTT |
| `VIBRATE` | Vibración en notificaciones de alerta |
| `FOREGROUND_SERVICE` | Servicio MQTT en segundo plano |
| `POST_NOTIFICATIONS` | Mostrar notificaciones (Android 13+) |

### Configuración de Red

#### Para Emulador
```java
// 10.0.2.2 apunta al localhost del host
private static final String BROKER_URL = "tcp://10.0.2.2:1883";
private static final String BASE_URL = "http://10.0.2.2:8080/";
```

#### Para Dispositivo Físico
```java
// IP del servidor en la red local
private static final String BROKER_URL = "tcp://192.168.1.76:1883";
private static final String BASE_URL = "http://192.168.1.76:8080/";
```

---

## 📊 Resumen de Funcionalidades

### Comparativa de Features

| Funcionalidad | Tecnología | Persistencia | Tiempo Real | Offline |
|---------------|------------|--------------|-------------|---------|
| Monitoreo en Tiempo Real | MQTT | ❌ No | ✅ Sí | ❌ No |
| Datos Históricos | REST API | ✅ BD PostgreSQL | ❌ No | ❌ No |
| Gráficas | MPAndroidChart | ✅ BD PostgreSQL | ❌ No | ❌ No |
| Notificaciones Push | MQTT Background | ✅ SharedPreferences | ✅ Sí | ❌ No |
| Historial Alertas | Local Storage | ✅ SharedPreferences | ✅ Sí | ✅ Sí |

### Flujo de Datos

```
┌─────────────┐
│   ESP32     │ (Sensores físicos)
│ + Sensores  │
└──────┬──────┘
       │ WiFi
       ▼
┌─────────────────┐
│ Mosquitto MQTT  │ (Puerto 1883)
│   Broker        │
└────┬─────┬──────┘
     │     │
     │     └─────────────────────┐
     │                           │
     ▼                           ▼
┌──────────────┐        ┌──────────────────┐
│ SensorService│        │ App Android      │
│ (Subscriber) │        │ - Realtime       │
└──────┬───────┘        │ - Notifications  │
       │                └──────────────────┘
       ▼
┌──────────────┐
│  PostgreSQL  │ (Puerto 5432)
│   Database   │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ REST API     │ (Puerto 8080)
│ (Tomcat)     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ App Android  │
│ - Historic   │
│ - Charts     │
└──────────────┘
```

---

## 🎯 Conclusiones

La aplicación Android implementa un **sistema completo de monitoreo IoT** con las siguientes capacidades:

✅ **Comunicación bidireccional** con infraestructura IoT (MQTT + REST)  
✅ **Visualización en tiempo real** de datos de sensores  
✅ **Análisis histórico** con filtros avanzados  
✅ **Representación gráfica** interactiva de tendencias  
✅ **Sistema de alertas proactivo** con notificaciones push  
✅ **Persistencia de datos** local y remota  
✅ **Diseño Material Design** responsive y moderno  

### Cumplimiento de Requisitos PECL3

| Requisito | Estado | Detalles |
|-----------|--------|----------|
| Mínimo 3 Activities | ✅ Superado | 6 Activities implementadas |
| Conexión API REST | ✅ Cumplido | Retrofit con endpoints de mediciones |
| Conexión MQTT | ✅ Cumplido | Eclipse Paho en tiempo real y background |
| Visualización datos | ✅ Cumplido | Realtime + Histórico + Detalles + Gráficas |
| Sistema de alertas | ⭐ Extra | Push notifications con vibración |
| Historial persistente | ⭐ Extra | SharedPreferences con Gson |
| Pantalla detalles grande | ⭐ Extra | DataDetailsActivity con diseño ampliado |

---

**Versión:** 1.0  
**Fecha:** 18 de Diciembre de 2025  
**Estación:** ST_1657 - Weather Station USE_1657  
**Plataforma:** Android 7.0+ (API 24-34)
