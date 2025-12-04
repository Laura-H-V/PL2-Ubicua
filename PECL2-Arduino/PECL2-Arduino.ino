#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <DHT.h>
#include <Wire.h>
#include <Adafruit_SSD1306.h>
#include <math.h>
#include <time.h>

// ----- Configuración WiFi -----
const char* ssid = "MOVISTAR_79CA";
const char* password = "GGn4VC2ri5XE6AY2fiT5";

// ----- Configuración MQTT -----
const char* mqtt_server = "192.168.1.76";
const int mqtt_port = 1883;
const char* mqtt_topic_pub = "sensors/ST_1657/weather_station/WS_USE_1657";  // Tópico para publicar
const char* mqtt_client_id = "ESP32-weather_station_ST_1657";
const char* mqtt_topic_cmd = "sensors/ST_1657/weather_station/WS_USE_1657/alarms";

String alarma_Pantalla = "";

bool alertaRemota = false;

// ----- credenciales MQTT -----
const char* mqtt_user = "ubicua";
const char* mqtt_password = "ubicua1234";

WiFiClient espClient;
PubSubClient client(espClient);

// ----- Pines de sensores -----
const int MQ135_PIN = 34;
const int GUVA_PIN = 35;
const int MAX4466_PIN = 32;
const int DHT_SENSOR_PIN = 4;

// ----- Sensor DHT22 -----
DHT dht(DHT_SENSOR_PIN, DHT22);

// ----- Pantalla -----
Adafruit_SSD1306 display(128, 64, &Wire, -1);

// ----- LEDs -----
#define LED_ALERTA 2  // LED de alertas
#define LED_MQTT 15   // LED de publicaciones en MQTT
#define LED_SUB 13    // LED para indicar mensajes recibidos por suscripción

// ----- Variables -----
float temperatura, humedad, calidadAire, nivelUV, nivelSonido;

// ----- Función lectura sonido -----
float leerNivelSonido() {
  int minVal = 4095;
  int maxVal = 0;

  unsigned long start = millis();
  while (millis() - start < 50) {
    int lectura = analogRead(MAX4466_PIN);
    if (lectura < minVal) minVal = lectura;
    if (lectura > maxVal) maxVal = lectura;
  }

  int picoPico = maxVal - minVal;
  if (picoPico <= 0) return 0.0;

  float voltaje = (picoPico / 2.0) * (3.3f / 4095.0f);
  float referencia = 0.0003f;
  float dB = 20.0f * log10f(max(voltaje, 1e-6f) / referencia);
  if (dB < 0) dB = 0;
  return dB;
}

// ----- Conexión WiFi -----
void setup_wifi() {
  Serial.print("Conectando a WiFi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  int intentos = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    if (++intentos > 30) {
      Serial.println("\nNo se pudo conectar al WiFi. Reiniciando...");
      ESP.restart();
    }
  }
  Serial.println("\nWiFi conectado!");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
}

// ----- Configurar hora -----
void setup_time() {
  Serial.println("Sincronizando con servidor NTP...");
  configTzTime("CET-1CEST,M3.5.0,M10.5.0/3", "pool.ntp.org");
  delay(2000);
  struct tm timeinfo;
  if (getLocalTime(&timeinfo)) {
    Serial.print("Hora actual: ");
    Serial.println(asctime(&timeinfo));
  } else {
    Serial.println("No se pudo obtener hora NTP.");
  }
}

// ----- Callback de mensajes MQTT -----
void callback(char* topic, byte* payload, unsigned int length) {
  String topicStr = String(topic);
  String mensaje  = "";
  alarma_Pantalla="";

  for (unsigned int i = 0; i < length; i++) {
    mensaje += (char)payload[i];
  }

  Serial.print("Mensaje recibido en alarmas: ");
  Serial.println(topicStr);
  Serial.print("contenido Alarma: ");
  Serial.println(mensaje);

  // Parpadeo LED_SUB para indicar recepción
  for (int i = 0; i < 2; i++) {
    digitalWrite(LED_SUB, HIGH);
    delay(100);
    digitalWrite(LED_SUB, LOW);
    delay(100);
  }

  // Si es un mensaje de alarmas desde el servidor
  if (topicStr.equals(mqtt_topic_cmd)) {
    StaticJsonDocument<512> doc;
    DeserializationError err = deserializeJson(doc, mensaje);
    if (err) {
      Serial.print("Error parseando JSON de alarmas: ");
      Serial.println(err.c_str());
      return;
    }

    bool alert = doc["alert"] | false;

    if (alert) {
      display.println("ALERTA REMOTA:");
      if (doc.containsKey("messages") && doc["messages"].is<JsonArray>()) {
        JsonArray msgs = doc["messages"].as<JsonArray>();
        for (JsonVariant v : msgs) {
          const char* txt = v.as<const char*>();
          alarma_Pantalla += txt;
          alarma_Pantalla += "\n";

        }
      }
      alertaRemota = true;
    } else {

      alertaRemota = false;

    }
  }
}

