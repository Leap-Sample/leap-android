package is.leap.android.aui.ui.view.shapes;

import android.graphics.Rect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static is.leap.android.aui.ui.view.shapes.TooltipPositioner.TooltipAlignment.BOTTOM;
import static is.leap.android.aui.ui.view.shapes.TooltipPositioner.TooltipAlignment.CENTER;
import static is.leap.android.aui.ui.view.shapes.TooltipPositioner.TooltipAlignment.LEFT;
import static is.leap.android.aui.ui.view.shapes.TooltipPositioner.TooltipAlignment.RIGHT;
import static is.leap.android.aui.ui.view.shapes.TooltipPositioner.TooltipAlignment.TOP;

public class TooltipPositioner {

    Rect toolTipBounds;
    Rect screenBounds;
    Rect anchorViewPosition;
    private int highlightPadding;
    private @TooltipMaskView.TipAlignment
    int position;
    private Rect toolTipPosition;
    // Margin on the bounds of Tooltip View so that it doesn't touch screen left/top/right/bottom
    private int boundaryMargin;
    private int tipAlignment;

    public TooltipPositioner(Rect toolTipBounds, Rect screenBounds, Rect anchorViewPosition,
                             int boundaryMargin, int highlightPadding, int statusBarHeight,
                             int associatedIconHeight) {
        this.toolTipBounds = toolTipBounds;
        this.screenBounds = screenBounds;
        this.anchorViewPosition = anchorViewPosition;
        this.boundaryMargin = boundaryMargin;
        this.highlightPadding = highlightPadding;
        calculateTooltipPosition(statusBarHeight, associatedIconHeight);
    }

    private void calculateTooltipPosition(int statusBarHeight, int associatedIconHeight) {
        int anchorTop = anchorViewPosition.top;
        if (highlightPadding > 0) anchorTop -= highlightPadding;
        int availableHeightInTop = anchorTop - screenBounds.top - statusBarHeight - boundaryMargin;
        if (availableHeightInTop > toolTipBounds.height() + associatedIconHeight) {
            position = TooltipMaskView.TipAlignment.BOTTOM;
        } else {
            position = TooltipMaskView.TipAlignment.TOP;
        }
    }

    public Rect getTooltipPosition() {
        toolTipPosition = new Rect();
        int toolTipHeight = toolTipBounds.height();
        switch (position) {
            case TooltipMaskView.TipAlignment.TOP:
                toolTipPosition.top = anchorViewPosition.bottom + highlightPadding;
                toolTipPosition.bottom = toolTipPosition.top + toolTipHeight;
                calculateHorizontalPosition();
                break;
            case TooltipMaskView.TipAlignment.BOTTOM:
                toolTipPosition.bottom = anchorViewPosition.top - highlightPadding;
                toolTipPosition.top = toolTipPosition.bottom - toolTipHeight;
                calculateHorizontalPosition();
                break;
        }
        return toolTipPosition;
    }

    private void calculateHorizontalPosition() {
        int toolTipWidth = toolTipBounds.width();
        int horizontalAlignment = tipAlignment = findHorizontalAlignment();
        switch (horizontalAlignment) {
            case LEFT:
                toolTipPosition.left = screenBounds.left + boundaryMargin;
                toolTipPosition.right = screenBounds.left + toolTipWidth + boundaryMargin;
                break;
            case RIGHT:
                toolTipPosition.right = screenBounds.right - boundaryMargin;
                toolTipPosition.left = screenBounds.right - toolTipWidth - boundaryMargin;
                break;
            case CENTER:
                toolTipPosition.left = anchorViewPosition.centerX() - toolTipWidth / 2;
                toolTipPosition.right = anchorViewPosition.centerX() + toolTipWidth / 2;
                break;
        }
        // Adjust tooltip bounds if it goes beyond left or right
        if (toolTipPosition.right > (screenBounds.right - boundaryMargin)) {
            toolTipPosition.right = (screenBounds.right - boundaryMargin);
        }
        if (toolTipPosition.left < (screenBounds.left + boundaryMargin)) {
            toolTipPosition.left = (screenBounds.left + boundaryMargin);
        }
    }

    private
    @TooltipAlignment
    int findHorizontalAlignment() {
        int toolTipWidth = toolTipBounds.width();
        int anchorCentreX = anchorViewPosition.centerX();
        int screenCentreX = screenBounds.centerX();
        if (anchorCentreX > screenCentreX) {
            if ((screenBounds.right - anchorCentreX) >= toolTipWidth / 2) return CENTER;
            return RIGHT;
        }
        if (anchorCentreX < screenCentreX) {
            if ((anchorCentreX - screenBounds.left) >= toolTipWidth / 2) return CENTER;
            return LEFT;
        }
        return CENTER;
    }

    public int getTipPosition() {
        return position;
    }

    public float getTipX() {
        switch (position) {
            case TooltipMaskView.TipAlignment.BOTTOM:
            case TooltipMaskView.TipAlignment.TOP:
                if (RIGHT == tipAlignment)
                    return toolTipPosition.width() - (toolTipPosition.right - anchorViewPosition.centerX());
                else
                    return anchorViewPosition.centerX() - toolTipPosition.left;
        }

        return 0;
    }

    public float getTipY() {
        switch (position) {
            case TooltipMaskView.TipAlignment.BOTTOM:
                return anchorViewPosition.top;
            case TooltipMaskView.TipAlignment.TOP:
                return anchorViewPosition.bottom;
        }
        return 0;
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface TooltipAlignment {
        int LEFT = 0;
        int TOP = 1;
        int RIGHT = 2;
        int BOTTOM = 3;
        int CENTER = 4;
    }


}
