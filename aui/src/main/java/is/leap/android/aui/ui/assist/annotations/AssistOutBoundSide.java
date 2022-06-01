package is.leap.android.aui.ui.assist.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface AssistOutBoundSide {
    int NONE = 1;
    int TOP= 2;
    int BOTTOM = 3;
}
