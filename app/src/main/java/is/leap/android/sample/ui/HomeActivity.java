package is.leap.android.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import is.leap.android.aui.Leap;
import is.leap.android.sample.R;
import is.leap.android.sample.data.LeapSampleCache;

public class HomeActivity extends AppCompatActivity {

    public static final String IS_LEAP_INIT = "IS_LEAP_INIT";
    WebView appWebView;
    private boolean isLeapInit;
    private static String apiKey;
    private static String projectID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        handleIntent(getIntent());

//        if (savedInstanceState != null) isLeapInit = savedInstanceState.getBoolean(IS_LEAP_INIT);
//        if (!isLeapInit) initLeap();

        appWebView = findViewById(R.id.webView);
        appWebView.getSettings().setJavaScriptEnabled(true);
        appWebView.setWebViewClient(new WebViewClient());
        appWebView.loadUrl(LeapSampleCache.WEB_URL);
        Leap.enableWeb(appWebView);
    }

//    private void initLeap() {
//        if (apiKey != null) {
//            Leap.start(apiKey);
//            LeapCreator.start(apiKey);
//        }
//        if (projectID != null) Leap.startProject(projectID);
//        isLeapInit = true;
//    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_LEAP_INIT, isLeapInit);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
//        initLeap();
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

    private void handleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d("extras:", extras.toString());
            String apiKey = extras.getString("apiKey");
            if (apiKey != null) {
                Log.d("apiKey:", apiKey);
                Leap.start(apiKey);
            }

            String projectID = extras.getString("projectID");
            if (projectID != null) {
                Log.d("projectID:", projectID);
                Leap.startProject(projectID);
            }
        }
    }
}