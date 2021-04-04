package is.leap.android.sample;

import android.app.Application;
import android.content.IntentFilter;

import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.receiver.SampleAppReceiver;

public class LeapSampleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeapSampleSharedPref.init(this);
        initialiseReceiver(this);
    }

    private void initialiseReceiver(Application application) {
        SampleAppReceiver receiver = new SampleAppReceiver();
        IntentFilter filter = new IntentFilter("is.leap.android.sample.receiver.SampleAppReceiver");
        application.registerReceiver(receiver, filter);
    }
}
