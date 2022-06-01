package is.leap.android.aui.ui.assist.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.util.AppUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static is.leap.android.aui.ui.assist.view.HighlightDescView.ConnectorAlignment.BOTTOM;
import static is.leap.android.aui.ui.assist.view.HighlightDescView.ConnectorAlignment.TOP;
import static is.leap.android.core.Constants.ExtraProps.NONE;
import static is.leap.android.core.Constants.ExtraProps.DASH_GAP;
import static is.leap.android.core.Constants.ExtraProps.DASH_GAP_WITH_CIRCLE;
import static is.leap.android.core.Constants.ExtraProps.SOLID_WITH_CIRCLE;

public class HighlightDescView extends HighlightView implements LeapCustomViewGroup {

    public static final String DEFAULT_CONNECTOR_COLOR = "#FFFFFF";
    public static final int DEFAULT_CONNECTOR_LENGTH = 40;
    public static final int DEFAULT_INVISIBLE_CONNECTOR_LENGTH = 12;
    public static final int DEFAULT_CONNECTOR_CIRCLE = 3;
    public static final int DEFAULT_CONNECTOR_DASH = 5;
    public static final int DEFAULT_CONNECTOR_GAP = 5;
    private Rect oldBounds;
    final Paint paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);

    private LeapWebView leapWebView;
    private String connectorType;
    private int connectorLength;
    private String connectorColor = DEFAULT_CONNECTOR_COLOR;
    private int startX;
    private int startY;
    @ConnectorAlignment
    private int connectorAlignment;
    private int stopY;
    private float circleRadius;

    public HighlightDescView(Context context) {
        super(context);
        addDescLayout();
        setUpDefaultValues();
    }

    public void setConnectorLineProps(int startX, int startY,
                                      @ConnectorAlignment int connectorAlignment) {
        this.startX = startX;
        this.startY = startY;
        this.connectorAlignment = connectorAlignment;
        calculateConnectorEnd(connectorLength);
    }

    public void setProps(String connectorType, int connectorLength, String connectorColor) {
        this.connectorType = (connectorType == null || connectorType.isEmpty())
                ? SOLID_WITH_CIRCLE
                : connectorType;

        int defaultConnectorLength =
                NONE.equals(connectorType)
                        ? DEFAULT_INVISIBLE_CONNECTOR_LENGTH
                        : DEFAULT_CONNECTOR_LENGTH;
        this.connectorLength = connectorLength != -1
                ? AppUtils.dpToPxInt(getContext(), connectorLength)
                : AppUtils.dpToPxInt(getContext(), defaultConnectorLength);
        this.connectorColor = (connectorColor == null || connectorColor.isEmpty())
                ? DEFAULT_CONNECTOR_COLOR
                : connectorColor;
        // Update paint
        updateConnectorPaint();
    }

    private void calculateConnectorEnd(int connectorLength) {
        switch (connectorAlignment) {
            case TOP:
                stopY = startY - connectorLength;
                break;
            case BOTTOM:
            default:
                stopY = startY + connectorLength;
        }
    }

    private void updateConnectorPaint() {
        if (NONE.equals(connectorType)) return;
        paintLine.setColor(Color.parseColor(connectorColor));
        paintCircle.setColor(Color.parseColor(connectorColor));
        if (DASH_GAP_WITH_CIRCLE.equals(connectorType) || DASH_GAP.equals(connectorType)) {
            paintLine.setPathEffect(new DashPathEffect(new float[]{DEFAULT_CONNECTOR_DASH, DEFAULT_CONNECTOR_GAP}, 0f));
        }
    }

    private void setUpDefaultValues() {
        // Setup paint circle
        paintCircle.setStyle(Paint.Style.FILL);
        // Setup paint line
        paintLine.setStyle(Paint.Style.FILL);
        //
        Context context = getContext();
        circleRadius = AppUtils.dpToPx(context, DEFAULT_CONNECTOR_CIRCLE);
    }

    public LeapWebView getLeapWebView() {
        return leapWebView;
    }

    private void addDescLayout() {
        setupView(getContext());
        this.addView(this.leapWebView);
        FrameLayout.LayoutParams layoutParams = (LayoutParams) leapWebView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        leapWebView.setLayoutParams(layoutParams);
    }

    @Override
    public void setupView(Context context) {
        leapWebView = new LeapWebView(context);
    }

    @Override
    public void updateBounds(final Rect newBounds) {
        if (!AppUtils.isBoundsSame(this.oldBounds, newBounds)) {
            hideDescView();
        }
        super.updateBounds(newBounds);
        this.oldBounds = newBounds;
    }

    private void hideDescView() {
        leapWebView.setVisibility(INVISIBLE);
    }

    private void showDescView() {
        leapWebView.setVisibility(VISIBLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (NONE.equals(connectorType)) return;

        canvas.drawLine(startX, startY, startX, stopY, paintLine);
        if (DASH_GAP_WITH_CIRCLE.equals(connectorType) || SOLID_WITH_CIRCLE.equals(connectorType)) {
            canvas.drawCircle(startX, stopY, circleRadius, paintCircle);
        }
    }

    @Override
    public void hide() {
        super.hide();
        hideDescView();
    }

    @Override
    public void show() {
        super.show();
        showDescView();
    }

    public void updateContentLayout(int pageWidth, int pageHeight) {
        LayoutParams layoutParams = (LayoutParams) leapWebView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LayoutParams(pageWidth, pageHeight);
        } else {
            layoutParams.width = pageWidth;
            layoutParams.height = pageHeight;
        }
        leapWebView.setLayoutParams(layoutParams);
    }

    public void updateContentLayout(int pageWidth) {
        ViewGroup.LayoutParams layoutParams = leapWebView.getLayoutParams();
        layoutParams.width = pageWidth;
        leapWebView.setLayoutParams(layoutParams);
    }

    public void updateDescLayoutParams(Rect descViewPosition, Rect rootViewBounds) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) leapWebView.getLayoutParams();
        switch (connectorAlignment) {
            case TOP: {
                layoutParams.setMargins(descViewPosition.left, 0, 0, rootViewBounds.bottom - descViewPosition.bottom);
                layoutParams.gravity = Gravity.START | Gravity.BOTTOM;
                break;
            }
            case BOTTOM: {
                layoutParams.setMargins(descViewPosition.left, descViewPosition.top, 0, 0);
                layoutParams.gravity = Gravity.START | Gravity.TOP;
                break;
            }
        }
        leapWebView.setLayoutParams(layoutParams);
        contentRect = descViewPosition;
    }

    public int getTotalConnectorLength() {
        if (NONE.equals(connectorType)) return connectorLength;
        if (DASH_GAP_WITH_CIRCLE.equals(connectorType) || SOLID_WITH_CIRCLE.equals(connectorType))
            return (int) (connectorLength + circleRadius);
        return connectorLength;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectorAlignment {
        int LEFT = 1;
        int RIGHT = 2;
        int TOP = 3;
        int BOTTOM = 4;
        int CENTER = 5;
    }

}
