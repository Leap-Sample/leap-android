package is.leap.android.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import is.leap.android.LeapSharedPref;
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

            String stringProjectIdList = extras.getString("projectIDs");
            if(stringProjectIdList != null && !stringProjectIdList.isEmpty()) {
                Log.d("projectIDs: ", stringProjectIdList);
                List<String> stringifiedProjectIdList = new ArrayList<>();
                stringifiedProjectIdList.add(stringProjectIdList);
                Map<String, List<String>> deploymentHeaderMap = new HashMap<>();
                deploymentHeaderMap.put("x-jiny-deployment-ids", stringifiedProjectIdList);
                saveResponseHeaders(deploymentHeaderMap);
            }

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

            String embedProjectID = extras.getString("embedProjectID");
            if(embedProjectID != null) {
                Log.d("embedProjectID: ", embedProjectID);
                Leap.embedProject(embedProjectID);
            }
        }
    }

    public static void saveResponseHeaders(Map<String, List<String>> respHeaders) {
        if (respHeaders == null || respHeaders.isEmpty()) return;
        JSONObject configResponse = new JSONObject();
        Set<String> resKeySet = respHeaders.keySet();
        for (String resKey : resKeySet) {
            if (resKey.startsWith("x-jiny")) {
                String headerValue = getResponseValue(respHeaders, resKey);
                try {
                    configResponse.put(resKey, headerValue);
                } catch (JSONException ignored) {
                }
            }
        }
        Log.d("projectIDs: header: ", configResponse.toString());
        LeapSharedPref.getInstance().saveConfigResponseHeaders(configResponse);
    }

    private static String getResponseValue(Map<String, List<String>> respHeaders, String resKey) {
        List<String> value = respHeaders.get(resKey);
        if (value != null && value.size() >= 1) return value.get(0);
        return null;
    }
}