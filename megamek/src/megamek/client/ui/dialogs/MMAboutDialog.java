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
package megamek.client.ui.dialogs;

import com.formdev.flatlaf.FlatClientProperties;
import megamek.MMConstants;
import megamek.client.ui.Messages;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MMAboutDialog extends AbstractAboutDialog {

    public MMAboutDialog(JFrame parentFrame) {
        super(parentFrame);
    }

    @Override
    protected JComponent version() {
        JLabel program = new JLabel(MMConstants.PROJECT_NAME);
        program.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        JLabel version = new JLabel(Messages.getString("about.version", MMConstants.VERSION));
        var panel = Box.createVerticalBox();
        panel.add(program);
        panel.add(Box.createVerticalStrut(8));
        panel.add(version);
        return panel;
    }
}
