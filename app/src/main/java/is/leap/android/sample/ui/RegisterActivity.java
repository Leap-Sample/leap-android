package is.leap.android.sample.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import is.leap.android.sample.R;
import is.leap.android.sample.custom.BarCodeProcessor;
import is.leap.android.sample.custom.ScannerView;
import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.listeners.ValidationListener;
import is.leap.android.sample.service.LeapService;

public class RegisterActivity extends AppCompatActivity implements ValidationListener {

    private ScannerView leapScannerView;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private Handler handler;
    private BarcodeDetector barcodeDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        leapScannerView = findViewById(R.id.surfaceView);

        //Set to only parse QR code, NOT-BAR-CODE!
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE) //Set to only parse QR code, NOT-BAR-CODE!
                .build();

        handler = new Handler(Looper.getMainLooper());
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        leapScannerView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // draw bounds
                try {
                    if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        if (cameraSource != null )cameraSource.start(leapScannerView.getHolder());

                    } else {
                        ActivityCompat.requestPermissions(RegisterActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (cameraSource != null) cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new BarCodeProcessor(this));
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
                try {
                    if (cameraSource == null ) {
                        initialiseCamera();
                    }
                    cameraSource.start(leapScannerView.getHolder());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void openHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if( cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseCamera();
        initialiseDetectorsAndSources();
    }

    private void initialiseCamera() {
        if (cameraSource != null ) return;
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1080, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();
    }

    @Override
    public void onSuccessfulValidation(JSONObject config) {
        try {
            LeapSampleSharedPref.getInstance().saveLeapQRConfiguration(config);
            openHomeActivity();
            finish();
        } catch (JSONException e) {
            // e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onFailedValidation() {
        //Nothing to do here except show toast
    }
}