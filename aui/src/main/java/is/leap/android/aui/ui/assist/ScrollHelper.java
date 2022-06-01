package is.leap.android.aui.ui.assist;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;

import is.leap.android.aui.LeapAUILogger;
import is.leap.android.aui.util.AppUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ScrollHelper {

    public static final float SCROLL_PERCENT = 0.30f;
    public static final float KEYBOARD_SCROLL_PERCENT = 0.50f;
    public static final int KEYBOARD_SCROLL_TO = -2;
    public static final int NORMAL_SCROLL = -1;

    static boolean autoScroll(Activity currentActivity, Rect viewLocation, View scrollContainer,
                              View appBarLayout, ScrollListener scrollListener, int scrollTo) {
        if (appBarLayout != null) {
            try {
                Method setExpanded = getAppBarLayoutSetExpanded(appBarLayout);
                if (setExpanded != null) {
                    setExpanded.setAccessible(true);
                    setExpanded.invoke(appBarLayout, true, true);
                    return true;
                }
            } catch (IllegalAccessException ignored) {
                LeapAUILogger.debugAUI("setExpanded not found. May be not a AppBarLayout. Ignored");
            } catch (InvocationTargetException ignored) {
                LeapAUILogger.debugAUI("setExpanded not found. May be not a AppBarLayout. Ignored");
            }
        }
        if (scrollContainer == null) return false;

        Rect effectiveBounds = AppUtils.getEffectiveBounds(currentActivity);

        // Calculate the location to scroll (30% of screen height)
        int _scrollToDistance;
        if(KEYBOARD_SCROLL_TO == scrollTo) {
            _scrollToDistance = (int) (effectiveBounds.top + (effectiveBounds.height() * KEYBOARD_SCROLL_PERCENT));
        } else if (NORMAL_SCROLL == scrollTo) {
            _scrollToDistance = (int) (effectiveBounds.top + (effectiveBounds.height() * SCROLL_PERCENT));
        } else {
            _scrollToDistance = scrollTo;
        }
        int viewMidY = viewLocation.top + viewLocation.height() / 2;

        //First check for Recycler View
        try {
            Method smoothScrollBy = getRecyclerSmoothScrollBy(scrollContainer);
            if (smoothScrollBy != null) {
                smoothScrollBy.setAccessible(true);
                smoothScrollBy.invoke(scrollContainer, 0, viewMidY - _scrollToDistance);
                return true;
            }
        } catch (IllegalAccessException ignored) {
            LeapAUILogger.debugAUI("smoothScrollBy not found. May be not a recyclerView. Ignored");
        } catch (InvocationTargetException ignored) {
            LeapAUILogger.debugAUI("smoothScrollBy not found. May be not a recyclerView. Ignored");
        }

        if (scrollListener != null) scrollListener.onScrollStart();
        int scrollY = scrollContainer.getScrollY();
            int yDistance = scrollY + viewMidY - _scrollToDistance;
        setScroll(scrollContainer, yDistance);

        if (scrollListener != null) scrollListener.onScrollStop();
        return true;
    }

    private static void setScroll(View scrollContainer, int yDistance) {
        if (scrollContainer instanceof ListView) {
            ListView listViewScrollContainer = (ListView) scrollContainer;
            listViewScrollContainer.smoothScrollBy(yDistance, 0);
        } else if (scrollContainer instanceof ScrollView) {
            ScrollView scrollViewScrollContainer = (ScrollView) scrollContainer;
            scrollViewScrollContainer.scrollTo(0, yDistance);
        } else if (scrollContainer instanceof NestedScrollView) {
            NestedScrollView nestedScrollViewScrollContainer = (NestedScrollView) scrollContainer;
            nestedScrollViewScrollContainer.scrollTo(0, yDistance);
        } else {
            scrollContainer.setScrollY(yDistance);
        }
    }

    private static Method getAppBarLayoutSetExpanded(View appbarLayout) {
        try {
            Class<? extends View> clazz = appbarLayout.getClass();
            // Check if smoothScrollToPosition exist before checking smoothScrollBy since smoothScrollBy also exist in other views
            return clazz.getMethod("setExpanded", boolean.class, boolean.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Method getRecyclerSmoothScrollBy(View scrollContainer) {
        if (scrollContainer == null || scrollContainer instanceof ScrollView
                || scrollContainer instanceof NestedScrollView || scrollContainer instanceof ListView)
            return null;
        try {
            Class<? extends View> scrollContainerClass = scrollContainer.getClass();
            // Check if smoothScrollToPosition exist before checking smoothScrollBy since smoothScrollBy also exist in other views
            scrollContainerClass.getMethod("smoothScrollToPosition", int.class);
            return scrollContainerClass.getMethod("smoothScrollBy", int.class, int.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public interface ScrollListener {
        void onScrollStart();

        void onScrollStop();
    }

}
