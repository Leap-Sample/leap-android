package is.leap.android.sample.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import is.leap.android.sample.R;

public class NonScannerFragment extends Fragment {

    final NonScanCaptureListener nonScanCaptureListener;

    public NonScannerFragment(NonScanCaptureListener nonScanCaptureListener) {
        this.nonScanCaptureListener = nonScanCaptureListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_layout_non_scanner, parent, false);
        Button captureBtn = root.findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(v -> {
            if (nonScanCaptureListener != null) nonScanCaptureListener.onCameraOpened();
        });
        return root;
    }

    public interface NonScanCaptureListener {
        void onCameraOpened();
    }
}
