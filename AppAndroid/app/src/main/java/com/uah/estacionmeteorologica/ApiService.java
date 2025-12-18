package com.uah.estacionmeteorologica;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    /**
     * Endpoint para obtener mediciones por fecha
     * URL: http://IP:8080/api/mediciones?fecha=DD-MM-YYYY
     */
    @GET("api/mediciones")
    Call<List<Medicion>> getMediciones(@Query("fecha") String fecha);

    /**
     * Endpoint para obtener mediciones por rango de fechas
     * URL: http://IP:8080/api/mediciones?desde=DD-MM-YYYY&hasta=DD-MM-YYYY
     */
    @GET("api/mediciones")
    Call<List<Medicion>> getMedicionesPorRango(
            @Query("desde") String desde,
            @Query("hasta") String hasta
    );

    /**
     * Endpoint para todo el hsitorial
     * URL: http://IP:8080/api/mediciones
     */
    @GET("api/mediciones")
    Call<List<Medicion>> getAllMediciones();
}