package is.leap.android.sample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.ui.HomeActivity;

public class SampleAppReceiver extends BroadcastReceiver {

    private static final String API_KEY = "API_KEY";
    private static final String WEB_URL = "WEB_URL";

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        if (extras == null) return;
        String apiKey = extras.getString(API_KEY);
        String webUrl = extras.getString(WEB_URL);

        LeapSampleSharedPref.getInstance().setApiKey(apiKey);
        LeapSampleSharedPref.getInstance().setWebUrl(webUrl);

        Intent transitionToHome = new Intent(context, HomeActivity.class);
        transitionToHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(transitionToHome);
    }
}
