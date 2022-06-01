package is.leap.android.aui.ui.assist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import org.json.JSONObject;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.annotations.ArrowAction;
import is.leap.android.aui.ui.assist.annotations.AssistOutBoundSide;
import is.leap.android.aui.ui.assist.listener.AssistBoundListener;
import is.leap.android.aui.ui.assist.view.Arrow;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.data.LeapCoreCache;

public class AssistBoundsHelper {

    private static @AssistOutBoundSide
    int getOutBoundSide(Rect pointerLocation, Rect screenBound) {
        if (pointerLocation.top + (pointerLocation.height() * 0.6) > screenBound.bottom) {
            return AssistOutBoundSide.BOTTOM;
        }
        if (pointerLocation.bottom - (pointerLocation.height() * 0.4) < screenBound.top) {
            return AssistOutBoundSide.TOP;
        }
        return AssistOutBoundSide.NONE;
    }

    /***
     * Returns Visual location
     * @param rootView View
     * @param visualView View
     * @return Rect
     */
    public static Rect getAssistLocation(View rootView, View visualView) {
        if (visualView == null || rootView == null) return null;

        Rect offsetViewBounds = getRelativeRect(visualView);

        Rect bounds = new Rect();

        int paddingLeft = rootView.getPaddingLeft();
        int paddingTop = rootView.getPaddingTop();

        // Remove padding is only for cases where rootView is dialog.
        bounds.left = offsetViewBounds.left - paddingLeft;
        bounds.top = offsetViewBounds.top - paddingTop;
        bounds.right = offsetViewBounds.right - paddingLeft;
        bounds.bottom = offsetViewBounds.bottom - paddingTop;

        return bounds;
    }

    /***
     * Returns Visual location
     * @param rootView View
     * @param visualView View
     * @return Rect
     */
    public static Rect getGlobalViewLocation(View rootView, View visualView) {
        if (visualView == null || rootView == null) return null;


        Rect bounds = new Rect();
        visualView.getGlobalVisibleRect(bounds);

        // Remove padding is only for cases where rootView is dialog.
        int paddingLeft = rootView.getPaddingLeft();
        int paddingTop = rootView.getPaddingTop();
        bounds.left -= paddingLeft;
        bounds.top -= paddingTop;
        bounds.right -= paddingLeft;
        bounds.bottom -= paddingTop;

        return bounds;
    }

    public static Rect getAssistLocation(JSONObject dimensions, int[] bounds) {
        if (dimensions == null) return null;
        Rect pointerLocation = new Rect();
        float webViewScale = LeapCoreCache.webViewScale;

        int left = (int) dimensions.optDouble(AUIConstants.LEFT, 0.0);
        int top = (int) dimensions.optDouble(AUIConstants.TOP, 0.0);
        int right = (int) dimensions.optDouble(AUIConstants.RIGHT, 0.0);
        int bottom = (int) dimensions.optDouble(AUIConstants.BOTTOM, 0.0);

        pointerLocation.left = (int) (left * webViewScale);
        pointerLocation.top = (int) (top * webViewScale) + bounds[1];
        pointerLocation.right = (int) (right * webViewScale);
        pointerLocation.bottom = (int) (bottom * webViewScale) + bounds[1];
        return pointerLocation;
    }

    public static Rect getArrowPointerLocation(int width, int height, int defaultBottomMargin) {
        Rect pointerLocation = new Rect();
        Context context = LeapAUIInternal.getInstance().getContext();
        int margin = AppUtils.dpToPxInt(context, AUIConstants.DEFAULT_MARGIN_8);
        int arrowHeight = AppUtils.dpToPxInt(context, Arrow.ARROW_HEIGHT_IN_DP);
        pointerLocation.left = margin;
        int softButtonBarHeight = LeapAUICache.getSoftButtonBarHeight(context);
        pointerLocation.bottom = height - softButtonBarHeight - defaultBottomMargin;
        pointerLocation.top = pointerLocation.bottom - arrowHeight;
        pointerLocation.right = width - margin;
        return pointerLocation;
    }

    private static Rect getRelativeRect(View view) {
        Rect offsetViewBounds = new Rect();
        if (view == null) return offsetViewBounds;

        int[] locAnchorView = new int[2];

        view.getLocationInWindow(locAnchorView);

        //The method 'getGlobalVisibleRect()' was causing tooltip flicker so not used

        // if the drawing is not complete and getLocationInWindow returns invalid [0,0] values need to figure a solution
        int left = locAnchorView[0];
        int top = locAnchorView[1];
        int right = left + (int) (view.getWidth() * view.getScaleX());
        int bottom = top + (int) (view.getHeight() * view.getScaleY());
        return new Rect(left, top, right, bottom);
    }

    static void checkBounds(Activity currentActivity, View rootView, View assistView, Rect assistLocation, AssistBoundListener assistBoundListener) {
        if (currentActivity == null || rootView == null) return;
        Rect rootWindowBounds = AssistUtils.getEffectiveRootWindowBounds(currentActivity, rootView);
        if (rootWindowBounds == null || assistLocation == null) return;

        // If keyboard is open
        if (AppUtils.isKeyboardOpen(currentActivity)) {
            int softInputMode = AppUtils.getCurrentActivitySoftInputMode(currentActivity);
            if (AppUtils.isSoftInputModeAdjustPan(softInputMode)) {
                assistLocation = getGlobalViewLocation(rootView, assistView);
                rootWindowBounds = AssistUtils.getRootRect(rootView);
            }

            int statusHeight = LeapAUICache.getStatusBarHeight(currentActivity);
            if (!AssistUtils.isVisualAboveKeyboard(assistLocation, rootWindowBounds, statusHeight)) {
                assistBoundListener.onOutBound(AssistOutBoundSide.BOTTOM, ArrowAction.KEYBOARD, assistLocation);
                return;
            }
        }

        int outBoundSide = getOutBoundSide(assistLocation, rootWindowBounds);
        if (outBoundSide == AssistOutBoundSide.NONE) {
            assistBoundListener.onInBound(assistLocation);
            return;
        }

        assistBoundListener.onOutBound(outBoundSide, ArrowAction.SCROLL, assistLocation);
    }

}
