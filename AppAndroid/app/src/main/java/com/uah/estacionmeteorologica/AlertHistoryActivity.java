package com.uah.estacionmeteorologica;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder; // Cambio a Material 3
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlertHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AlertaAdapter adapter;
    private AlertaManager alertaManager;
    private View layoutNoAlertas;
    private Button btnBorrarTodo;
    private View btnShareAlertas;
    private View btnSaveAlertas;
    private static final int REQ_SAVE_ALERTS_CSV = 3001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_history);

        recyclerView = findViewById(R.id.recyclerAlertas);
        layoutNoAlertas = findViewById(R.id.tvNoAlertas);
        btnBorrarTodo = findViewById(R.id.btnBorrarTodo);
        btnShareAlertas = findViewById(R.id.btnShareAlertas);
        btnSaveAlertas = findViewById(R.id.btnSaveAlertas);

        alertaManager = new AlertaManager(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarAlertas();

        btnBorrarTodo.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¿Borrar historial?")
                    .setMessage("Esta acción eliminará todas las alertas registradas permanentemente.")
                    .setPositiveButton("Borrar todo", (dialog, which) -> {
                        alertaManager.borrarTodasLasAlertas();
                        cargarAlertas();
                        Toast.makeText(this, "Historial vaciado", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        btnShareAlertas.setOnClickListener(v -> compartirAlertas());
        btnSaveAlertas.setOnClickListener(v -> guardarAlertasCsv());
    }

    private void cargarAlertas() {
        List<Alerta> alertas = alertaManager.obtenerAlertas();

        if (alertas.isEmpty()) {
            layoutNoAlertas.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnBorrarTodo.setVisibility(View.GONE);
            btnShareAlertas.setVisibility(View.VISIBLE);
            btnShareAlertas.setEnabled(false);
            btnSaveAlertas.setVisibility(View.VISIBLE);
            btnSaveAlertas.setEnabled(false);
        } else {
            layoutNoAlertas.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnBorrarTodo.setVisibility(View.VISIBLE);
            btnShareAlertas.setVisibility(View.VISIBLE);
            btnSaveAlertas.setVisibility(View.VISIBLE);
            btnShareAlertas.setEnabled(true);
            btnSaveAlertas.setEnabled(true);

            adapter = new AlertaAdapter(alertas);
            recyclerView.setAdapter(adapter);
        }
    }

    private void guardarAlertasCsv() {
        List<Alerta> alertas = alertaManager.obtenerAlertas();
        if (alertas.isEmpty()) {
            Toast.makeText(this, "No hay alertas para guardar", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "alertas_ST_1657.csv");
        startActivityForResult(intent, REQ_SAVE_ALERTS_CSV);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SAVE_ALERTS_CSV && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                List<Alerta> alertas = alertaManager.obtenerAlertas();
                String csv = construirCsvAlertas(alertas);
                try (java.io.OutputStream os = getContentResolver().openOutputStream(uri)) {
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

    private String construirCsvAlertas(List<Alerta> alertas) {
        StringBuilder sb = new StringBuilder();
        sb.append("fecha,tipo,titulo,mensaje\n");
        for (Alerta a : alertas) {
            sb.append(a.getFechaFormateada()).append(',')
                    .append(a.getTipo()).append(',')
                    .append(a.getTitulo().replace(',', ';')).append(',')
                    .append(a.getMensaje().replace(',', ';'))
                    .append('\n');
        }
        return sb.toString();
    }
    private void compartirAlertas() {
        List<Alerta> alertas = alertaManager.obtenerAlertas();
        if (alertas.isEmpty()) {
            Toast.makeText(this, "No hay alertas para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Historial de alertas (" )
                .append(alertas.size())
                .append(" registros)\n\n");

        for (Alerta a : alertas) {
            sb.append(a.getFechaFormateada()).append(" | ")
                    .append(a.getTitulo()).append(" | ")
                    .append(a.getMensaje()).append('\n');
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Historial de alertas - Estación ST_1657");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString().trim());
        startActivity(Intent.createChooser(intent, "Compartir historial de alertas"));
    }

    private class AlertaAdapter extends RecyclerView.Adapter<AlertaAdapter.AlertaViewHolder> {
        private List<Alerta> alertas;

        public AlertaAdapter(List<Alerta> alertas) {
            this.alertas = alertas;
        }

        @NonNull
        @Override
        public AlertaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_alerta, parent, false);
            return new AlertaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AlertaViewHolder holder, int position) {
            holder.bind(alertas.get(position));
        }

        @Override
        public int getItemCount() {
            return alertas.size();
        }

        class AlertaViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitulo, tvMensaje, tvFecha, tvTipo;

            public AlertaViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitulo = itemView.findViewById(R.id.tvAlertaTitulo);
                tvMensaje = itemView.findViewById(R.id.tvAlertaMensaje);
                tvFecha = itemView.findViewById(R.id.tvAlertaFecha);
                tvTipo = itemView.findViewById(R.id.tvAlertaTipo);
            }

            public void bind(Alerta alerta) {
                tvTitulo.setText(alerta.getTitulo());
                tvMensaje.setText(alerta.getMensaje());
                tvFecha.setText(alerta.getFechaFormateada());
                tvTipo.setText(alerta.getTipo());

            }
        }
    }
}