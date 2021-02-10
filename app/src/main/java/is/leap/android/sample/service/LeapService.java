package is.leap.android.sample.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationBuilderWithBuilderAccessor;
import androidx.core.app.NotificationCompat;

import is.leap.android.sample.R;
import is.leap.android.sample.Utils;
import is.leap.android.sample.listeners.NotificationListener;
import is.leap.android.sample.ui.RegisterActivity;

public class LeapService extends Service {

    private final LeapBinder leapBinder = new LeapBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return leapBinder;
    }

    public class LeapBinder extends Binder{
        public LeapService getService(){
            return LeapService.this;
        }
    }

    @Override
    public boolean stopService(Intent name) {
        stopSelf();
        return super.stopService(name);
    }
}
