package is.leap.android.aui.ui.assist.listener;

import android.graphics.Rect;

import is.leap.android.aui.ui.assist.annotations.ArrowAction;
import is.leap.android.aui.ui.assist.annotations.AssistOutBoundSide;

public interface AssistBoundListener {

    void onBoundsCalculated(Rect assistLocation);

    void onInBound(Rect assistLocation);

    void onOutBound(@AssistOutBoundSide int outBoundSide, @ArrowAction int arrowAction, Rect assistLocation);

}