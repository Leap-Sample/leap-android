package is.leap.android.aui.ui.assist.view;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.R;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.annotations.AssistOutBoundSide;
import is.leap.android.aui.util.AppUtils;

public class Arrow {

    private static final int ARROW_WIDTH_IN_DP = 50;
    public static final int ARROW_HEIGHT_IN_DP = 50;

    private final View.OnClickListener arrowCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            arrowClickListener.onArrowClicked(arrowAction);
        }
    };
    private ImageView arrowImageView;
    private View arrowLayout;
    private int arrowAction;
    private WeakReference<ViewGroup> leapRootView;
    private final boolean isIconLeftAligned;
    private final ArrowClickListener arrowClickListener;
    private int arrowWidth;
    private int arrowHeight;

    public Arrow(View rootView, boolean isIconLeftAligned, ArrowClickListener arrowClickListener) {
        this.isIconLeftAligned = isIconLeftAligned;
        this.arrowClickListener = arrowClickListener;
        init(rootView);
    }


    public void init(View root) {
        this.leapRootView = new WeakReference<>((ViewGroup) root);
        arrowLayout = LeapAUIInternal.getInstance().inflate(R.layout.leap_arrow_layout);
        arrowImageView = arrowLayout.findViewById(R.id.img_arrow);
        arrowImageView.setOnClickListener(arrowCL);

        Context context = root.getContext();
        arrowWidth = AppUtils.dpToPxInt(context, ARROW_WIDTH_IN_DP);
        arrowHeight = AppUtils.dpToPxInt(context, ARROW_HEIGHT_IN_DP);

        hide();
        addToRoot();
    }

    public void hide() {
        arrowLayout.setVisibility(View.INVISIBLE);
    }

    public void show() {
        arrowLayout.setVisibility(View.VISIBLE);
    }

    private ViewGroup getRootView() {
        return leapRootView == null ? null : leapRootView.get();
    }

    protected void addToRoot() {
        removeFromParent(arrowLayout);
        ViewGroup rootView = getRootView();
        if (rootView != null) {
            rootView.addView(arrowLayout);
        }
    }

    private void removeFromParent(View view) {
        if (view != null && view.getParent() != null) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(view);
            }
        }
    }

    public void updateLayoutParams(final Rect rect) {
        arrowLayout.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.UNSPECIFIED);
        FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams) arrowLayout.getLayoutParams();

        fl.width = arrowWidth;
        fl.height = arrowHeight;

        int arrowBottom = LeapAUICache.screenHeight - rect.bottom;
        if (fl.bottomMargin != arrowBottom) fl.bottomMargin = arrowBottom;
        if (!isIconLeftAligned) {
            if (fl.leftMargin != rect.left) fl.leftMargin = rect.left;
        } else {
            if (fl.leftMargin != rect.right - fl.width)
                fl.leftMargin = rect.right - fl.width;
        }
        fl.gravity = Gravity.BOTTOM | Gravity.START;
        arrowLayout.setLayoutParams(fl);
    }

    public void rotateArrow(@AssistOutBoundSide int pointerOutBoundSide) {
        if (pointerOutBoundSide == AssistOutBoundSide.BOTTOM && arrowImageView.getRotation() == 0f) {
            arrowImageView.setRotation(180f);
        } else if (pointerOutBoundSide == AssistOutBoundSide.TOP && arrowImageView.getRotation() == 180f) {
            arrowImageView.setRotation(0f);
        }
    }

    public void setArrowAction(int arrowAction) {
        this.arrowAction = arrowAction;
    }

    public void remove() {
        removeFromParent(arrowLayout);
    }

    public void setContentDescription(int outBoundSide) {
        String arrowAccessibilityText = getArrowAccessibilityText(outBoundSide);
        AppUtils.setContentDescription(arrowImageView, arrowAccessibilityText);
    }

    private String getArrowAccessibilityText(int outBoundSide) {
        String arrowAccessibilityText = null;
        if (outBoundSide == AssistOutBoundSide.BOTTOM) {
            arrowAccessibilityText = LeapAUICache.getAccessibilityText(AUIConstants.AccessibilityText.ARROW_DOWN);
        } else if (outBoundSide == AssistOutBoundSide.TOP) {
            arrowAccessibilityText = LeapAUICache.getAccessibilityText(AUIConstants.AccessibilityText.ARROW_UP);
        }
        return arrowAccessibilityText;
    }

    public interface ArrowClickListener {
        void onArrowClicked(int arrowAction);
    }
}