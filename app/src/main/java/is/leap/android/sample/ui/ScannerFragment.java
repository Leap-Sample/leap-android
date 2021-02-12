package is.leap.android.sample.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import is.leap.android.sample.R;
import is.leap.android.sample.Utils;
import is.leap.android.sample.custom.BarCodeProcessor;
import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.listeners.ValidationListener;

public class ScannerFragment extends Fragment implements ValidationListener {

    private SurfaceView leapScannerView;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private BarcodeDetector barcodeDetector;
    final ScannerListener scannerListener;
    private Activity activity;

    public ScannerFragment(ScannerListener scannerListener) {
        this.scannerListener = scannerListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set to only parse QR code, NOT-BAR-CODE!
        barcodeDetector = new BarcodeDetector.Builder(activity)
                .setBarcodeFormats(Barcode.QR_CODE) //Set to only parse QR code, NOT-BAR-CODE!
                .build();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_layout_scanner, parent, false);
        leapScannerView = root.findViewById(R.id.surfaceView);
        return root;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        initialiseCamera();
        initialiseSources();
    }

    private void initialiseSources() {
        leapScannerView.getHolder()
                .addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        // draw bounds
                        try {
                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                                    == PackageManager.PERMISSION_GRANTED) {
                                if (cameraSource != null)
                                    cameraSource.start(leapScannerView.getHolder());
                            } else {
                                ActivityCompat.requestPermissions(activity, new
                                        String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                            }

                        } catch (IOException ignored) {
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


    private void initialiseCamera() {
        if (cameraSource != null) return;

        int screenHeight = Utils.getScreenHeight(activity);
        int screenWidth = Utils.getScreenWidth(activity);

        cameraSource = new CameraSource.Builder(activity, barcodeDetector)
                .setRequestedPreviewSize(screenWidth, screenHeight)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    @Override
    public void onSuccessfulValidation(JSONObject config) {
        try {
            LeapSampleSharedPref.getInstance().saveLeapQRConfiguration(config);
            scannerListener.onScanSuccessful();
        } catch (JSONException e) {
            //
        }
    }

    @Override
    public void onFailedValidation() {
        Toast.makeText(getContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
    }

    public void beginScan() {
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
                return;
            if (cameraSource == null)
                initialiseCamera();
            cameraSource.start(leapScannerView.getHolder());

        } catch (IOException ignored) {

        }
    }

    public interface ScannerListener {
        void onScanSuccessful();

        void onScanFailed();
    }
}
