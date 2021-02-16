package is.leap.android.sample.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import is.leap.android.aui.LeapAUI;
import is.leap.android.sample.R;
import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.service.LeapService;
import is.leap.android.snap.LeapSnapSDK;

public class HomeActivity extends AppCompatActivity {

    String webUrl, apiKey;
    LeapSampleSharedPref sharedPref;
    WebView appWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        /*
        Start the service
         */
        triggerService();
        sharedPref = LeapSampleSharedPref.getInstance();
        webUrl = sharedPref.getWebUrl();
        apiKey = sharedPref.getAppApiKey();

        /*
        Initialise the SDKs
         */
        LeapAUI.withBuilder(HomeActivity.this, apiKey)
                .setDebugModeEnabled(true)
                .init();
        LeapSnapSDK.withBuilder(HomeActivity.this, apiKey)
                .setDebugModeEnabled(true)
                .init();

        appWebView = findViewById(R.id.webView);
        appWebView.getSettings().setJavaScriptEnabled(true);
        appWebView.setWebViewClient(new WebViewClient());
        LeapAUI.addWebInterface(appWebView);
        appWebView.loadUrl(webUrl);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        triggerService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, LeapService.class));
    }

    private void exitAndKillApp() {
        finishAndRemoveTask();
        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        if(appWebView.canGoBack()){
            appWebView.goBack();
            return;
        }
        super.onBackPressed();
        exitAndKillApp();
    }

    private void triggerService() {
        Intent intent = new Intent(this, LeapService.class);
        intent.putExtra("appName", LeapSampleSharedPref.getInstance().getRegisteredApp());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }

}