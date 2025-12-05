package servlets;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import logic.ActuatorService;
import logic.SensorService;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;

@WebListener
public class AppStartupListener implements ServletContextListener {

    private Thread sensorThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            String brokerUrl = "tcp://ubicomp_mqtt:1883";
            String clientId  = "java_server";

            IMqttClient client = new MqttClient(brokerUrl, clientId);

            ActuatorService actuatorService = new ActuatorService(client);
            SensorService sensorService = new SensorService(client, actuatorService);

            sensorThread = new Thread(sensorService);
            sensorThread.setDaemon(true);
            sensorThread.start();

            sce.getServletContext().setAttribute("actuatorService", actuatorService);

            System.out.println("SensorService iniciado en segundo plano.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error iniciando SensorService/ActuatorService: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (sensorThread != null && sensorThread.isAlive()) {
                sensorThread.interrupt();
            }
        } catch (Exception ignored) {}
    }
}
