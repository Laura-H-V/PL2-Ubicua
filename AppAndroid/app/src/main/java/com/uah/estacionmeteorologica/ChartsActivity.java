package com.uah.estacionmeteorologica;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    private TextInputEditText etFechaCharts, etFechaDesde, etFechaHasta;
    private MaterialButton btnConsultarCharts, btnLineChart, btnBarChart, btnShareCharts, btnSaveCharts, btnSaveChartImage;
    private static final int REQ_SAVE_CHARTS_CSV = 2001;
    private static final int REQ_SAVE_CHART_IMAGE = 2002;
    private MaterialCardView cardLineChart, cardBarChart;
    private MaterialSwitch switchTodoHistorial;
    private ChipGroup chipGroupVariables;
    private LinearLayout layoutInputsFechas, layoutRangoFechas;
    private MaterialRadioButton rbFechaUnica;

    private List<Medicion> medicionesActuales = null;
    private TextInputLayout tilFechaUnica;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        // Inicialización corregida para Material 3
        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
        tvEstadoCharts = findViewById(R.id.tvEstadoCharts);
        etFechaCharts = findViewById(R.id.etFechaCharts);
        etFechaDesde = findViewById(R.id.etFechaDesde);
        etFechaHasta = findViewById(R.id.etFechaHasta);
        btnConsultarCharts = findViewById(R.id.btnConsultarCharts);
        btnLineChart = findViewById(R.id.btnLineChart);
        btnBarChart = findViewById(R.id.btnBarChart);
        btnShareCharts = findViewById(R.id.btnShareCharts);
        btnSaveCharts = findViewById(R.id.btnSaveCharts);
        btnSaveChartImage = findViewById(R.id.btnSaveChartImage);
        cardLineChart = findViewById(R.id.cardLineChart);
        cardBarChart = findViewById(R.id.cardBarChart);
        switchTodoHistorial = findViewById(R.id.switchTodoHistorial);
        chipGroupVariables = findViewById(R.id.chipGroupVariables);
        layoutInputsFechas = findViewById(R.id.layoutInputsFechas);
        layoutRangoFechas = findViewById(R.id.layoutRangoFechas);
        rbFechaUnica = findViewById(R.id.rbFechaUnica);
        tilFechaUnica = findViewById(R.id.tilFechaUnica);


        // Lógica del Switch "Todo el historial"
        switchTodoHistorial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutInputsFechas.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        // Cambio de filtros rango/único
        RadioGroup rgTipoFiltro = findViewById(R.id.rgTipoFiltro);
        rgTipoFiltro.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFechaUnica) {
                layoutRangoFechas.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbRangoFechas) {
                layoutRangoFechas.setVisibility(View.VISIBLE);
            }
        });

        LinearLayout layoutRangoFechas = findViewById(R.id.layoutRangoFechas);
        TextInputEditText etFechaCharts = findViewById(R.id.etFechaCharts);

        rgTipoFiltro.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFechaUnica) {
                // Día único: mostrar solo la caja única
                tilFechaUnica.setVisibility(View.GONE);
                layoutRangoFechas.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbRangoFechas) {
                // Rango: ocultar fecha única y mostrar las dos de rango
                tilFechaUnica.setVisibility(View.GONE);
                layoutRangoFechas.setVisibility(View.VISIBLE);
            }
        });

        btnConsultarCharts.setOnClickListener(v -> {
            if (switchTodoHistorial.isChecked()) {
                cargarTodoElHistorial();
            } else if (rbFechaUnica.isChecked()) {
                cargarDatos(etFechaCharts.getText().toString().trim());
            } else {
                cargarDatosRango(etFechaDesde.getText().toString().trim(), etFechaHasta.getText().toString().trim());
            }
        });

        // Actualizar gráfica al cambiar de variable (Chip)
        chipGroupVariables.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (medicionesActuales != null) actualizarGrafica();
        });

        btnLineChart.setOnClickListener(v -> {
            cardLineChart.setVisibility(View.VISIBLE);
            cardBarChart.setVisibility(View.GONE);
            chipGroupVariables.setVisibility(View.VISIBLE);

            if (medicionesActuales != null) actualizarGrafica();
        });

        btnBarChart.setOnClickListener(v -> {
            cardLineChart.setVisibility(View.GONE); // Ocultamos la de líneas
            cardBarChart.setVisibility(View.VISIBLE); // Mostramos la de barras

            chipGroupVariables.clearCheck();
            chipGroupVariables.setVisibility(View.GONE);

            if (medicionesActuales != null) actualizarGrafica();
        });

        btnShareCharts.setOnClickListener(v -> compartirMediciones());
        btnSaveCharts.setOnClickListener(v -> guardarMedicionesCsv());
        btnSaveChartImage.setOnClickListener(v -> guardarGraficaPng());
    }
        private void guardarMedicionesCsv() {
            if (medicionesActuales == null || medicionesActuales.isEmpty()) {
                Toast.makeText(this, "No hay datos para guardar", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, "graficas_ST_1657.csv");
            startActivityForResult(intent, REQ_SAVE_CHARTS_CSV);
        }

        private void guardarGraficaPng() {
            Bitmap bmp = null;
            if (cardLineChart.getVisibility() == View.VISIBLE) {
                bmp = lineChart.getChartBitmap();
            } else if (cardBarChart.getVisibility() == View.VISIBLE) {
                bmp = barChart.getChartBitmap();
            }
            if (bmp == null) {
                Toast.makeText(this, "No hay gráfica para guardar", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_TITLE, "grafica_ST_1657.png");
            startActivityForResult(intent, REQ_SAVE_CHART_IMAGE);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri == null) return;
                try {
                    if (requestCode == REQ_SAVE_CHARTS_CSV) {
                        String csv = construirCsvMediciones(medicionesActuales);
                        try (java.io.OutputStream os = getContentResolver().openOutputStream(uri)) {
                            if (os != null) {
                                os.write(csv.getBytes("UTF-8"));
                                Toast.makeText(this, "CSV guardado", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else if (requestCode == REQ_SAVE_CHART_IMAGE) {
                        Bitmap bmp = (cardLineChart.getVisibility() == View.VISIBLE) ? lineChart.getChartBitmap() : barChart.getChartBitmap();
                        try (java.io.OutputStream os = getContentResolver().openOutputStream(uri)) {
                            if (os != null && bmp != null) {
                                bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private String construirCsvMediciones(List<Medicion> lista) {
            StringBuilder sb = new StringBuilder();
            sb.append("fecha,temperatura,humedad,uv,ruido,aire\n");
            for (Medicion m : lista) {
                String fechaFormateada = m.getTimestamp().replace("T", " ").replace("Z", "");
                sb.append(fechaFormateada).append(',')
                        .append(String.format("%.1f", m.getTemperatura())).append(',')
                        .append(String.format("%.1f", m.getHumedad())).append(',')
                        .append(String.format("%.1f", m.getRadiacion_uv())).append(',')
                        .append(String.format("%.1f", m.getRuido_db())).append(',')
                        .append(String.format("%.0f", m.getCalidad_aire()))
                        .append('\n');
            }
            return sb.toString();
        }
    private void cargarDatos(String fecha) {
        tvEstadoCharts.setText("⏳ Cargando datos...");

        // Si el switch de "Todo el historial" envía "ALL",
        // asegúrate de que tu API soporte recibir ese valor o una fecha vacía
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        Call<List<Medicion>> call = apiService.getMediciones(fecha);

        call.enqueue(new Callback<List<Medicion>>() {
            @Override
            public void onResponse(Call<List<Medicion>> call, Response<List<Medicion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    medicionesActuales = response.body();
                    if (medicionesActuales.isEmpty()) {
                        tvEstadoCharts.setText("ℹ️ No hay datos");
                    } else {
                        tvEstadoCharts.setText("✅ " + medicionesActuales.size() + " mediciones. Pulsa una gráfica.");
                        actualizarGrafica(); // Llama a pintar la gráfica automáticamente
                    }
                } else {
                    tvEstadoCharts.setText("❌ Error del servidor");
                }
            }

            @Override
            public void onFailure(Call<List<Medicion>> call, Throwable t) {
                tvEstadoCharts.setText("❌ Error de conexión");
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    private void cargarDatosRango(String fechaDesde, String fechaHasta) {
        tvEstadoCharts.setText("⏳ Consultando rango de días...");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        List<Medicion> todasMediciones = new ArrayList<>();

        try {
            Date dateDesde = sdf.parse(fechaDesde);
            Date dateHasta = sdf.parse(fechaHasta);

            if (dateDesde.after(dateHasta)) {
                tvEstadoCharts.setText("❌ La fecha inicial es posterior a la final");
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateDesde);

            // Calculamos cuántos días hay para saber cuándo terminar
            List<String> listaFechas = new ArrayList<>();
            while (!calendar.getTime().after(dateHasta)) {
                listaFechas.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            final int totalPeticiones = listaFechas.size();
            final int[] peticionesFinalizadas = {0};

            for (String fechaActual : listaFechas) {
                ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
                apiService.getMediciones(fechaActual).enqueue(new Callback<List<Medicion>>() {
                    @Override
                    public void onResponse(Call<List<Medicion>> call, Response<List<Medicion>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            todasMediciones.addAll(response.body());
                        }

                        peticionesFinalizadas[0]++;
                        if (peticionesFinalizadas[0] == totalPeticiones) {
                            procesarResultadosFinales(todasMediciones);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Medicion>> call, Throwable t) {
                        peticionesFinalizadas[0]++;
                        if (peticionesFinalizadas[0] == totalPeticiones) {
                            procesarResultadosFinales(todasMediciones);
                        }
                    }
                });
            }

        } catch (ParseException e) {
            tvEstadoCharts.setText("❌ Formato de fecha inválido");
        }
    }

    private void procesarResultadosFinales(List<Medicion> resultados) {
        if (resultados.isEmpty()) {
            tvEstadoCharts.setText("ℹ️ No hay datos en este rango");
            medicionesActuales = null;
        } else {
            medicionesActuales = resultados;
            tvEstadoCharts.setText("✅ " + resultados.size() + " mediciones cargadas.");
            actualizarGrafica();
        }
    }

    private void compartirMediciones() {
        if (medicionesActuales == null || medicionesActuales.isEmpty()) {
            Toast.makeText(this, "No hay datos para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Resultados de gráficas (" )
                .append(medicionesActuales.size())
                .append(" registros)\n\n");

        for (Medicion m : medicionesActuales) {
            String fechaFormateada = m.getTimestamp()
                    .replace("T", " ")
                    .replace("Z", "");
            sb.append("Fecha: ").append(fechaFormateada).append('\n');
            sb.append("Temp: ").append(String.format("%.1f °C", m.getTemperatura())).append('\n');
            sb.append("Hum: ").append(String.format("%.1f %%", m.getHumedad())).append('\n');
            sb.append("UV: ").append(String.format("%.1f", m.getRadiacion_uv())).append('\n');
            sb.append("Ruido: ").append(String.format("%.1f dB", m.getRuido_db())).append('\n');
            sb.append("Aire: ").append(String.format("%.0f ppm", m.getCalidad_aire())).append("\n\n");
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Datos para gráficas - Estación ST_1657");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString().trim());
        startActivity(Intent.createChooser(intent, "Compartir datos de gráficas"));
    }
    private void cargarTodoElHistorial() {
        tvEstadoCharts.setText("⏳ Cargando historial completo...");

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        // Usamos el método sin parámetros
        Call<List<Medicion>> call = apiService.getAllMediciones();

        call.enqueue(new Callback<List<Medicion>>() {
            @Override
            public void onResponse(Call<List<Medicion>> call, Response<List<Medicion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    medicionesActuales = response.body();
                    if (medicionesActuales.isEmpty()) {
                        tvEstadoCharts.setText("ℹ️ El historial está vacío");
                    } else {
                        tvEstadoCharts.setText("✅ Historial cargado: " + medicionesActuales.size() + " registros");
                        actualizarGrafica();
                    }
                } else {
                    tvEstadoCharts.setText("❌ Error al obtener historial");
                }
            }

            @Override
            public void onFailure(Call<List<Medicion>> call, Throwable t) {
                tvEstadoCharts.setText("❌ Error de conexión al historial");
            }
        });
    }

    private void actualizarGrafica() {
        if (medicionesActuales == null || medicionesActuales.isEmpty()) {
            tvEstadoCharts.setText("ℹ️ No hay datos para mostrar");
            return;
        }

        if (cardLineChart.getVisibility() == View.VISIBLE) {
            crearLineChart(medicionesActuales);
        } else if (cardBarChart.getVisibility() == View.VISIBLE) {
            crearBarChart(medicionesActuales);
        }
    }

    private void crearLineChart(List<Medicion> mediciones) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int selectedChipId = chipGroupVariables.getCheckedChipId();
        String label = "Dato";
        int color = Color.BLUE;

        for (int i = 0; i < mediciones.size(); i++) {
            Medicion m = mediciones.get(i);
            float valor = 0;

            if (selectedChipId == R.id.chipTemp) { valor = (float) m.getTemperatura(); label = "Temp °C"; color = Color.RED; }
            else if (selectedChipId == R.id.chipHum) { valor = (float) m.getHumedad(); label = "Humedad %"; color = Color.CYAN; }
            else if (selectedChipId == R.id.chipUV) { valor = (float) m.getRadiacion_uv(); label = "UV"; color = Color.YELLOW; }
            else if (selectedChipId == R.id.chipRuido) { valor = (float) m.getRuido_db(); label = "Ruido dB"; color = Color.MAGENTA; }
            else if (selectedChipId == R.id.chipAire) { valor = (float) m.getCalidad_aire(); label = "Aire ppm"; color = Color.GREEN; }

            entries.add(new Entry(i, valor));
            labels.add(m.getTimestamp().substring(11, 16));
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Gráfica suave

        lineChart.setData(new LineData(dataSet));
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.invalidate();
    }
    private void mostrarBarChart() {
        cardLineChart.setVisibility(View.GONE);
        cardBarChart.setVisibility(View.VISIBLE);
        crearBarChart(medicionesActuales);
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
