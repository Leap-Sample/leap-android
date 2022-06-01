package is.leap.android.aui.ui.listener;

import android.view.ViewGroup;

public interface AssistDisplayListener {

    void onAnchorAssistDetected(float elevation);

    void onDialogAssistDetected(ViewGroup dialogRoot);

}
