package com.uah.estacionmeteorologica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;
    
    private Button btnRealtime;
    private Button btnHistoric;
    private Button btnCharts;
    private Button btnAlertas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRealtime = findViewById(R.id.btnRealtime);
        btnHistoric = findViewById(R.id.btnHistoric);
        btnCharts = findViewById(R.id.btnCharts);
        btnAlertas = findViewById(R.id.btnAlertas);

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

        // Iniciar servicio automáticamente al abrir la app
        iniciarServicioAlertas();
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
