package com.remote.noctis.remotecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Noctis on 2015-11-13.
 */
public class ServerService extends Service {
    private static final String TAG = "noctis";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
