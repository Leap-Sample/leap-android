package is.leap.android.sample.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Spanned;

import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;

import java.util.Map;

import is.leap.android.creator.R;
import is.leap.android.sample.ui.HomeActivity;

public class NotificationUtils {

    public static final String LEAP_SAMPLE_NOTIFICATION_CHANNEL = "leap_sample_notification";
    public static final String LEAP_SAMPLE = "LeapSample";
    private static final String NOTIFICATION_BG_COLOR = "#0A0B12";
    public static final String NOTIFICATION_ACTION_TEXT_COLOR = "#5B6CFF";
    public static final String NOTIFICATION_BUTTON = "Launch";
    public static final String PROJECT_ID = "project_id";

    public static final String FONT_COLOR = "<font color=\"";
    public static final String B = "\"><b>";
    public static final String B_FONT = "</b></font>";

    public static Notification getNotification(Context context, String contentTitle,
                                               String contentText, String action, Map<String, String> data) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder leapNotifyBuilder = new NotificationCompat.Builder(context, LEAP_SAMPLE_NOTIFICATION_CHANNEL);

        leapNotifyBuilder.setContentTitle(contentTitle);
        leapNotifyBuilder.setContentText(contentText);
        leapNotifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        leapNotifyBuilder.setSmallIcon(R.drawable.ic_leap_logo);
        leapNotifyBuilder.setColor(Color.parseColor(NOTIFICATION_BG_COLOR));
        leapNotifyBuilder.setColorized(true);

        String notificationActionText = getNotificationActionText(action);
        Spanned actionText = HtmlCompat.fromHtml(notificationActionText, HtmlCompat.FROM_HTML_MODE_LEGACY);

        leapNotifyBuilder.addAction(0, actionText, getPendingIntent(context, action, data));

        leapNotifyBuilder.setAutoCancel(false); //dismissed when tapped automatically
        leapNotifyBuilder.setOngoing(true);
        leapNotifyBuilder.setPriority(Notification.PRIORITY_HIGH);
        leapNotifyBuilder.setOnlyAlertOnce(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = LEAP_SAMPLE_NOTIFICATION_CHANNEL;
            NotificationChannel channel = new NotificationChannel(channelId, LEAP_SAMPLE, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            leapNotifyBuilder.setChannelId(channelId);
        }
        return leapNotifyBuilder.build();
    }

    private static String getNotificationActionText(String action) {
        return FONT_COLOR + NOTIFICATION_ACTION_TEXT_COLOR + B + action + B_FONT;
    }

    private static PendingIntent getPendingIntent(Context context, String action, Map<String, String> data) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(PROJECT_ID, data.get(PROJECT_ID));
        if (NOTIFICATION_BUTTON.equals(action)) {
            return PendingIntent.getActivity(context, 10101,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return null;
    }

}
