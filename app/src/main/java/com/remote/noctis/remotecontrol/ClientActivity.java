package com.remote.noctis.remotecontrol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Noctis on 2015-11-13.
 */
@SuppressLint("NewView")
public class ClientActivity extends Activity implements  View.OnTouchListener{ //SurfaceHolder.Callback, View.OnTouchListener {
    private static final String TAG = "noctis";

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Thread thread;

    int deviceWidth;
    int deviceHeight;

    String address;
    String port;

    private boolean locker=true;


    byte[] bytearray = new byte[1024];

    public static final String KEY_FINGER_DOWN = "fingerdown";
    public static final String KEY_FINGER_UP = "fingerup";
    public static final String KEY_FINGER_MOVE = "fingermove";
    public static final String KEY_EVENT_TYPE = "type";

    private static Socket client = null;
    private static Socket touchSocket = null;
    private static ObjectInputStream ois = null;
    private static InputStream in = null;
    private static OutputStream out = null;
    Canvas canvas;
    private static boolean started=false;

    Bitmap mutableBitmap;
    Thread t;
    Thread connection;
    private static boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics dm = new DisplayMetrics();

        address = getIntent().getStringExtra(AddressInputDialog.KEY_ADDRESS_EXTRA);
        port = getIntent().getStringExtra(AddressInputDialog.KEY_PORT_EXTRA);
        hideSystemUI();
        setContentView(R.layout.activity_client);
        surfaceView = (SurfaceView) findViewById(R.id.client_surface_view);
        holder = surfaceView.getHolder();
        //surfaceView.getHolder().addCallback(this);
        surfaceView.setOnTouchListener(this);

        running = true;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        deviceWidth = dm.widthPixels;
        deviceHeight = dm.heightPixels;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (!InetAddress.getByName(address).isReachable(2000)) {
                        Intent startIntent = new Intent(ClientActivity.this, MainActivity.class);

                        startActivity(startIntent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }}).start();

       connection =  new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    client = new Socket(address, Integer.parseInt(port));
                    client.getOutputStream();
                    ois = new ObjectInputStream(client.getInputStream());


                    while (running) {
                        if(!InetAddress.getByName(address).isReachable(2000)){
                            Intent startIntent = new Intent(ClientActivity.this, MainActivity.class);

                            startActivity(startIntent);
                        }
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        int size = ois.readInt();
                        //System.out.println(size);
                        byte[] data = new byte[size];
                        int length = 0;
                        int nreceived = 0;

                        while ((length = ois.read(data, 0, size)) != -1) {
                            out.write(data, 0, length);
                            out.flush();
                            nreceived += length;

                            if (nreceived == size) {
                                break;
                            }
                        }
                        System.out.println("Image recieved");
                        byte[] bytePicture = out.toByteArray();
                        out.close();

                        Bitmap screen = BitmapFactory.decodeByteArray(bytePicture, 0, bytePicture.length);
                        screen.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        mutableBitmap = screen.copy(Bitmap.Config.ARGB_8888, true);

                        //canvas = new Canvas(screen);
                        /*thread = new Thread(this);
                        thread.start();*/
                        //runSurface(mutableBitmap);
                        if(!started){
                        t.start();
                        started=true;}
                        t.join(100);




                    }
                } catch (ConnectException e){
                    showToast("Could't connect wiht server");
                    Intent startIntent = new Intent(ClientActivity.this, MainActivity.class);
                    startActivity(startIntent);
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        connection.start();

        /*try {

            run();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        t = new Thread(new Runnable() {
            public void run() {
                while(locker && running){
                    if(!holder.getSurface().isValid()){
                        continue;
                    }
                    Canvas canvas = holder.lockCanvas(); //Starting to edit pixels on the surface

                    draw(canvas, mutableBitmap); //All drawing in this method

                    holder.unlockCanvasAndPost(canvas); //End of drawing. System will now paint canvas on surface
                }
            }
        });




    }


    public void runSurface(Bitmap bitmap) {
        while(locker && running){
            if(!holder.getSurface().isValid()){
                continue;
            }
            Canvas canvas = holder.lockCanvas(); //Starting to edit pixels on the surface

            draw(canvas, bitmap); //All drawing in this method

            holder.unlockCanvasAndPost(canvas); //End of drawing. System will now paint canvas on surface
        }
    }

    private void draw(Canvas canvas, Bitmap bitmap) {
        int border = 20;
        RectF r = new RectF(border, border, canvas.getWidth()-20, canvas.getHeight()-20);
        Paint paint = new Paint();
        paint.setARGB(200, 135, 135, 135); //paint color GRAY+SEMY TRANSPARENT
        canvas.drawRect(r , paint );

        canvas.drawBitmap(bitmap,0,0,null);

    }

    /*private void run() throws IOException {
        while (true) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int size = ois.readInt();
            System.out.println(size);
            byte[] data = new byte[size];
            int length = 0;
            int nreceived = 0;

            while ((length = ois.read(data, 0, size)) != -1) {
                out.write(data, 0, length);
                out.flush();
                nreceived += length;
                if (nreceived == size) {
                    break;
                }
            }
            byte[] bytePicture = out.toByteArray();
            out.close();

            Bitmap screen = BitmapFactory.decodeByteArray(bytePicture, 0, bytePicture.length);
            screen.compress(Bitmap.CompressFormat.PNG, 100, out);


        }
    }*/


    /*@Override
    public void surfaceCreated(SurfaceHolder holder) {



        showToast("IP = " + address);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        *//*if(!client.equals(null)) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*//*
        showToast("connection CLOSED");
        Intent mainIntent = new Intent(ClientActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
*/
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
            touchData.put("x", motionEvent.getX() / deviceWidth);
            touchData.put("y", motionEvent.getY() / deviceHeight);
            Log.d(TAG, "Sending = " + touchData.toString());
            if (touchSocket != null) {
                try (OutputStreamWriter out = new OutputStreamWriter(
                        touchSocket.getOutputStream(), StandardCharsets.UTF_8)) {
                    out.write(touchData.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
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

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(ClientActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        running = false;
        stopThread(t);
        stopThread(connection);
        showToast("stopped all Threads");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            running = false;
            started = false;
            try {
                client.close();
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            stopThread(t);
            stopThread(connection);
            showToast("stopped all Threads");
            Intent i = new Intent(ClientActivity.this, MainActivity.class);
            startActivity(i);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void pause() {
        //CLOSE LOCKER FOR run();
        locker = false;
       /* while(true){
            try {
                //WAIT UNTIL THREAD DIE, THEN EXIT WHILE LOOP AND RELEASE a thread
                thread.join();
            } catch (InterruptedException e) {e.printStackTrace();
            }
            break;
        }
        thread = null;*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume();
    }

    private void resume() {
        //RESTART THREAD AND OPEN LOCKER FOR run();
        locker = true;

    }

    public synchronized void stopThread(Thread runner){
        if(runner != null){
            runner.interrupt();
            runner = null;
        }
    }
}
