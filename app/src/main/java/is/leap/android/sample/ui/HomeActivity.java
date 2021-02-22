package is.leap.android.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import is.leap.android.aui.LeapAUI;
import is.leap.android.creator.LeapCreator;
import is.leap.android.sample.R;
import is.leap.android.sample.Utils;
import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.service.LeapService;

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
        initLeap();

        appWebView = findViewById(R.id.webView);
        appWebView.getSettings().setJavaScriptEnabled(true);
        appWebView.setWebViewClient(new WebViewClient());
        appWebView.loadUrl(webUrl);
        LeapAUI.enableWeb(appWebView);
    }

    private void initLeap() {
        sharedPref = LeapSampleSharedPref.getInstance();
        webUrl = sharedPref.getWebUrl();
        apiKey = sharedPref.getAppApiKey();

        /*
        Initialise the SDKs
         */
        boolean isDebuggable = Utils.isNotReleaseBuild(this);
        LeapAUI.withBuilder(HomeActivity.this, apiKey)
                .setDebugModeEnabled(isDebuggable)
                .init();
        LeapCreator.withBuilder(HomeActivity.this, apiKey)
                .setDebugModeEnabled(isDebuggable)
                .init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        triggerService();
        initLeap();
        LeapAUI.enableWeb(appWebView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeapAUI.disable();
        LeapCreator.disable();
    }

    private void exitAndKillApp() {
        finishAndRemoveTask();
    }

    @Override
    public void onBackPressed() {
        if (appWebView.canGoBack()) {
            appWebView.goBack();
            return;
        }
        super.onBackPressed();
        exitAndKillApp();
    }

    private void triggerService() {
        Intent intent = new Intent(this, LeapService.class);
        intent.setAction(LeapService.START_FOREGROUND_SERVICE);
        startService(intent);
    }

}