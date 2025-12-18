package com.uah.estacionmeteorologica;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricDataActivity extends AppCompatActivity {

    private static final String TAG = "HistoricData";

    private EditText etFecha;
    private Button btnConsultar;
    private LinearLayout layoutResultados;
    private ScrollView scrollResultados;
    private TextView tvEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_data);

        etFecha = findViewById(R.id.etFecha);
        btnConsultar = findViewById(R.id.btnConsultar);
        layoutResultados = findViewById(R.id.layoutResultados);
        scrollResultados = findViewById(R.id.scrollResultados);
        tvEstado = findViewById(R.id.tvEstadoHistorico);

        // Fecha de ejemplo
        etFecha.setHint("dd-MM-yyyy (ej: 18-12-2025)");

        btnConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fecha = etFecha.getText().toString().trim();
                if (!fecha.isEmpty()) {
                    consultarHistorico(fecha);
                } else {
                    Toast.makeText(HistoricDataActivity.this, 
                        "Ingresa una fecha", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void consultarHistorico(String fecha) {
        tvEstado.setText("⏳ Consultando servidor...");
        layoutResultados.removeAllViews();

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Medicion>> call = apiService.getMediciones(fecha);

        call.enqueue(new Callback<List<Medicion>>() {
            @Override
            public void onResponse(Call<List<Medicion>> call, Response<List<Medicion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Medicion> mediciones = response.body();
                    Log.i(TAG, "Mediciones recibidas: " + mediciones.size());
                    
                    if (mediciones.isEmpty()) {
                        tvEstado.setText("ℹ️ No hay datos para esta fecha");
                    } else {
                        tvEstado.setText("✅ " + mediciones.size() + " mediciones encontradas");
                        mostrarResultados(mediciones);
                    }
                } else {
                    tvEstado.setText("❌ Error del servidor");
                    Toast.makeText(HistoricDataActivity.this, 
                        "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Medicion>> call, Throwable t) {
                Log.e(TAG, "Error API: " + t.getMessage(), t);
                tvEstado.setText("❌ Error de conexión");
                Toast.makeText(HistoricDataActivity.this, 
                    "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarResultados(List<Medicion> mediciones) {
        layoutResultados.removeAllViews();

        for (Medicion m : mediciones) {
            TextView tv = new TextView(this);
            tv.setPadding(16, 16, 16, 16);
            tv.setTextSize(14);
            
            String texto = String.format(
                "📅 %s\n" +
                "🌡️ Temp: %.2f°C | 💧 Hum: %.2f%%\n" +
                "☀️ UV: %.2f | 🔊 Ruido: %.2f dB\n" +
                "💨 Aire: %.2f ppm\n" +
                "─────────────────────",
                m.getTimestamp(),
                m.getTemperatura(),
                m.getHumedad(),
                m.getRadiacion_uv(),
                m.getRuido_db(),
                m.getCalidad_aire()
            );
            
            tv.setText(texto);
            tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            tv.setLayoutParams(params);
            
            layoutResultados.addView(tv);
        }

        scrollResultados.post(new Runnable() {
            @Override
            public void run() {
                scrollResultados.fullScroll(View.FOCUS_UP);
            }
        });
    }
}
