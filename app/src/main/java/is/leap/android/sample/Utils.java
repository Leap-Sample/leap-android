package is.leap.android.sample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import is.leap.android.sample.listeners.ValidationListener;

import static is.leap.android.sample.Constants.LEAP;
import static is.leap.android.sample.Constants.OWNER;

public class Utils {

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

}
