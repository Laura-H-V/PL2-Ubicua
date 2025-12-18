package com.uah.estacionmeteorologica;

import java.io.Serializable;

public class Alerta implements Serializable {
    
    private String titulo;
    private String mensaje;
    private String tipo;
    private long timestamp;

    public Alerta() {
    }

    public Alerta(String titulo, String mensaje, String tipo, long timestamp) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.timestamp = timestamp;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFechaFormateada() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
}
