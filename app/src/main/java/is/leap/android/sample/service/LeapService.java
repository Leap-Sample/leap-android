package is.leap.android.sample.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import is.leap.android.sample.R;
import is.leap.android.sample.Utils;
import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.ui.RegisterActivity;

import static is.leap.android.sample.data.LeapSampleSharedPref.APP_NAME;

public class LeapService extends Service {

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, Utils.getNotification(this, LeapSampleSharedPref.getInstance().getRegisteredApp()));
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, Utils.getNotification(this, LeapSampleSharedPref.getInstance().getRegisteredApp()));
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        this.stopSelf();
        this.onDestroy();
    }
}
