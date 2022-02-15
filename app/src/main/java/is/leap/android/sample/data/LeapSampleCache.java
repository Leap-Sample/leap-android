package is.leap.android.sample.data;

import static is.leap.android.sample.data.LeapSampleSharedPref.APP_API_KEY;
import static is.leap.android.sample.data.LeapSampleSharedPref.APP_WEB_URL;

import android.app.Application;

public class LeapSampleCache {

    public static String WEB_URL;
    public static String API_KEY;

    public static LeapSampleSharedPref leapSampleSharedPref;

    public static void init(Application application, String apiKey, String webUrl) {
        LeapSampleSharedPref.init(application);
        leapSampleSharedPref = LeapSampleSharedPref.getInstance();
        API_KEY = leapSampleSharedPref.getString(APP_API_KEY, apiKey);
        WEB_URL = leapSampleSharedPref.getString(APP_WEB_URL, webUrl);
    }

    private LeapSampleCache() {
    }

    public static void setApiKey(String apiKey) {
        API_KEY = apiKey;
        leapSampleSharedPref.save(APP_API_KEY, apiKey);
    }

    public static void setWebUrl(String webUrl) {
        WEB_URL = webUrl;
        leapSampleSharedPref.save(APP_WEB_URL, webUrl);
    }
}
