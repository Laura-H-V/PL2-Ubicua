# 🎯 RESUMEN PROYECTO ANDROID - PECL3

## ✅ PROYECTO CREADO EXITOSAMENTE

He creado un proyecto Android completo en: 
**`C:\Users\laura\Documents\GitHub\PL2-Ubicua\AppAndroid`**

---

## 📱 ESTRUCTURA DE LAS 3 ACTIVITIES

### 1️⃣ MainActivity (Menú Principal)
**Propósito**: Pantalla inicial de navegación

**Características**:
- Diseño limpio con logo y título
- 2 botones principales:
  - 🌡️ "Monitoreo en Tiempo Real" → Va a RealtimeMonitoringActivity
  - 📊 "Consultar Histórico" → Va a HistoricDataActivity

**Archivo**: `MainActivity.java`
**Layout**: `activity_main.xml`

---

### 2️⃣ RealtimeMonitoringActivity (Monitoreo MQTT)
**Propósito**: Mostrar datos de sensores en tiempo real

**Características**:
- ✅ **Conexión MQTT** al broker Mosquitto
- Topic: `sensors/ST_1657/weather_station/WS_USE_1657`
- Credenciales: ubicua / ubicua1234
- Actualización automática cada 5 segundos

**Datos mostrados** (parsea JSON del ESP32):
- 🌡️ Temperatura (°C)
- 💧 Humedad (%)
- ☀️ Radiación UV (mW/cm²)
- 🔊 Nivel de Ruido (dB)
- 💨 Calidad del Aire (ppm)
- ⏰ Timestamp de última actualización

**Interfaz**:
- CardViews con diseño Material Design
- Indicador de estado de conexión
- Emojis para mejor visualización
- ScrollView para dispositivos pequeños

**Archivo**: `RealtimeMonitoringActivity.java`
**Layout**: `activity_realtime_monitoring.xml`

---

### 3️⃣ HistoricDataActivity (Consulta API REST)
**Propósito**: Consultar mediciones históricas almacenadas

**Características**:
- ✅ **Llamada API REST** con Retrofit
- Endpoint: `http://IP:8080/api/mediciones?fecha=DD-MM-YYYY`
- Input de fecha con formato DD-MM-YYYY
- Botón "Consultar" para ejecutar la búsqueda

**Funcionalidad**:
- Muestra todas las mediciones de la fecha seleccionada
- Lista scrolleable con formato legible
- Cada medición muestra: timestamp, temperatura, humedad, UV, ruido, calidad aire
- Indicador de estado (cargando, error, sin datos, éxito)

**Archivo**: `HistoricDataActivity.java`
**Layout**: `activity_historic_data.xml`

---

## 🔧 COMPONENTES TÉCNICOS

### Clases de Modelo
- **Medicion.java**: POJO para las mediciones (mapea campos de tu BD)
  - id, timestamp, temperatura, humedad, radiacion_uv, ruido_db, calidad_aire
  - Anotaciones @SerializedName para Gson

### Servicios de Red
- **ApiService.java**: Interface Retrofit con endpoints
  - `getMediciones(String fecha)`: Consulta por fecha
  - `getMedicionesPorRango(String desde, String hasta)`: Rango (opcional)

- **RetrofitClient.java**: Singleton para cliente HTTP
  - Base URL: `http://10.0.2.2:8080/` (emulador)
  - Conversor Gson incluido

### Configuración
- **AndroidManifest.xml**: Permisos y declaración de Activities
  - INTERNET
  - ACCESS_NETWORK_STATE
  - WAKE_LOCK
  - usesCleartextTraffic=true (HTTP sin SSL)

---

## 📦 DEPENDENCIAS

```gradle
// HTTP REST
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

// MQTT
implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5'
implementation 'org.eclipse.paho:org.eclipse.paho.android.service:1.1.1'

// UI
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'com.google.android.material:material:1.11.0'
```

---

## ⚙️ CONFIGURACIÓN NECESARIA

### 🚨 IMPORTANTE: Cambiar IPs antes de ejecutar

#### Para EMULADOR Android:
Deja las IPs como están (`10.0.2.2` apunta a localhost del host)

#### Para DISPOSITIVO FÍSICO:
Cambia en 2 archivos:

**1. RetrofitClient.java** (línea 12):
```java
private static final String BASE_URL = "http://TU_IP:8080/";
// Ejemplo: "http://192.168.1.76:8080/"
```

**2. RealtimeMonitoringActivity.java** (línea 27):
```java
private static final String MQTT_BROKER = "tcp://TU_IP:1883";
// Ejemplo: "tcp://192.168.1.76:1883"
```

**Obtén tu IP**:
```powershell
ipconfig
```
Busca "IPv4 Address" en tu adaptador de red.

---

## 🚀 PASOS PARA EJECUTAR

### 1. Abrir en Android Studio
```
File → Open → Selecciona: C:\Users\laura\Documents\GitHub\PL2-Ubicua\AppAndroid
```

### 2. Sincronizar Gradle
```
Click en "Sync Now" o Tools → Sync Project with Gradle Files
```

### 3. Configurar IPs (ver arriba)

