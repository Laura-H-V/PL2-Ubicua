package logic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import Database.ConectionDDBB;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class SensorService implements Runnable {

    private final String brokerUrl = "tcp://ubicomp_mqtt:1883";
    private final String clientId = "java_server";
    private final String topic = "sensors/ST_1657/weather_station/WS_USE_1657";

    @Override
    public void run() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            IMqttClient client = new MqttClient(brokerUrl, clientId);
            client.connect(options);
            Log.logmqtt.info("Conectado a MQTT, suscribiéndose al tópico: " + topic);

            client.subscribe(topic, (t, msg) -> {
                try {
                    String payload = new String(msg.getPayload());
                    Log.logmqtt.info("Mensaje recibido: " + payload);
                    insertarEnBD(payload);
                } catch (Exception ex) {
                    Log.logmqtt.error("Error procesando mensaje MQTT: ", ex);
                }
            });

        } catch (MqttException e) {
            Log.logmqtt.error("Error conectando a MQTT: ", e);
        }
    }

    private void insertarEnBD(String json) {
        try (Connection con = new ConectionDDBB().obtainConnection(true)) {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            Instant instant = Instant.parse(obj.get("timestamp").getAsString());

            JsonObject data = obj.getAsJsonObject("data");

            double temp = data.get("temperature_celsius").getAsDouble();
            double hum = data.get("humidity_percent").getAsDouble();
            double uv = data.get("uv_mw_cm2").getAsDouble();
            double ruido = data.get("sound_level_db").getAsDouble();
            double aire = data.get("air_quality_ppm").getAsDouble();

            String sql = "INSERT INTO mediciones (timestamp, temperatura, humedad, radiacion_uv, ruido_db, calidad_aire) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement st = con.prepareStatement(sql)) {
                st.setTimestamp(1, Timestamp.from(instant));
                st.setDouble(2, temp);
                st.setDouble(3, hum);
                st.setDouble(4, uv);
                st.setDouble(5, ruido);
                st.setDouble(6, aire);

                st.executeUpdate();
            }

            Log.logdb.info("Medición insertada correctamente: " + json);

        } catch (Exception e) {
            Log.logdb.error("Error insertando en DB: ", e);
        }
    }
}
