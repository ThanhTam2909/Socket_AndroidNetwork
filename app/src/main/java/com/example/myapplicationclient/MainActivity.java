package com.example.myapplicationclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    TextView textget_data;
    EditText nhapData_Client, edt_serverHost, edt_serverport;
    Button send_data, btn_connect;

    private Socket socket;
    private String serverName;
    private int serverPort;

    private BufferedReader br_input;
    private PrintWriter output;
    private boolean connected = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textget_data = (TextView) findViewById(R.id.get_data);
        nhapData_Client = (EditText) findViewById(R.id.nhap_data);
        edt_serverHost = (EditText) findViewById(R.id.nhap_serverHost);
        edt_serverport = (EditText) findViewById(R.id.nhap_serverPort);
        send_data = (Button) findViewById(R.id.button_send);
        btn_connect = (Button) findViewById(R.id.button_connect);

        send_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageSend = nhapData_Client.getText().toString();
                if (!messageSend.isEmpty() && connected) {
                    sendMessageServer(messageSend);
                    Toast.makeText(MainActivity.this, "Gửi tin nhắn thành công", Toast.LENGTH_SHORT).show();
                    nhapData_Client.setText("");
                }
            }
        });

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!connected) {
                    serverName = edt_serverHost.getText().toString();
                    serverPort = Integer.valueOf(edt_serverport.getText().toString());
                    onClickClient(serverName, serverPort);
                }else {
                    disconnectServer();
                }
            }
        });
    }

    public void onClickClient(String serverName, int serverPort){


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(serverName, serverPort);
                    br_input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    output = new PrintWriter(socket.getOutputStream(), true);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            connected = true;
                        }
                    });

                    String txtFormServer;
                    while ((txtFormServer = br_input.readLine()) != null){
                        final String finalMes = txtFormServer;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Bạn nhận được tin nhắn từ Server", Toast.LENGTH_SHORT).show();
                                textget_data.setText("Tin nhan Server: " + finalMes);
                            }
                        });
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void disconnectServer(){
        if ( socket != null){
            try {
                socket.close();
                connected = false;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendMessageServer(final String message){
        if (output != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    output.println(message);
                }
            }).start();
        }
    }


}