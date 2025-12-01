package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import megamek.client.ui.clientGUI.GUIPreferences;
import megamek.client.ui.util.FontHandler;
import megamek.client.ui.util.UIUtil;
import megamek.common.Configuration;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Collections;
import java.util.Map;

public class SimpleASForceBuilder {

    private static final String QUICKSAVE_FILENAME = "ASForceQuickSave.mmu";

    // TODO: The field inits below are order-sensitive, bad bad
    final JFrame frame = new JFrame("Alpha Strike Force");
    final JTabbedPane mainPanel = new JTabbedPane();

    private boolean isInitialised = false;

    final Action deleteAction = new DeleteAction(this);
    final Action quickSaveListAction = new QuickSaveListAction(this);
    final Action quickLoadListAction = new QuickLoadListAction(this);
    final Action loadListAction = new LoadListAction(this);
    final Action saveListAction = new SaveForceAction(this);
    final Action addUnitAction = new AddUnitFromCacheAction(this);
    final Action undoAction = new UndoAction(this);
    final Action redoAction = new RedoAction(this);
    final Action printAllAction = new PrintAllAction(this);
    final Action clearAction = new ClearAction(this);
    final Action loadForceAction = new LoadForceAction(this);
    final Action newForceAction = new NewForceAction();
    final Action addLanceAction = new AddLanceAction(this);
    final Map<Integer, Action> skillActions = Map.of(0, new SetSkillAction(this, 0),
          1, new SetSkillAction(this, 1), 2, new SetSkillAction(this, 2),
          3, new SetSkillAction(this, 3), 4, new SetSkillAction(this, 4),
          5, new SetSkillAction(this, 5), 6, new SetSkillAction(this, 6),
          7, new SetSkillAction(this, 7), 8, new SetSkillAction(this, 8)
    );

    SimpleASForceBuilder() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setJMenuBar(new ASFBMenuBar(this));
        mainPanel.putClientProperty("JTabbedPane.hideTabAreaWithOneTab", true);

        var contentPane = frame.getContentPane();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(new ToolBar(this), BorderLayout.NORTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void initialize() {
        if (!isInitialised) {
            SimpleASForceBuilderTab mainTab = new SimpleASForceBuilderTab(this);
            mainTab.setName("Force");
            addTab(mainTab);
            frame.pack();
            updateActionStates();
            isInitialised = true;
        }
    }

    void addTab(SimpleASForceBuilderTab tab) {
        mainPanel.add(tab);
        //TODO tab change listener
//            mainPanel.addChangeListener(e -> updateActionStates());
        tab.unitTable.getSelectionModel().addListSelectionListener(e -> updateActionStates());
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
        // TODO check these again, not all match multiple tabs
        // TODO add tab switch listener
        deleteAction.setEnabled(currentUnitTable().getSelectedRow() != -1);
        skillActions.values().forEach(action -> action.setEnabled(currentUnitTable().getSelectedRow() != -1));
        saveListAction.setEnabled(!isForceEmpty());
        quickSaveListAction.setEnabled(!currentModel().isEmpty());
        quickLoadListAction.setEnabled(getQuicksaveFile().exists());
        undoAction.setEnabled(currentModel().canUseUndo());
        redoAction.setEnabled(currentModel().canUseRedo());
        printAllAction.setEnabled(!currentModel().isEmpty());
        clearAction.setEnabled(!currentModel().isEmpty());
    }

    SimpleASForceBuilderTab getCurrentTab() {
        return (SimpleASForceBuilderTab) mainPanel.getSelectedComponent();
    }

    SimpleASUnitTableModel currentModel() {
        return getCurrentTab().model;
    }

    ASUnitTable currentUnitTable() {
        return getCurrentTab().unitTable;
    }

    boolean isForceEmpty() {
        for (int i = 0; i < mainPanel.getComponentCount(); i++) {
            if (!((SimpleASForceBuilderTab) mainPanel.getComponentAt(i)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        FontHandler.initialize();
        System.setProperty("flatlaf.uiScale", Double.toString(1.6d));
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#353"));
        try {
            UIManager.setLookAndFeel(GUIPreferences.getInstance().getUITheme());
            FlatInspector.install("ctrl shift alt X");
            UIManager.put("Table.alternateRowColor", UIUtil.alternateTableBGColor());
            UIUtil.updateAfterUiChange();
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new SimpleASForceBuilder().setVisible(true));
    }
}
