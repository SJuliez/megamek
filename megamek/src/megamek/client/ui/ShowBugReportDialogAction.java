/*
 * Copyright (C) 2026 The MegaMek Team. All Rights Reserved.
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

package megamek.client.ui;

import megamek.client.ui.clientGUI.BugReportDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static megamek.client.ui.Messages.getString;

public class ShowBugReportDialogAction extends AbstractAction {

    private final Window parent;
    private final Action copySystemDataAction;

    /**
     * Creates an action that shows the Bug Report helper dialog. The given parent Window is used as a parent frame for
     * the dialog. The given Action is used for a "Copy System Data" button. This action needs to know the current
     * project (MM/MML/MHQ) which is why it isn't created internally.
     *
     * @param parent               The parent window
     * @param copySystemDataAction An Action shown as a button in the dialog
     *
     * @see BugReportDialog
     */
    public ShowBugReportDialogAction(Window parent, CopySystemDataAction copySystemDataAction) {
        super(getString("CommonMenuBar.helpReportBug"));
        this.parent = parent;
        this.copySystemDataAction = copySystemDataAction;
    }

    /**
     * Creates an action that shows the Bug Report helper dialog. The given parent Component's ancestor Window is
     * determined automatically and used as a parent frame for the dialog. The given Action is used for a "Copy System
     * Data" button. This action needs to know the current project (MM/MML/MHQ) which is why it isn't created
     * internally.
     *
     * @param parent               The parent window
     * @param copySystemDataAction An Action shown as a button in the dialog
     *
     * @see BugReportDialog
     */
    public ShowBugReportDialogAction(Component parent, CopySystemDataAction copySystemDataAction) {
        this(SwingUtilities.getWindowAncestor(parent), copySystemDataAction);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new BugReportDialog(parent, copySystemDataAction).show();
    }
}
