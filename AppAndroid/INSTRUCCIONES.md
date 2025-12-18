# INSTRUCCIONES PARA ABRIR EN ANDROID STUDIO

## 📱 Pasos para abrir y ejecutar el proyecto

### 1. Abrir Android Studio
- Inicia Android Studio
- Selecciona: **File → Open**
- Navega a: `C:\Users\laura\Documents\GitHub\PL2-Ubicua\AppAndroid`
- Click en **OK**

### 2. Sincronizar Gradle
- Espera a que Android Studio indexe el proyecto
- Si aparece un banner que dice "Gradle files have changed", click en **Sync Now**
- O manualmente: **File → Sync Project with Gradle Files**

### 3. IMPORTANTE: Configurar IPs

Antes de ejecutar, debes cambiar las IPs por la IP de tu ordenador:

#### Para obtener tu IP:
```powershell
ipconfig
```
Busca la línea "IPv4 Address" en tu adaptador WiFi/Ethernet (ej: 192.168.1.76)

#### Archivos a modificar:

**a) RetrofitClient.java** (línea 12):
```java
private static final String BASE_URL = "http://192.168.1.76:8080/";
```

**b) RealtimeMonitoringActivity.java** (línea 27):
```java
private static final String MQTT_BROKER = "tcp://192.168.1.76:1883";
```

**NOTA**: 
- Si usas **emulador**: deja `10.0.2.2` (apunta a localhost del host)
- Si usas **dispositivo físico**: cambia por tu IP real

### 4. Verificar que Docker está corriendo

Abre PowerShell y ejecuta:
```powershell
cd C:\Users\laura\Documents\GitHub\PL2-Ubicua\ServerUbicua-master
docker compose -f Docker_compose.yml up -d
```

Verifica que los 3 contenedores están activos:
```powershell
docker ps
```
Deberías ver: ubicomp_db, ubicomp_mqtt, ubicomp_tomcat

### 5. Crear Emulador (AVD)

Si no tienes un emulador configurado:

1. En Android Studio: **Tools → Device Manager**
2. Click en **Create Device**
3. Selecciona: **Pixel 5** (recomendado)
4. Click **Next**
5. Descarga **API Level 34** (Android 14) si no está instalado
6. Selecciona la imagen descargada
7. Click **Next → Finish**

### 6. Ejecutar la App

1. Selecciona el emulador/dispositivo en la barra superior
2. Click en el botón **Run** (▶️ verde) o presiona **Shift + F10**
3. Espera a que se construya el proyecto y se instale la app

### 7. Probar la App

#### MainActivity (Menú)
- Verás 2 botones: "Monitoreo en Tiempo Real" y "Consultar Histórico"

#### Monitoreo en Tiempo Real
- Click en el primer botón
- Verás "Conectando a MQTT..."
- Si todo está bien, aparecerán los datos en tiempo real del ESP32
- Los valores se actualizan cada 5 segundos

#### Consultar Histórico
- Click en el segundo botón
- Ingresa una fecha en formato: **18-12-2025**
- Click en "Consultar"
- Verás una lista con todas las mediciones de esa fecha

## 🐛 Solución de Problemas

### Error: "Failed to connect to /10.0.2.2:8080"
- Verifica que Docker está corriendo: `docker ps`
- Verifica que Tomcat está en puerto 8080: `http://localhost:8080`
- Si usas dispositivo físico, cambia `10.0.2.2` por tu IP real

### Error: "MqttException: Connection refused"
- Verifica que Mosquitto está corriendo
- Prueba desde tu PC: `mosquitto_sub -h localhost -p 1883 -u ubicua -P ubicua1234 -t "#"`

### Error: "Gradle sync failed"
- **File → Invalidate Caches → Invalidate and Restart**
- Asegúrate de tener conexión a Internet (para descargar dependencias)

### Los datos no se muestran en tiempo real
- Verifica que el ESP32 está conectado y publicando datos
- Revisa los logs en Android Studio: **View → Tool Windows → Logcat**
- Filtra por: "RealtimeMQTT"

### La consulta histórica no devuelve resultados
- Verifica que hay datos en la base de datos
- Conecta a PostgreSQL y ejecuta: `SELECT COUNT(*) FROM mediciones;`
- Asegúrate de usar el formato correcto: DD-MM-YYYY

## 📊 Verificar Base de Datos

```powershell
docker compose -f Docker_compose.yml exec db bash
psql -U ubicomp -d ubicomp
\c ubicomp
SELECT * FROM mediciones ORDER BY timestamp DESC LIMIT 5;
```

## ✅ Lista de Verificación

Antes de ejecutar la app, verifica:

- [ ] Docker está corriendo (3 contenedores activos)
- [ ] ESP32 está conectado y publicando datos
- [ ] IPs configuradas correctamente en el código
- [ ] Emulador/Dispositivo configurado
- [ ] Proyecto sincronizado con Gradle
- [ ] Hay datos en la base de datos

## 🎓 Cumplimiento de Requisitos

✅ **3 Activities**:
1. MainActivity (Menú principal)
2. RealtimeMonitoringActivity (MQTT en tiempo real)
3. HistoricDataActivity (API REST histórico)

✅ **Conexión API REST**: Endpoint `/api/mediciones` con filtro por fecha

✅ **Conexión MQTT**: Topic `sensors/ST_1657/weather_station/WS_USE_1657`

✅ **Visualización en UI**: Todos los datos se muestran en pantalla

---

**¡Todo listo! 🚀**

Si tienes dudas, revisa los logs en Logcat o contacta con el profesor.
