package is.leap.android.aui.ui.assist.listener;

import is.leap.android.core.data.model.WebContentAction;

public interface AssistActionListener {
    void onAssistActionPerformed(String actionType);

    void onAssistActionPerformed(String actionType, Object value);

    void onAssistActionPerformed(WebContentAction webContentAction);

}
