package servlets;

import logic.SensorService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Thread sensorThread = new Thread(new SensorService());
        sensorThread.start();
        System.out.println("SensorService iniciado en segundo plano.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Opcional: cerrar recursos si hace falta
    }
}
