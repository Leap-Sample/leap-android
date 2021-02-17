package is.leap.android.sample.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import is.leap.android.sample.Utils;
import is.leap.android.sample.data.LeapSampleSharedPref;

public class LeapService extends Service {

    private static final int NOTIFICATION_ID = 1;

    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (START_FOREGROUND_SERVICE.equals(action)) {
                Notification notification = Utils.getNotification(this.getApplicationContext(), LeapSampleSharedPref.getInstance().getRegisteredApp());
                startForeground(NOTIFICATION_ID, notification);
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopForegroundService();
    }

    private void stopForegroundService() {
        stopForeground(true);
        stopSelf();
    }
}
