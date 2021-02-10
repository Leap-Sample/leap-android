package is.leap.android.sample.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class ScannerView  extends SurfaceView {

    public ScannerView(Context context) {
        super(context);
        init(context);
    }

    public ScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public ScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    public ScannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);

    }

    private void init(Context context){

    }
}
