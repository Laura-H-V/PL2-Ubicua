package com.uah.estacionmeteorologica;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlertHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AlertaAdapter adapter;
    private AlertaManager alertaManager;
    private TextView tvNoAlertas;
    private Button btnBorrarTodo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_history);

        recyclerView = findViewById(R.id.recyclerAlertas);
        tvNoAlertas = findViewById(R.id.tvNoAlertas);
        btnBorrarTodo = findViewById(R.id.btnBorrarTodo);

        alertaManager = new AlertaManager(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        cargarAlertas();

        btnBorrarTodo.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Borrar Historial")
                .setMessage("¿Estás seguro de que quieres borrar todas las alertas?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    alertaManager.borrarTodasLasAlertas();
                    cargarAlertas();
                    Toast.makeText(this, "Historial borrado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
        });
    }

    private void cargarAlertas() {
        List<Alerta> alertas = alertaManager.obtenerAlertas();
        
        if (alertas.isEmpty()) {
            tvNoAlertas.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnBorrarTodo.setEnabled(false);
        } else {
            tvNoAlertas.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnBorrarTodo.setEnabled(true);
            
            adapter = new AlertaAdapter(alertas);
            recyclerView.setAdapter(adapter);
        }
    }

    // Adapter para RecyclerView
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
            Alerta alerta = alertas.get(position);
            holder.bind(alerta);
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

                // Color según tipo
                int color;
                switch (alerta.getTipo()) {
                    case "TEMPERATURA":
                        color = 0xFFFF5722; // Naranja
                        break;
                    case "HUMEDAD":
                        color = 0xFF2196F3; // Azul
                        break;
                    case "UV":
                        color = 0xFFFFEB3B; // Amarillo
                        break;
                    case "RUIDO":
                        color = 0xFF9C27B0; // Púrpura
                        break;
                    case "AIRE":
                        color = 0xFF607D8B; // Gris
                        break;
                    default:
                        color = 0xFF9E9E9E; // Gris por defecto
                }
                tvTipo.setTextColor(color);
            }
        }
    }
}
