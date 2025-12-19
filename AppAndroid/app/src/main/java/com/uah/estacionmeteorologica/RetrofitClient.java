package com.uah.estacionmeteorologica;

import android.content.SharedPreferences;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    private static String getBaseUrl(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE);
        String host = prefs.getString("broker_host", "10.0.2.2");
        return "http://" + host + ":8080/";
    }

    public static Retrofit getRetrofitInstance(android.content.Context context) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl(context))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

