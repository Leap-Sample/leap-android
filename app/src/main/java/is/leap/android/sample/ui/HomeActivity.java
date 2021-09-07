package is.leap.android.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import is.leap.android.aui.Leap;
import is.leap.android.creator.LeapCreator;
import is.leap.android.sample.R;
import is.leap.android.sample.data.LeapSampleSharedPref;

public class HomeActivity extends AppCompatActivity {

    String webUrl, apiKey;
    LeapSampleSharedPref sharedPref;
    WebView appWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initLeap();

        appWebView = findViewById(R.id.webView);
        appWebView.getSettings().setJavaScriptEnabled(true);
        appWebView.setWebViewClient(new WebViewClient());
        appWebView.loadUrl(webUrl);
        Leap.enableWeb(appWebView);
    }

    private void initLeap() {
        sharedPref = LeapSampleSharedPref.getInstance();
        webUrl = sharedPref.getWebUrl();
        apiKey = sharedPref.getApiKey();

        /*
        Initialise the SDKs
         */
        Leap.start(apiKey);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && intent.hasExtra("project_id")) {
            String projectId = intent.getStringExtra("project_id");
            Leap.embedProject(projectId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Leap.disable();
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

}