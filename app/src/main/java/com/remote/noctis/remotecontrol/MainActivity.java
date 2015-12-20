package com.remote.noctis.remotecontrol;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends Activity {

    private Button startClient;
    private Button startServer;
    private TextView ip_address;
    TextView requirement_hint;
    TextView instruction_hint;
    TextView keys_hint;
    private boolean started = true;
    String errMessage = "";


    SharedPreferences prefs;
    private static final String KEY_SYSTEM_PRIVILEGE_PREF = "has_system_privilege";
    boolean hasSystemPrivileges = false;

    private static final String INSTALL_SCRIPT =
            "mount -o rw,remount /system\n" +
                    "cat %s > /system/priv-app/RemoteControl.apk.tmp\n" +
                    "chmod 644 /system/priv-app/RemoteControl.apk.tmp\n" +
                    "pm uninstall %s\n" +
                    "mv /system/priv-app/RemoteControl.apk.tmp /system/priv-app/RemoteControl.apk\n" +
                    "pm install -r /system/priv-app/RemoteControl.apk\n" +
                    "sleep 5\n" +
                    "am start -n com.remote.noctis.remotecontrol.app/.MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        startClient = (Button) findViewById(R.id.button1);   //reference to start Client
        startServer = (Button) findViewById(R.id.button2);  //reference to start server
        ip_address = (TextView) findViewById(R.id.ip_address); //reference to ip address textview
        requirement_hint = (TextView) findViewById(R.id.requirement);
        instruction_hint = (TextView) findViewById(R.id.instruction);
        keys_hint = (TextView) findViewById(R.id.keys_hint);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        params.setMargins(width / 10, height / 9, width / 10, 0);
        requirement_hint.setLayoutParams(params);
        instruction_hint.setLayoutParams(params);
        keys_hint.setLayoutParams(params);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        hasSystemPrivileges = prefs.getBoolean(KEY_SYSTEM_PRIVILEGE_PREF, false);

        if (Utils.getIPAddress(true).equals("")) {
            ip_address.setText("Device is not connected to a Hot-Spot");
        } else {
            ip_address.setText("IP address of this device: " + Utils.getIPAddress(true));
        }

        if (Shell.SU.available() && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            requirement_hint.setText("Hint: Your device is rooted and can act as server");
        }

        if (getIntent() != null && getIntent().getAction() == "ERR") {

            errMessage = getIntent().getStringExtra(AddressInputDialog.ERR_MSG);

            if (!errMessage.equals(null) && errMessage != "") {
                showToast(errMessage);
            }

            errMessage = getIntent().getStringExtra(PortInputDialog.ERR_MSG);
            if (!errMessage.equals(null) && errMessage != "") {
                showToast(errMessage);
            }
        }


        if (savedInstanceState == null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    final boolean isRooted = Shell.SU.available();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isRooted) {
                                Toast.makeText(MainActivity.this, "Device is rooted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Device us unrooted! You won't be able to use" +
                                        "this device as a server", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    return null;
                }
            }.execute();
        }


        //Button press event listener
        startClient.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) { //client button

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        if (Utils.getIPAddress(true).equals("")) {
                            showToast("Connect to a Hot-Spot first");
                        } else {
                            new AddressInputDialog().show(getFragmentManager(), "Address Dialog");
                        }
                    }
                }).start();


            }
        });

        startServer.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) { //server button


                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && Shell.SU.available() && !Utils.getIPAddress(true).equals("")) {
                    new PortInputDialog().show(getFragmentManager(), "Address Dialog");
                    //new StartServerServiceDialog().show(getFragmentManager(), "Start service");
                } else {
                    if (Utils.getIPAddress(true).equals("")) {
                        showToast("Connect to a Hot-Spot first");
                    } else {
                        // new PortInputDialog().show(getFragmentManager(), "Address Dialog");
                        showToast("Your device doesn't meet requirements to start the server");
                    }
                }
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (Shell.SU.available() && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            requirement_hint.setText("Hint: Your device is rooted and can act as server");
        }

        if (Utils.getIPAddress(true).equals("")) {
            ip_address.setText("Device is not connected to a Hot-Spot");
        } else {
            ip_address.setText("IP address of this device: " + Utils.getIPAddress(true));
        }
        return super.onTouchEvent(event);
    }


    @SuppressLint("ValidFragment")
    private class StartServerServiceDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Notice");
            builder.setMessage("For using the server mode, the device MUST be rooted and the app MUST be installed " +
                    "to \\system partition");
            builder.setPositiveButton("Start Server", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent startServerIntent = new Intent(MainActivity.this, ServerService.class);
                    startServerIntent.setAction("START");
                    startService(startServerIntent);
                }
            });
            builder.setNegativeButton("Install to /system", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new InstallDialog().show(getFragmentManager(), "INSTALL_DIALOG");
                }
            });
            return builder.create();
        }
    }

    @SuppressLint("ValidFragment")
    private class InstallDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Install the script");
            builder.setMessage("It's necessary to install this app in the /system partition. Proceed?");
            builder.setPositiveButton("Install", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,
                                            "Installing", Toast.LENGTH_SHORT).show();
                                }
                            });
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(KEY_SYSTEM_PRIVILEGE_PREF, true);
                            editor.commit();
                            Shell.SU.run(String.format(INSTALL_SCRIPT,
                                    new String[]{
                                            MainActivity.this.getPackageCodePath(),
                                            MainActivity.this.getPackageName()
                                    }));
                            return null;
                        }
                    }.execute();
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this,
                                    "This app won't run unless it is installed in the system partition",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            return builder.create();
        }
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_install) {
            InstallDialog installDialog = new InstallDialog();
            installDialog.show(getFragmentManager(), "INSTALL_DIALOG");
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {


            showToast("RemoteControl application closing");

            this.finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


}
