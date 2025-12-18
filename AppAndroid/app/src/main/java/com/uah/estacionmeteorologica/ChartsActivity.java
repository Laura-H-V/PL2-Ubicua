package com.uah.estacionmeteorologica;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChartsActivity extends AppCompatActivity {

    private static final String TAG = "ChartsActivity";

    private LineChart lineChart;
    private BarChart barChart;
    private TextView tvEstadoCharts;
    private EditText etFechaCharts, etFechaDesde, etFechaHasta;
    private Button btnConsultarCharts, btnLineChart, btnBarChart;
    private CardView cardLineChart, cardBarChart;
    private RadioGroup rgTipoFiltro;
    private RadioButton rbFechaUnica, rbRangoFechas;
    private LinearLayout layoutFechaUnica, layoutRangoFechas;

    private List<Medicion> medicionesActuales = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
        tvEstadoCharts = findViewById(R.id.tvEstadoCharts);
        etFechaCharts = findViewById(R.id.etFechaCharts);
        etFechaDesde = findViewById(R.id.etFechaDesde);
        etFechaHasta = findViewById(R.id.etFechaHasta);
        btnConsultarCharts = findViewById(R.id.btnConsultarCharts);
        btnLineChart = findViewById(R.id.btnLineChart);
        btnBarChart = findViewById(R.id.btnBarChart);
        cardLineChart = findViewById(R.id.cardLineChart);
        cardBarChart = findViewById(R.id.cardBarChart);
        rgTipoFiltro = findViewById(R.id.rgTipoFiltro);
        rbFechaUnica = findViewById(R.id.rbFechaUnica);
        rbRangoFechas = findViewById(R.id.rbRangoFechas);
        layoutFechaUnica = findViewById(R.id.layoutFechaUnica);
        layoutRangoFechas = findViewById(R.id.layoutRangoFechas);

        // Listener para cambiar entre fecha única y rango
        rgTipoFiltro.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbFechaUnica) {
                    layoutFechaUnica.setVisibility(View.VISIBLE);
                    layoutRangoFechas.setVisibility(View.GONE);
                } else {
                    layoutFechaUnica.setVisibility(View.GONE);
                    layoutRangoFechas.setVisibility(View.VISIBLE);
                }
            }
        });

        btnConsultarCharts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbFechaUnica.isChecked()) {
                    String fecha = etFechaCharts.getText().toString().trim();
                    if (fecha.isEmpty()) {
                        Toast.makeText(ChartsActivity.this, 
                            "Introduce una fecha (DD-MM-YYYY)", Toast.LENGTH_SHORT).show();
                    } else {
                        cargarDatos(fecha);
                    }
                } else {
                    String fechaDesde = etFechaDesde.getText().toString().trim();
                    String fechaHasta = etFechaHasta.getText().toString().trim();
                    if (fechaDesde.isEmpty() || fechaHasta.isEmpty()) {
                        Toast.makeText(ChartsActivity.this, 
                            "Introduce ambas fechas (DD-MM-YYYY)", Toast.LENGTH_SHORT).show();
                    } else {
                        cargarDatosRango(fechaDesde, fechaHasta);
                    }
                }
            }
        });

        btnLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (medicionesActuales != null && !medicionesActuales.isEmpty()) {
                    mostrarLineChart();
                } else {
                    Toast.makeText(ChartsActivity.this, 
                        "Primero consulta datos con una fecha", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (medicionesActuales != null && !medicionesActuales.isEmpty()) {
                    mostrarBarChart();
                } else {
                    Toast.makeText(ChartsActivity.this, 
                        "Primero consulta datos con una fecha", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cargarDatosRango(String fechaDesde, String fechaHasta) {
        tvEstadoCharts.setText("⏳ Cargando datos del rango...");
        cardLineChart.setVisibility(View.GONE);
        cardBarChart.setVisibility(View.GONE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        List<Medicion> todasMediciones = new ArrayList<>();
        
        try {
            Date dateDesde = sdf.parse(fechaDesde);
            Date dateHasta = sdf.parse(fechaHasta);
            
            if (dateDesde.after(dateHasta)) {
                Toast.makeText(this, "La fecha desde debe ser anterior a la fecha hasta", 
                    Toast.LENGTH_SHORT).show();
                tvEstadoCharts.setText("❌ Fechas inválidas");
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateDesde);
            
            // Contador para saber cuántas peticiones quedan
            final int[] diasRestantes = {0};
            
            // Calcular número de días
            while (!calendar.getTime().after(dateHasta)) {
                diasRestantes[0]++;
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            // Resetear calendario
            calendar.setTime(dateDesde);
            final int totalDias = diasRestantes[0];
            
            // Hacer peticiones para cada día
            while (!calendar.getTime().after(dateHasta)) {
                String fechaActual = sdf.format(calendar.getTime());
                
                ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
                Call<List<Medicion>> call = apiService.getMediciones(fechaActual);
                
                call.enqueue(new Callback<List<Medicion>>() {
                    @Override
                    public void onResponse(Call<List<Medicion>> call, Response<List<Medicion>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            todasMediciones.addAll(response.body());
                        }
                        
                        diasRestantes[0]--;
                        
                        if (diasRestantes[0] == 0) {
                            // Todas las peticiones completadas
                            if (todasMediciones.isEmpty()) {
                                tvEstadoCharts.setText("ℹ️ No hay datos en ese rango");
                                medicionesActuales = null;
                            } else {
                                medicionesActuales = todasMediciones;
                                tvEstadoCharts.setText("✅ " + todasMediciones.size() + 
                                    " mediciones cargadas de " + totalDias + " días");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Medicion>> call, Throwable t) {
                        Log.e(TAG, "Error API: " + t.getMessage(), t);
                        diasRestantes[0]--;
                        
                        if (diasRestantes[0] == 0) {
                            tvEstadoCharts.setText("❌ Error cargando datos");
                            Toast.makeText(ChartsActivity.this, 
                                "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido. Use DD-MM-YYYY", 
                Toast.LENGTH_SHORT).show();
            tvEstadoCharts.setText("❌ Formato de fecha incorrecto");
        }
    }

    private void cargarDatos(String fecha) {
        tvEstadoCharts.setText("⏳ Cargando datos...");
        cardLineChart.setVisibility(View.GONE);
        cardBarChart.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Medicion>> call = apiService.getMediciones(fecha);

        call.enqueue(new Callback<List<Medicion>>() {
            @Override
            public void onResponse(Call<List<Medicion>> call, Response<List<Medicion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    medicionesActuales = response.body();
                    Log.i(TAG, "Mediciones recibidas: " + medicionesActuales.size());

                    if (medicionesActuales.isEmpty()) {
                        tvEstadoCharts.setText("ℹ️ No hay datos para esa fecha");
                        Toast.makeText(ChartsActivity.this, 
                            "No hay datos disponibles", Toast.LENGTH_SHORT).show();
                    } else {
                        tvEstadoCharts.setText("✅ " + medicionesActuales.size() + 
                            " mediciones cargadas. Elige una gráfica.");
                    }
                } else {
                    tvEstadoCharts.setText("❌ Error del servidor");
                    Toast.makeText(ChartsActivity.this, 
                        "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Medicion>> call, Throwable t) {
                Log.e(TAG, "Error API: " + t.getMessage(), t);
                tvEstadoCharts.setText("❌ Error de conexión");
                Toast.makeText(ChartsActivity.this, 
                    "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarLineChart() {
        cardLineChart.setVisibility(View.VISIBLE);
        cardBarChart.setVisibility(View.GONE);
        crearLineChart(medicionesActuales);
    }

    private void mostrarBarChart() {
        cardLineChart.setVisibility(View.GONE);
        cardBarChart.setVisibility(View.VISIBLE);
        crearBarChart(medicionesActuales);
    }

    private void crearLineChart(List<Medicion> mediciones) {
        ArrayList<Entry> tempEntries = new ArrayList<>();
        ArrayList<Entry> humEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // Tomar máximo 20 mediciones para no saturar la gráfica
        int maxMediciones = Math.min(mediciones.size(), 20);
        
        for (int i = 0; i < maxMediciones; i++) {
            Medicion m = mediciones.get(i);
            tempEntries.add(new Entry(i, (float) m.getTemperatura()));
            humEntries.add(new Entry(i, (float) m.getHumedad()));
            
            // Extraer hora del timestamp
            String timestamp = m.getTimestamp();
            try {
                String hora = timestamp.substring(11, 16); // HH:MM
                labels.add(hora);
            } catch (Exception e) {
                labels.add("--:--");
            }
        }

        // Dataset de Temperatura
        LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperatura (°C)");
        tempDataSet.setColor(Color.rgb(229, 57, 53)); // Rojo
        tempDataSet.setCircleColor(Color.rgb(229, 57, 53));
        tempDataSet.setLineWidth(2f);
        tempDataSet.setCircleRadius(4f);
        tempDataSet.setValueTextSize(10f);
        tempDataSet.setDrawFilled(false);

        // Dataset de Humedad
        LineDataSet humDataSet = new LineDataSet(humEntries, "Humedad (%)");
        humDataSet.setColor(Color.rgb(30, 136, 229)); // Azul
        humDataSet.setCircleColor(Color.rgb(30, 136, 229));
        humDataSet.setLineWidth(2f);
        humDataSet.setCircleRadius(4f);
        humDataSet.setValueTextSize(10f);
        humDataSet.setDrawFilled(false);

        LineData lineData = new LineData(tempDataSet, humDataSet);
        lineChart.setData(lineData);

        // Configuración del eje X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);

        // Descripción deshabilitada para no tapar la gráfica
        lineChart.getDescription().setEnabled(false);

        // Animación y refresh
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void crearBarChart(List<Medicion> mediciones) {
        // Calcular promedios
        double sumTemp = 0, sumHum = 0, sumUV = 0, sumRuido = 0, sumAire = 0;
        
        for (Medicion m : mediciones) {
            sumTemp += m.getTemperatura();
            sumHum += m.getHumedad();
            sumUV += m.getRadiacion_uv();
            sumRuido += m.getRuido_db();
            sumAire += m.getCalidad_aire();
        }

        int count = mediciones.size();
        float avgTemp = (float) (sumTemp / count);
        float avgHum = (float) (sumHum / count);
        float avgUV = (float) (sumUV / count);
        float avgRuido = (float) (sumRuido / count);
        float avgAire = (float) (sumAire / count);

        // Normalizar valores para que se vean proporcionados en la gráfica
        // Temperatura y humedad en su escala, UV escalado x10, ruido/10, aire/10
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, avgTemp));
        entries.add(new BarEntry(1, avgHum));
        entries.add(new BarEntry(2, avgUV * 10)); // Escalar UV
        entries.add(new BarEntry(3, avgRuido / 10)); // Reducir ruido
        entries.add(new BarEntry(4, avgAire / 100)); // Reducir calidad aire

        BarDataSet dataSet = new BarDataSet(entries, "Promedios de Sensores");
        
        // Colores diferentes para cada barra
        int[] colors = {
            Color.rgb(229, 57, 53),   // Temperatura - Rojo
            Color.rgb(30, 136, 229),  // Humedad - Azul
            Color.rgb(251, 140, 0),   // UV - Naranja
            Color.rgb(142, 36, 170),  // Ruido - Morado
            Color.rgb(67, 160, 71)    // Aire - Verde
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Etiquetas del eje X
        String[] labels = {"Temp", "Hum", "UV×10", "Ruido/10", "Aire/100"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);

        // Descripción deshabilitada para no tapar la gráfica
        barChart.getDescription().setEnabled(false);

        // Animación y refresh
        barChart.animateY(1000);
        barChart.invalidate();
    }
}
