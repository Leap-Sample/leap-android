package is.leap.android.sample.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class LeapSampleSharedPref {

    private static final String LEAP_SAMPLE_CACHE = "leap_sample_shared_pref";

    private static LeapSampleSharedPref INSTANCE;
    private final SharedPreferences sharedPreferences;

    public static final String APP_NAME = "appName";
    public static final String USER_NAME = "userName";
    public static final String APP_API_KEY = "apiKey";
    public static final String PLATFORM_TYPE = "platformType";
    public static final String WEB_URL = "webUrl";

    public static final String NOT_AVAILABLE = "not_available";
    private final static String HTTP = "http://";
    private final static String HTTPS = "https://";

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

    private String readString(String key, String defaultValue) {
        try {
            return sharedPreferences.getString(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }

    public String getRegisteredApp(){
        return readString(APP_NAME, NOT_AVAILABLE);
    }

    public String getUserName() {
        return readString(USER_NAME, NOT_AVAILABLE);
    }

    public String getAppApiKey(){
        return readString(APP_API_KEY, NOT_AVAILABLE);
    }

    public String getWebUrl(){
        return readString(WEB_URL, NOT_AVAILABLE);
    }

    public void saveLeapQRConfiguration(JSONObject qrCodeJSON) throws JSONException {
        if( qrCodeJSON ==null || qrCodeJSON.length() == 0 ) return;
        String appName = qrCodeJSON.getString(APP_NAME);
        String apiKey = qrCodeJSON.getString(APP_API_KEY);
        String platform = qrCodeJSON.getString(PLATFORM_TYPE);
        String webUrl = qrCodeJSON.getString(WEB_URL);

        if (webUrl == null || webUrl.isEmpty()){
            throw new JSONException("Web Url cant be null or empty");
        }
        if (!(webUrl.startsWith(HTTP) || webUrl.startsWith(HTTPS))){
            throw new JSONException(" Invalid web url ");
        }

        save(APP_NAME, appName);
        save(APP_API_KEY, apiKey);
        save(PLATFORM_TYPE, platform);
        save(WEB_URL, webUrl);
    }
}