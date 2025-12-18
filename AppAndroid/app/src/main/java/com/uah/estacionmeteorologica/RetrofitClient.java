package com.uah.estacionmeteorologica;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;
    
    // IMPORTANTE: Cambia esta IP por la IP de tu ordenador en la red local
    // Para emulador Android usa 10.0.2.2 que apunta a localhost del host
    // Para dispositivo físico usa la IP real (ej: 192.168.1.76)
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
