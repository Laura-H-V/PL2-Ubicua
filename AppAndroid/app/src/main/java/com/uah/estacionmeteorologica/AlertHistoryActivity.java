package com.uah.estacionmeteorologica;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_history);

        recyclerView = findViewById(R.id.recyclerAlertas);
        layoutNoAlertas = findViewById(R.id.tvNoAlertas);
        btnBorrarTodo = findViewById(R.id.btnBorrarTodo);

        alertaManager = new AlertaManager(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarAlertas();

        btnBorrarTodo.setOnClickListener(v -> {
            // Cambio: Diálogo con estilo Material 3
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
    }

    private void cargarAlertas() {
        List<Alerta> alertas = alertaManager.obtenerAlertas();

        if (alertas.isEmpty()) {
            layoutNoAlertas.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnBorrarTodo.setVisibility(View.GONE);
        } else {
            layoutNoAlertas.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnBorrarTodo.setVisibility(View.VISIBLE);

            adapter = new AlertaAdapter(alertas);
            recyclerView.setAdapter(adapter);
        }
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