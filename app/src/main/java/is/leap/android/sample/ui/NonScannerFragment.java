package is.leap.android.sample.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import is.leap.android.sample.R;

public class NonScannerFragment extends Fragment {

    final NonScanCaptureListener nonScanCaptureListener;
    private static NonScannerFragment instance;

    public NonScannerFragment(NonScanCaptureListener nonScanCaptureListener) {
        this.nonScanCaptureListener = nonScanCaptureListener;
    }

    public static NonScannerFragment getInstance(NonScannerFragment.NonScanCaptureListener captureListener) {
        if (instance == null)
            instance = new NonScannerFragment(captureListener);
        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_layout_non_scanner, parent, false);
        Button captureBtn = root.findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nonScanCaptureListener != null) nonScanCaptureListener.onCameraOpened();
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    // This method is called when the fragment is no longer connected to the Activity
    // Any references saved in onAttach should be nulled out here to prevent memory leaks.
    @Override
    public void onDetach() {
        super.onDetach();
        instance = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface NonScanCaptureListener{
        void onCameraOpened();
    }
}
