package is.leap.android.sample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.text.Spanned;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;

import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import is.leap.android.sample.listeners.ValidationListener;
import is.leap.android.sample.ui.HomeActivity;
import is.leap.android.sample.ui.RegisterActivity;

import static is.leap.android.sample.Constants.LEAP;
import static is.leap.android.sample.Constants.NOTIFICATION_ID;
import static is.leap.android.sample.Constants.OWNER;

public class Utils {

    public static final String NOTIFICATION_BG_COLOR = "#0A0B12";
    public static final String CONNECTED = "Connected";
    public static final String TICKER_TEXT_LEAP = "LeapSample";


    public static boolean isNotReleaseBuild(Context context) {
        return (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public static boolean isLeapValidatedApp(SparseArray<Barcode> barcodeSparseArray, ValidationListener validationListener) throws JSONException {
        if (barcodeSparseArray == null || barcodeSparseArray.size() == 0) return false;
        for (int _i = 0; _i < barcodeSparseArray.size(); _i++) {
            if (isOwnerLeap(barcodeSparseArray.valueAt(_i), validationListener))
                return true;
        }
        if (validationListener != null) validationListener.onFailedValidation();
        return false;
    }

    private static boolean isOwnerLeap(Barcode barcodeAtValue, ValidationListener validationListener) throws JSONException {
        if (barcodeAtValue == null || barcodeAtValue.displayValue == null || barcodeAtValue.displayValue.isEmpty())
            return false;
        JSONObject configuration = new JSONObject(barcodeAtValue.displayValue);
        if (configuration == null || configuration.length() == 0) return false;
        boolean isOwnerLeap = LEAP.equals(configuration.optString(OWNER));
        if (!isOwnerLeap) {
            return false;
        }
        if (validationListener != null) validationListener.onSuccessfulValidation(configuration);
        return true;
    }

    public static int getScreenWidth(Activity currentActivity) {
        if (currentActivity == null) return 0;
        Rect screenBoundWithoutNav = getScreenBoundWithoutNav(currentActivity);
        return screenBoundWithoutNav.width();
    }

    public static Rect getScreenBoundWithoutNav(Activity activity) {
        Rect rect = new Rect();
        if (activity == null) return rect;
        activity.getWindowManager().getDefaultDisplay().getRectSize(rect);
        return rect;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static int getScreenHeight(Activity activity) {
        if (activity == null) return 0;
        return getDisplaySizeY(activity);
    }

    private static int getDisplaySizeY(@NonNull Activity activity) {
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(point);
        return point.y;
    }

    public static void showNotification(Context context, String applicationName, boolean hideNotification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (hideNotification) {
            notificationManager.cancel(NOTIFICATION_ID);
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, getNotification(context, applicationName));
    }

    public static Notification getNotification(Context context, String applicationName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder leapNotifyBuilder = new NotificationCompat.Builder(context.getApplicationContext(), "leap_notification");


        PendingIntent rescanPendingIntent = getRescanPendingIntent(context);

        leapNotifyBuilder.setSmallIcon(R.drawable.ic_leap_logo);
        leapNotifyBuilder.setContentTitle(applicationName);
        leapNotifyBuilder.setContentText(CONNECTED);
        leapNotifyBuilder.setTicker(TICKER_TEXT_LEAP);
        leapNotifyBuilder.setColor(Color.parseColor(NOTIFICATION_BG_COLOR));
        leapNotifyBuilder.setColorized(true);
        Spanned actionText = HtmlCompat.fromHtml("<font color=\"#5B6CFF\">" + "<b>Rescan</b>" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY);
        leapNotifyBuilder.addAction(new NotificationCompat.Action.Builder(
                0, // Don't show icon
                actionText,
                rescanPendingIntent).build());

        leapNotifyBuilder.setAutoCancel(false); //dismissed when tapped automatically
        leapNotifyBuilder.setOngoing(false);
        leapNotifyBuilder.setPriority(Notification.PRIORITY_HIGH);
        leapNotifyBuilder.setOnlyAlertOnce(true);

        Intent homeIntent = new Intent(context, HomeActivity.class);
        PendingIntent pendingHomeIntent = PendingIntent.getActivity(context, 10101,
                homeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        leapNotifyBuilder.setContentIntent(pendingHomeIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "leap_notification";
            NotificationChannel channel = new NotificationChannel(channelId, "Leap", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            leapNotifyBuilder.setChannelId(channelId);
        }
        return leapNotifyBuilder.build();
    }

    private static PendingIntent getRescanPendingIntent(Context context) {
        Intent switchIntent = new Intent(context, RegisterActivity.class);
        switchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 10100, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void hideNotification(Context context, boolean disappearNotification) {
        showNotification(context, null, disappearNotification);
    }
}
