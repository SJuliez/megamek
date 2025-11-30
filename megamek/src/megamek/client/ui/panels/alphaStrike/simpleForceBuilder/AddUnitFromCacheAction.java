package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.client.ui.Messages;
import megamek.client.ui.dialogs.UnitLoadingDialog;
import megamek.client.ui.dialogs.unitSelectorDialogs.MainMenuUnitBrowserDialog;
import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.alphaStrike.conversion.ASConverter;
import megamek.common.loaders.MekSummaryCache;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

class AddUnitFromCacheAction extends AbstractAction {

    private final SimpleASForceBuilder forceBuilder;
    private final JFrame frame;

    public AddUnitFromCacheAction(SimpleASForceBuilder forceBuilder) {
        super("Add Unit From Cache");
        this.forceBuilder = forceBuilder;
        frame = forceBuilder.frame;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
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
                forceBuilder.model.addUnit(element);
                if (close) {
                    setVisible(false);
                }
            }
        };
        new Thread(browserDialog, "Mek Selector Dialog").start();
        browserDialog.setVisible(true);
    }

}
