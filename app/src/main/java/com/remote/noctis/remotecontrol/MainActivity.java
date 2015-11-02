package com.remote.noctis.remotecontrol;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends Activity  {
    //client side
    private Socket client;
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private OutputStream outputStream;
    private Button button;
    private TextView text;

    //server side
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;
    private static String message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        button = (Button) findViewById(R.id.button1);   //reference to the send button
        text = (TextView) findViewById(R.id.textView1);   //reference to the text view


        //Button press event listener
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            Socket client = new Socket("192.168.0.12", 8187);
                            DataOutputStream DOS = new DataOutputStream(client.getOutputStream());
                            DOS.writeUTF("COSIK!!!!!!!!!!!!!");
                            client.close();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }).start();


            }
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(8187); // Server socket

                } catch (IOException e) {
                    System.out.println("Could not listen on port: 8187");
                }

                System.out.println("Server started. Listening to the port 88187");

                while (true) {
                    try {

                        clientSocket = serverSocket.accept(); // accept the client
                        // connection
                        inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                        bufferedReader = new BufferedReader(inputStreamReader); // get
                        // client
                        // msg
                        message = bufferedReader.readLine();

                        System.out.println(message);

                        showToast(message);
                        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                        inputStreamReader.close();
                        clientSocket.close();

                    } catch (IOException ex) {
                        System.out.println("Problem in message reading");
                    }
                }

            }
        };

        thread.start();



    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
    }


    public class ExcuteNetworkOperation extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            /**
             * show dialog
             */
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            /**
             * Do network related stuff
             * return string response.
             */
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            /**
             * update ui thread and remove dialog
             */
            super.onPostExecute(result);
        }
    }


}
