package com.example.assignment.api;


import com.example.assignment.models.HackNasa;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiNasa {
    @GET("planetary/apod")
    Call<HackNasa> getDataFromNasa(@Query("api_key") String apiKey, @Query("date") String date);

}
