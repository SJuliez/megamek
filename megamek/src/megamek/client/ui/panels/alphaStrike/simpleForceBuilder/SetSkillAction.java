package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.alphaStrike.conversion.ASConverter;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

class SetSkillAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;
    private final int skill;

    SetSkillAction(SimpleASForceBuilder forceBuilder, int newSkill) {
        super("" + newSkill);
        this.forceBuilder = forceBuilder;
        this.skill = newSkill;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int row : forceBuilder.unitTable.getSelectedRows()) {
            AlphaStrikeElement element = forceBuilder.model.getUnitAt(row);
            element.setSkill(skill);
            ASConverter.updateCalculatedValues(element);
            forceBuilder.model.fireTableDataChanged();
        }
    }
}
