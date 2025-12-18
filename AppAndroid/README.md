# 📱 Aplicación Android - Estación Meteorológica

Aplicación Android para monitoreo de datos meteorológicos en tiempo real y consulta histórica.

## 🎯 Características

- **3 Activities** cumpliendo requisitos PECL3
- **Conexión MQTT** para datos en tiempo real
- **API REST** para consultas históricas
- **Interfaz intuitiva** con Material Design

## 🏗️ Estructura de Activities

### 1. MainActivity
Menú principal con dos opciones:
- Monitoreo en Tiempo Real
- Consultar Histórico

### 2. RealtimeMonitoringActivity
- Conexión MQTT al broker Mosquitto
- Topic: `sensors/ST_1657/weather_station/WS_USE_1657`
- Muestra en tiempo real:
  - Temperatura
  - Humedad
  - Radiación UV
  - Nivel de ruido
  - Calidad del aire

### 3. HistoricDataActivity
- Consulta API REST: `http://IP:8080/api/mediciones`
- Filtro por fecha (DD-MM-YYYY)
- Lista scrolleable con resultados

## 📦 Dependencias

- **Retrofit 2.9.0**: Cliente HTTP para API REST
- **Gson**: Serialización JSON
- **Eclipse Paho MQTT**: Cliente MQTT

## ⚙️ Configuración

### Importante: Configurar IPs

Antes de ejecutar, edita estos archivos:

1. **RetrofitClient.java**:
```java
private static final String BASE_URL = "http://TU_IP:8080/";
```

2. **RealtimeMonitoringActivity.java**:
```java
private static final String MQTT_BROKER = "tcp://TU_IP:1883";
```

### Para Emulador Android
- Usa `10.0.2.2` (apunta a localhost del host)

### Para Dispositivo Físico
- Usa la IP real de tu ordenador (ej: `192.168.1.76`)
- Obtén tu IP con: `ipconfig` (Windows) o `ifconfig` (Linux/Mac)

## 🚀 Cómo Ejecutar

1. **Abre el proyecto en Android Studio**:
   - File → Open → Selecciona carpeta `AppAndroid`

2. **Sincroniza Gradle**:
   - Tools → Sync Project with Gradle Files

3. **Configura las IPs** (ver arriba)

4. **Asegúrate que tu servidor está corriendo**:
   ```powershell
   docker compose -f Docker_compose.yml up -d
   ```

5. **Ejecuta la app**:
   - Run → Run 'app'

## 📱 Crear AVD (Emulador)

1. Tools → Device Manager
2. Create Device
3. Selecciona: Pixel 5 (recomendado)
4. System Image: API 34 (Android 14)
5. Finish

## 🔧 Troubleshooting

### Error de conexión MQTT/HTTP
- Verifica que Docker está corriendo
- Verifica la IP configurada
- Para emulador, debe ser `10.0.2.2`
- Para dispositivo físico, debe estar en la misma red WiFi

### Gradle sync failed
- File → Invalidate Caches → Invalidate and Restart

## 📋 Requisitos PECL3 Cumplidos

✅ 3 Activities  
✅ Conexión API REST (consulta histórico)  
✅ Conexión MQTT (monitoreo tiempo real)  
✅ Información mostrada en UI  

## 👤 Autor

PECL3 - Computación Ubicua  
Universidad de Alcalá
