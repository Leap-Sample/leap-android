package is.leap.android.aui.ui.assist.view;

import android.graphics.Rect;

import static is.leap.android.aui.ui.assist.view.HighlightDescView.ConnectorAlignment.CENTER;
import static is.leap.android.aui.ui.assist.view.HighlightDescView.ConnectorAlignment.LEFT;
import static is.leap.android.aui.ui.assist.view.HighlightDescView.ConnectorAlignment.RIGHT;

public class HighlightDescViewPositioner {

    private Rect descViewBounds;
    private Rect screenBounds;
    private Rect anchorViewPosition;
    private float connectorLength;
    private int boundaryMargin;
    private int highlightPadding;
    @HighlightDescView.ConnectorAlignment
    private int alignment = HighlightDescView.ConnectorAlignment.BOTTOM;
    private Rect descViewPosition;

    public HighlightDescViewPositioner(Rect descViewBounds, Rect screenBounds,
                                       Rect anchorViewPosition, float connectorLength,
                                       int boundaryMargin, int highlightPadding,
                                       int statusBarHeight, int associatedIconHeight) {
        this.descViewBounds = descViewBounds;
        this.screenBounds = screenBounds;
        this.anchorViewPosition = anchorViewPosition;
        this.connectorLength = connectorLength;
        this.boundaryMargin = boundaryMargin;
        this.highlightPadding = highlightPadding;
        calculateConnectorAlignment(statusBarHeight, associatedIconHeight);
    }

    public int getConnectorStartY(@HighlightDescView.ConnectorAlignment int alignment) {
        if (HighlightDescView.ConnectorAlignment.BOTTOM == alignment) {
            return anchorViewPosition.bottom + highlightPadding;
        }
        return anchorViewPosition.top - highlightPadding;
    }

    public int getConnectorStartX() {
        return anchorViewPosition.left + anchorViewPosition.width() / 2;
    }

    private void calculateConnectorAlignment(int statusBarHeight, int associatedIconHeight) {
        int anchorTop = anchorViewPosition.top;
        if (highlightPadding > 0) anchorTop -= highlightPadding;
        int availableHeightInTop = anchorTop - screenBounds.top - statusBarHeight - boundaryMargin;
        if (availableHeightInTop > connectorLength + descViewBounds.height() + associatedIconHeight) {
            alignment = HighlightDescView.ConnectorAlignment.TOP;
            return;
        }
        alignment = HighlightDescView.ConnectorAlignment.BOTTOM;
    }

    public Rect getDescViewPosition() {
        descViewPosition = new Rect();
        int descViewHeight = descViewBounds.height();
        switch (alignment) {
            case HighlightDescView.ConnectorAlignment.BOTTOM:
                descViewPosition.top = (int) (connectorLength + anchorViewPosition.bottom + highlightPadding);
                descViewPosition.bottom = descViewPosition.top + descViewHeight;
                calculateHorizontalPosition();
                break;
            case HighlightDescView.ConnectorAlignment.TOP:
                descViewPosition.bottom = (int) (anchorViewPosition.top - connectorLength - highlightPadding);
                descViewPosition.top = descViewPosition.bottom - descViewHeight;
                calculateHorizontalPosition();
        }
        return descViewPosition;
    }

    private void calculateHorizontalPosition() {
        int toolTipWidth = descViewBounds.width();
        int horizontalAlignment = findHorizontalAlignment();
        switch (horizontalAlignment) {
            case LEFT:
                descViewPosition.left = screenBounds.left + boundaryMargin;
                descViewPosition.right = screenBounds.left + toolTipWidth + boundaryMargin;
                break;
            case RIGHT:
                descViewPosition.right = screenBounds.right - boundaryMargin;
                descViewPosition.left = screenBounds.right - toolTipWidth - boundaryMargin;
                break;
            case CENTER:
                descViewPosition.left = anchorViewPosition.centerX() - toolTipWidth / 2;
                descViewPosition.right = anchorViewPosition.centerX() + toolTipWidth / 2;
                break;
        }
        // Adjust tooltip bounds if it goes beyond left or right
        if (descViewPosition.right > screenBounds.right) {
            descViewPosition.right = (screenBounds.right - boundaryMargin);
        }
        if (descViewPosition.left < screenBounds.left) {
            descViewPosition.left = (screenBounds.left + boundaryMargin);
        }
    }

    private
    @HighlightDescView.ConnectorAlignment
    int findHorizontalAlignment() {
        int toolTipWidth = descViewBounds.width();
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

    @HighlightDescView.ConnectorAlignment
    public int getConnectorAlignment() {
        return alignment;
    }
}
