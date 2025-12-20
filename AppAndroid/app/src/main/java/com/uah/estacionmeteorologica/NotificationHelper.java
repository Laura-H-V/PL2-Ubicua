package com.uah.estacionmeteorologica;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID_ALERTS = "weather_alerts";
    private static final String CHANNEL_ID_SERVICE = "weather_service";
    private static final String CHANNEL_NAME_ALERTS = "Alertas Meteorológicas";
    private static final String CHANNEL_NAME_SERVICE = "Servicio de Monitoreo";
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_ALERT_FLASH = "alert_flash";

    private Context context;
    private NotificationManager notificationManager;
    private int notificationId = 2; // ID 1 está reservado para foreground

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para alertas
            NotificationChannel alertChannel = new NotificationChannel(
                CHANNEL_ID_ALERTS,
                CHANNEL_NAME_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            );
            alertChannel.setDescription("Notificaciones de alertas meteorológicas");
            alertChannel.enableVibration(true);
            alertChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            alertChannel.enableLights(true);
            alertChannel.setLightColor(0xFFFF0000);
            alertChannel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                null
            );

            // Canal para el servicio foreground
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID_SERVICE,
                CHANNEL_NAME_SERVICE,
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Servicio de monitoreo en segundo plano");
            serviceChannel.enableVibration(false);
            serviceChannel.setSound(null, null);

            notificationManager.createNotificationChannel(alertChannel);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    // Notificación de foreground para el servicio
    public Notification createForegroundNotification() {
        Intent notificationIntent = new Intent(context, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setContentTitle("Monitoreo de Estación Meteorológica")
            .setContentText("Escuchando alertas en segundo plano...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    // Notificación de alerta con vibración y sonido
    public void showAlertNotification(String titulo, String mensaje) {
        Intent intent = new Intent(context, AlertHistoryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(new long[]{0, 500, 200, 500});

        notificationManager.notify(notificationId++, builder.build());

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean usarLinterna = prefs.getBoolean(KEY_ALERT_FLASH, false);

        if (usarLinterna) {
            activarLinterna();
        } else {
            vibrar();
        }
    }

    private void vibrar() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, 500, 200, 500}, 
                    -1
                ));
            } else {
                vibrator.vibrate(new long[]{0, 500, 200, 500}, -1);
            }
        }
    }

    private void activarLinterna() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            vibrar();
            return;
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            vibrar();
            return;
        }

        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            vibrar();
            return;
        }

        try {
            String cameraId = null;
            for (String id : cameraManager.getCameraIdList()) {
                Boolean hasFlash = cameraManager.getCameraCharacteristics(id)
                        .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (hasFlash != null && hasFlash) {
                    cameraId = id;
                    break;
                }
            }

            if (cameraId == null) {
                vibrar();
                return;
            }

            cameraManager.setTorchMode(cameraId, true);
            final String finalCameraId = cameraId;

            // Apagar linterna tras 1.5 segundos
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    cameraManager.setTorchMode(finalCameraId, false);
                } catch (CameraAccessException e) {}
            }, 1500);

        } catch (CameraAccessException e) {
            vibrar();
        }
    }
}
