package is.leap.android.aui.ui.assist.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface ArrowAction {
    int KEYBOARD = 1;
    int SCROLL = 2;
}
