package com.uah.estacionmeteorologica;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricDataActivity extends AppCompatActivity {

    private static final String TAG = "HistoricData";

    private TextInputEditText etFechaDesde, etFechaHasta;
    private MaterialButton btnConsultar, btnBorrarFiltros;
    private MaterialSwitch switchTodoHistorial;
    private ChipGroup chipGroupSensores;
    private MaterialButtonToggleGroup toggleMaxMin;
    private LinearLayout layoutResultados, layoutFiltrosFecha;
    private TextView tvEstado;

    private List<Medicion> medicionesCargadas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_data);

        // Vinculación
        etFechaDesde = findViewById(R.id.etFechaDesde);
        etFechaHasta = findViewById(R.id.etFechaHasta);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnBorrarFiltros = findViewById(R.id.btnBorrarFiltros);
        switchTodoHistorial = findViewById(R.id.switchTodoHistorial);
        chipGroupSensores = findViewById(R.id.chipGroupSensores);
        toggleMaxMin = findViewById(R.id.toggleMaxMin);
        layoutResultados = findViewById(R.id.layoutResultados);
        layoutFiltrosFecha = findViewById(R.id.layoutFiltrosFecha);
        tvEstado = findViewById(R.id.tvEstadoHistorico);

        // Lógica para ocultar fechas si se pide todo el historial
        switchTodoHistorial.setOnCheckedChangeListener((v, isChecked) -> {
            layoutFiltrosFecha.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        // Botón Limpiar
        btnBorrarFiltros.setOnClickListener(v -> limpiarFiltros());

        // Botón Consultar Principal
        btnConsultar.setOnClickListener(v -> ejecutarConsulta());
    }

    private void ejecutarConsulta() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Medicion>> call;

        if (switchTodoHistorial.isChecked()) {
            call = apiService.getAllMediciones();
        } else {
            String desde = etFechaDesde.getText().toString().trim();
            String hasta = etFechaHasta.getText().toString().trim();

            if (desde.isEmpty()) {
                Toast.makeText(this, "Escribe al menos la fecha de inicio", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hasta.isEmpty()) {
                call = apiService.getMediciones(desde); // Un solo día
            } else {
                call = apiService.getMedicionesPorRango(desde, hasta); // Rango
            }
        }

        tvEstado.setText("⏳ Consultando...");
        call.enqueue(new Callback<List<Medicion>>() {
            @Override
            public void onResponse(Call<List<Medicion>> call, Response<List<Medicion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    medicionesCargadas = response.body();
                    procesarYMostrarResultados();
                } else {
                    tvEstado.setText("Error del servidor");
                }
            }

            @Override
            public void onFailure(Call<List<Medicion>> call, Throwable t) {
                tvEstado.setText("Error de conexión");
            }
        });
    }

    private void procesarYMostrarResultados() {
        if (medicionesCargadas.isEmpty()) {
            tvEstado.setText("No hay datos");
            layoutResultados.removeAllViews();
            return;
        }

        List<Medicion> aMostrar = new ArrayList<>(medicionesCargadas);

        // Lógica de Máximo / Mínimo si hay un chip y un modo seleccionado
        int chipId = chipGroupSensores.getCheckedChipId();
        int toggleId = toggleMaxMin.getCheckedButtonId();

        if (chipId != View.NO_ID && toggleId != View.NO_ID) {
            boolean buscarMax = (toggleId == R.id.btnMax);
            Medicion extrema = encontrarExtremo(aMostrar, chipId, buscarMax);
            aMostrar.clear();
            if (extrema != null) aMostrar.add(extrema);
            tvEstado.setText("Valor " + (buscarMax ? "Máximo" : "Mínimo") + " encontrado");
        } else {
            tvEstado.setText(aMostrar.size() + " registros encontrados");
        }

        mostrarEnLista(aMostrar);
    }

    private Medicion encontrarExtremo(List<Medicion> lista, int chipId, boolean max) {
        Comparator<Medicion> comp;
        if (chipId == R.id.chipTemp) comp = Comparator.comparingDouble(Medicion::getTemperatura);
        else if (chipId == R.id.chipHum) comp = Comparator.comparingDouble(Medicion::getHumedad);
        else if (chipId == R.id.chipRuido) comp = Comparator.comparingDouble(Medicion::getRuido_db);
        else comp = Comparator.comparingDouble(Medicion::getRadiacion_uv);

        if (lista.isEmpty()) return null;
        return max ? Collections.max(lista, comp) : Collections.min(lista, comp);
    }

    private void mostrarEnLista(List<Medicion> lista) {
        layoutResultados.removeAllViews();
        for (Medicion m : lista) {
            MaterialCardView card = new MaterialCardView(this);
            card.setRadius(40f);
            card.setCardElevation(0);
            card.setStrokeWidth(3);
            card.setStrokeColor(getColor(android.R.color.darker_gray));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(0, 0, 0, 24);
            card.setLayoutParams(params);

            TextView tv = new TextView(this);
            tv.setPadding(40, 40, 40, 40);
            tv.setText(String.format("📅 %s\n\n🌡️ %.1f°C  💧 %.1f%%\n☀️ %.1f UV  🔊 %.1f dB\n💨 %.1f ppm",
                    m.getTimestamp(), m.getTemperatura(), m.getHumedad(),
                    m.getRadiacion_uv(), m.getRuido_db(), m.getCalidad_aire()));

            card.addView(tv);
            layoutResultados.addView(card);
        }
    }

    private void limpiarFiltros() {
        etFechaDesde.setText("");
        etFechaHasta.setText("");
        switchTodoHistorial.setChecked(false);
        chipGroupSensores.clearCheck();
        toggleMaxMin.clearChecked();
        layoutResultados.removeAllViews();
        tvEstado.setText("ℹ️ Filtros limpiados");
    }
}