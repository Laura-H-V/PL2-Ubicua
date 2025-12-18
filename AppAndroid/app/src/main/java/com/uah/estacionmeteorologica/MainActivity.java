package com.uah.estacionmeteorologica;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnRealtime;
    private Button btnHistoric;
    private Button btnCharts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRealtime = findViewById(R.id.btnRealtime);
        btnHistoric = findViewById(R.id.btnHistoric);
        btnCharts = findViewById(R.id.btnCharts);

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
    }
}
