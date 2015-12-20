package com.remote.noctis.remotecontrol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
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
import java.io.EOFException;
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
public class ClientActivity extends Activity implements View.OnTouchListener {
    private static final String TAG = "noctis";

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Thread thread;

    int deviceWidth;
    int deviceHeight;

    String address;
    String port;

    private boolean locker = true;


    byte[] bytearray = new byte[1024];

    public static final String KEY_FINGER_DOWN = "fingerdown";
    public static final String KEY_FINGER_UP = "fingerup";
    public static final String KEY_FINGER_MOVE = "fingermove";
    public static final String KEY_EVENT_TYPE = "type";

    private static Socket client = null;
    private static Socket touchSocket = null;
    private static Socket keySocket = null;
    private static ObjectInputStream ois = null;
    private static boolean started = false;

    Bitmap mutableBitmap;
    Thread t;
    Thread connection;
    Thread touchConnection;
    Thread keyConnection;
    Thread exitThread;
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
        surfaceView.setOnTouchListener(this);

        running = true;
        started = false;

        final int timeout = 500;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        deviceWidth = dm.widthPixels;
        deviceHeight = dm.heightPixels;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (!InetAddress.getByName(address).isReachable(2000)) {
                        /*Intent startIntent = new Intent(ClientActivity.this, MainActivity.class);

                        startActivity(startIntent);*/
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        connection = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    client = new Socket(address, Integer.parseInt(port));
                    //client.setSoTimeout(timeout);
                    client.getOutputStream();
                    ois = new ObjectInputStream(client.getInputStream());


                    while (running) {
                        if (!InetAddress.getByName(address).isReachable(2000)) {
                            /*Intent startIntent = new Intent(ClientActivity.this, MainActivity.class);

                            startActivity(startIntent);*/
                            finish();
                        }
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        int size = ois.readInt();
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
                        screen.compress(Bitmap.CompressFormat.PNG, 100, out);

                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);

                        int width = screen.getWidth();
                        int height = screen.getHeight();

                        float scaleWidth = ((float) metrics.widthPixels) / width;
                        float scaleHeight = ((float) metrics.heightPixels) / height;


                        // create a matrix for the manipulation
                        Matrix matrix = new Matrix();
                        // resize the bit map
                        matrix.postScale(scaleWidth, scaleHeight);

                        // recreate the new Bitmap
                        Bitmap resizedScreen = Bitmap.createBitmap(screen, 0, 0, width, height, matrix, false);

                        mutableBitmap = resizedScreen.copy(Bitmap.Config.ARGB_8888, true);


                        if (!started) {
                            t.start();
                            started = true;
                        }
                        //t.join(200);


                    }
                } catch (ConnectException e) {
                    showToast("Could't connect with server");
                    finish();
                    e.printStackTrace();
                } catch (EOFException e) {
                    showToast("Server has disconnected");
                    finish();
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        connection.start();


        touchConnection = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    touchSocket = new Socket(address, Integer.parseInt(port) + 1);
                    //touchSocket.setSoTimeout(timeout);
                } catch (ConnectException e) {
                    //showToast("Could't connect with touch server");
                    finish();
                    e.printStackTrace();
                } catch (EOFException e) {
                    //showToast("Touch Server has disconnected");
                    finish();
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {


                    @Override
                    public void run() {

                    }
                });
            }
        });

        touchConnection.start();


        keyConnection = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    keySocket = new Socket(address, Integer.parseInt(port) + 2);
                    //keySocket.setSoTimeout(timeout);
                } catch (ConnectException e) {
                    //showToast("Could't connect with key server");
                    finish();
                    e.printStackTrace();
                } catch (EOFException e) {
                    //showToast("Key Server has disconnected");
                    finish();
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {


                    @Override
                    public void run() {

                    }
                });
            }
        });
        keyConnection.start();

        t = new Thread(new Runnable() {
            public void run() {
                while (locker && running) {
                    if (!holder.getSurface().isValid()) {
                        continue;
                    }
                    Canvas canvas = holder.lockCanvas(); //Starting to edit pixels on the surface

                    draw(canvas, mutableBitmap); //All drawing in this method

                    holder.unlockCanvasAndPost(canvas); //End of drawing. System will now paint canvas on surface
                }
            }
        });

        exitThread = new Thread(new Runnable() {
            public void run() {
                if (keySocket != null) {
                    try {
                        DataOutputStream DOS = new DataOutputStream(keySocket.getOutputStream());
                        DOS.writeUTF(Integer.toString(15001900));
                        System.out.println("ExitCode Sended: " + 15001900);
                    } catch (IOException e) {
                        e.printStackTrace();


                    }
                } else {
                    Log.e(TAG, "Can't send ExitCode. Socket is null.");
                    //showToast("Couldn't connect with server");
                    finish();
                }
            }
        });


    }


    public void runSurface(Bitmap bitmap) {
        while (locker && running) {
            if (!holder.getSurface().isValid()) {
                continue;
            }
            Canvas canvas = holder.lockCanvas(); //Starting to edit pixels on the surface

            draw(canvas, bitmap); //All drawing in this method

            holder.unlockCanvasAndPost(canvas); //End of drawing. System will now paint canvas on surface
        }
    }

    private void draw(Canvas canvas, Bitmap bitmap) {
        int border = 20;
        RectF r = new RectF(border, border, canvas.getWidth() - 20, canvas.getHeight() - 20);
        Paint paint = new Paint();
        paint.setARGB(200, 135, 135, 135); //paint color GRAY+SEMY TRANSPARENT
        canvas.drawRect(r, paint);

        canvas.drawBitmap(bitmap, 0, 0, null);

    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        JSONObject touchData = new JSONObject();
        try {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchData.put(KEY_EVENT_TYPE, KEY_FINGER_DOWN);
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
                try {
                    DataOutputStream DOS = new DataOutputStream(touchSocket.getOutputStream());
                    DOS.writeUTF(touchData.toString());
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
    protected void onDestroy() {
        running = false;
        exitThread.start();
        stopThread(t);
        stopThread(connection);
        stopThread(touchConnection);
        stopThread(keyConnection);
        stopThread(exitThread);
        //showToast("stopped all Threads");
        super.onDestroy();

    }

/*    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.w(TAG, "LONG PRESS ");
            if (keyCode == KeyEvent.KEYCODE_BACK ) {
                *//*running = false;
                //started = false;
                try {
                    client.close();
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }

                stopThread(t);
                stopThread(connection);
                stopThread(touchConnection);
                stopThread(keyConnection);
                showToast("stopped all Threads");
                this.finish();*//*
                return false;
            }
        }
        return super.onKeyLongPress(keyCode, event);
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keySocket != null) {
            try {
                DataOutputStream DOS = new DataOutputStream(keySocket.getOutputStream());
                DOS.writeUTF(Integer.toString(keyCode));
                System.out.println("Key Sended: " + keyCode);
            } catch (IOException e) {
                e.printStackTrace();


            }
        } else {
            Log.e(TAG, "Can't send key events. Socket is null.");
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // event.startTracking();
            //onBackPressed();
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // event.startTracking();
            //onBackPressed();
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {


            return false;
        }

        return false;


    }

    boolean doubleMenuToPowerPressedOnce = false;


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {


        if (keyCode == KeyEvent.KEYCODE_BACK) {

            event.startTracking();
            onBackPressed();
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // event.startTracking();
            //onBackPressed();
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {

            event.startTracking();

            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (doubleMenuToPowerPressedOnce) {

                DataOutputStream DOS = null;
                try {
                    DOS = new DataOutputStream(keySocket.getOutputStream());
                    DOS.writeUTF(Integer.toString(26));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Key Sended: " + 26);
            }

            this.doubleMenuToPowerPressedOnce = true;
            showToast("Please click MENU again to POWER");


            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleMenuToPowerPressedOnce = false;
                }
            }, 800);
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    private void pause() {
        //CLOSE LOCKER FOR run();
        locker = false;
        //started = false;
        running = false;
        stopThread(t);
        stopThread(connection);
        stopThread(touchConnection);
        stopThread(keyConnection);
        //showToast("stopped all Threads");
        this.finish();
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

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            running = false;
            //started = false;
            try {
                client.close();
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            stopThread(t);
            stopThread(connection);
            stopThread(touchConnection);
            stopThread(keyConnection);
            //showToast("stopped all Threads");
            this.finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        showToast("Please click BACK again to exit");


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 800);
    }


    public void onMenuPressed() {
        if (doubleBackToExitPressedOnce) {


            return;
        }

        this.doubleMenuToPowerPressedOnce = true;
        showToast("Please click MENU again to POWER");


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleMenuToPowerPressedOnce = false;
            }
        }, 800);
    }

    @Override
    protected void onResume() {
        //this.recreate();

        /*started = true;
        running = true;*/
        super.onResume();
        resume();
    }

    private void resume() {
        //RESTART THREAD AND OPEN LOCKER FOR run();

        locker = true;
        /*running = true;
        started = false;
        connection.start();
        touchConnection.start();
        keyConnection.start();*/


    }


    public synchronized void stopThread(Thread runner) {
        if (runner != null) {
            runner.interrupt();
            runner = null;
        }
    }
}
