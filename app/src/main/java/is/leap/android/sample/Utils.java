package is.leap.android.sample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import is.leap.android.sample.listeners.ValidationListener;
import is.leap.android.sample.ui.HomeActivity;
import is.leap.android.sample.ui.RegisterActivity;

import static is.leap.android.sample.Constants.DISABLE;
import static is.leap.android.sample.Constants.LEAP;
import static is.leap.android.sample.Constants.NOTIFICATION_ID;
import static is.leap.android.sample.Constants.OWNER;

public class Utils {

    public static boolean isLeapValidatedApp(SparseArray<Barcode> barcodeSparseArray, ValidationListener validationListener) throws JSONException {
        if( barcodeSparseArray == null || barcodeSparseArray.size() == 0) return false;
        for( int _i = 0; _i < barcodeSparseArray.size(); _i++){
            if ( isOwnerLeap(barcodeSparseArray.valueAt(_i), validationListener))
                return true;
        }
        if (validationListener != null) validationListener.onFailedValidation();
        return false;
    }

    private static boolean isOwnerLeap(Barcode barcodeAtValue, ValidationListener validationListener) throws JSONException {
        if (barcodeAtValue == null || barcodeAtValue.displayValue == null || barcodeAtValue.displayValue.isEmpty() ) return false;
        JSONObject configuration = new JSONObject(barcodeAtValue.displayValue);
        if (configuration == null || configuration.length() == 0) return false;
        boolean isOwnerLeap = LEAP.equals(configuration.optString(OWNER));
        if( !isOwnerLeap ){
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return getWindowHeightAPI23Plus(activity);
        return getWindowHeightLollipop(activity);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static int getWindowHeightAPI23Plus(@NonNull Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        return decorView.getHeight();
    }

    private static int getWindowHeightLollipop(@NonNull Activity activity) {
        // getDisplaySizeY - works correctly expect for landscape due to a bug.
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return getWindowVisibleDisplayFrame(activity).height();
        //  getWindowVisibleDisplayFrame - Doesn't work for portrait as it subtracts the keyboard height.
        return getDisplaySizeY(activity);
    }

    private static @NonNull
    Rect getWindowVisibleDisplayFrame(@NonNull Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect;
    }

    private static int getDisplaySizeY(@NonNull Activity activity) {
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(point);
        return point.y;
    }

    public static void showNotification(Context context, String applicationName, boolean hideNotification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if( hideNotification ){
            notificationManager.cancel(NOTIFICATION_ID);
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, getNotification(context, applicationName));
    }

    public static Notification getNotification(Context context, String applicationName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder leapNotifyBuilder = new NotificationCompat.Builder(context.getApplicationContext(), "leap_notification");


        RemoteViews contentView = getCustomLayout(context, applicationName);

        leapNotifyBuilder.setSmallIcon(R.drawable.ic_combined_shape_copy_7);
        leapNotifyBuilder.setContent(contentView);
        leapNotifyBuilder.setAutoCancel(true); //dismissed when tapped automatically
        leapNotifyBuilder.setOngoing(false);
        leapNotifyBuilder.setPriority(Notification.PRIORITY_HIGH);
        leapNotifyBuilder.setOnlyAlertOnce(true);
        leapNotifyBuilder.build().flags = Notification.FLAG_NO_CLEAR | Notification.PRIORITY_HIGH | Notification.FLAG_AUTO_CANCEL;;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "leap_notification";
            NotificationChannel channel = new NotificationChannel(channelId, "Leap", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            leapNotifyBuilder.setChannelId(channelId);
        }

        return leapNotifyBuilder.build();
    }

    private static RemoteViews getCustomLayout(Context context, String applicationName) {
        Intent switchIntent = new Intent(context, RegisterActivity.class);
        switchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        switchIntent.putExtra(DISABLE, true);
        PendingIntent pendingSwitchIntent = PendingIntent.getActivity(context, 0, switchIntent, 0);

        Intent homeIntent = new Intent(context, HomeActivity.class);
        switchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        switchIntent.putExtra(DISABLE, true);
        PendingIntent pendingHomeIntent = PendingIntent.getActivity(context, 0, homeIntent, 0);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        contentView.setTextViewText(R.id.appNameTitle, applicationName);
        contentView.setImageViewResource(R.id.logo, R.drawable.ic_combined_shape_copy_7);
        contentView.setImageViewResource(R.id.logoText, R.drawable.ic_leap_text);
        contentView.setImageViewResource(R.id.connected, R.drawable.ic_connected_text);
        contentView.setImageViewResource(R.id.spacingView, -1);
        contentView.setImageViewResource(R.id.rescanBtn, R.drawable.ic_rescan);
        contentView.setOnClickPendingIntent(R.id.notification_layout, pendingHomeIntent);
        contentView.setOnClickPendingIntent(R.id.rescanBtn, pendingSwitchIntent);
        return contentView;
    }

    public static void hideNotification(Context context, boolean disappearNotification) {
        showNotification(context, null, disappearNotification);

    }
}
