package is.leap.android.sample;

import android.app.Application;

import is.leap.android.sample.data.LeapSampleSharedPref;

public class LeapSampleApp  extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeapSampleSharedPref.init(this);
    }
}
