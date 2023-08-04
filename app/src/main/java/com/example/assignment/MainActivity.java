package com.example.assignment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.assignment.api.ApiMyServer;
import com.example.assignment.api.ApiNasa;
import com.example.assignment.api.ApiResponeNasa;
import com.example.assignment.databinding.ActivityMainBinding;
import com.example.assignment.models.HackNasa;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private TextView tvServerName, tvServerPort, tvStatus,tvReceivedMessage;
    private Button bntSend;
    private String serverIP = "192.168.0.107"; // ĐỊA CHỈ IP MÁY
    private int serverPort = 1234; // PORT
    private ServerThread serverThread;
    private EditText edMessage;
    // Sử dụng Handler để làm việc với giao diện trong Thread
    private Handler handler = new Handler(Looper.getMainLooper());
    private FirebaseAuth auth;

    TextView textView;
    private Button logout;
    FirebaseUser user;
    private ActivityMainBinding binding;
    private HackNasa hackNasa;
    private static final String API_KEY = "SkHr3gLrVR8WpMuEWoLxazM6uFpoHHvddjf2nx6w";
    private ApiNasa apiNasa;
    String base64UrlHd;
    String base64url;

    private String dateSelected, daySelected, monthSelected, yearSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();
        logout=findViewById(R.id.logout);
        textView=findViewById(R.id.userdetail);
        user=auth.getCurrentUser();
        tvServerName = findViewById(R.id.tvsevername);
        tvServerPort = findViewById(R.id.tvServerpory);
        tvStatus = findViewById(R.id.tvstatus);
        tvReceivedMessage = findViewById(R.id.tv_nhantinnhan);


        serverThread = new ServerThread();
        Toast.makeText(this, "SERVER ĐÃ CHẠY", Toast.LENGTH_SHORT).show();
        serverThread.startServer();



        // Hiển thị địa chỉ IP và cổng của Server lên giao diện

        tvServerName.setText(serverIP);
        tvServerPort.setText(String.valueOf(serverPort));

        if(user==null){
            Intent intent=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            textView.setText(user.getEmail());
        }
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        hackNasa = new HackNasa();

        initViews();
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
    class ServerThread extends Thread {
        private boolean serverRunning;
        private ServerSocket serverSocket;
        // Phương thức start
        public void startServer() {
            serverRunning = true;
            start();
        }
        public void stopServer() {
            serverRunning = false;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tvStatus.setText("Server stopped");
                }
            });
        }
        public void sendMessageToClients(final String message) {
            if (serverSocket != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (ClientHandler client : clientsList) {
                            client.sendMessageToClient(message);
                        }
                    }
                }).start();
            }
        }
        private ArrayList<ClientHandler> clientsList = new ArrayList<ClientHandler>();

        @Override
        public void run() {
            try {
                // Tạo Socket Server
                serverSocket = new ServerSocket(serverPort);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("Waiting for Clients");
                    }
                });

                while (serverRunning) {
                    // Chấp nhận các kết nối từ Client
                    java.net.Socket socket = serverSocket.accept();
                    // Xử lý Client kết nối mới trong một luồng riêng biệt

                    ServerThread.ClientHandler client = new ServerThread.ClientHandler(socket);
                    client.start();
                    clientsList.add(client);
                    // Cập nhật giao diện khi có Client kết nối thành công

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Connected to: " + socket.getInetAddress() + " : " + socket.getLocalPort());
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        class ClientHandler extends Thread {
            private java.net.Socket clientSocket;
            private BufferedReader br_input;
            private PrintWriter output_Client;

            public ClientHandler(java.net.Socket socket) {
                clientSocket = socket;
                try {
                    // Lấy luồng đầu vào và luồng đầu ra của Client
                    br_input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    output_Client = new PrintWriter(clientSocket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Phương thức gửi tin nhắn đến Client
            public void sendMessageToClient(String message) {
                output_Client.println(message);
            }
            @Override
            public void run() {
                try {
                    // Hiển thị tin nhắn từ Client lên giao diện
                    String messageFromClient;
                    while ((messageFromClient = br_input.readLine()) != null) {
                        // Hiển thị tin nhắn từ Client lên giao diện
                        final String finalMessage = messageFromClient;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Set tin nhắn lên texview giao diện
                                tvReceivedMessage.setText("Tin nhắn từ client: " + finalMessage);
                                // thông báo co tin nhắn mới
                                final Dialog dialog = new Dialog(MainActivity.this);
                                // set layout dialog
                                dialog.setContentView(R.layout.dialogthongbao);
                                TextView tvmes=dialog.findViewById(R.id.tvmess);
                                // set tin nhắn lên texviewở dialog
                                tvmes.setText(""+finalMessage);
                                dialog.setCancelable(true);

                                // show dialog
                                dialog.show();

                                // set dialog sau 4s thì ẩn
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                    }
                                }, 4000);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}