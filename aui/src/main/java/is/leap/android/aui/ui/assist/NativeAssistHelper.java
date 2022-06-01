package is.leap.android.aui.ui.assist;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;

import is.leap.android.aui.ui.assist.listener.AssistBoundListener;
import is.leap.android.aui.ui.listener.IUIChangeHolder;
import is.leap.android.core.AppExecutors;

import static is.leap.android.aui.util.AppUtils.getView;

public class NativeAssistHelper {

    private final AppExecutors.ThreadHandler bgThread;
    private final AssistBoundListener boundListener;
    private final IUIChangeHolder uiChangeHolder;
    private WeakReference<View> pointerViewRef;
    private Rect assistLocation = new Rect();

    private final Runnable checkBoundsRunnable = new Runnable() {
        @Override
        public void run() {
            View pointerView = getPointerView();
            if (pointerView == null || pointerView.getVisibility() != View.VISIBLE) return;
            View rootView = uiChangeHolder.getRootView();
            if (rootView == null) return;
            final Rect assistLocation = AssistBoundsHelper.getAssistLocation(rootView, pointerView);
            if (assistLocation == null) return;
            if (NativeAssistHelper.this.assistLocation != null
                    && !NativeAssistHelper.this.assistLocation.isEmpty() &&
                    NativeAssistHelper.this.assistLocation.equals(assistLocation)) return;
            NativeAssistHelper.this.assistLocation = assistLocation;
            AssistBoundsHelper.checkBounds(uiChangeHolder.getCurrentActivity(), rootView, pointerView, assistLocation, boundListener);
        }
    };

    NativeAssistHelper(AppExecutors appExecutors, AssistBoundListener boundListener,
                       IUIChangeHolder uiChangeHolder) {
        bgThread = appExecutors.bgThread();
        this.boundListener = boundListener;
        this.uiChangeHolder = uiChangeHolder;
    }

    private View getPointerView() {
        return getView(pointerViewRef);
    }

    private final ViewTreeObserver.OnDrawListener drawListener
            = new ViewTreeObserver.OnDrawListener() {
        @Override
        public void onDraw() {
            bgThread.post(checkBoundsRunnable);
        }
    };

    private final ViewTreeObserver.OnScrollChangedListener scrollChangedListener
            = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            bgThread.post(checkBoundsRunnable);
        }
    };

    public void stop() {
        assistLocation = new Rect();
        bgThread.removeCallbacks(checkBoundsRunnable);
        View rootView = uiChangeHolder.getRootView();
        if (rootView != null) {
            ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
            if (viewTreeObserver.isAlive())
                viewTreeObserver.removeOnScrollChangedListener(scrollChangedListener);
        }

        View pointerView = getPointerView();
        if (pointerView != null) {
            ViewTreeObserver viewTreeObserver = pointerView.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                try {
                    viewTreeObserver.removeOnDrawListener(drawListener);
                } catch (IllegalStateException ignored) { }
            }
        }
    }

    // We have the assistLocation ,
    // we need check bounds - inside visible or outside
    void detectViewInBounds(View rootView, View pointerView) {
        // remove previous listeners if any
        stop();
        if (pointerView == null) return;
        Rect assistLocation = AssistBoundsHelper.getAssistLocation(rootView, pointerView);
        boundListener.onBoundsCalculated(assistLocation);
    }

    public void checkGlobalUpdateBounds(View rootView, View pointerView, Rect assistLocation) {
        this.pointerViewRef = new WeakReference<>(pointerView);
        if (assistLocation != null) this.assistLocation = assistLocation;
        // Start listening to layout change so that layout bounds can be updated
        ViewTreeObserver rootViewTreeObserver = rootView.getViewTreeObserver();
        if (rootViewTreeObserver.isAlive())
            rootViewTreeObserver.addOnScrollChangedListener(scrollChangedListener);

        // Draw Listener is required only for pointer whenever pointer animates
        if (pointerView != null) {
            ViewTreeObserver viewTreeObserver = pointerView.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                try {
                    viewTreeObserver.addOnDrawListener(drawListener);
                } catch (IllegalStateException ignored) { }
            }
        }

        AssistBoundsHelper.checkBounds(uiChangeHolder.getCurrentActivity(), rootView, pointerView, assistLocation, boundListener);
    }

}
