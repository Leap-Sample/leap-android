package is.leap.android.aui.ui;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.view.TrashState;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.AppExecutors;

import static is.leap.android.aui.AUIConstants.DEFAULT_MARGIN_20;
import static is.leap.android.aui.AUIConstants.DEFAULT_MARGIN_5;

public class IconDragTouch implements View.OnTouchListener {

    private static final int DRAG_CLICK_MARGIN = DEFAULT_MARGIN_5;
    private float dY = 0f;
    private float dX = 0f;
    private float prevViewY = -1;
    private float prevViewX = -1;
    private boolean hasMoved = false;
    private boolean isLongPressed = false;
    private int screenHeight = LeapAUICache.screenHeight;
    private final AppExecutors appExecutors;
    private final int iconSize;
    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            isLongPressed = true;
        }
    };
    private final DragListener dragListener;
    private int screenWidth = LeapAUICache.screenWidth;
    private boolean dismissible = false;
    private View delegateOnClickListenerView;
    private boolean leftAlign;

    public IconDragTouch(DragListener dragListener, AppExecutors appExecutors, int iconSize) {
        this.dragListener = dragListener;
        this.appExecutors = appExecutors;
        this.iconSize = iconSize;
    }

    public void setUpdatedWidth(int updatedWidth) {
        this.screenWidth = updatedWidth;
    }

    public void setUpdatedHeight(int updatedHeight) {
        this.screenHeight = updatedHeight;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                prevViewY = view.getY();
                prevViewX = view.getX();
                hasMoved = false;
                dY = view.getY() - event.getRawY();
                dX = view.getX() - event.getRawX();
                appExecutors.mainThread().postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
                return true;
            case MotionEvent.ACTION_MOVE:
                float newY = event.getRawY() + dY;
                if (prevViewY != -1 && (newY > prevViewY + DRAG_CLICK_MARGIN || newY < prevViewY - DRAG_CLICK_MARGIN))
                    hasMoved = true;

                // Check if view is in not in visible bound so that it cannot be dragged anymore.
                newY = getNewY(view, event);
                float newX = getNewX(view, event);
                animate(view, newY, newX);
                if (dismissible) {
                    if (dragListener.isTrashAndLeapIconIntersecting()) {
                        dragListener.onTrash(TrashState.TRASH_ICON_INTERSECTED);
                    } else if (isLongPressed) {
                        dragListener.onTrash(TrashState.NORMAL);
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                final boolean tmpHasMoved = hasMoved;
                final boolean tmpIsLongPressed = isLongPressed;

                if (!tmpIsLongPressed && !tmpHasMoved) {
                    newY = getNewY(view, event);
                    animate(view, newY, dragListener.getDefaultIconXValue());
                    if (dismissible) dragListener.onTrash(TrashState.NONE);
                    if (delegateOnClickListenerView == null)
                        delegateOnClickListenerView = view;
                    delegateOnClickListenerView.performClick();
                } else {
                    if (tmpHasMoved) {
                        dragListener.onDragged();
                        if (dismissible) {
                            if (dragListener.isTrashAndLeapIconIntersecting()) {
                                dragListener.onTrash(TrashState.TRASH_COLLECTED);
                                animate(view, dragListener.getDefaultIconYValue(), dragListener.getDefaultIconXValue());
                                reset();
                                return true;
                            }
                        }
                    }

                    // When tmpHasMoved is sometimes false
                    // Make sure the icon is reset.
                    if (dismissible) dragListener.onTrash(TrashState.NONE);
                    newY = getNewY(view, event);
                    newX = event.getRawX() + dX;
                    if (newX != prevViewX || newY != prevViewY)
                        animate(view, newY, dragListener.getDefaultIconXValue(), 400);
                }

                reset();
                return true;
            case MotionEvent.ACTION_CANCEL:
                reset();
                return false;
        }
        return false;
    }

    private float getNewY(View view, MotionEvent event) {
        float newY = event.getRawY() + dY;
        int _20dpMargin = AppUtils.dpToPxInt(view.getContext(), DEFAULT_MARGIN_20);
        if (newY <= _20dpMargin) {
            newY = _20dpMargin;
        }

        int rightMost = screenHeight - iconSize - _20dpMargin;
        if (newY >= rightMost) {
            newY = rightMost;
        }
        return newY;
    }

    private float getNewX(View view, MotionEvent event) {
        float newX = event.getRawX() + dX;
        int _20dpMargin = AppUtils.dpToPxInt(view.getContext(), DEFAULT_MARGIN_20);

        if (leftAlign) {
            if (newX <= _20dpMargin) {
                newX = _20dpMargin;
            }

            int rightMost = screenWidth - iconSize - _20dpMargin;
            if (newX >= rightMost) {
                newX = rightMost;
            }
        } else {
            if (newX - dX - iconSize / 2f <= _20dpMargin) {
                newX = dX + _20dpMargin + iconSize / 2f;
            }

            float rightMost = screenWidth - iconSize / 2f - _20dpMargin;
            if (newX - dX >= rightMost) {
                newX = dX + rightMost;
            }
        }
        return newX;
    }

    public void reset() {
        isLongPressed = false;
        hasMoved = false;
        prevViewY = -1;
        prevViewX = -1;
        appExecutors.stopMainRunnable(longPressRunnable);
    }

    public void resetToDefaultPos(View view) {
        animate(view, dragListener.getDefaultIconYValue(), dragListener.getDefaultIconXValue());
        reset();
    }

    private void animate(View view, float newY, float newX) {
        animate(view, newY, newX, 0);
    }

    private void animate(final View view, float newY, float newX, int duration) {
        view.animate()
                .x(newX)
                .y(newY)
                .setDuration(duration)
                .start();
    }

    public void setDismissible(boolean dismissible) {
        this.dismissible = dismissible;
    }

    // Delegates the OnClick listener to the provided view
    public void setDelegateOnClickListenerView(View delegateOnClickListenerView) {
        this.delegateOnClickListenerView = delegateOnClickListenerView;
    }

    public void setIsLeftAligned(boolean leftAlign) {
        this.leftAlign = leftAlign;
    }

    public interface DragListener {
        void onDragged();

        boolean isTrashAndLeapIconIntersecting();

        float getDefaultIconXValue();

        float getDefaultIconYValue();

        void onTrash(@TrashState int trashState);
    }
}
