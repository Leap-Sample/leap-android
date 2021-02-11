package is.leap.android.sample.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import is.leap.android.sample.R;
import is.leap.android.sample.Utils;

import static is.leap.android.sample.Constants.DISABLE;

public class RegisterActivity extends AppCompatActivity implements
        ScannerFragment.ScannerListener, NonScannerFragment.NonScanCaptureListener {

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private Handler handler;
    private final static int MODE_SCANNER = 1;
    private final static int MODE_NON_SCANNER = 2;
    private int modeOfScan = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        modeOfScan = MODE_SCANNER;
        getIntents();
        handler = new Handler(Looper.getMainLooper());

        showContainer(modeOfScan);
    }

    private void showContainer(int modeOfScan) {
        Fragment currentFragment = getFragment(modeOfScan);
        // Begin the transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.placeholder, currentFragment);
        ft.commit();
    }

    private Fragment getFragment(int modeOfScan) {
        switch (modeOfScan){
            case MODE_SCANNER:
                return ScannerFragment.getInstance(this);

            case MODE_NON_SCANNER:
                return NonScannerFragment.getInstance(this);

        }
        return null;
    }

    private void getIntents() {
        Intent intent = getIntent();
        boolean shouldDisableLeap = intent.getBooleanExtra(DISABLE, true);
        if (!shouldDisableLeap){
            return;
        }
        hideNotification();
        disableLeap();
    }

    private void disableLeap() {
//        LeapAUI.terminate();
//        LeapSnapSDK.terminate();
    }

    private void hideNotification() {
        Utils.hideNotification(this.getApplicationContext(),true );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CAMERA_PERMISSION == requestCode) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                ScannerFragment.getInstance(this).beginScan();
                return;
            }
            showContainer(MODE_NON_SCANNER);
        }
    }

    private void openHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onScanSuccessful() {
        openHomeActivity();
        finish();
    }

    @Override
    public void onScanFailed() {

    }

    @Override
    public void onCameraOpened() {
        showContainer(MODE_SCANNER);
    }
}