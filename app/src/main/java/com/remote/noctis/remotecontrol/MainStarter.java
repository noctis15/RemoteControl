package com.remote.noctis.remotecontrol;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Noctis on 2015-11-13.
 */
public class MainStarter {

    private static final String TAG = "MainStarter";

    private static final String COMMAND = "su -c \"CLASSPATH=%s /system/bin/app_process32 " +
            "/system/bin com.remote.noctis.remotecontrol.Main\"\n";

    private Context context;

    public MainStarter(Context context) {
        this.context = context;
    }

    public void start() {
//        Shell.SU.run(String.format(COMMAND, getApkLocation()));
        try {
            Log.d(TAG, "===EXECUTING=== " + String.format(COMMAND, getApkLocation()));
            Process process = Runtime.getRuntime().exec("sh");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes(String.format(COMMAND, getApkLocation()));
            outputStream.flush();
            Log.d(TAG, "Starting Waiting for process");
            process.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getApkLocation() {
        PackageManager pm = context.getPackageManager();

        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
//            Log.d("PackageList", "package: " + app.packageName + ", sourceDir: " + app.sourceDir);
            if (app.packageName.equals(context.getPackageName())) {
                return app.sourceDir;
            }
        }
        return null;
    }
}
