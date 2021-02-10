package is.leap.android.sample.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.webkit.WebView;

import is.leap.android.aui.LeapAUI;
import is.leap.android.sample.R;
import is.leap.android.sample.Utils;
import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.service.LeapService;
import is.leap.android.snap.LeapSnapSDK;

public class HomeActivity extends AppCompatActivity {

    String webUrl, apiKey;
    LeapSampleSharedPref sharedPref;

    private LeapService leapService;
    private boolean toShowNotification = false;
    private ServiceConnection leapServiceConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPref = LeapSampleSharedPref.getInstance();
        webUrl = sharedPref.getWebUrl();
        apiKey = sharedPref.getAppApiKey();
        LeapAUI.init(this, apiKey);
        LeapSnapSDK.init(this, apiKey);
        WebView appWebView = findViewById(R.id.webView);
        LeapAUI.addWebInterface(appWebView);
        appWebView.loadUrl(webUrl);

        leapServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LeapService.LeapBinder leapBinder = (LeapService.LeapBinder) service;
                leapService = leapBinder.getService();
                toShowNotification = true;
                Utils.showNotification(HomeActivity.this, LeapSampleSharedPref.getInstance().getRegisteredApp(), toShowNotification);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                toShowNotification = false;
            }
        };

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        leapService.stopSelf();
        Utils.showNotification(HomeActivity.this,toShowNotification);
        finish();
    }

    private void triggerService() {
        Intent intent = new Intent(this, LeapService.class);
        bindService(intent, leapServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        triggerService();
    }
}