package com.example.android.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SunshineSyncService extends Service {
    private static final String LOG_TAG = "SunshineSyncService";
    private static final Object syncAdapterLock = new Object();
    private static SunshineSyncAdapter sunshineSyncAdapter;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate - SunshineSyncService");
        synchronized (syncAdapterLock) {
            if (sunshineSyncAdapter == null) {
                sunshineSyncAdapter = new SunshineSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sunshineSyncAdapter.getSyncAdapterBinder();
    }
}
