/*
 * Copyright (C) 2003, 2004, 2005 Ben Mazur (bmazur@sev.org)
 * Copyright (C) 2013 Edward Cullen (eddy@obsessedcomputers.co.uk)
 * Copyright (C) 2003-2026 The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 *
 * MechWarrior Copyright Microsoft Corporation. MegaMek was created under
 * Microsoft's "Game Content Usage Rules"
 * <https://www.xbox.com/en-US/developers/rules> and it is not endorsed by or
 * affiliated with Microsoft.
 */
package megamek.client.ui.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import megamek.client.ui.Messages;
import megamek.client.ui.util.UIUtil;

/**
 * A base "About..." dialog showing a version block ("MegaMek Version: 0.50.xx") that subclasses supply and the license
 * block.
 */
public abstract class AbstractAboutDialog {

    private static final String LICENSE_FORMAT = "<html><body width='%d'>%s</body></html>";
    private static final int BASE_WIDTH = 500;
    private final Window parentFrame;

    protected AbstractAboutDialog(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    private JComponent setupContent() {
        JPanel content = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.ipady = 30;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.setBorder(new EmptyBorder(40, 15, 0, 15));
        content.add(version(), gbc);
        content.add(licenseSection(), gbc);
        return content;
    }

    protected final JComponent licenseSection() {
        JEditorPane licenseSection = new JEditorPane();
        licenseSection.setContentType("text/html");
        licenseSection.setEditable(false);
        licenseSection.setText(buildAboutHtml());
        licenseSection.addHyperlinkListener(UIUtil::handleHyperlink);
        licenseSection.setBorder(null);
        return licenseSection;
    }

    /**
     * Override this to return a block showing the current program and its version (and possibly versions of the other
     * programs).
     *
     * @return A component (label or panel) showing the program and version
     */
    protected abstract JComponent version();

    /**
     * Displays the dialog (modal).
     */
    public void show() {
        JOptionPane.showMessageDialog(parentFrame, setupContent(), Messages.getString("CommonAboutDialog.title"),
              JOptionPane.PLAIN_MESSAGE, null);
    }

    private String buildAboutHtml() {
        return LICENSE_FORMAT.formatted(UIUtil.scaleForGUI(BASE_WIDTH), LicensingDialog.buildLegalHtml());
    }
}
