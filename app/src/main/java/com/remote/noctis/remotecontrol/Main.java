package com.remote.noctis.remotecontrol;

import android.content.SharedPreferences;
import android.os.*;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.view.InputDeviceCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Noctis on 2015-11-13.
 */
public class Main {

    private static Socket clientSocket;
    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;
    private static ServerSocket serverSocket;

    private static String touchEvent ="s";

    private static EventInput input;


    public static final String TAG = "RemoteControl_MAIN";


    public static void main(String[] args) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(ServerService.getServerPort()); // Server socket

                } catch (IOException e) {
                    System.out.println("Could not listen on port: " + ServerService.getServerPort());
                }
                Log.d(TAG, "Server started. Listening to the port" +ServerService.getServerPort());
                System.out.println("Server started. Listening to the port " + ServerService.getServerPort());

                while (true) {
                    try {

                        clientSocket = serverSocket.accept(); // accept the client
                        // connection
                        Log.d(TAG, "Accepted connection from client");
                        inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                        bufferedReader = new BufferedReader(inputStreamReader); // get
                        // client
                        // msg
                        touchEvent = bufferedReader.readLine();

                        System.out.println(touchEvent);

                        inputStreamReader.close();


                        JSONObject touch = new JSONObject(touchEvent);
                        float x = Float.parseFloat(touch.getString("x")) * ServerService.deviceWidth;
                        float y = Float.parseFloat(touch.getString("y")) * ServerService.deviceHeight;
                        String eventType = touch.getString(ClientActivity.KEY_EVENT_TYPE);
                        if (eventType.equals(ClientActivity.KEY_FINGER_DOWN)) {
                            input.injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 0,
                                    SystemClock.uptimeMillis(), x, y, 1.0f);
                        } else if (eventType.equals(ClientActivity.KEY_FINGER_UP)) {
                            input.injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 1,
                                    SystemClock.uptimeMillis(), x, y, 1.0f);
                        } else if (eventType.equals(ClientActivity.KEY_FINGER_MOVE)) {
                            input.injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 2,
                                    SystemClock.uptimeMillis(), x, y, 1.0f);
                        }

                        clientSocket.close();




                    } catch (IOException ex) {
                        System.out.println("Problem in message reading");
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        thread.start();

    }

    /*static Looper looper;

    public static final String TAG = "RemoteDroid_MAIN";
    public static void main(String[] args) {

        Looper.prepare();
        looper = Looper.myLooper();

        Log.d(TAG, "current process id = " + android.os.Process.myPid());
        Log.d(TAG, "current process uid = " + Process.myUid());
        try {
            input = new EventInput();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            serverSocket = new ServerSocket(Integer.parseInt(editText2.getText().toString())); // Server socket

        } catch (IOException e) {
            System.out.println("Could not listen on port: " + editText2.getText().toString());
        }

        System.out.println("Server started. Listening to the port " + editText2.getText().toString());

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

                inputStreamReader.close();
                clientSocket.close();

            } catch (IOException ex) {
                System.out.println("Problem in message reading");
            }
        }

        AsyncHttpServer server = new AsyncHttpServer();
        server.websocket("/", null, new AsyncHttpServer.WebSocketRequestCallback() {

            @Override
            public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
                Log.d(TAG, "Touch client connected");
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        if (ex != null) {
                            ex.printStackTrace();
                        }
                        Main.looper.quit();
                        Log.d(TAG, "Main WebSocket closed");
                    }
                });
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        Log.d(TAG, "Received string = " + s);
                        try {
                            JSONObject touch = new JSONObject(s);
                            float x = Float.parseFloat(touch.getString("x")) * ServerService.deviceWidth;
                            float y = Float.parseFloat(touch.getString("y")) * ServerService.deviceHeight;
                            String eventType = touch.getString(ClientActivity.KEY_EVENT_TYPE);
                            if (eventType.equals(ClientActivity.KEY_FINGER_DOWN)) {
                                input.injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 0,
                                        SystemClock.uptimeMillis(), x, y, 1.0f);
                            } else if (eventType.equals(ClientActivity.KEY_FINGER_UP)) {
                                input.injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 1,
                                        SystemClock.uptimeMillis(), x, y, 1.0f);
                            } else if (eventType.equals(ClientActivity.KEY_FINGER_MOVE)) {
                                input.injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 2,
                                        SystemClock.uptimeMillis(), x, y, 1.0f);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        server.listen(6059);
        Log.d(TAG, "Touch server listening at port 6059");

        if (input == null) {
            Log.e(TAG, "THIS SHIT IS NULL");
        } else {
            Log.e(TAG, "THIS SHIT NOT NULL");
        }

        Log.d(TAG, "Waiting for main to finish");
        Looper.loop();
        Log.d(TAG, "Returning from MAIN");
    }*/


}
