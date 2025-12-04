package logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;

public class ActuatorService {

    private final IMqttClient client;
    private final String commandTopic = "sensors/ST_1657/weather_station/WS_USE_1657/alarms";

    public ActuatorService(IMqttClient client) {
        this.client = client;
    }

    public void enviarAlarma(boolean alert, List<String> mensajes) throws Exception {
        JsonObject root = new JsonObject();
        root.addProperty("alert", alert);

        JsonArray arr = new JsonArray();
        for (String m : mensajes) {
            arr.add(m);
        }
        root.add("messages", arr);

        byte[] payload = root.toString().getBytes();
        MqttMessage msg = new MqttMessage(payload);
        msg.setQos(1);
        msg.setRetained(false);

        client.publish(commandTopic, msg);
    }
}
