package com.uah.estacionmeteorologica;

import com.google.gson.annotations.SerializedName;

public class Medicion {
    
    private int id;
    
    @SerializedName("timestamp")
    private String timestamp;
    
    @SerializedName("temperatura")
    private double temperatura;
    
    @SerializedName("humedad")
    private double humedad;
    
    @SerializedName("radiacion_uv")
    private double radiacion_uv;
    
    @SerializedName("ruido_db")
    private double ruido_db;
    
    @SerializedName("calidad_aire")
    private double calidad_aire;

    // Constructor vacío
    public Medicion() {}

    // Getters
    public int getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public double getHumedad() {
        return humedad;
    }

    public double getRadiacion_uv() {
        return radiacion_uv;
    }

    public double getRuido_db() {
        return ruido_db;
    }

    public double getCalidad_aire() {
        return calidad_aire;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public void setHumedad(double humedad) {
        this.humedad = humedad;
    }

    public void setRadiacion_uv(double radiacion_uv) {
        this.radiacion_uv = radiacion_uv;
    }

    public void setRuido_db(double ruido_db) {
        this.ruido_db = ruido_db;
    }

    public void setCalidad_aire(double calidad_aire) {
        this.calidad_aire = calidad_aire;
    }
}