// ----- Reconexión MQTT -----
void reconnect_mqtt() {
  while (!client.connected()) {
    Serial.print("Conectando a MQTT... ");

    bool conectado = false;
    if (strlen(mqtt_user) > 0 && strlen(mqtt_password) > 0) {
      conectado = client.connect(mqtt_client_id, mqtt_user, mqtt_password);
    } else {
      conectado = client.connect(mqtt_client_id);
    }

    if (conectado) {
      Serial.println("Conectado a MQTT");
      client.subscribe(mqtt_topic_cmd);
      digitalWrite(LED_MQTT, HIGH);
      delay(300);
      digitalWrite(LED_MQTT, LOW);
    } else {
      Serial.print("Error rc=");
      Serial.print(client.state());
      Serial.println(" Reintentando en 2 segundos.");
      delay(2000);
    }
  }
}

// ----- Timestamp ISO -----
const char* obtenerTimestampISO() {
  static char buffer[40];
  struct timeval tv_now;
  gettimeofday(&tv_now, NULL);
  time_t now = tv_now.tv_sec;
  int millisec = tv_now.tv_usec / 1000;
  struct tm timeinfo;
  gmtime_r(&now, &timeinfo);
  size_t written = strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%S", &timeinfo);
  snprintf(buffer + written, sizeof(buffer) - written, ".%03dZ", millisec);
  return buffer;
}

// ----- Enviar datos MQTT -----
void enviarDatosMQTT() {
  StaticJsonDocument<512> doc;
  doc["timest"] = obtenerTimestampISO();
  doc["sens_type"] = "weather";
  doc["str_id"] = "ST_1657";
  
  /*
  JsonObject location = doc.createNestedObject("location");
  location["lat"] = 40.4134769;
  location["long"] = -3.6981655;
  location["alt"] = 657;
  location["distr"] = "Salamanca";
  location["neighborhood"] = "Goya";
*/

  JsonObject data = doc.createNestedObject("data");
  data["tem"] = temperatura;
  data["hum"] = humedad;
  data["airq"] = calidadAire;
  data["uv"] = nivelUV;  
  data["sound"] = nivelSonido;
 
  char output[512];
  serializeJson(doc, output);
  Serial.print("Publicando en MQTT: ");
  Serial.println(output);

  /*
  size_t len = strlen(output);
  Serial.print("Longitud JSON: ");
  Serial.println(len);
  */

  if (client.publish(mqtt_topic_pub, output)) {
    Serial.println("Mensaje enviado correctamente.");
    digitalWrite(LED_MQTT, HIGH);
    delay(200);
    digitalWrite(LED_MQTT, LOW);
  } else {
    Serial.println("Error al enviar mensaje MQTT.");
    Serial.print("Estado MQTT (client.state()): ");
    Serial.println(client.state());
  }
}

// ----- SETUP -----
void setup() {
  Serial.begin(115200);
  Wire.begin(25, 26);
  pinMode(LED_ALERTA, OUTPUT);
  pinMode(LED_MQTT, OUTPUT);
  digitalWrite(LED_ALERTA, LOW);
  digitalWrite(LED_MQTT, LOW);
  pinMode(LED_SUB, OUTPUT);
  digitalWrite(LED_SUB, LOW);

  dht.begin();

  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println(F("Error al iniciar pantalla OLED."));
  } else {
    display.clearDisplay();
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(0, 0);
    display.println("Iniciando...");
    display.display();
  }

  setup_wifi();
  setup_time();

  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);  

  reconnect_mqtt();

  client.publish(mqtt_topic_pub, "ESP32 conectado correctamente");
}

// ----- LOOP -----
void loop() {
  if (!client.connected()) reconnect_mqtt();
  client.loop();

  humedad = dht.readHumidity();
  temperatura = dht.readTemperature();

  int lecturaAireRaw = analogRead(MQ135_PIN);
  int lecturaUVRaw = analogRead(GUVA_PIN);
  nivelSonido = leerNivelSonido();
  calidadAire = (lecturaAireRaw / 4095.0f) * 1000.0f;
  nivelUV = (lecturaUVRaw / 4095.0f) * 15.0f;

  // ----- Mostrar en monitor serial -----
  Serial.println("\n----- LECTURAS DE SENSORES -----");
  Serial.printf("Temperatura: %.2f °C\n", temperatura);
  Serial.printf("Humedad: %.2f %%\n", humedad);
  Serial.printf("Calidad del aire: %.2f ppm\n", calidadAire);
  Serial.printf("Nivel UV: %.2f mW/cm²\n", nivelUV);
  Serial.printf("Nivel de sonido: %.2f dB\n", nivelSonido);
  Serial.println("------------------------------\n");

  display.clearDisplay();
  display.setCursor(0, 0);
  char buf[128];
  snprintf(buf, sizeof(buf), "Temp: %.1f C", temperatura);
  display.println(buf);
  snprintf(buf, sizeof(buf), "Hum: %.1f %%", humedad);
  display.println(buf);
  snprintf(buf, sizeof(buf), "Aire: %.1f ppm", calidadAire);
  display.println(buf);
  snprintf(buf, sizeof(buf), "UV: %.1f mW/cm2", nivelUV);
  display.println(buf);
  snprintf(buf, sizeof(buf), "Sonido: %.1f dB", nivelSonido);
  display.println(buf);
  snprintf(buf, sizeof(buf), alarma_Pantalla.c_str(), nivelSonido);
  display.println(buf);
  
  enviarDatosMQTT();

  display.display();

  digitalWrite(LED_ALERTA, (alertaRemota) ? HIGH : LOW);

  delay(5000);
}
