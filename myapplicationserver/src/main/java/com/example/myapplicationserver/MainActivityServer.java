package com.example.myapplicationserver;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivityServer extends AppCompatActivity {

    TextView textget_data, status;
    EditText nhapData_server;
    Button send_data, btn_start, btn_stop;

    private String serverHost = " 192.168.1.22";
    private  int serverPort = 9999;

    private Handler handler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_server);

        textget_data = (TextView) findViewById(R.id.get_data);
        status = (TextView) findViewById(R.id.status_server);
        nhapData_server = (EditText) findViewById(R.id.nhap_data);
        send_data = (Button) findViewById(R.id.button_send);
        btn_start = (Button) findViewById(R.id.button_start);
        btn_stop = (Button) findViewById(R.id.button_stop);

    }

    private ServerThread serverThread;

    public void onClickStartServer(View view){
        //textget_data.setText("Host: " + serverHost, TextView.BufferType.valueOf("Port: " + serverPort));
        btn_start.setEnabled(false);
        btn_stop.setEnabled(true);
        send_data.setEnabled(true);
        serverThread = new ServerThread();
        serverThread.startServer();
    }

    public void onClickStopServer(View view){
        btn_start.setEnabled(true);
        btn_stop.setEnabled(false);
        send_data.setEnabled(false);
        serverThread = new ServerThread();
        if (serverThread == null) {
            serverThread.stopServer();
            status.setText("Stop Server");
        }
    }

    public void  onclickSend(View view){
        String tinNhan = nhapData_server.getText().toString();
        if (!tinNhan.isEmpty() && serverThread != null) {
            serverThread.sendMessageClient(tinNhan);
            Toast.makeText(this, "Gửi tin nhắn thành công!", Toast.LENGTH_SHORT).show();
            nhapData_server.setText("");
        }
    }

    class ServerThread extends Thread implements Runnable{
        private boolean serverRunning;
        private ServerSocket serverSocket;

        public void startServer(){
            serverRunning = true;
            start();
        }

        public void stopServer(){
            serverRunning = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                status.setText("Stop Serverr");
                            }
                        });
                    }
                }
            });
        }

        private ArrayList<Client> clients = new ArrayList<>();

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(serverPort);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        status.setText("Waiting for Client");
                    }
                });

                while(serverRunning){
                    Socket socket = serverSocket.accept();

                    Client client = new Client(socket);
                    client.start();
                    clients.add(client);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("Kết nối thành công!");
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessageClient(final String message){
            if (serverSocket != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (Client client : clients){
                            client.sendMessageClient(message);
                        }
                    }
                }).start();
            }
        }

        class Client extends Thread{
            private Socket clientSocket;
            private BufferedReader br;
            private PrintWriter output;
            public Client(Socket socket) {
                clientSocket = socket;
                try {
                    br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void sendMessageClient(String message){
                output.println(message);
            }

            @Override
            public void run(){
                String messageClient;
                while (true){
                    try {
                        if ((messageClient = br.readLine()) != null){
                            final String finalMes = messageClient;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivityServer.this, "Bạn nhận được tin nhắn từ Client", Toast.LENGTH_SHORT).show();
                                    textget_data.setText("Tin nhan tu Clients: " + finalMes);
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


}