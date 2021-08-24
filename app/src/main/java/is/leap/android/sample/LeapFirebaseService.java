package is.leap.android.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import is.leap.android.sample.util.NotificationUtils;

public class LeapFirebaseService extends FirebaseMessagingService {

    public static final int NOTIFICATION_ID = 123256;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        Notification notification = NotificationUtils.getNotification(this, "Hello there",
                "Let's launch the project", NotificationUtils.NOTIFICATION_BUTTON, remoteMessage.getData());

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID ,notification);

        Log.d("LeapFirebaseService", remoteMessage.toString());
    }
}
