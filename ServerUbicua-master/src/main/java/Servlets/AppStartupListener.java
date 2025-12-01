package servlets;

import logic.SensorService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {

    private Thread sensorThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sensorThread = new Thread(new SensorService());
        sensorThread.setDaemon(true);  // 🔥 IMPORTANTE: no bloquea el cierre de Tomcat
        sensorThread.start();

        System.out.println("SensorService iniciado en segundo plano.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (sensorThread != null && sensorThread.isAlive()) {
                sensorThread.interrupt();   // apaga el hilo si hiciera falta
            }
        } catch (Exception ignored) {}
    }
}
