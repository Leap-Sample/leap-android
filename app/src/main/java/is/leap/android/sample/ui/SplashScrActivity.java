package is.leap.android.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import is.leap.android.sample.R;
import is.leap.android.sample.data.LeapSampleSharedPref;

public class SplashScrActivity extends AppCompatActivity {

    LeapSampleSharedPref sampleSharedPref;
    Handler handler;
    Runnable transitionRunnable = this::transitionActivity;
    private static final int TIME_OUT = 2000;
    private boolean splashLoaded = false;

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
        String scannerActivity = "is.leap.android.creator.ui.activity.ScannerActivity";
        try {
            Class<?> scannerClass = Class.forName(scannerActivity);
            Intent intent = new Intent(this, scannerClass);
            startActivity(intent);
        } catch (ClassNotFoundException ignored) {
            // Shouldn't happen as the creator SDK is already part of Sample App
        }
    }

    private void goToHome() {
        Intent transitionToHome = new Intent(this, HomeActivity.class);
        startActivity(transitionToHome);
    }

    private boolean isAppRegisteredAlready() {
        String appApiKey = sampleSharedPref.getApiKey();
        return appApiKey != null && !appApiKey.isEmpty();
    }
}