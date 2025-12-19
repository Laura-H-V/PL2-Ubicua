package com.uah.estacionmeteorologica;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataDetailsActivity extends AppCompatActivity {

    private TextView tvTotalRegistros;
    private LinearLayout layoutDetalles;
    private List<Medicion> mediciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_details);

        tvTotalRegistros = findViewById(R.id.tvTotalRegistros);
        layoutDetalles = findViewById(R.id.layoutDetalles);

        // Recibir datos del Intent
        Serializable extra = getIntent().getSerializableExtra("mediciones");
        if (extra instanceof ArrayList) {
            mediciones = (ArrayList<Medicion>) extra;
            mostrarDatos();
        }
    }

    private void mostrarDatos() {
        if (mediciones == null || mediciones.isEmpty()) {
            tvTotalRegistros.setText("No hay datos para mostrar");
            return;
        }

        tvTotalRegistros.setText("Total: " + mediciones.size() + " registros");

        for (Medicion m : mediciones) {
            MaterialCardView card = crearCardDetalle(m);
            layoutDetalles.addView(card);
        }
    }

    private MaterialCardView crearCardDetalle(Medicion m) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(24f);
        card.setCardElevation(4f);
        card.setStrokeWidth(0);
        card.setCardBackgroundColor(getColor(android.R.color.white));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 32);
        card.setLayoutParams(cardParams);

        LinearLayout contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(48, 48, 48, 48);

        // Título con fecha (formatear para quitar T y Z)
        String fechaFormateada = m.getTimestamp()
                .replace("T", " ")
                .replace("Z", "");
        
        TextView tvFecha = new TextView(this);
        tvFecha.setText("📅 " + fechaFormateada);
        tvFecha.setTextAppearance(this, com.google.android.material.R.style.TextAppearance_Material3_TitleLarge);
        tvFecha.setTextColor(getColor(android.R.color.holo_blue_dark));
        LinearLayout.LayoutParams fechaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        fechaParams.setMargins(0, 0, 0, 32);
        tvFecha.setLayoutParams(fechaParams);
        contenido.addView(tvFecha);

        // Temperatura
        contenido.addView(crearLineaDato("🌡️ Temperatura", 
                String.format("%.1f °C", m.getTemperatura()), 
                android.R.color.holo_red_light));

        // Humedad
        contenido.addView(crearLineaDato("💧 Humedad", 
                String.format("%.1f %%", m.getHumedad()), 
                android.R.color.holo_blue_light));

        // Radiación UV
        contenido.addView(crearLineaDato("☀️ Radiación UV", 
                String.format("%.1f", m.getRadiacion_uv()), 
                android.R.color.holo_orange_light));

        // Ruido
        contenido.addView(crearLineaDato("🔊 Ruido", 
                String.format("%.1f dB", m.getRuido_db()), 
                android.R.color.holo_purple));

        // Calidad del aire
        contenido.addView(crearLineaDato("💨 Calidad del Aire", 
                String.format("%.0f ppm", m.getCalidad_aire()), 
                android.R.color.holo_green_light));

        card.addView(contenido);
        return card;
    }

    private LinearLayout crearLineaDato(String titulo, String valor, int colorRes) {
        LinearLayout linea = new LinearLayout(this);
        linea.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lineaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lineaParams.setMargins(0, 0, 0, 24);
        linea.setLayoutParams(lineaParams);

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText(titulo);
        tvTitulo.setTextAppearance(this, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        tvTitulo.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView tvValor = new TextView(this);
        tvValor.setText(valor);
        tvValor.setTextAppearance(this, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        tvValor.setTextColor(getColor(colorRes));
        tvValor.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);

        linea.addView(tvTitulo);
        linea.addView(tvValor);

        return linea;
    }
}
