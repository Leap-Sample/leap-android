package is.leap.android.aui.ui.assist;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import is.leap.android.aui.util.AppUtils;

public class AssistUtils {

    static boolean autoFocus(View pointerView) {
        if (pointerView == null) return false;
        if (pointerView instanceof EditText) {
            focusView(pointerView);
            return true;
        }
        if (pointerView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) pointerView;
            int children = viewGroup.getChildCount();
            for (int childIterator = 0; childIterator < children; childIterator++) {
                boolean autoFocused = autoFocus(viewGroup.getChildAt(childIterator));
                if (autoFocused) return true;
            }
        }
        return false;
    }

    private static void focusView(View edtText) {
        if (edtText.isFocused()) return;
        edtText.setFocusableInTouchMode(true);
        edtText.requestFocus();
    }

    public static boolean isVisualAboveKeyboard(Rect pointerBounds, Rect effectiveScreenBounds, int statusHeight) {
        // need to subtract top of status bar height since pointerBounds is calculated with status bar
        int y = (pointerBounds.top + pointerBounds.height() / 2 - statusHeight);
        return y <= effectiveScreenBounds.height();
    }

    public static Rect getRootRect(View rootView) {
        return new Rect(rootView.getLeft(),
                rootView.getTop(),
                rootView.getRight() - rootView.getPaddingRight() - rootView.getPaddingLeft(),
                rootView.getBottom() - rootView.getPaddingTop() - rootView.getPaddingBottom());
    }

    public static Rect getEffectiveRootWindowBounds(Activity currentActivity, View rootView) {
        return AppUtils.isRootViewAndDecorViewSame(currentActivity, rootView)
                ? AppUtils.getEffectiveBounds(currentActivity)
                : getRootRect(rootView);
    }

}
