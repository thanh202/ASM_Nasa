package com.example.assignment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;


import com.bumptech.glide.Glide;
import com.example.assignment.api.ApiMyServer;
import com.example.assignment.api.ApiNasa;
import com.example.assignment.api.ApiResponeNasa;
import com.example.assignment.databinding.ActivityMainBinding;
import com.example.assignment.models.HackNasa;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HackNasa hackNasa;
    private static final String API_KEY = "RueVK9M3tKUAymycg5BYB1DM3Wea8KWBSlmjde6x";
    private ApiNasa apiNasa;
    String base64UrlHd;
    String base64url;

    private FirebaseAuth firebaseAuth;


    private String dateSelected, daySelected, monthSelected, yearSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        hackNasa = new HackNasa();

        initViews();
        //init firebase auth
        firebaseAuth =FirebaseAuth.getInstance();
        checkUser();

        //handle click, logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

    }

    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser==null){
            startActivity(new Intent(this,MainActivity2.class));
            finish();
        }
        else {
            //logged in, get user info
            String email = firebaseUser.getEmail();
            //set in textview of toolbar
            binding.subtitleTv.setText(email);
        }
    }

    private void initViews() {

        List<String> days = new ArrayList<>();

        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }
        List<String> months = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            months.add(String.valueOf(i));
        }
        List<String> years = new ArrayList<>();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= currentYear - 100; i--) {
            years.add(String.valueOf(i));
        }
        days.add(0, "days");
        months.add(0, "months");
        years.add(0, "years");

        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);

        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spnYear.setAdapter(yearsAdapter);
        binding.spnMonth.setAdapter(monthsAdapter);
        binding.spnDate.setAdapter(daysAdapter);

        binding.spnDate.setOnItemSelectedListener(new CustomOnItemSelectedListener());
        binding.spnYear.setOnItemSelectedListener(new CustomOnItemSelectedListener());
        binding.spnMonth.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        binding.btnGetDataFormNasa.setOnClickListener(v -> callApiGetDataFormNasa(API_KEY, dateSelected));

        binding.layoutShowData.setVisibility(View.GONE);

        binding.btnPushData.setOnClickListener(v -> sendDataToServer());

        binding.btnGetDataFormMyServer.setOnClickListener(v->{
            startActivity(new Intent(MainActivity.this, DataFromMyServerActivity.class));
        });

    }

    private void callApiGetDataFormNasa(String api_key, String date) {
        apiNasa = ApiResponeNasa.getApiNasa();
        apiNasa.getDataFromNasa(api_key, date).enqueue(new Callback<HackNasa>() {
            @Override
            public void onResponse(Call<HackNasa> call, Response<HackNasa> response) {
                hackNasa = response.body();
                binding.layoutShowData.setVisibility(View.VISIBLE);
                binding.tvTitle.setText(hackNasa.getTitle());
                binding.tvDate.setText(hackNasa.getDate());
                binding.tvExplanation.setText(hackNasa.getExplanation());
                if (hackNasa.getHdurl() != null) {
                    Glide.with(MainActivity.this).load(hackNasa.getHdurl()).error(R.drawable.baseline_error_24).into(binding.imgHd);
                } else {
                    Glide.with(MainActivity.this).load(hackNasa.getUrl()).error(R.drawable.baseline_error_24).into(binding.imgHd);
                }
                binding.tvNotification.setText("get data from Nasa successfully");
                binding.tvNotification.setTextColor(Color.parseColor("#198754"));

                Log.d("AAA", response.body().toString());
            }

            @Override
            public void onFailure(Call<HackNasa> call, Throwable t) {
                binding.layoutShowData.setVisibility(View.GONE);
                Log.d("EEE", t.getMessage());
                binding.tvNotification.setText("get data from Nasa failed");
                binding.tvNotification.setTextColor(Color.RED);
            }
        });
    }

    private void sendDataToServer() {


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
             base64UrlHd = convertUrlToBase64(hackNasa.getHdurl());
             base64url = convertUrlToBase64(hackNasa.getUrl());
        }

        hackNasa.setHdurl(base64UrlHd);
        hackNasa.setUrl(base64url);

        Log.d("sendDataToServer", hackNasa.toString());
        ApiMyServer.apiService.postData(hackNasa).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                binding.tvNotification.setText("push data to my server successfully");
                binding.tvNotification.setTextColor(Color.parseColor("#198754"));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                binding.tvNotification.setText("post data to my server failed");
                binding.tvNotification.setTextColor(Color.RED);
                Log.d("API", t.getMessage());
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private String convertUrlToBase64(String url) {
        byte[] byteInput = url.getBytes();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        String encodedString = base64Encoder.encodeToString(byteInput);
        return encodedString;
    }

    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            daySelected = binding.spnDate.getSelectedItem().toString();
            monthSelected = binding.spnMonth.getSelectedItem().toString();
            yearSelected = binding.spnYear.getSelectedItem().toString();
            if (!daySelected.equals("days") && !monthSelected.equals("months") && !yearSelected.equals("years")) {
                dateSelected = yearSelected + "-" + monthSelected + "-" + daySelected;
                Log.d("Selected Date", dateSelected);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

}

