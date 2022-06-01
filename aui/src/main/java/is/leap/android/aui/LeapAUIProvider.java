package is.leap.android.aui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

public class LeapAUIProvider extends ContentProvider {

    private static final String LEAP_AUTHORITY = ".LeapAUIProvider";

    //AttachInfo triggers onCreate() therefore put a check if authority is not Leap then dont initialise
    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        //No need to initialise when authority is not leap
        if (!isLeapAuthority(info)) return;
        super.attachInfo(context, info);
    }

    private boolean isLeapAuthority(ProviderInfo info) {
        return info != null
                && info.authority.contains(LEAP_AUTHORITY)
                && info.name.contains(LEAP_AUTHORITY);
    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean onCreate() {
        //get application context here and initialise SDK
        Context applicationContext = getContext();
        if (applicationContext == null) return false;
        Application applicationInstance = (Application) applicationContext;
        Leap.init(applicationInstance);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
