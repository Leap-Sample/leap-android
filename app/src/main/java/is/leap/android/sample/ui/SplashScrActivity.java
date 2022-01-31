package is.leap.android.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import is.leap.android.sample.R;
import is.leap.android.sample.data.LeapSampleCache;
import is.leap.android.sample.data.LeapSampleSharedPref;

public class SplashScrActivity extends AppCompatActivity {

    LeapSampleSharedPref sampleSharedPref;
    Handler handler;
    Runnable transitionRunnable = this::transitionActivity;
    private static final int TIME_OUT = 2000;
    private boolean splashLoaded = false;
    static final String PREVIEW_DEVICE_DOC_LINK_KEY = "previewDeviceDocLink";
    static final String CONNECT_SAMPLE_APP_DOC_LINK_KEY = "connectSampleAppDocLink";
    static final String GENERATE_QR_DOC_LINK_KEY = "generateQrHelp";
    static final String PREVIEW_DEVICE_DOC_LINK = "https://docs.leap.is/getting-started-with-leap/in-app-experience/how-to-preview-projects-on-device";
    static final String CONNECT_SAMPLE_APP_DOC_LINK = "https://docs.leap.is/getting-started-with-leap/leap-sample-app/how-to-connect-sample-app";
    static final String GENERATE_QR_DOC_LINK = "https://docs.leap.is/getting-started-with-leap/in-app-experience/how-to-generate-qr-secret-otp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        sampleSharedPref = LeapSampleSharedPref.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (splashLoaded) {
            transitionActivity();
            return;
        }
        splashLoaded = true;
        beginTransition();
    }

    private void beginTransition() {
        handler.postDelayed(transitionRunnable, TIME_OUT);
    }

    private void transitionActivity() {
        if (isAppRegisteredAlready()) {
            goToHome();
            finish();
            return;
        }
        registerApp();
        finish();
    }

    private void registerApp() {
        String scannerActivity = "is.leap.android.creator.ui.activity.LeapScannerActivity";
        try {
            Class<?> scannerClass = Class.forName(scannerActivity);
            Intent intent = new Intent(this, scannerClass);
            intent.putExtra(CONNECT_SAMPLE_APP_DOC_LINK_KEY, CONNECT_SAMPLE_APP_DOC_LINK);
            intent.putExtra(PREVIEW_DEVICE_DOC_LINK_KEY, PREVIEW_DEVICE_DOC_LINK);
            intent.putExtra(GENERATE_QR_DOC_LINK_KEY, GENERATE_QR_DOC_LINK);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.e("SplashScrActivity", e.getMessage());
        }
    }

    private void goToHome() {
        Intent transitionToHome = new Intent(this, HomeActivity.class);
        startActivity(transitionToHome);
    }

    private boolean isAppRegisteredAlready() {
        String appApiKey = LeapSampleCache.API_KEY;
        return appApiKey != null && !appApiKey.isEmpty();
    }
}