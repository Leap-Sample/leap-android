package is.leap.android.aui.ui.assist;

import android.app.Activity;
import android.view.View;

import is.leap.android.aui.ui.assist.output.Assist;
import is.leap.android.aui.ui.assist.output.Beacon;
import is.leap.android.aui.ui.assist.output.BottomUp;
import is.leap.android.aui.ui.assist.output.Carousel;
import is.leap.android.aui.ui.assist.output.Delight;
import is.leap.android.aui.ui.assist.output.Drawer;
import is.leap.android.aui.ui.assist.output.Finger;
import is.leap.android.aui.ui.assist.output.FingerRipple;
import is.leap.android.aui.ui.assist.output.FullScreen;
import is.leap.android.aui.ui.assist.output.HighlightDescriptionAssist;
import is.leap.android.aui.ui.assist.output.InAppNotification;
import is.leap.android.aui.ui.assist.output.Label;
import is.leap.android.aui.ui.assist.output.Ping;
import is.leap.android.aui.ui.assist.output.Popup;
import is.leap.android.aui.ui.assist.output.SlideIn;
import is.leap.android.aui.ui.assist.output.Spot;
import is.leap.android.aui.ui.assist.output.Tooltip;

import static is.leap.android.core.Constants.Visual.ACTION_TYPE_CLICK;
import static is.leap.android.core.Constants.Visual.ANIMATION_SWIPE_DOWN;
import static is.leap.android.core.Constants.Visual.ANIMATION_SWIPE_LEFT;
import static is.leap.android.core.Constants.Visual.ANIMATION_SWIPE_RIGHT;
import static is.leap.android.core.Constants.Visual.ANIMATION_SWIPE_UP;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_BEACON;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_BOTTOMUP;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_CAROUSEL;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_DELIGHT;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_DRAWER;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_FINGER_RIPPLE;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_FULLSCREEN;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_HIGHLIGHT_WITH_DESC;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_LABEL;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_NONE;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_NOTIFICATION;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_PING;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_POPUP;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_SLIDE_IN;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_SPOT;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_SWIPE_DOWN;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_SWIPE_LEFT;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_SWIPE_RIGHT;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_SWIPE_UP;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_TOOLTIP;

class AssistFactory {

    static Assist makeAssist(Activity currentActivity, String pointerType, String accessibilityText,
                             View rootView) {
        switch (pointerType) {

            case VISUAL_TYPE_BEACON:
                return new Beacon(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_SWIPE_LEFT:
                Finger swipeLeft = new Finger(currentActivity, rootView, accessibilityText);
                swipeLeft.setPointerAnimationType(ANIMATION_SWIPE_LEFT);
                return swipeLeft;

            case VISUAL_TYPE_SWIPE_RIGHT:
                Finger swipeRight = new Finger(currentActivity, rootView, accessibilityText);
                swipeRight.setPointerAnimationType(ANIMATION_SWIPE_RIGHT);
                return swipeRight;

            case VISUAL_TYPE_SWIPE_UP:
                Finger swipeUp = new Finger(currentActivity, rootView, accessibilityText);
                swipeUp.setPointerAnimationType(ANIMATION_SWIPE_UP);
                return swipeUp;

            case VISUAL_TYPE_SWIPE_DOWN:
                Finger swipeDown = new Finger(currentActivity, rootView, accessibilityText);
                swipeDown.setPointerAnimationType(ANIMATION_SWIPE_DOWN);
                return swipeDown;

            case VISUAL_TYPE_FINGER_RIPPLE:
                return new FingerRipple(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_HIGHLIGHT_WITH_DESC:
                return new HighlightDescriptionAssist(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_SPOT:
                return new Spot(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_TOOLTIP:
                return new Tooltip(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_POPUP:
                return new Popup(currentActivity, accessibilityText);

            case VISUAL_TYPE_BOTTOMUP:
                return new BottomUp(currentActivity, accessibilityText);

            case VISUAL_TYPE_FULLSCREEN:
                return new FullScreen(currentActivity, accessibilityText);

            case VISUAL_TYPE_DELIGHT:
                return new Delight(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_DRAWER:
                return new Drawer(currentActivity, accessibilityText);

            case VISUAL_TYPE_LABEL:
                return new Label(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_PING:
                return new Ping(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_NOTIFICATION:
                return new InAppNotification(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_SLIDE_IN:
                return new SlideIn(currentActivity, rootView, accessibilityText);

            case VISUAL_TYPE_CAROUSEL:
                return new Carousel(currentActivity, accessibilityText);

            case VISUAL_TYPE_NONE:
            case ACTION_TYPE_CLICK:
            default:
                return null;
        }
    }

}
