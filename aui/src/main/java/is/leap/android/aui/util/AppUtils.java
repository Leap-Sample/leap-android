package is.leap.android.aui.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import is.leap.android.aui.LeapAUILogger;
import is.leap.android.aui.R;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.AssistBoundsHelper;
import is.leap.android.aui.ui.assist.AssistUtils;
import is.leap.android.core.util.StringUtils;

public class AppUtils {

    private static final String RES_STATUS_BAR_HEIGHT = "status_bar_height";
    private static final String RES_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String RES_NAVIGATION_BAR_WIDTH = "navigation_bar_width";
    private static final String RES_CONFIG_SHOW_NAVIGATION_BAR = "config_showNavigationBar";

    private static final String DEF_TYPE_DIMEN = "dimen";
    private static final String DEF_PACKAGE_ANDROID = "android";
    private static final String DEF_TYPE_BOOL = "bool";
    private static final int KEYBOARD_THRESHOLD_MARGIN_DP = 50;
    private static int KEYBOARD_THRESHOLD_MARGIN_PX = -1;

    public static boolean isTablet(Context context){
        return context.getResources().getBoolean(R.bool.leapTabletCheck);
    }

    public static View getView(WeakReference<View> ref) {
        return ref == null ? null : ref.get();
    }

    public static WebView getWebView(WeakReference<WebView> ref) {
        return ref == null ? null : ref.get();
    }

    public static Rect getBounds(View view) {
        int[] pos = new int[2];
        view.getLocationOnScreen(pos);
        return new Rect(pos[0], pos[1], pos[0] + view.getWidth(), pos[1] + view.getHeight());
    }

    public static int getCurrentActivitySoftInputMode(Activity currentActivity) {
        if (currentActivity == null || currentActivity.getWindow() == null
                || currentActivity.getWindow().getAttributes() == null) return -1;
        return currentActivity.getWindow().getAttributes().softInputMode;
    }

    public static Rect getEffectiveBounds(Activity activity) {
        Rect effectiveScreenBounds = new Rect();
        if (activity == null) return effectiveScreenBounds;
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(effectiveScreenBounds);
        return effectiveScreenBounds;
    }

    public static int dpToPxInt(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static float dpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getStatusBarHeight(Context context) {
        if (context == null) return 0;
        int result = 0;
        int resourceId = context.getResources().getIdentifier(RES_STATUS_BAR_HEIGHT, DEF_TYPE_DIMEN, DEF_PACKAGE_ANDROID);
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getSoftButtonsBarHeight(Context context) {
        // getRealMetrics is only available with API 17 and +
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(metrics);
        }
        int usableHeight = metrics.heightPixels;
        if (wm != null) {
            wm.getDefaultDisplay().getRealMetrics(metrics);
        }
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return realHeight - usableHeight;
        else
            return 0;
    }

    public static int getNavBarHeight(Context context) {
        if (context == null) return 0;
        int result = 0;
        if (!hasNavBar(context)) return result;

        Resources resources = context.getResources();
        //The device has a navigation bar
        int orientation = getCurrentOrientation(context);

        int resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? RES_NAVIGATION_BAR_HEIGHT : RES_NAVIGATION_BAR_WIDTH, DEF_TYPE_DIMEN, DEF_PACKAGE_ANDROID);

        if (resourceId > 0) return resources.getDimensionPixelSize(resourceId);

        return result;
    }

    public static boolean hasNavBar(Context context) {
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        return (!hasMenuKey && !hasBackKey) || hasNavBarAboveM(context);
    }

    private static boolean hasNavBarAboveM(Context context) {
        int id = context.getResources().getIdentifier(RES_CONFIG_SHOW_NAVIGATION_BAR, DEF_TYPE_BOOL, DEF_PACKAGE_ANDROID);
        return id > 0 && context.getResources().getBoolean(id);
    }

    private static int getCurrentOrientation(Context context) {
        Resources resources = context.getResources();
        return resources.getConfiguration().orientation;
    }

    public static boolean isKeyboardOpen(Activity activity) {
        if (activity == null) return false;
        Rect effectiveScreenBounds = getEffectiveBounds(activity);
        Rect displayBounds = getScreenBoundWithoutNav(activity);
        int diff = displayBounds.height() - (effectiveScreenBounds.height() + LeapAUICache.getStatusBarHeight(activity));
        if (KEYBOARD_THRESHOLD_MARGIN_PX == -1)
            KEYBOARD_THRESHOLD_MARGIN_PX = dpToPxInt(activity, KEYBOARD_THRESHOLD_MARGIN_DP);
        return diff > KEYBOARD_THRESHOLD_MARGIN_PX;
    }

    public static Rect getScreenBoundWithoutNav(Activity activity) {
        Rect rect = new Rect();
        if (activity == null) return rect;
        activity.getWindowManager().getDefaultDisplay().getRectSize(rect);
        return rect;
    }

    public static int getInsetBottom(Activity currentActivity) {
        int stableInsetBottom = AppUtils.getNavBarHeight(currentActivity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (currentActivity == null || currentActivity.getWindow() == null
                    || currentActivity.getWindow().getDecorView().getRootWindowInsets() == null)
                return stableInsetBottom;
            stableInsetBottom = currentActivity.getWindow().getDecorView().getRootWindowInsets().getStableInsetBottom();
        }

        return stableInsetBottom;
    }

