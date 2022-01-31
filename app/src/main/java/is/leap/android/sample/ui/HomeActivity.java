package is.leap.android.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import is.leap.android.aui.Leap;
import is.leap.android.creator.LeapCreator;
import is.leap.android.sample.R;
import is.leap.android.sample.data.LeapSampleCache;

public class HomeActivity extends AppCompatActivity {

    public static final String IS_LEAP_INIT = "IS_LEAP_INIT";
    WebView appWebView;
    private boolean isLeapInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState != null) isLeapInit = savedInstanceState.getBoolean(IS_LEAP_INIT);
        if (!isLeapInit) initLeap();

        appWebView = findViewById(R.id.webView);
        appWebView.getSettings().setJavaScriptEnabled(true);
        appWebView.setWebViewClient(new WebViewClient());
        appWebView.loadUrl(LeapSampleCache.WEB_URL);
        Leap.enableWeb(appWebView);
    }

    private void initLeap() {
        Leap.start(LeapSampleCache.API_KEY);
        LeapCreator.start(LeapSampleCache.API_KEY);
        isLeapInit = true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_LEAP_INIT, isLeapInit);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initLeap();
        Leap.enableWeb(appWebView);
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

}