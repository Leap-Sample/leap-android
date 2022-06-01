package is.leap.android.aui.manager;

import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;

import is.leap.android.aui.ui.listener.IUIChangeHolder;
import is.leap.android.aui.util.AppUtils;

public class KeyboardVisibilityManager {
    private final KeyboardVisibilityListener keyboardVisibilityListener;
    private final IUIChangeHolder uiChangeHolder;
    private WeakReference<View> rootViewRef;

    private final ViewTreeObserver.OnGlobalLayoutListener globalLayoutChangeListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            boolean keyboardOpen = AppUtils.isKeyboardOpen(uiChangeHolder.getCurrentActivity());
            if (isKeyBoardOpen != keyboardOpen) {
                isKeyBoardOpen = keyboardOpen;
                keyboardVisibilityListener.onKeyboardToggled(keyboardOpen);
            }
        }
    };
    private boolean isKeyBoardOpen;

    public KeyboardVisibilityManager(KeyboardVisibilityListener keyboardVisibilityListener,
                                     IUIChangeHolder uiChangeHolder) {
        this.keyboardVisibilityListener = keyboardVisibilityListener;
        this.uiChangeHolder = uiChangeHolder;
        isKeyBoardOpen = AppUtils.isKeyboardOpen(uiChangeHolder.getCurrentActivity());
    }

    public void start() {
        // Same root view? return
        View prevRootView = getRootView();
        View currentRootView = uiChangeHolder.getRootView();
        if (prevRootView != null) {
            if (prevRootView == currentRootView) return;
            // Different root view? stop previous one
            stop();
        }
        if (currentRootView == null) return;
        rootViewRef = new WeakReference<>(currentRootView);
        ViewTreeObserver viewTreeObserver = currentRootView.getViewTreeObserver();
        if (viewTreeObserver.isAlive())
            viewTreeObserver.addOnGlobalLayoutListener(globalLayoutChangeListener);
    }

    private View getRootView() {
        return AppUtils.getView(rootViewRef);
    }

    public void stop() {
        View rootView = getRootView();
        if (rootView == null) return;
        ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        if (viewTreeObserver.isAlive())
            viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutChangeListener);
        rootViewRef.clear();
    }

    public interface KeyboardVisibilityListener {
        void onKeyboardToggled(boolean keyboardDetected);
    }
}
