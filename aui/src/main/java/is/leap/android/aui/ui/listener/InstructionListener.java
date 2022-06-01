package is.leap.android.aui.ui.listener;

import is.leap.android.core.data.model.Instruction;
import is.leap.android.core.data.model.WebContentAction;

public interface InstructionListener {

    void onAssistDismissed(String auiExpType, Instruction instruction);

    void onAssistActionTaken(String auiExpType, Instruction instruction, String actionType);

    void onEndFlowFromAUI(String auiExpType, WebContentAction webContentAction);

    void onAssistActionTaken(String auiExpType, WebContentAction contentAction);

    void onInstructionVisible(String auiExpType, Instruction instruction);

    void onIndependentIconAnimationCanStart(String assistType);

    void onAudioStarted();

    void onAudioFinish();
}
