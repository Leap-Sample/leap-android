package is.leap.android.aui.ui.assist.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.R;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.util.AppUtils;

import static is.leap.android.aui.util.AppUtils.getImageView;

public class TrashView extends FrameLayout implements LeapCustomViewGroup {

    public static final int TRASH_VIEW_HEIGHT = 200;
    public static final int CLOSE_VIEW_SIZE = 40;
    public static final int TRASH_ICON_BOTTOM_MARGIN = 50;
    private ImageView trashIcon;

    public TrashView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setupView(context);
    }

    @Override
    public void setupView(Context context) {
        FrameLayout rootView = new FrameLayout(context);
        int height = AppUtils.dpToPxInt(context, TRASH_VIEW_HEIGHT);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
        rootView.setLayoutParams(params);
        Context leapContext = LeapAUIInternal.getInstance().getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rootView.setBackground(leapContext.getDrawable(R.drawable.shape_leap_trash_gradient));
        }

        trashIcon = getImageView(leapContext, R.drawable.ic_leap_close);
        rootView.addView(trashIcon);
        int size = AppUtils.dpToPxInt(context, CLOSE_VIEW_SIZE);
        LayoutParams closeViewParams = new LayoutParams(size, size);
        closeViewParams.bottomMargin = AppUtils.dpToPxInt(context, TRASH_ICON_BOTTOM_MARGIN);
        closeViewParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
        trashIcon.setLayoutParams(closeViewParams);

        addView(rootView);
    }

    public Rect getTrashIconBound() {
        Rect rect = new Rect();
        trashIcon.getGlobalVisibleRect(rect);
        return rect;
    }

    private void scale(float scaleFactor) {
        trashIcon.setScaleX(scaleFactor);
        trashIcon.setScaleY(scaleFactor);
    }

    public void scale() {
        scale(1.3f);
    }

    public void scaleToNormal() {
        scale(1);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }


}
