package megamek.client.ui.swing.dialog;

import megamek.client.ratgenerator.AvailabilityRating;
import megamek.client.ratgenerator.ModelRecord;
import megamek.client.ratgenerator.RATGenerator;
import megamek.client.ui.swing.util.SpringUtilities;
import megamek.client.ui.swing.util.UIUtil;
import megamek.common.ERAS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AvailabilityPanel {

    private final static RATGenerator RAT_GENERATOR = RATGenerator.getInstance();
    private static Collection<ModelRecord> MODEL_LIST;
    private static Integer[] RG_ERAS;

    private final JPanel panel = new JPanel();
    private int columns;
    private final ModelRecord record;

    public static void main(String... args) {
        while (!RAT_GENERATOR.isInitialized()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
                // Do nothing
            }
        }
        RAT_GENERATOR.getEraSet().forEach(RAT_GENERATOR::loadYear);
        RAT_GENERATOR.initRemainingUnits();
        MODEL_LIST = RAT_GENERATOR.getModelList();
        RG_ERAS = RAT_GENERATOR.getEraSet().toArray(new Integer[0]);
        SwingUtilities.invokeLater(AvailabilityPanel::new);
    }

    AvailabilityPanel() {
        record = getUnitKey();
        initializePanel();
        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private ModelRecord getUnitKey() {
        return MODEL_LIST.stream().findFirst().orElseGet(null);
    }


    private void initializePanel() {
        panel.setLayout(new SpringLayout());
        addHeader("Faction");
        for (ERAS era : ERAS.values()) {
            String link = "<HTML><BODY><DIV ALIGN=CENTER><A HREF = http://www.masterunitlist.info/Era/Details/9/age-of-war>" + era.toString() + "</A></BODY></HTML>";
            addHeader(link);
        }

        Set<String> currentChassisFactions = new HashSet<>();

        for (int i = 0; i < RAT_GENERATOR.getEraSet().size(); i++) {
            Collection<AvailabilityRating> chassisRecs = RAT_GENERATOR.getChassisFactionRatings(RG_ERAS[i], record.getChassisKey());
            if (chassisRecs != null) {
                for (AvailabilityRating rec : chassisRecs) {
                    currentChassisFactions.add(rec.getFactionCode());
                }
            }
        }


        int row = 1;
        for (String faction : currentChassisFactions) {
           addGridElementLeftAlign(faction, false);
            for (ERAS era : ERAS.values()) {
                String text = "--";
                for (int i = 0; i < RAT_GENERATOR.getEraSet().size(); i++) {
                    if (ERAS.getEra(RG_ERAS[i]) != era) {
                        continue;
                    }
                    Collection<AvailabilityRating> chassisRecs = RAT_GENERATOR.getChassisFactionRatings(RG_ERAS[i], record.getChassisKey());
                    if (chassisRecs != null) {
                        for (AvailabilityRating rec : chassisRecs) {
                            if (faction.equals(rec.getFactionCode())) {
                                text = "Yes";
                            }
                        }
                    }
                }
                addGridElement(text, false);
            }
            row++;
        }

        SpringUtilities.makeCompactGrid(panel, row, columns, 5, 5, 1, 1);
    }

    private void addHeader(String text, float alignment) {
        columns++;
        var headerPanel = new UIUtil.FixedYPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.PAGE_AXIS));
        var textLabel = new JLabel(text);
        textLabel.setAlignmentX(alignment);
        textLabel.setFont(panel.getFont().deriveFont(Font.BOLD));
        textLabel.setForeground(UIUtil.uiLightBlue());
        headerPanel.add(textLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(new JSeparator());
        panel.add(headerPanel);
    }

    private void addHeader(String text) {
        addHeader(text, JComponent.CENTER_ALIGNMENT);
    }

    private void addGridElement(String text, boolean coloredBG) {
        var elementPanel = new UIUtil.FixedYPanel();
        if (coloredBG) {
            elementPanel.setBackground(UIUtil.alternateTableBGColor());
        }
        elementPanel.add(new JLabel(text));
        panel.add(elementPanel);
    }

    private void addGridElementLeftAlign(String text, boolean coloredBG) {
        var elementPanel = new UIUtil.FixedYPanel(new FlowLayout(FlowLayout.LEFT));
        if (coloredBG) {
            elementPanel.setBackground(UIUtil.alternateTableBGColor());
        }
        var textLabel = new JLabel(text);
        elementPanel.add(textLabel);
        panel.add(elementPanel);
    }
}
