package com.example.assignment.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiResponeNasa {
    private static final String BASE_URL = "https://api.nasa.gov/";
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd").create()))
                    .build();
        }
        return retrofit;
    }

    public static ApiNasa getApiNasa() {
        return getInstance().create(ApiNasa.class);
    }
}
