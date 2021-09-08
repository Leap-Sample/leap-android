package is.leap.android.sample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Spanned;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import is.leap.android.sample.ui.HomeActivity;

public class LeapFirebaseService extends FirebaseMessagingService {

    public static final int NOTIFICATION_ID = 123256;

    public static final String LEAP_SAMPLE_NOTIFICATION_CHANNEL = "leap_sample_notification";
    public static final String LEAP_SAMPLE = "LeapSample";
    private static final String NOTIFICATION_BG_COLOR = "#0A0B12";
    public static final String NOTIFICATION_ACTION_TEXT_COLOR = "#5B6CFF";

    public static final String FONT_COLOR = "<font color=\"";
    public static final String B = "\"><b>";
    public static final String B_FONT = "</b></font>";
    public static final int REQUEST_CODE = 10101;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        RemoteMessage.Notification remoteNotification = remoteMessage.getNotification();
        if (remoteNotification != null) {

            //1. Receive the data from Firebase
            Map<String, String> data = remoteMessage.getData();
            String projectId = data.get("project_id");

            //2. Create an intent (with project_id data as extra) and specify the destination
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("project_id", projectId);

            //3. Since we are launching a notification, we need to create a PendingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //4. Let's create a notification with the above created PendingIntent
            Notification notification = getNotification(this,
                    remoteNotification.getTitle(),
                    remoteNotification.getBody(),
                    "Launch",
                    pendingIntent);

            //5. Let's launch the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        Log.d("LeapFirebaseService", remoteMessage.toString());
    }

    public static Notification getNotification(Context context, String contentTitle,
                                               String contentText, String action,
                                               PendingIntent pendingIntent) {
        NotificationCompat.Builder leapNotifyBuilder = getNotificationBuilder(context,
                contentTitle,
                contentText,
                action,
                pendingIntent);

        return leapNotifyBuilder.build();
    }

    @NonNull
    private static NotificationCompat.Builder getNotificationBuilder(Context context, String contentTitle, String contentText, String action, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder leapNotifyBuilder = new NotificationCompat.Builder(context, LEAP_SAMPLE_NOTIFICATION_CHANNEL);

        leapNotifyBuilder.setContentTitle(contentTitle);
        leapNotifyBuilder.setContentText(contentText);
        leapNotifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        leapNotifyBuilder.setSmallIcon(is.leap.android.creator.R.drawable.ic_leap_logo);
        leapNotifyBuilder.setColor(Color.parseColor(NOTIFICATION_BG_COLOR));
        leapNotifyBuilder.setColorized(true);

        String notificationActionText = getNotificationActionText(action);
        Spanned actionText = HtmlCompat.fromHtml(notificationActionText, HtmlCompat.FROM_HTML_MODE_LEGACY);

        leapNotifyBuilder.addAction(0, actionText, pendingIntent);
        leapNotifyBuilder.setPriority(Notification.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = LEAP_SAMPLE_NOTIFICATION_CHANNEL;
            NotificationChannel channel = new NotificationChannel(channelId, LEAP_SAMPLE, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            leapNotifyBuilder.setChannelId(channelId);
        }
        return leapNotifyBuilder;
    }

    private static String getNotificationActionText(String action) {
        return FONT_COLOR + NOTIFICATION_ACTION_TEXT_COLOR + B + action + B_FONT;
    }
}
