package com.remote.noctis.remotecontrol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Noctis on 2015-11-13.
 */
@SuppressLint("NewView")
public class ClientActivity extends Activity implements SurfaceHolder.Callback, View.OnTouchListener{
    private static final String TAG ="noctis";

    SurfaceView surfaceView;

    int deviceWidth;
    int deviceHeight;

    String address;
    String port;

    public static final String KEY_FINGER_DOWN = "fingerdown";
    public static final String KEY_FINGER_UP = "fingerup";
    public static final String KEY_FINGER_MOVE = "fingermove";
    public static final String KEY_EVENT_TYPE = "type";

    private static Socket client = null;
    private static Socket touchSocket = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        deviceWidth = dm.widthPixels;
        deviceHeight = dm.heightPixels;
        address = getIntent().getStringExtra(AddressInputDialog.KEY_ADDRESS_EXTRA);
        port = getIntent().getStringExtra(AddressInputDialog.KEY_PORT_EXTRA);
        hideSystemUI();
        setContentView(R.layout.activity_client);
        surfaceView = (SurfaceView) findViewById(R.id.client_surface_view);
        surfaceView.getHolder().addCallback(this);
        surfaceView.setOnTouchListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);




        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                new AddressInputDialog().show(getFragmentManager(), "Address Dialog");




                            try {


                                    client = new Socket(address, Integer.parseInt(port));
                                touchSocket = client;
                                    showToast("connected");
                                    //DataOutputStream DOS = new DataOutputStream(client.getOutputStream());
                                    //DOS.writeUTF(editText0.getText().toString());


                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }



            }
        }).start();


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showToast("connection CLOSED");
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        JSONObject touchData = new JSONObject();
        try {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchData.put("type", KEY_FINGER_DOWN);
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchData.put(KEY_EVENT_TYPE, KEY_FINGER_MOVE);
                    break;
                case MotionEvent.ACTION_UP:
                    touchData.put(KEY_EVENT_TYPE, KEY_FINGER_UP);
                    break;
                default:
                    return true;
            }
            touchData.put("x", motionEvent.getX()/deviceWidth);
            touchData.put("y", motionEvent.getY()/deviceHeight);
            Log.d(TAG, "Sending = " + touchData.toString());
            if (touchSocket != null) {
                try (OutputStreamWriter out = new OutputStreamWriter(
                        touchSocket.getOutputStream(), StandardCharsets.UTF_8)) {
                    out.write(touchData.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Log.e(TAG, "Can't send touch events. Socket is null.");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(ClientActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
