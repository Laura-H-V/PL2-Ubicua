package logic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import Database.ConectionDDBB;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SensorService implements Runnable {

    private final String brokerUrl = "tcp://ubicomp_mqtt:1883";
    private final String clientId = "java_server";
    private final String topic = "sensors/ST_1657/weather_station/WS_USE_1657";

    private final IMqttClient client;
    private final ActuatorService actuatorService;

    public SensorService(IMqttClient client, ActuatorService actuatorService) {
        this.client = client;
        this.actuatorService = actuatorService;
    }


    @Override
    public void run() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            if (!client.isConnected()) {
                client.connect(options);
            }

            Log.logmqtt.info("Conectado a MQTT, suscribiéndose al tópico: " + topic);

            client.subscribe(topic, (t, msg) -> {
                try {
                    String payload = new String(msg.getPayload());
                    Log.logmqtt.info("Mensaje recibido: " + payload);
                    insertarEnBDyEvaluarAlarma(payload);
                } catch (Exception ex) {
                    Log.logmqtt.error("Error procesando mensaje MQTT: ", ex);
                }
            });

        } catch (MqttException e) {
            Log.logmqtt.error("Error conectando a MQTT: ", e);
        }
    }

    private void insertarEnBDyEvaluarAlarma(String json) {
        double temp = 0, hum = 0, uv = 0, ruido = 0, aire = 0;

        try (Connection con = new ConectionDDBB().obtainConnection(true)) {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            Instant instant = Instant.parse(obj.get("timest").getAsString());

            JsonObject data = obj.getAsJsonObject("data");

            temp = data.get("tem").getAsDouble();
            hum = data.get("hum").getAsDouble();
            uv = data.get("uv").getAsDouble();
            ruido = data.get("sound").getAsDouble();
            aire = data.get("airq").getAsDouble();

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

        // ----- Evaluar alarmas y enviar JSON a MQTT -----
        try {
            List<String> mensajesAlarma = new ArrayList<>();

            if (temp >= 23.0) {
                mensajesAlarma.add("Temperatura elevada");
            }
            if (hum <= 20.0) {
                mensajesAlarma.add("Humedad baja");
            }
            if (hum >= 80.0) {
                mensajesAlarma.add("Humedad excesiva");
            }
            if (aire >= 700.0) {
                mensajesAlarma.add("Mala calidad aire");
            }
            if (uv >= 10.0) {
                mensajesAlarma.add("Radiación UV elevada");
            }
            if (ruido >= 80.0) {
                mensajesAlarma.add(" ruido excesivo");
            }

            boolean hayAlarma = !mensajesAlarma.isEmpty();
            actuatorService.enviarAlarma(hayAlarma, mensajesAlarma);

        } catch (Exception e) {
            Log.logmqtt.error("Error enviando alarma a MQTT: ", e);
        }
    }

}


