package is.leap.android.aui.ui.assist;

import android.view.View;
import android.view.ViewGroup;

import java.util.Map;
import java.util.Set;

import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.listener.AssistActionListener;
import is.leap.android.aui.ui.assist.listener.WebAnchorElementClickListener;
import is.leap.android.aui.ui.listener.AssistAnimationListener;
import is.leap.android.aui.ui.listener.AssistDisplayListener;
import is.leap.android.aui.ui.listener.IUIChangeHolder;
import is.leap.android.aui.ui.listener.InstructionListener;
import is.leap.android.aui.ui.sound.SoundListener;
import is.leap.android.aui.ui.sound.SoundManager;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.Constants;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.AssistInfo;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.Instruction;
import is.leap.android.core.data.model.SoundInfo;
import is.leap.android.core.data.model.WebContentAction;
import is.leap.android.core.util.StringUtils;

public class InstructionManager implements AssistActionListener, SoundListener,
        WebAnchorElementClickListener, AssistAnimationListener, AssistDisplayListener,
        AssistManager.InstructionChangeListener {

    private final AssistManager assistManager;
    private final SoundManager soundManager;
    private final InstructionListener instructionListener;
    private Instruction instruction;
    private String auiExpType;
    private final AssistDisplayListener assistDisplayListener;
    private final AppExecutors appExecutors;
    private final Runnable autoDismissAUIRunnable = new Runnable() {
        @Override
        public void run() {
            appExecutors.mainThread().post(new Runnable() {
                @Override
                public void run() {
                    assistManager.reset();
                }
            });
            soundManager.setSoundCompleted();
            instructionListener.onAssistDismissed(auiExpType, instruction);
        }
    };
    private String audioLocale;

    public InstructionManager(InstructionListener instructionListener,
                              AssistDisplayListener assistDisplayListener,
                              AppExecutors appExecutors, IUIChangeHolder uiChangeHolder) {
        this.instructionListener = instructionListener;
        this.assistDisplayListener = assistDisplayListener;
        this.appExecutors = appExecutors;
        assistManager = new AssistManager(appExecutors,
                this, this, this,
                this, uiChangeHolder, this);
        soundManager = new SoundManager(this);
    }

    public void show(String auiExpType, String audioLocale) {
        show(auiExpType, LeapAUICache.iconSettings, audioLocale, null);
    }

    public void show(String auiExpType, IconSetting iconSetting, String audioLocale,
                     Set<String> flowProjectIds) {
        this.audioLocale = audioLocale;
        if (instruction == null) return;
        this.auiExpType = auiExpType;
        assistManager.reset();
        removeAutoDismissRunnable();
        if (instruction.assistInfo == null) {
            playSound(audioLocale);
            instructionListener.onInstructionVisible(auiExpType, instruction);
            return;
        }
        AssistInfo assistInfo = instruction.assistInfo;
        assistManager.initAssist(assistInfo, instruction.contentFileUriMap, iconSetting, audioLocale,
                flowProjectIds, instruction.shouldTrackTouchOnAUI());
        handleAssist(assistInfo);
    }

    private void handleAssist(AssistInfo assistInfo) {
        if (assistInfo == null) return;

        // handle common to both web and native
        if (assistManager.isNonAnchorAssist()) {
            assistManager.removeArrowOutput();
            assistManager.show();
            return;
        }

        if (assistInfo.isWeb) {
            assistManager.handleAssistWebContext();  // activityWindowType not needed here
            return;
        }
        View pointerView = instruction.getPointerView();
        if (pointerView == null) return;
        assistManager.handleAssist(pointerView);
    }

    private void playSound(String audioLocale) {
        if (instruction.isAssistClickType()) return;
        Map<String, SoundInfo> soundInfoMap = instruction.soundInfoMap;
        if (soundInfoMap == null || soundInfoMap.isEmpty()) return;
        SoundInfo soundInfo = soundInfoMap.get(audioLocale);
        if (soundInfo == null) return;
        soundManager.play(soundInfo, audioLocale);
    }

    public void reset() {
        this.instruction = null;
        removeAutoDismissRunnable();
        assistManager.reset();
    }

    @Override
    public void onAudioStarted() {
        instructionListener.onAudioStarted();
    }

    @Override
    public void onAudioFinished() {
        instructionListener.onAudioFinish();

        if (instruction != null && instruction.getAutoDismissDelay() > 0) {
            //Delay auto dismiss after audio finish
            appExecutors.bgThread()
                    .setDelay(instruction.getAutoDismissDelay())
                    .executeDelayed(autoDismissAUIRunnable);
            return;
        }
        soundManager.setSoundCompleted();
        if (Constants.AUIExperienceType.DISCOVERY.equals(auiExpType)) return;
        if (instruction != null && instruction.dismissible) {
            assistManager.hideAssist();
            assistManager.resetOutput();
        }
        instructionListener.onAssistDismissed(auiExpType, instruction);
    }

    public void onKeyboardToggled() {
        if (instruction == null) return;
        if (instruction.assistInfo == null) return;
        //TODO If assist is dismissed don't call handleAssist()
        handleAssist(instruction.assistInfo);
    }

    @Override
    public void onAssistActionPerformed(String actionType) {
        onAssistActionPerformed(actionType, null);
    }

    @Override
    public void onAssistActionPerformed(String actionType, Object value) {
        removeAutoDismissRunnable();
        if (!Constants.AUIExperienceType.DISCOVERY.equals(auiExpType)) {
            if (EventConstants.ANCHOR_CLICK.equals(actionType)) {
                if (instruction != null && instruction.assistInfo != null
                        && !instruction.assistInfo.isDismissibleOnAnchorClick()) {
                    return;
                }
                assistManager.reset();
            }
        }
        if (instruction != null && instruction.dismissibleOnUserInput) {
            assistManager.hideAssist();
            assistManager.resetOutput();
        }
        stopSound();
        instructionListener.onAssistActionTaken(auiExpType, instruction, actionType);
    }

    @Override
    public void onAssistActionPerformed(WebContentAction webContentAction) {
        removeAutoDismissRunnable();
        if (webContentAction == null) return;
        if (webContentAction.endFlow && !Constants.AUIExperienceType.ASSIST.equals(auiExpType)) {
            assistManager.stopListeners();
            instructionListener.onEndFlowFromAUI(auiExpType, webContentAction);
            return;
        }
        if (instruction != null && instruction.dismissibleOnUserInput) {
            assistManager.hideAssist();
            assistManager.resetOutput();
        }
        instructionListener.onAssistActionTaken(auiExpType, webContentAction);
    }

    @Override
    public void onWebAnchorElementClick() {
        removeAutoDismissRunnable();
        instructionListener.onAssistActionTaken(auiExpType, instruction, null);
    }

    private void removeAutoDismissRunnable() {
        appExecutors.bgThread().removeCallbacks(autoDismissAUIRunnable);
    }

    public void onActivityPause() {
        reset();
        assistManager.onActivityPause();
    }

    @Override
    public void onEntryAnimationStarted() {
        if (instruction == null) return;
        instructionListener.onInstructionVisible(auiExpType, instruction);
        if (StringUtils.isNotNullAndNotEmpty(instruction.soundName)) {
            playSound(audioLocale);
            return;
        }
        if (instruction.getAutoDismissDelay() > 0) {
            //Delay auto dismiss after audio finish
            appExecutors.bgThread()
                    .setDelay(instruction.getAutoDismissDelay())
                    .executeDelayed(autoDismissAUIRunnable);
        }
    }

    @Override
    public void onIndependentIconAnimationCanStart(String assistType) {
        if (Constants.AUIExperienceType.ASSIST.equals(auiExpType)) return;
        instructionListener.onIndependentIconAnimationCanStart(assistType);
    }

    @Override
    public void onEntryAnimationFinished() {

    }

    @Override
    public void onExitAnimationStarted() {

    }

    @Override
    public void onExitAnimationFinished() {
        stopSound();
        instructionListener.onAudioFinish();
    }

    public void stopSound() {
        soundManager.stop();
    }

    @Override
    public void onAnchorAssistDetected(float elevation) {
        if (assistDisplayListener != null)
            assistDisplayListener.onAnchorAssistDetected(elevation);
    }

    @Override
    public void onDialogAssistDetected(ViewGroup dialogRoot) {
        if (!Constants.AUIExperienceType.FLOW.equals(auiExpType)) return;
        if (Instruction.isAssistNotDailogType(instruction)) return;
        if (assistDisplayListener != null) assistDisplayListener.onDialogAssistDetected(dialogRoot);
    }

    public void resetPreviousSound() {
        soundManager.resetPreviousSound();
    }

    public void setInstruction(Instruction instruction) {
        if (this.instruction != instruction) resetPreviousSound();
        this.instruction = instruction;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public ViewGroup getDialogAssistRootView() {
        return assistManager.getDialogAssistRootView();
    }

    public boolean isDialogAssist() {
        return assistManager.isDialog();
    }

    public void updateAnchorView(View newAnchorView) {
        assistManager.updateAnchorView(newAnchorView);
    }

    @Override
    public View getCurrentScrollView() {
        if (instruction == null) return null;
        return instruction.getScrollView();
    }

    @Override
    public View getCurrentAppBarLayout() {
        if (instruction == null) return null;
        return instruction.getAppBarLayoutView();
    }
}
