package is.leap.android.sample.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import is.leap.android.sample.R;
import is.leap.android.sample.data.LeapSampleSharedPref;

public class SplashScrActivity extends AppCompatActivity {

    LeapSampleSharedPref sampleSharedPref;
    Handler handler;
    Runnable transitionRunnable = this::transitionActivity;
    private static final int TIME_OUT = 2000;

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
        beginTransition();
    }

    private void beginTransition() {
        handler.postDelayed(transitionRunnable, TIME_OUT);
    }

    private void transitionActivity() {
        if (isAppRegisteredAlready()){
            goToHome();
            finish();
            return;
        }
        registerApp();
        finish();
    }

    private void registerApp() {
        Intent transitionToHome = new Intent(this, RegisterActivity.class);
        startActivity(transitionToHome);
    }

    private void goToHome() {
        Intent transitionToHome = new Intent(this, HomeActivity.class);
        transitionToHome.putExtra(LeapSampleSharedPref.APP_API_KEY, sampleSharedPref.getAppApiKey());
        startActivity(transitionToHome);
    }

    private boolean isAppRegisteredAlready() {
        return !sampleSharedPref.getRegisteredApp().equals(LeapSampleSharedPref.NOT_AVAILABLE);
    }
}