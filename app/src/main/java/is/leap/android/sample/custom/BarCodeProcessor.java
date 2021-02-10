package is.leap.android.sample.custom;

import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import is.leap.android.sample.Utils;
import is.leap.android.sample.data.LeapSampleSharedPref;
import is.leap.android.sample.listeners.ValidationListener;
import is.leap.android.sample.ui.RegisterActivity;

public class BarCodeProcessor implements Detector.Processor {

    private final ValidationListener validationListener;

    public BarCodeProcessor(ValidationListener validationListener){
        this.validationListener = validationListener;
    }

    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(@NonNull Detector.Detections detections) {
        final SparseArray barcodes = detections.getDetectedItems();
        try {
            Utils.isLeapValidatedApp(barcodes, new ValidationListener() {
                @Override
                public void onSuccessfulValidation(JSONObject config) {
                    validationListener.onSuccessfulValidation(config);
                }

                @Override
                public void onFailedValidation() {
                    validationListener.onFailedValidation();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
