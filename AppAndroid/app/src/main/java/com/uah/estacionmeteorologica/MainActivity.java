package com.uah.estacionmeteorologica;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

import android.widget.Toast;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_BROKER_HOST = "broker_host";
    private Button btnRealtime;
    private Button btnHistoric;
    private Button btnCharts;
    private Button btnAlertas;
    private Button btnConfigIp;
    private MaterialSwitch switchDarkMode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Aplicar el tema guardado ANTES de setContentView
        aplicarTemaGuardado();
        
        setContentView(R.layout.activity_main);

        btnRealtime = findViewById(R.id.btnRealtime);
        btnHistoric = findViewById(R.id.btnHistoric);
        btnCharts = findViewById(R.id.btnCharts);
        btnAlertas = findViewById(R.id.btnAlertas);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnConfigIp = findViewById(R.id.btnConfigIp);


        // Configurar estado inicial del switch según preferencia guardada
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDarkMode);

        // Listener para cambio de modo oscuro
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Guardar preferencia
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_DARK_MODE, isChecked);
            editor.apply();
            
            // Aplicar tema
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            
            // Recrear la actividad para aplicar el cambio
            recreate();
        });

        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                    REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        btnRealtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RealtimeMonitoringActivity.class);
                startActivity(intent);
            }
        });

        btnHistoric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoricDataActivity.class);
                startActivity(intent);
            }
        });

        btnCharts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChartsActivity.class);
                startActivity(intent);
            }
        });

        btnAlertas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlertHistoryActivity.class);
                startActivity(intent);
            }
        });
        btnConfigIp.setOnClickListener(v -> {
            String ipActual = prefs.getString(KEY_BROKER_HOST, "10.0.2.2");

            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
            builder.setTitle("IP del servidor MQTT");

            // Contenedor vertical con mensaje + caja de texto
            android.widget.LinearLayout layout = new android.widget.LinearLayout(MainActivity.this);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            layout.setPadding(padding, padding, padding, padding);

            android.widget.TextView info = new android.widget.TextView(MainActivity.this);
            info.setText(
                    "• Usa la IP del PC que ejecuta Docker.\n" +
                            "• Si usas el emulador de Android Studio, pon 10.0.2.2"
            );
            info.setTextSize(14);

            final android.widget.EditText input = new android.widget.EditText(MainActivity.this);
            input.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
            input.setText(ipActual);

            layout.addView(info);
            layout.addView(input);

            builder.setView(layout);

            builder.setPositiveButton("Guardar", (dialog, which) -> {
                String nuevaIp = input.getText().toString().trim();
                if (!nuevaIp.isEmpty()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_BROKER_HOST, nuevaIp);
                    editor.apply();
                    Toast.makeText(MainActivity.this,
                            "IP guardada: " + nuevaIp, Toast.LENGTH_SHORT).show();
                }

                Intent serviceIntent = new Intent(MainActivity.this, MqttBackgroundService.class);
                stopService(serviceIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }

            });

            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            builder.show();
        });
        // Iniciar servicio automáticamente al abrir la app
        iniciarServicioAlertas();
    }



    private void aplicarTemaGuardado() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void iniciarServicioAlertas() {
        Intent serviceIntent = new Intent(this, MqttBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}