### 4. Iniciar Docker
```powershell
cd C:\Users\laura\Documents\GitHub\PL2-Ubicua\ServerUbicua-master
docker compose -f Docker_compose.yml up -d
```

### 5. Verificar servicios
```powershell
docker ps
```
Deberías ver: ubicomp_db, ubicomp_mqtt, ubicomp_tomcat

### 6. Crear Emulador (si no tienes)
```
Tools → Device Manager → Create Device → Pixel 5 → API 34
```

### 7. Ejecutar App
```
Click en Run ▶️ o Shift + F10
```

---

## ✅ REQUISITOS PECL3 CUMPLIDOS

| Requisito | Cumplido | Detalles |
|-----------|----------|----------|
| 3 Activities | ✅ | MainActivity, RealtimeMonitoringActivity, HistoricDataActivity |
| Conexión API REST | ✅ | `/api/mediciones?fecha=DD-MM-YYYY` con Retrofit |
| Conexión MQTT | ✅ | Topic `sensors/ST_1657/...` con Eclipse Paho |
| Información en UI | ✅ | Todo se muestra en pantalla, no solo logs |
| Interacción con PECL2 | ✅ | Conecta con Docker (Tomcat + MQTT + PostgreSQL) |

---

## 🎨 DISEÑO DE INTERFAZ

- **Material Design** con CardViews
- **Colores consistentes** por tipo de dato
- **Emojis** para mejorar UX
- **Indicadores de estado** (conectando, error, éxito)
- **ScrollViews** para compatibilidad con pantallas pequeñas
- **Layouts responsivos** con LinearLayout y ConstraintLayout

---

## 🐛 DEBUGGING

### Ver logs en Android Studio:
```
View → Tool Windows → Logcat
```

**Filtros útiles**:
- `RealtimeMQTT`: Logs de conexión MQTT
- `HistoricData`: Logs de API REST
- `System.out`: Logs generales

### Verificar conexión MQTT desde PC:
```powershell
docker compose -f Docker_compose.yml exec mqtt sh
mosquitto_sub -h localhost -p 1883 -u ubicua -P ubicua1234 -t "sensors/ST_1657/#" -v
```

### Verificar API REST desde navegador:
```
http://localhost:8080/api/mediciones?fecha=18-12-2025
```

### Verificar datos en BD:
```powershell
docker compose -f Docker_compose.yml exec db bash
psql -U ubicomp -d ubicomp
SELECT * FROM mediciones ORDER BY timestamp DESC LIMIT 10;
```

---

## 📁 ARCHIVOS CREADOS

```
AppAndroid/
├── app/
│   ├── build.gradle                    # Configuración y dependencias
│   ├── src/main/
│   │   ├── AndroidManifest.xml         # Configuración de la app
│   │   ├── java/com/uah/estacionmeteorologica/
│   │   │   ├── MainActivity.java              # Activity 1: Menú
│   │   │   ├── RealtimeMonitoringActivity.java # Activity 2: MQTT
│   │   │   ├── HistoricDataActivity.java      # Activity 3: API REST
│   │   │   ├── Medicion.java                  # Modelo de datos
│   │   │   ├── ApiService.java                # Interface Retrofit
│   │   │   └── RetrofitClient.java            # Cliente HTTP
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── activity_main.xml
│   │       │   ├── activity_realtime_monitoring.xml
│   │       │   └── activity_historic_data.xml
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   ├── colors.xml
│   │       │   └── themes.xml
│   │       └── mipmap-*/              # Carpetas para iconos
├── build.gradle                       # Config proyecto raíz
├── settings.gradle                    # Módulos del proyecto
├── gradle.properties                  # Properties de Gradle
├── README.md                          # Documentación general
└── INSTRUCCIONES.md                   # Guía paso a paso
```

---

## 🎓 DIFERENCIAS CON EJEMPLO DEL PROFESOR

Tu app es **específica para tu proyecto**:

| Aspecto | Ejemplo Profesor | Tu App |
|---------|-----------------|--------|
| Propósito | Monitoreo genérico de calles | Estación meteorológica específica |
| Activities | Splash + Selector + Monitor | Menú + MQTT Real-time + API Histórico |
| API REST | Lista de calles | Mediciones históricas por fecha |
| MQTT | Topic dinámico por calle | Topic fijo de tu ESP32 |
| Datos | Genéricos | Temperatura, humedad, UV, ruido, aire |
| UI | Básica con logs | CardViews, emojis, indicadores estado |

---

## 📚 DOCUMENTACIÓN ADICIONAL

- **README.md**: Documentación técnica del proyecto
- **INSTRUCCIONES.md**: Guía detallada paso a paso para ejecutar
- Este archivo (**RESUMEN.md**): Visión general completa

---

## 🎯 PRÓXIMOS PASOS

1. ✅ Abre el proyecto en Android Studio
2. ✅ Configura las IPs según tu entorno
3. ✅ Ejecuta Docker
4. ✅ Prueba la app en el emulador
5. 📱 (Opcional) Prueba en dispositivo físico
6. 🎨 (Opcional) Personaliza colores/iconos
7. 📊 (Opcional) Agrega gráficas con MPAndroidChart

---

**¡Proyecto Android completo y listo para usar! 🚀**

Cualquier duda, revisa INSTRUCCIONES.md o los comentarios en el código.
