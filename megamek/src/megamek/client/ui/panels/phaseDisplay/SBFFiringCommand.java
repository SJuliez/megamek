package megamek.client.ui.panels.phaseDisplay;

import megamek.client.ui.Messages;
import megamek.common.strategicBattleSystems.SBFFormation;

import java.util.function.Predicate;

enum SBFFiringCommand implements StatusBarPhaseDisplay.PhaseCommand {
    FIRE_NEXT("moveNext"),
    FIRE_PREVIOUS("movePrevious"),
    //        FIRE_MORE("MoveMore"),
    FIRE_UNIT("fireunit");

    private final String cmd;
    private int priority;

    SBFFiringCommand(String c) {
        this(c, formation -> true);
    }

    SBFFiringCommand(String c, Predicate<SBFFormation> isEligible) {
        cmd = c;
        priority = ordinal();
    }

    @Override
    public String getCmd() {
        return cmd;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int p) {
        priority = p;
    }

    @Override
    public String toString() {
        return Messages.getString("SBFFiringDisplay." + getCmd());
    }
}
