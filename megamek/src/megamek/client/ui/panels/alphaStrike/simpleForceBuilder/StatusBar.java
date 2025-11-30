package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.common.alphaStrike.ASDamage;
import megamek.common.alphaStrike.ASDamageVector;
import megamek.common.alphaStrike.ASUnitType;
import megamek.common.alphaStrike.AlphaStrikeElement;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

class StatusBar extends JPanel {

    private static final String NO_VALUE = "--";
    private static final int H_GAP = 15;

    private final SimpleASForceBuilder forceBuilder;
    private final SimpleASUnitTableModel model;

    private final JLabel totalPVLabel = new BadgeLabel(NO_VALUE);
    private final JLabel totalUnitsLabel = new BadgeLabel(NO_VALUE);
    private final JLabel averageSkillLabel = new BadgeLabel(NO_VALUE);
    private final JLabel totalDmgLabel = new BadgeLabel(NO_VALUE);

    private final JPanel unitTypesPanel = new JPanel();
    private Map<ASUnitType, Long> unitTypes = new HashMap<>();

    public StatusBar(SimpleASForceBuilder forceBuilder) {
        super(new FlowLayout(FlowLayout.RIGHT));
        this.forceBuilder = forceBuilder;
        model = forceBuilder.model;

        totalDmgLabel.setToolTipText("The sum of all damage values, excluding those of units that use Arcs. The E "
              + "range value is only shown when not 0.");

        setBorder(new EmptyBorder(5, 10, 5, 10));
        add(unitTypesPanel);
        add(Box.createHorizontalStrut(H_GAP));
        add(new JLabel("Total Dmg: "));
        add(totalDmgLabel);
        add(Box.createHorizontalStrut(H_GAP));
        add(new JLabel("Average Skill: "));
        add(averageSkillLabel);
        add(Box.createHorizontalStrut(H_GAP));
        add(new JLabel("Units: "));
        add(totalUnitsLabel);
        add(Box.createHorizontalStrut(H_GAP));
        add(new JLabel("PV: "));
        add(totalPVLabel);
    }

    void initialize() {
        forceBuilder.model.addTableModelListener(e -> update());
    }

    private void update() {
        updateTotalPV();
        updateTotalUnits();
        updateAverageSkill();
        updateDmgLabels();
        updateUnitTypes();
    }

    private void updateUnitTypes() {
        unitTypes.clear();
        unitTypes = forceBuilder.model.getUnits()
              .stream()
              .map(AlphaStrikeElement::getASUnitType)
              .collect(groupingBy(i -> i, counting()));
        unitTypesPanel.removeAll();
        for (ASUnitType type : unitTypes.keySet()) {
            if (unitTypes.get(type) > 0) {
                BadgeLabel label = new BadgeLabel("%d %s".formatted(unitTypes.get(type), type));
                unitTypesPanel.add(label);
            }
        }
    }

    private void updateDmgLabels() {
        int totalS = (int) model.getUnits().stream()
              .map(AlphaStrikeElement::getStandardDamage)
              .map(ASDamageVector::S)
              .mapToDouble(ASDamage::asDoubleValue)
              .sum();
        int totalM = (int) model.getUnits().stream()
              .map(AlphaStrikeElement::getStandardDamage)
              .map(ASDamageVector::M)
              .mapToDouble(ASDamage::asDoubleValue)
              .sum();
        int totalL = (int) model.getUnits().stream()
              .map(AlphaStrikeElement::getStandardDamage)
              .map(ASDamageVector::L)
              .mapToDouble(ASDamage::asDoubleValue)
              .sum();
        int totalE = (int) model.getUnits().stream()
              .map(AlphaStrikeElement::getStandardDamage)
              .map(ASDamageVector::E)
              .mapToDouble(ASDamage::asDoubleValue)
              .sum();
        ASDamageVector total = totalE == 0
              ? ASDamageVector.createNormRndDmg(totalS, totalM, totalL)
              : ASDamageVector.createNormRndDmg(totalS, totalM, totalL, totalE);
        totalDmgLabel.setText(total.toString());
    }

    private void updateAverageSkill() {
        double averageSkill = model.getUnits().stream().mapToInt(AlphaStrikeElement::getSkill).average().orElse(0);
        averageSkillLabel.setText(averageSkill == 0 ? NO_VALUE : "%1.1f".formatted(averageSkill));
    }

    private void updateTotalPV() {
        int totalPV = model.getUnits().stream().mapToInt(AlphaStrikeElement::getPointValue).sum();
        totalPVLabel.setText(totalPV == 0 ? NO_VALUE : Integer.toString(totalPV));
    }

    private void updateTotalUnits() {
        int totalUnits = model.getUnits().size();
        totalUnitsLabel.setText(totalUnits == 0 ? NO_VALUE : Integer.toString(totalUnits));
    }
}
