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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends Activity  {
    //client side
    private Socket client;
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private OutputStream outputStream;
    private Button startClient;
    private Button startServer;
    private TextView text;
    private EditText editText0;

    //server side
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;
    private static String message;

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
        text = (TextView) findViewById(R.id.textView1);   //reference to the text view
        editText0 = (EditText) findViewById(R.id.editText0);  //reference to message


        //Button press event listener
        startClient.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        new AddressInputDialog().show(getFragmentManager(), "Address Dialog");



                       /* if (editText0.getText().toString().isEmpty()) {
                            showToast("Proszę wpisać wiadomość");
                        } else {
                            try {


                                if (editText1.getText().toString().isEmpty() || editText2.getText().toString().isEmpty()) {
                                    showToast("Proszę wpisać adres IP i/lub Port");
                                } else {

                                    Socket client = new Socket("192.168.0.5", "8187");
                                    DataOutputStream DOS = new DataOutputStream(client.getOutputStream());
                                    DOS.writeUTF(editText0.getText().toString());
                                    client.close();
                                }
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }*/


                    }
                }).start();


            }
        });

        startServer.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {


                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    new StartServerServiceDialog().show(getFragmentManager(), "Start service");
                } else {
                    //startScreenCapture();
                }
                /*if (editText2.getText().toString().isEmpty()) {
                    showToast("prosze wpisac numer Portu");
                } else {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
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

                        }
                    };

                    thread.start();

                }*/
            }
        });

    }

    /*@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScreenCapture() {
        startActivityForResult(
                mMediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }*/

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
                    //finish();
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
                        protected Void doInBackground(Void... voids){
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
                                    new String[] {
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

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
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
