package com.uah.estacionmeteorologica;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.io.OutputStream;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataDetailsActivity extends AppCompatActivity {

    private TextView tvTotalRegistros;
    private LinearLayout layoutDetalles;
    private List<Medicion> mediciones;
        private MaterialButton btnCompartir;
        private MaterialButton btnGuardar;
        private static final int REQ_SAVE_DETAILS_CSV = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_details);


        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);


        tvTotalRegistros = findViewById(R.id.tvTotalRegistros);
        layoutDetalles = findViewById(R.id.layoutDetalles);
                btnCompartir = findViewById(R.id.btnCompartir);
                btnGuardar = findViewById(R.id.btnGuardar);

        // Recibir datos del Intent
        Serializable extra = getIntent().getSerializableExtra("mediciones");
        if (extra instanceof ArrayList) {
            mediciones = (ArrayList<Medicion>) extra;
            mostrarDatos();
        }

                btnCompartir.setOnClickListener(v -> compartirResultados());
                btnGuardar.setOnClickListener(v -> guardarComoCsv());
    }

        private void guardarComoCsv() {
                if (mediciones == null || mediciones.isEmpty()) {
                        Toast.makeText(this, "No hay datos para guardar", Toast.LENGTH_SHORT).show();
                        return;
                }

                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "datos_ST_1657.csv");
                startActivityForResult(intent, REQ_SAVE_DETAILS_CSV);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == REQ_SAVE_DETAILS_CSV && resultCode == RESULT_OK && data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                                String csv = construirCsvMediciones();
                                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                                        if (os != null) {
                                                os.write(csv.getBytes("UTF-8"));
                                                Toast.makeText(this, "CSV guardado", Toast.LENGTH_SHORT).show();
                                        }
                                } catch (Exception e) {
                                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                                }
                        }
                }
        }

        private String construirCsvMediciones() {
                StringBuilder sb = new StringBuilder();
                sb.append("fecha,temperatura,humedad,uv,ruido,aire\n");
                for (Medicion m : mediciones) {
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
        tvTitulo.setTextColor(getColor(android.R.color.black));
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

    private void compartirResultados() {
        if (mediciones == null || mediciones.isEmpty()) {
            Toast.makeText(this, "No hay datos para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        String contenido = construirTextoCompartir();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Datos históricos - Estación ST_1657");
        intent.putExtra(Intent.EXTRA_TEXT, contenido);
        startActivity(Intent.createChooser(intent, "Compartir resultados"));
    }

    private String construirTextoCompartir() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resultados de la consulta (" )
                .append(mediciones.size())
                .append(" registros)\n\n");

        for (Medicion m : mediciones) {
            String fechaFormateada = m.getTimestamp()
                    .replace("T", " ")
                    .replace("Z", "");

            sb.append("Fecha: ").append(fechaFormateada).append('\n');
            sb.append("Temperatura: ")
                    .append(String.format("%.1f °C", m.getTemperatura()))
                    .append('\n');
            sb.append("Humedad: ")
                    .append(String.format("%.1f %%", m.getHumedad()))
                    .append('\n');
            sb.append("Radiación UV: ")
                    .append(String.format("%.1f", m.getRadiacion_uv()))
                    .append('\n');
            sb.append("Ruido: ")
                    .append(String.format("%.1f dB", m.getRuido_db()))
                    .append('\n');
            sb.append("Calidad del aire: ")
                    .append(String.format("%.0f ppm", m.getCalidad_aire()))
                    .append("\n\n");
        }

        return sb.toString().trim();
    }
}
