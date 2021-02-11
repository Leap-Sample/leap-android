package is.leap.android.sample.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import is.leap.android.aui.LeapAUI;
import is.leap.android.sample.R;
import is.leap.android.sample.Utils;
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
        triggerService();
        sharedPref = LeapSampleSharedPref.getInstance();
        webUrl = sharedPref.getWebUrl();
        apiKey = sharedPref.getAppApiKey();

        LeapAUI.init(HomeActivity.this, apiKey);
        LeapSnapSDK.init(HomeActivity.this, apiKey);

        appWebView = findViewById(R.id.webView);
        appWebView.getSettings().setJavaScriptEnabled(true);
        appWebView.setWebViewClient(new WebViewClient());
        LeapAUI.addWebInterface(appWebView);
        appWebView.loadUrl(webUrl);
    }

    @Override
    public void onBackPressed() {
        if(appWebView.canGoBack()){
            appWebView.goBack();
            return;
        }
        super.onBackPressed();
        stopRunningService();
        finish();
    }

    private void stopRunningService() {
        Intent intent = new Intent(this, LeapService.class);
        stopService(intent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}