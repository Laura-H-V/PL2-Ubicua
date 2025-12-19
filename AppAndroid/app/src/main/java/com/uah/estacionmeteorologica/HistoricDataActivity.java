package com.uah.estacionmeteorologica;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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

    private RadioGroup radioGroupTipoFecha;
    private LinearLayout layoutFechaUnica, layoutRangoFechas;
    private TextInputEditText etFechaUnica, etFechaDesde, etFechaHasta;
    private MaterialButton btnConsultar, btnBorrarFiltros;
    private MaterialSwitch switchTodoHistorial;
    private ChipGroup chipGroupSensores;
    private MaterialButtonToggleGroup toggleMaxMin;
    private LinearLayout layoutFiltrosFecha;
    private TextView tvEstado;

    private List<Medicion> medicionesCargadas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_data);

        // Vinculación
        radioGroupTipoFecha = findViewById(R.id.radioGroupTipoFecha);
        layoutFechaUnica = findViewById(R.id.layoutFechaUnica);
        layoutRangoFechas = findViewById(R.id.layoutRangoFechas);
        etFechaUnica = findViewById(R.id.etFechaUnica);
        etFechaDesde = findViewById(R.id.etFechaDesde);
        etFechaHasta = findViewById(R.id.etFechaHasta);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnBorrarFiltros = findViewById(R.id.btnBorrarFiltros);
        switchTodoHistorial = findViewById(R.id.switchTodoHistorial);
        chipGroupSensores = findViewById(R.id.chipGroupSensores);
        toggleMaxMin = findViewById(R.id.toggleMaxMin);
        layoutFiltrosFecha = findViewById(R.id.layoutFiltrosFecha);
        tvEstado = findViewById(R.id.tvEstadoHistorico);

        // Lógica para cambiar entre fecha única y rango
        radioGroupTipoFecha.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioFechaUnica) {
                layoutFechaUnica.setVisibility(View.VISIBLE);
                layoutRangoFechas.setVisibility(View.GONE);
            } else {
                layoutFechaUnica.setVisibility(View.GONE);
                layoutRangoFechas.setVisibility(View.VISIBLE);
            }
        });

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
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        Call<List<Medicion>> call;

        if (switchTodoHistorial.isChecked()) {
            call = apiService.getAllMediciones();
        } else {
            // Verificar qué tipo de fecha está seleccionado
            boolean esFechaUnica = radioGroupTipoFecha.getCheckedRadioButtonId() == R.id.radioFechaUnica;
            
            if (esFechaUnica) {
                String fecha = etFechaUnica.getText().toString().trim();
                if (fecha.isEmpty()) {
                    Toast.makeText(this, "Escribe la fecha", Toast.LENGTH_SHORT).show();
                    return;
                }
                call = apiService.getMediciones(fecha);
            } else {
                String desde = etFechaDesde.getText().toString().trim();
                String hasta = etFechaHasta.getText().toString().trim();

                if (desde.isEmpty()) {
                    Toast.makeText(this, "Escribe la fecha de inicio", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (hasta.isEmpty()) {
                    call = apiService.getMediciones(desde); // Un solo día
                } else {
                    call = apiService.getMedicionesPorRango(desde, hasta); // Rango
                }
            }
        }

        tvEstado.setText("Consultando...");
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
            return;
        }

        List<Medicion> aMostrar = new ArrayList<>(medicionesCargadas);

        // Lógica de Máximo / Mínimo si hay un chip y un modo seleccionado
        int chipId = chipGroupSensores.getCheckedChipId();
        int toggleId = toggleMaxMin.getCheckedButtonId();

        // Si hay toggle seleccionado pero NO hay chip, mostrar advertencia
        if (toggleId != View.NO_ID && chipId == View.NO_ID) {
            Toast.makeText(this, "Selecciona un sensor para buscar Máximo/Mínimo", Toast.LENGTH_LONG).show();
            tvEstado.setText("Selecciona un sensor para aplicar filtro Máx/Mín");
            return;
        }

        // Si hay AMBOS seleccionados, filtrar
        if (chipId != View.NO_ID && toggleId != View.NO_ID) {
            boolean buscarMax = (toggleId == R.id.btnMax);
            Medicion extrema = encontrarExtremo(aMostrar, chipId, buscarMax);
            aMostrar.clear();
            if (extrema != null) {
                aMostrar.add(extrema);
                tvEstado.setText("Valor " + (buscarMax ? "Máximo" : "Mínimo") + " encontrado");
            } else {
                tvEstado.setText("No se pudo encontrar el extremo");
                return;
            }
        } else {
            tvEstado.setText(aMostrar.size() + " registros encontrados");
        }

        // Abrir nueva Activity con los datos en grande
        Intent intent = new Intent(HistoricDataActivity.this, DataDetailsActivity.class);
        intent.putExtra("mediciones", (ArrayList<Medicion>) aMostrar);
        startActivity(intent);
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

    private void limpiarFiltros() {
        etFechaUnica.setText("");
        etFechaDesde.setText("");
        etFechaHasta.setText("");
        switchTodoHistorial.setChecked(false);
        radioGroupTipoFecha.check(R.id.radioFechaUnica);
        chipGroupSensores.clearCheck();
        toggleMaxMin.clearChecked();
        tvEstado.setText("ℹ️ Filtros limpiados");
    }
}