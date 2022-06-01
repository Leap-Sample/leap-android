package is.leap.android.aui.ui.assist.view;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.R;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.util.AppUtils;

public class SwipeGestureView extends FrameLayout implements LeapCustomViewGroup {

    private static final int CIRCLE_RADIUS = 10;
    public static final int CIRCLE_SIZE = 20;
    private FrameLayout rootView;

    public SwipeGestureView(Context context) {
        super(context);
        init();
    }

    public void init() {
        rootView = (FrameLayout) LeapAUIInternal.getInstance().inflate(R.layout.leap_finger_ripple_layout);
        setupView(getContext());
        addView(rootView);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(INVISIBLE);
    }

    @Override
    public void setupView(Context context) {
        RippleView circle = new RippleView(context);
        int size = AppUtils.dpToPxInt(getContext(), CIRCLE_SIZE);
        LayoutParams params = new LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        circle.setLayoutParams(params);
        circle.setRadius(AppUtils.dpToPx(context, CIRCLE_RADIUS));
        rootView.addView(circle, 0);
    }
}
