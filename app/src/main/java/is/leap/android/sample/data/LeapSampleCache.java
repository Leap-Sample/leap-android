package is.leap.android.sample.data;

import static is.leap.android.sample.data.LeapSampleSharedPref.APP_API_KEY;

import android.app.Application;

public class LeapSampleCache {

    public static String WEB_URL;
    public static String API_KEY;

    public static LeapSampleSharedPref leapSampleSharedPref;

    public static void init(Application application) {
        LeapSampleSharedPref.init(application);
        leapSampleSharedPref = LeapSampleSharedPref.getInstance();
        API_KEY = leapSampleSharedPref.getString(APP_API_KEY, null);
        WEB_URL = leapSampleSharedPref.getString(WEB_URL, null);
    }

    private LeapSampleCache() {
    }

    public static void setApiKey(String apiKey) {
        API_KEY = apiKey;
        leapSampleSharedPref.save(APP_API_KEY, apiKey);
    }

    public static void setWebUrl(String webUrl) {
        WEB_URL = webUrl;
        leapSampleSharedPref.save(WEB_URL, webUrl);
    }
}
