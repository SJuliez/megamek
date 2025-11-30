package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import com.formdev.flatlaf.extras.FlatInspector;
import megamek.client.ui.clientGUI.GUIPreferences;
import megamek.client.ui.util.UIUtil;
import megamek.common.Configuration;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Map;

public class SimpleASForceBuilder {

    private static final String QUICKSAVE_FILENAME = "ASForceQuickSave.mmu";

    // TODO: The field inits below are order-sensitive, bad bad
    final JFrame frame = new JFrame("Alpha Strike Force");
    final SimpleASUnitTableModel model = new SimpleASUnitTableModel();
    final ASUnitTable unitTable = new ASUnitTable(this);
    final JScrollPane tableScrollPane = new JScrollPane(unitTable);
    final TablePanel mainPanel;

    private boolean isInitialised = false;

    final Action deleteAction = new DeleteAction(this);
    final Action quickSaveListAction = new QuickSaveListAction(this);
    final Action quickLoadListAction = new QuickLoadListAction(this);
    final Action loadListAction = new LoadListAction(this);
    final Action saveListAction = new SaveListAction(this);
    final Action addUnitAction = new AddUnitFromCacheAction(this);
    final Action undoAction = new UndoAction(this);
    final Action redoAction = new RedoAction(this);
    final Action printAllAction = new PrintAllAction(this);
    final Action clearAction = new ClearAction(this);
    final Action loadForceAction = new LoadForceAction(this);
    final Map<Integer, Action> skillActions = Map.of(0, new SetSkillAction(this, 0),
          1, new SetSkillAction(this, 1), 2, new SetSkillAction(this, 2),
          3, new SetSkillAction(this, 3), 4, new SetSkillAction(this, 4),
          5, new SetSkillAction(this, 5), 6, new SetSkillAction(this, 6),
          7, new SetSkillAction(this, 7), 8, new SetSkillAction(this, 8)
    );

    private final StatusBar statusBar = new StatusBar(this);
    private final ToolBar toolBar = new ToolBar(this);

    SimpleASForceBuilder() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setJMenuBar(new ASFBMenuBar(this));

        mainPanel = new TablePanel(this, tableScrollPane);

        var contentPane = frame.getContentPane();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(statusBar, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void initialize() {
        if (!isInitialised) {
            statusBar.initialize();
            unitTable.initialize();
            mainPanel.initialize();
            updateActionStates();
            model.addTableModelListener(e -> updateActionStates());
            unitTable.getSelectionModel().addListSelectionListener(e -> updateActionStates());
            isInitialised = true;
        }
    }

    File getQuicksaveFile() {
        return new File(Configuration.configDir(), QUICKSAVE_FILENAME);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            initialize();
        }
        frame.setVisible(visible);
    }

    void updateActionStates() {
        deleteAction.setEnabled(unitTable.getSelectedRow() != -1);
        skillActions.values().forEach(action -> action.setEnabled(unitTable.getSelectedRow() != -1));
        saveListAction.setEnabled(!model.isEmpty());
        quickSaveListAction.setEnabled(!model.isEmpty());
        quickLoadListAction.setEnabled(getQuicksaveFile().exists());
        undoAction.setEnabled(model.canUseUndo());
        redoAction.setEnabled(model.canUseRedo());
        printAllAction.setEnabled(!model.isEmpty());
        clearAction.setEnabled(!model.isEmpty());
    }

    public static void main(String[] args) {
        System.setProperty("flatlaf.uiScale", Double.toString(1.6d));
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