    public static void closeKeyboard(Activity activity) {
        if (activity == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static int getScreenWidth(Activity currentActivity) {
        if (currentActivity == null) return 0;
        Rect screenBoundWithoutNav = getScreenBoundWithoutNav(currentActivity);
        return screenBoundWithoutNav.width();
    }

    public static int getScreenHeight(Activity currentActivity) {
        if (currentActivity == null) return 0;
        Rect screenBoundWithoutNav = getScreenBoundWithoutNav(currentActivity);
        return screenBoundWithoutNav.height();
    }

    public static boolean isSoftInputModeAdjustPan(int softInputMode) {
        return softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN ||
                softInputMode == (WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) ||
                softInputMode == (WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static Rect getTopWindowBound(Activity currentActivity, View topWindowView) {
        Rect rect = new Rect();
        if (currentActivity == null || currentActivity.getWindow() == null) return rect;
        FrameLayout activityRootView = (FrameLayout) currentActivity.getWindow().getDecorView();

        if (topWindowView != null && topWindowView != activityRootView && topWindowView.getWidth() >= activityRootView.getWidth() && topWindowView.getHeight() >= activityRootView.getHeight()) {
            rect.set(0, 0, topWindowView.getWidth(), topWindowView.getHeight());
            return rect;
        }

        int activityHeight = activityRootView.getHeight() - activityRootView.getPaddingTop()
                - activityRootView.getPaddingBottom();
        rect.set(0, 0, activityRootView.getWidth(), activityHeight);
        return rect;
    }

    public static boolean isBoundsSame(Rect oldBounds, Rect newBounds) {
        if (oldBounds == null || newBounds == null) return false;
        return oldBounds.left == newBounds.left && oldBounds.top == newBounds.top &&
                oldBounds.right == newBounds.right && oldBounds.bottom == newBounds.bottom;
    }

    public static boolean isRootViewAndDecorViewSame(Activity currentActivity, View rootView) {
        if (currentActivity == null || currentActivity.getWindow() == null || rootView == null)
            return false;
        return currentActivity.getWindow().getDecorView() == rootView;
    }

    public static Rect getRootViewBounds(Activity currentActivity, boolean isKeyboardOpen,
                                         View windowRootView) {
        if (!isKeyboardOpen)
            return AppUtils.getTopWindowBound(currentActivity, windowRootView);

        final Rect effectiveBounds = AppUtils.getEffectiveBounds(currentActivity);
        int statusHeight = LeapAUICache.getStatusBarHeight(currentActivity);
        return new Rect(0, 0, effectiveBounds.width(),
                effectiveBounds.height() + statusHeight);
    }

    public static void updateRootLayout(Activity currentActivity, boolean isKeyboardOpen,
                                        View windowRootView,
                                        View assistRootView, View mostBottomView) {
        if (currentActivity == null || windowRootView == null) return;
        Rect rootViewBounds = getRootViewBounds(currentActivity, isKeyboardOpen, windowRootView);
        int width = rootViewBounds.width();
        int height = rootViewBounds.height();
        int stableInsetBottom = AppUtils.getInsetBottom(currentActivity);
        if (isKeyboardOpen) {
            final Rect effectiveBounds = AppUtils.getEffectiveBounds(currentActivity);
            int statusHeight = LeapAUICache.getStatusBarHeight(currentActivity);

            final Rect leapIconBounds;

            int softInputMode = getCurrentActivitySoftInputMode(currentActivity);
            boolean isSoftInputModeAdjustPan = isSoftInputModeAdjustPan(softInputMode);
            if (isSoftInputModeAdjustPan) {
                leapIconBounds = AssistBoundsHelper.getGlobalViewLocation(windowRootView, mostBottomView);
            } else {
                leapIconBounds = AssistBoundsHelper.getAssistLocation(windowRootView, mostBottomView);
            }

            if (!AssistUtils.isVisualAboveKeyboard(leapIconBounds, effectiveBounds, statusHeight)) {
                setRootLayoutParams(assistRootView, width, height);
                stableInsetBottom = 0;
            }
        } else {
            setRootLayoutParams(assistRootView, width, height);
        }
        assistRootView.setPadding(0, 0, 0, stableInsetBottom);
    }


    private static void setRootLayoutParams(View assistView, int width, int height) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) assistView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(width, height);
        } else {
            layoutParams.width = width;
            layoutParams.height = height;
        }
        assistView.setLayoutParams(layoutParams);
    }

    public static boolean isDialogType(Integer type) {
        return type != null && type == WindowManager.LayoutParams.TYPE_APPLICATION;
    }

    public static boolean isLeapDialog(View view) {
        return view != null && view.findViewById(is.leap.android.core.R.id.leap_dialog_root) != null;
    }

    public static ImageView getImageView(Context context, int drawableResId) {
        ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(context.getResources().getDrawable(drawableResId));
        return imageView;
    }

    public static void setContentDescription(View view, String accessibilityText){
        if (view == null) return;
        if(StringUtils.isNullOrEmpty(accessibilityText)) return;
        LeapAUILogger.debugAUI("Accessibility Text for: " + accessibilityText);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        view.setContentDescription(accessibilityText);
    }
}
