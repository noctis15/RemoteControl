package com.remote.noctis.remotecontrol;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Noctis on 2015-11-13.
 */
public class ServerService extends Service {
    private static final String TAG = "noctis";

    private static int serverPort;

    SharedPreferences preferences;

    Handler mHandler;

    ServerSocket server;

    StartServerTask startServerTask;

    static int deviceWidth;
    static int deviceHeight;

    private class ToastRunnable implements Runnable {
        String mText;

        public ToastRunnable(String text) {
            mText = text;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_SHORT).show();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() == "STOP") {
            dispose();
            return START_NOT_STICKY;
        }
        if (server == null && intent.getAction().equals("START")) {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            setServerPort(Integer.parseInt(preferences.getString(PortInputDialog.KEY_PORT_EXTRA, "8187")));
            DisplayMetrics dm = new DisplayMetrics();
            Display mDisplay = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            mDisplay.getMetrics(dm);
            deviceWidth = dm.widthPixels;
            deviceHeight = dm.heightPixels;
            /*try {
                server = new ServerSocket(ServerService.getServerPort());
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            startServerTask = new StartServerTask();
            startServerTask.execute();
            //new StartServerTask().execute();
            updateNotification("Stream live at");
            Toast.makeText(this, "The new Service Started", Toast.LENGTH_LONG).show();
           /* new Thread(new Runnable() {
                @Override
                public void run() {
                    showToast("Starting main touch server");
                    new MainStarter(ServerService.this).start();
                    showToast("started main touch server");
                }
            }).start();*/

            mHandler = new Handler();
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateNotification(String message) {
        Intent intent = new Intent(this, ServerService.class);
        intent.setAction("STOP");
        PendingIntent stopServiceIntent = PendingIntent.getService(this, 0, intent, 0);
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_media_stop, "Stop", stopServiceIntent)
                        .setContentTitle(message)
                        .setContentText(Utils.getIPAddress(true) + ":" + serverPort);
        startForeground(6000, mBuilder.build());
    }

    private void showToast(final String message) {
        mHandler.post(new ToastRunnable(message));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        dispose();
    }


    private void dispose() {
        stopForeground(true);
        stopSelf();
        System.out.println("DONE DISPOSING SERVICE");
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //startServerTask.closeAll();
    }

    public static int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
