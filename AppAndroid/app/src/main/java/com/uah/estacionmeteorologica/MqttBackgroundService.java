package com.uah.estacionmeteorologica;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttBackgroundService extends Service {

    private static final String TAG = "MqttBackgroundService";
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_BROKER_HOST = "broker_host";
    private static final String TOPIC_ALERTAS = "sensors/ST_1657/weather_station/WS_USE_1657/alarms";
    private static final String USERNAME = "ubicua";
    private static final String PASSWORD = "ubicua";

    private MqttClient mqttClient;
    private NotificationHelper notificationHelper;
    private AlertaManager alertaManager;

    // Umbrales de alerta
    private static final double TEMP_MAX = 25.0;
    private static final double HUM_MIN = 10.0;
    private static final double UV_MAX = 20.0;
    private static final double RUIDO_MAX = 75.0;
    private static final double AIRE_MAX = 350.0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Servicio de alertas iniciado");
        
        notificationHelper = new NotificationHelper(this);
        alertaManager = new AlertaManager(this);
        
        // Mostrar notificación de foreground para mantener el servicio activo
        startForeground(1, notificationHelper.createForegroundNotification());
        
        conectarMqtt();
    }

    private void conectarMqtt() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    String host = prefs.getString(KEY_BROKER_HOST, "10.0.2.2");
                    String brokerUrl = "tcp://" + host + ":1883";

                    String clientId = "android_alert_" + System.currentTimeMillis();
                    mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setUserName(USERNAME);
                    options.setPassword(PASSWORD.toCharArray());
                    options.setCleanSession(true);
                    options.setAutomaticReconnect(true);
                    options.setKeepAliveInterval(60);

                    mqttClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            Log.e(TAG, "Conexión MQTT perdida", cause);
                            // Intentar reconectar
                            try {
                                Thread.sleep(5000);
                                conectarMqtt();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) {
                            String payload = new String(message.getPayload());
                            Log.i(TAG, "Mensaje recibido: " + payload);
                            procesarMensaje(payload);
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                        }
                    });

                    mqttClient.connect(options);
                    mqttClient.subscribe(TOPIC_ALERTAS, 1);
                    
                    Log.i(TAG, "Conectado al broker MQTT y suscrito a alertas");

                } catch (MqttException e) {
                    Log.e(TAG, "Error conectando MQTT", e);
                }
            }
        }).start();
    }

    private void procesarMensaje(String payload) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(payload, JsonObject.class);

            boolean alert = json.has("alert") && json.get("alert").getAsBoolean();

            if (!alert) {
                Log.i(TAG, "Mensaje de alarmas sin alert=true, no se genera alerta");
                return;
            }

            if (json.has("messages") && json.get("messages").isJsonArray()) {
                for (com.google.gson.JsonElement msgElement : json.get("messages").getAsJsonArray()) {
                    String mensaje = msgElement.getAsString();
                    crearAlerta(
                            "Alarma estación",
                            mensaje,
                            "GENERAL"
                    );
                }
            } else {
                // Si no hay array messages, al menos una alerta genérica
                crearAlerta("Alarma estación", "Se ha recibido una alarma.", "ST_1657");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error procesando mensaje", e);
        }
    }

    private void crearAlerta(String titulo, String mensaje, String tipo) {
        // Guardar en historial
        alertaManager.guardarAlerta(titulo, mensaje, tipo);
        
        // Mostrar notificación
        notificationHelper.showAlertNotification(titulo, mensaje);
        
        Log.i(TAG, "Alerta generada: " + titulo);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Reiniciar si el sistema mata el servicio
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
            } catch (MqttException e) {
                Log.e(TAG, "Error desconectando MQTT", e);
            }
        }
        Log.i(TAG, "Servicio de alertas detenido");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
