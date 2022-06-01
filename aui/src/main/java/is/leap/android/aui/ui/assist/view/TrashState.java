package is.leap.android.aui.ui.assist.view;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface TrashState {
    int TRASH_COLLECTED = 0;
    int TRASH_ICON_INTERSECTED = 1;
    int NONE = 2;
    int NORMAL = 3;
}
