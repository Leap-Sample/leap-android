package is.leap.android.aui;

import android.util.Log;

import is.leap.android.core.data.LeapCoreCache;

public class LeapAUILogger {
    public final static String TAG = "LEAP:AUI:";
    private final static String LEAP_INIT_TAG = TAG + "INIT:";
    private final static String LEAP_DOWNLOAD_TAG = TAG + "DOWNLOAD:";
    private final static String LEAP_SOUND_TAG = TAG + "SOUND:";

    public static void errorAUI(String message) {
        e(TAG, message, null);
    }

    public static void errorAUI(String message, Exception e) {
        e(TAG, message, e);
    }

    public static void errorDownload(String message) {
        e(LEAP_DOWNLOAD_TAG, message, null);
    }

    public static void errorSound(String message) {
        e(LEAP_SOUND_TAG, message, null);
    }

    private static void e(String tag, String message, Exception e) {
        if (!LeapCoreCache.isLoggingEnabled) return;
        if (e != null) {
            e.printStackTrace();
        }
        Log.e(tag, message);
    }

    public static void debugSound(String message) {
        d(LEAP_SOUND_TAG, message);
    }

    public static void debugDownload(String message) {
        d(LEAP_DOWNLOAD_TAG, message);
    }

    public static void debugAUI(String message) {
        d(TAG, message);
    }

    private static void d(String tag, String message) {
        if (!LeapCoreCache.isLoggingEnabled) return;
        Log.d(tag, message);
    }

    public static void debugInit(String message) {
        Log.d(LEAP_INIT_TAG, message);
    }
}
