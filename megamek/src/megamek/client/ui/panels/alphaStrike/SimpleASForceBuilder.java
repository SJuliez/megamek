package megamek.client.ui.panels.alphaStrike;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatInspector;
import megamek.client.ui.Messages;
import megamek.client.ui.clientGUI.GUIPreferences;
import megamek.client.ui.dialogs.UnitLoadingDialog;
import megamek.client.ui.dialogs.randomArmy.ForceGenerationOptionsPanel;
import megamek.client.ui.dialogs.unitSelectorDialogs.MainMenuUnitBrowserDialog;
import megamek.client.ui.util.UIUtil;
import megamek.common.Configuration;
import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.alphaStrike.conversion.ASConverter;
import megamek.common.jacksonAdapters.MMUReader;
import megamek.common.jacksonAdapters.MMUWriter;
import megamek.common.loaders.MULParser;
import megamek.common.loaders.MekSummaryCache;
import megamek.common.options.GameOptions;
import megamek.common.units.Entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleASForceBuilder {

    private static final String NO_VALUE = "--";
    private static final String VALUE_FORMAT = "arc: 12; border: 2,8,2,8,#444444; foreground: #6c6; background: #445";

    private final JFrame frame = new JFrame("Alpha Strike Force");
    private final JLabel totalPVLabel = new JLabel(NO_VALUE);
    private final JLabel totalUnitsLabel = new JLabel(NO_VALUE);
    private final JLabel averageSkillLabel = new JLabel(NO_VALUE);
    private final JButton undoButton = new JButton("Undo");
    private final JButton redoButton = new JButton("Redo");
    private final JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    private final SimpleASUnitTableModel model = new SimpleASUnitTableModel();
    private final ASUnitTable unitTable = new ASUnitTable(model);

    SimpleASForceBuilder() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        prepareTable();
        prepareToolBar();
        prepareStatusBar();

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JScrollPane(unitTable), BorderLayout.CENTER);
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(statusBar, BorderLayout.SOUTH);

        model.addTableModelListener(e -> unitListUpdated());

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void prepareStatusBar() {
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10));

        statusBar.add(new JLabel("Average Skill: "));
        statusBar.add(averageSkillLabel);
        averageSkillLabel.putClientProperty(FlatClientProperties.STYLE, VALUE_FORMAT);

        statusBar.add(Box.createHorizontalStrut(15));

        statusBar.add(new JLabel("Units: "));
        statusBar.add(totalUnitsLabel);
        totalUnitsLabel.putClientProperty(FlatClientProperties.STYLE, VALUE_FORMAT);

        statusBar.add(Box.createHorizontalStrut(15));

        statusBar.add(new JLabel("PV: "));
        statusBar.add(totalPVLabel);
        totalPVLabel.putClientProperty(FlatClientProperties.STYLE, VALUE_FORMAT);

    }

    private void prepareToolBar() {
        JButton addButton = new JButton("Add Unit");
        addButton.addActionListener(e -> addUnit());
        toolBar.add(addButton);

        JButton loadMulButton = new JButton("Load MUL");
        loadMulButton.addActionListener(e -> loadMul());
        toolBar.add(loadMulButton);

        undoButton.setEnabled(false);
        undoButton.addActionListener(e -> model.undoLastChange());
        toolBar.add(undoButton);

        redoButton.setEnabled(false);
        redoButton.addActionListener(e -> model.redo());
        toolBar.add(redoButton);

        JButton quicksave = new JButton("Quicksave");
        quicksave.addActionListener(e -> {
            File unitFile = new File(Configuration.configDir(), "ASForceQuickSave.mmu");
            try {
                new MMUWriter().writeMMUFileFullStats(unitFile, model.getUnits());
            } catch (IOException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, "The MMU file could not be written. " + ex.getMessage());
            }
        });
        toolBar.add(quicksave);

        JButton quickLoad = new JButton("Quickload");
        quickLoad.addActionListener(e -> {
            File unitFile = new File(Configuration.configDir(), "ASForceQuickSave.mmu");
            try {
                List<AlphaStrikeElement> elements = new ArrayList<>();
                List<Object> units = new MMUReader().read(unitFile);
                for (Object unit : units) {
                    if (unit instanceof AlphaStrikeElement element) {
                        elements.add(element);
                    }
                }
                model.addUnits(elements);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                      "Error loading units from the selected file. Some units may have been added."+ex.getMessage());
            }
            unitListUpdated();
        });
        toolBar.add(quickLoad);

        JButton forceGeneratorButton = new JButton("Force Generator");
        forceGeneratorButton.addActionListener(e -> createFormationPanel());
        toolBar.add(forceGeneratorButton);

        JButton saveMulButton = new JButton("Save MUL");
        toolBar.add(saveMulButton);

        JButton saveMMUButton = new JButton("Save MMU");
        toolBar.add(saveMMUButton);

        JButton printButton = new JButton("Print");
        toolBar.add(printButton);
    }

    private void addUnit() {
        UnitLoadingDialog unitLoadingDialog = new UnitLoadingDialog(frame);
        if (!MekSummaryCache.getInstance().isInitialized()) {
            unitLoadingDialog.setVisible(true);
        }
        MainMenuUnitBrowserDialog browserDialog = new MainMenuUnitBrowserDialog(frame, unitLoadingDialog) {
            //region Button Methods
            @Override
            protected JPanel createButtonsPanel() {
                GridBagConstraints gbc = new GridBagConstraints();
                JPanel panelButtons = new JPanel(new GridBagLayout());

                buttonSelect = new JButton(Messages.getString("MekSelectorDialog.m_bPick"));
                buttonSelect.addActionListener(this);
                panelButtons.add(buttonSelect, gbc);

                buttonSelectClose = new JButton(Messages.getString("MekSelectorDialog.m_bPickClose"));
                buttonSelectClose.addActionListener(this);
                panelButtons.add(buttonSelectClose, gbc);

                buttonClose = new JButton(Messages.getString("Close"));
                buttonClose.addActionListener(this);
                panelButtons.add(buttonClose, gbc);

                return panelButtons;
            }

            @Override
            protected void select(boolean close) {
                AlphaStrikeElement element = ASConverter.convert(getSelectedEntity());
                model.addUnit(element);
                unitListUpdated();
                if (close) {
                    setVisible(false);
                }
            }
        };
        new Thread(browserDialog, "Mek Selector Dialog").start();
        browserDialog.setVisible(true);
    }

    private void loadMul() {
        // Build the "load unit" dialog, if necessary.
        var dlgLoadList = new JFileChooser(".");
        dlgLoadList.setLocation(frame.getLocation().x + 150, frame.getLocation().y + 100);
        dlgLoadList.setDialogTitle(Messages.getString("ClientGUI.openUnitListFileDialog.title"));
        dlgLoadList.setFileFilter(new FileNameExtensionFilter("MUL files", "mul", "mmu"));

        int returnVal = dlgLoadList.showOpenDialog(frame);
        if ((returnVal != JFileChooser.APPROVE_OPTION) || (dlgLoadList.getSelectedFile() == null)) {
            return;
        }

        // Did the player select a file?
        File unitFile = dlgLoadList.getSelectedFile();
        try {
            List<AlphaStrikeElement> elements = new ArrayList<>();
            if (unitFile.toString().endsWith("mul")) {
                List<Entity> loadedUnits = new MULParser(unitFile, new GameOptions()).getEntities();
                for (Entity entity : loadedUnits) {
                    AlphaStrikeElement element = ASConverter.convert(entity);
                    if (element != null) {
                        elements.add(element);
                    }
                }
            } else if (unitFile.toString().endsWith("mmu")) {
                List<Object> units = new MMUReader().read(unitFile);
                for (Object unit : units) {
                    if (unit instanceof AlphaStrikeElement element) {
                        elements.add(element);
                    }
                }
            }
            model.addUnits(elements);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                  "Error loading units from the selected file. Some units may have been added.");
        }
        unitListUpdated();
    }

    private void prepareTable() {
        var columnModel = unitTable.getColumnModel();
        TableColumn pvColumn = columnModel.getColumn(SimpleASUnitTableModel.COL_PV);
        pvColumn.setCellRenderer(UIUtil.rightAlignedTableCellRenderer());
        TableColumn skillColumn = columnModel.getColumn(SimpleASUnitTableModel.COL_SKILL);
        skillColumn.setCellRenderer(UIUtil.centerAlignedTableCellRenderer());
        packColumn(unitTable, SimpleASUnitTableModel.COL_SKILL, "  XX  ");
        packColumn(unitTable, SimpleASUnitTableModel.COL_PV, "  XXXXXX  ");
        unitTable.setIntercellSpacing(new Dimension(2, 1));
    }

    public static void packColumn(JTable table, int columnIndex, int margin) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        int width = 0;

        // --- Header width ---
        TableCellRenderer headerRenderer = column.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }
        Component headerComp = headerRenderer.getTableCellRendererComponent(
              table, column.getHeaderValue(), false, false, 0, columnIndex);
        width = headerComp.getPreferredSize().width;

        // --- Cell widths ---
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer renderer = table.getCellRenderer(row, columnIndex);
            Component comp = renderer.getTableCellRendererComponent(
                  table, table.getValueAt(row, columnIndex), false, false, row, columnIndex);
            comp = new JLabel("  555555  ");
            width = Math.max(width, comp.getPreferredSize().width);
        }

        // Add margin for spacing
        width += 2 * margin;

        column.setPreferredWidth(width);
        column.setMaxWidth(5 * width);
    }

    public static void packColumn(JTable table, int columnIndex, String protoTypeText) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        Component comp = new JLabel(protoTypeText);
        int width = comp.getPreferredSize().width;
        column.setPreferredWidth(width);
        column.setMaxWidth(2 * width);
    }


    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    private void unitListUpdated() {
        updateTotalPV();
        updateTotalUnits();
        updateAverageSkill();
        undoButton.setEnabled(model.canUseUndo());
        redoButton.setEnabled(model.canRedo());
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

    private void createFormationPanel() {
        var forceGenPanel = new ForceGenerationOptionsPanel(ForceGenerationOptionsPanel.Use.RAT_GENERATOR);
        JOptionPane.showMessageDialog(frame, forceGenPanel, "Force Generation", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        System.setProperty("flatlaf.uiScale", Double.toString(2d));
        try {
            UIManager.setLookAndFeel(GUIPreferences.getInstance().getUITheme());
            FlatInspector.install("ctrl shift alt X");
            UIManager.put("Table.alternateRowColor", UIUtil.alternateTableBGColor());
            UIUtil.updateAfterUiChange();
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            var simpleASForceBuilder = new SimpleASForceBuilder();
            simpleASForceBuilder.setVisible(true);
        });

    }
}
