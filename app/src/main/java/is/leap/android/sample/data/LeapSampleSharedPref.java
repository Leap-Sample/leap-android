package is.leap.android.sample.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Set;

public class LeapSampleSharedPref {

    private static final String LEAP_SAMPLE_CACHE = "leap_sample_shared_pref";

    private static LeapSampleSharedPref INSTANCE;
    private final SharedPreferences sharedPreferences;

    public static final String APP_API_KEY = "apiKey";
    public static final String WEB_URL = "webUrl";

    private LeapSampleSharedPref(Context mContext) {
        sharedPreferences = mContext.getSharedPreferences(LEAP_SAMPLE_CACHE, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context mContext) {
        if (INSTANCE == null) INSTANCE = new LeapSampleSharedPref(mContext);
    }

    public static synchronized LeapSampleSharedPref getInstance() {
        if (INSTANCE == null) throw new RuntimeException("Make sure to call init at-least once.");
        return INSTANCE;
    }

    private void save(String key, JSONObject object) {
        save(key, object.toString());
    }

    private void save(String key, int value) {
        getEditor().putInt(key, value).apply();
    }

    private void save(String key, boolean value) {
        getEditor().putBoolean(key, value).apply();
    }

    public void save(String key, long value) {
        getEditor().putLong(key, value).apply();
    }

    public void save(String key, float value) {
        getEditor().putFloat(key, value).apply();
    }

    public void save(String key, Set<String> values) {
        getEditor().putStringSet(key, values).apply();
    }

    public void save(String key, String value) {
        getEditor().putString(key, value).apply();
    }

    // Remove & Clear methods
    public void remove(String key) {
        getEditor().remove(key).apply();
    }

    public void clear() {
        getEditor().clear().apply();
    }

    String getString(String key, String defaultValue) {
        try {
            return sharedPreferences.getString(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }

}