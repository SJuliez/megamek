package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import com.formdev.flatlaf.FlatClientProperties;
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
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

class StatusBar extends JPanel {

    private static final String NO_VALUE = "--";
    private static final int H_GAP = 15;

    private final SimpleASForceBuilderTab forceBuilder;
    private final SimpleASUnitTableModel model;

    private final JLabel totalPVLabel = new BadgeLabel(NO_VALUE);
    private final JLabel totalUnitsLabel = new BadgeLabel(NO_VALUE);
    private final JLabel averageSkillLabel = new BadgeLabel(NO_VALUE);
    private final JLabel totalDmgLabel = new BadgeLabel(NO_VALUE);
    private final JLabel averageMvLabel = new BadgeLabel(NO_VALUE);

    private final JPanel unitTypesPanel = new JPanel();
    private Map<ASUnitType, Long> unitTypes = new HashMap<>();

    public StatusBar(SimpleASForceBuilderTab forceBuilder) {
        super(new FlowLayout(FlowLayout.RIGHT));
        this.forceBuilder = forceBuilder;
        model = forceBuilder.model;

        totalDmgLabel.setToolTipText("Arc damage not included. E damage shown when not 0.");

        setBorder(new EmptyBorder(5, 10, 5, 10));
//        add(unitTypesPanel); // TODO: where to place? takes too much space or almost no space
        add(Box.createHorizontalStrut(H_GAP));
        add(new NotoLabel("\u03a3 Dmg: "));
        add(totalDmgLabel);
        add(Box.createHorizontalStrut(H_GAP));
        add(new NotoLabel("\u2300 MV: "));
        add(averageMvLabel);
        add(Box.createHorizontalStrut(H_GAP));
        add(new NotoLabel("\u2300 Skill: "));
        add(averageSkillLabel);
        add(Box.createHorizontalStrut(H_GAP));
        add(new NotoLabel("# Units: "));
        add(totalUnitsLabel);
        add(Box.createHorizontalStrut(H_GAP));
        add(new NotoLabel("\u03a3 PV: "));
        add(totalPVLabel);
    }

    static class NotoLabel extends JLabel {
        private static final String VALUE_FORMAT = "font: \"Noto Sans\"";
        public NotoLabel(String text) {
            super(text);
            putClientProperty(FlatClientProperties.STYLE, VALUE_FORMAT);
        }
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
        updateMove();
    }

    private void updateMove() {
        double averageMv = model.getUnits().stream().mapToInt(AlphaStrikeElement::getPrimaryMovementValue).average().orElse(0);
        averageMvLabel.setText(averageMv == 0 ? NO_VALUE : "%1.1f\"".formatted(averageMv));
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
        int totalS = damageSumForRange(ASDamageVector::S);
        int totalM = damageSumForRange(ASDamageVector::M);
        int totalL = damageSumForRange(ASDamageVector::L);
        int totalE = damageSumForRange(ASDamageVector::E);
        String damage = "%d / %d / %d".formatted(totalS, totalM, totalL)
              + (totalE > 0 ? " / " + totalE : "");
        totalDmgLabel.setText(damage);
    }

    private int damageSumForRange(Function<ASDamageVector, ASDamage> rangeValue) {
        return (int) model.getUnits().stream()
              .map(AlphaStrikeElement::getStandardDamage)
              .map(rangeValue)
              .mapToDouble(ASDamage::asDoubleValue)
              .sum();
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
