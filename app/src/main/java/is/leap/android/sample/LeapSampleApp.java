package is.leap.android.sample;

import android.app.Application;
import android.content.IntentFilter;

import is.leap.android.sample.data.LeapSampleCache;
import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.receiver.SampleAppReceiver;

public class LeapSampleApp extends Application {

//    public static String API_KEY_CLIENT = "1ea23aaa-7e8a-4e1e-bb0a-89a02fce28e9";
    public static String SAMPLE_APP_WEB_URL = "https://jiny.io/leap_sample_app/index";

    @Override
    public void onCreate() {
        super.onCreate();
        LeapSampleCache.init(this, API_KEY_CLIENT, SAMPLE_APP_WEB_URL);
        initialiseReceiver(this);
    }

    private void initialiseReceiver(Application application) {
        SampleAppReceiver receiver = new SampleAppReceiver();
        IntentFilter filter = new IntentFilter("is.leap.android.sample.receiver.SampleAppReceiver");
        application.registerReceiver(receiver, filter);
    }
}
