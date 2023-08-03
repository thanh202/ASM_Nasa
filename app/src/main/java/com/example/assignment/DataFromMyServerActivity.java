package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.assignment.adapter.HackNasaAdapter;
import com.example.assignment.api.ApiMyServer;
import  com.example.assignment.databinding.ActivityDataFromMyServerBinding;
import com.example.assignment.models.HackNasa;
import com.example.assignment.models.ResponeDataFromMyServer;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataFromMyServerActivity extends AppCompatActivity {

    private ActivityDataFromMyServerBinding binding;
    private List<HackNasa> listHackNasa;
    private HackNasaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityDataFromMyServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

    }

    private void initViews() {
        listHackNasa = new ArrayList<>();
        adapter = new HackNasaAdapter(this);
        binding.btnBack.setOnClickListener(v->finish());
        getData();
    }

    private void getData() {
        ApiMyServer.apiService.getData().enqueue(new Callback<ResponeDataFromMyServer>() {
            @Override
            public void onResponse(Call<ResponeDataFromMyServer> call, Response<ResponeDataFromMyServer> response) {
                listHackNasa = response.body().getData();
                adapter.setData(listHackNasa);
                binding.rcv.setAdapter(adapter);
                Log.d("CCC", listHackNasa.toString());
            }

            @Override
            public void onFailure(Call<ResponeDataFromMyServer> call, Throwable t) {

            }
        });
    }
}