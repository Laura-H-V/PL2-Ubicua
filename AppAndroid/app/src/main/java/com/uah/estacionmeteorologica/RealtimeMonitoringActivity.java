package com.uah.estacionmeteorologica;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class RealtimeMonitoringActivity extends AppCompatActivity {

    private static final String TAG = "RealtimeMQTT";
    
    // IMPORTANTE: Cambia esta IP por la IP de tu ordenador en la red local
    // Para obtenerla: ipconfig (Windows) o ifconfig (Linux/Mac)
    private static final String MQTT_BROKER = "tcp://10.0.2.2:1883";
    private static final String MQTT_TOPIC = "sensors/ST_1657/weather_station/WS_USE_1657";
    private static final String MQTT_USER = "ubicua";
    private static final String MQTT_PASS = "ubicua1234";

    private MqttClient mqttClient;
    
    private TextView tvTemperatura;
    private TextView tvHumedad;
    private TextView tvRadiacionUV;
    private TextView tvRuido;
    private TextView tvCalidadAire;
    private TextView tvTimestamp;
    private TextView tvEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_monitoring);

        // Vincular vistas
        tvTemperatura = findViewById(R.id.tvTemperatura);
        tvHumedad = findViewById(R.id.tvHumedad);
        tvRadiacionUV = findViewById(R.id.tvRadiacionUV);
        tvRuido = findViewById(R.id.tvRuido);
        tvCalidadAire = findViewById(R.id.tvCalidadAire);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvEstado = findViewById(R.id.tvEstado);

        tvEstado.setText("Conectando a MQTT...");

        // Conectar a MQTT
        conectarMQTT();
    }

    private void conectarMQTT() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mqttClient = new MqttClient(
                            MQTT_BROKER,
                            MqttClient.generateClientId(),
                            new MemoryPersistence()
                    );

                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(true);
                    options.setUserName(MQTT_USER);
                    options.setPassword(MQTT_PASS.toCharArray());
                    options.setAutomaticReconnect(true);
                    options.setConnectionTimeout(10);

                    mqttClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvEstado.setText("❌ Conexión perdida");
                                    Toast.makeText(RealtimeMonitoringActivity.this, 
                                        "Conexión MQTT perdida", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            String payload = new String(message.getPayload());
                            Log.i(TAG, "Mensaje recibido: " + payload);
                            procesarMensaje(payload);
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            // No se usa en suscripción
                        }
                    });

                    mqttClient.connect(options);
                    Log.i(TAG, "Conectado a MQTT broker");

                    mqttClient.subscribe(MQTT_TOPIC, 1);
                    Log.i(TAG, "Suscrito a: " + MQTT_TOPIC);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvEstado.setText("✅ Conectado - Esperando datos...");
                        }
                    });

                } catch (MqttException e) {
                    Log.e(TAG, "Error MQTT: " + e.getMessage(), e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvEstado.setText("❌ Error de conexión");
                            Toast.makeText(RealtimeMonitoringActivity.this, 
                                "Error conectando a MQTT: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void procesarMensaje(String json) {
        try {
            JsonObject root = new Gson().fromJson(json, JsonObject.class);
            
            String timestamp = root.has("timest") ? root.get("timest").getAsString() : "N/A";
            
            JsonObject data = root.getAsJsonObject("data");
            
            final double temperatura = data.get("tem").getAsDouble();
            final double humedad = data.get("hum").getAsDouble();
            final double uv = data.get("uv").getAsDouble();
            final double ruido = data.get("sound").getAsDouble();
            final double calidadAire = data.get("airq").getAsDouble();
            final String time = timestamp;

            // Actualizar UI en el hilo principal
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTemperatura.setText(String.format("🌡️ Temperatura: %.2f °C", temperatura));
                    tvHumedad.setText(String.format("💧 Humedad: %.2f %%", humedad));
                    tvRadiacionUV.setText(String.format("☀️ Radiación UV: %.2f mW/cm²", uv));
                    tvRuido.setText(String.format("🔊 Ruido: %.2f dB", ruido));
                    tvCalidadAire.setText(String.format("💨 Calidad Aire: %.2f ppm", calidadAire));
                    tvTimestamp.setText("⏰ " + time);
                    tvEstado.setText("✅ Datos recibidos");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error parseando JSON: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                Log.i(TAG, "Desconectado de MQTT");
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error desconectando: " + e.getMessage());
        }
    }
}
