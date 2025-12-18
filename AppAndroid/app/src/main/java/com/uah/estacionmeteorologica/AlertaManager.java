package com.uah.estacionmeteorologica;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlertaManager {

    private static final String PREFS_NAME = "AlertasPrefs";
    private static final String KEY_ALERTAS = "alertas_historial";
    private static final int MAX_ALERTAS = 100; // Límite de alertas guardadas

    private Context context;
    private SharedPreferences preferences;
    private Gson gson;

    public AlertaManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // Guardar nueva alerta
    public void guardarAlerta(String titulo, String mensaje, String tipo) {
        List<Alerta> alertas = obtenerAlertas();
        
        Alerta nuevaAlerta = new Alerta(
            titulo,
            mensaje,
            tipo,
            System.currentTimeMillis()
        );
        
        alertas.add(0, nuevaAlerta); // Añadir al principio (más reciente primero)
        
        // Mantener solo las últimas MAX_ALERTAS
        if (alertas.size() > MAX_ALERTAS) {
            alertas = alertas.subList(0, MAX_ALERTAS);
        }
        
        // Guardar en SharedPreferences
        String json = gson.toJson(alertas);
        preferences.edit().putString(KEY_ALERTAS, json).apply();
    }

    // Obtener todas las alertas
    public List<Alerta> obtenerAlertas() {
        String json = preferences.getString(KEY_ALERTAS, null);
        
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<ArrayList<Alerta>>() {}.getType();
        List<Alerta> alertas = gson.fromJson(json, listType);
        
        return alertas != null ? alertas : new ArrayList<>();
    }

    // Borrar todas las alertas
    public void borrarTodasLasAlertas() {
        preferences.edit().remove(KEY_ALERTAS).apply();
    }

    // Obtener número de alertas
    public int getNumeroAlertas() {
        return obtenerAlertas().size();
    }

    // Obtener alertas por tipo
    public List<Alerta> obtenerAlertasPorTipo(String tipo) {
        List<Alerta> todasLasAlertas = obtenerAlertas();
        List<Alerta> alertasFiltradas = new ArrayList<>();
        
        for (Alerta alerta : todasLasAlertas) {
            if (alerta.getTipo().equals(tipo)) {
                alertasFiltradas.add(alerta);
            }
        }
        
        return alertasFiltradas;
    }
}
