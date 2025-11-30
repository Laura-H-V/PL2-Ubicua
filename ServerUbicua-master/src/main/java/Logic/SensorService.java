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

    private final String brokerUrl = "tcp://mqtt:1883";
    private final String clientId = "java_server";
    private final String topic = "sensors/ST_1657/weather_station/WS_USE_1657";

    @Override
    public void run() {
        try {
            IMqttClient client = new MqttClient(brokerUrl, clientId);
            client.connect();
            Log.logmqtt.info("Conectado a MQTT, suscribiéndose al tópico: " + topic);

            client.subscribe(topic, (t, msg) -> {
                String payload = new String(msg.getPayload());
                Log.logmqtt.info("Mensaje recibido: " + payload);
                insertarEnBD(payload);
            });

        } catch (MqttException e) {
            Log.logmqtt.error("Error conectando a MQTT: ", e);
        }
    }

    private void insertarEnBD(String json) {
        try (Connection con = new ConectionDDBB().obtainConnection(true)) {

            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            String ts = obj.get("timestamp").getAsString();
            double temp = obj.get("temperature_celsius").getAsDouble();
            double hum = obj.get("humidity_percent").getAsDouble();
            double uv = obj.get("uv_mw_cm2").getAsDouble();
            double ruido = obj.get("sound_level_db").getAsDouble();
            double aire = obj.get("air_quality_ppm").getAsDouble();

            String sql = "INSERT INTO mediciones (timestamp, temperatura, humedad, radiacion_uv, ruido_db, calidad_aire) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement st = con.prepareStatement(sql);
            Instant instant = Instant.parse(ts);
            st.setTimestamp(1, Timestamp.from(instant));
            st.setDouble(2, temp);
            st.setDouble(3, hum);
            st.setDouble(4, uv);
            st.setDouble(5, ruido);
            st.setDouble(6, aire);

            st.executeUpdate();
            st.close();

            Log.logdb.info("Medición insertada correctamente: " + json);

        } catch (Exception e) {
            Log.logdb.error("Error insertando en DB: ", e);
        }
    }
}
