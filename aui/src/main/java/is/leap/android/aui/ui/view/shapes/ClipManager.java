package is.leap.android.aui.ui.view.shapes;

import android.graphics.Paint;
import android.graphics.Path;

public interface ClipManager {

    Path createMask(int width, int height);

    Path getShadowConvexPath();

    void setupClipLayout(int width, int height);

    Paint getPaint();

    boolean requiresBitmap();
}
