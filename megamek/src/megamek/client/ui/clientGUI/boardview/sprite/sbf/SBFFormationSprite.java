/*
 * Copyright (C) 2024-2025 The MegaMek Team. All Rights Reserved.
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
package megamek.client.ui.clientGUI.boardview.sprite.sbf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.Nonnull;
import megamek.client.ui.clientGUI.GUIPreferences;
import megamek.client.ui.clientGUI.boardview.BoardView;
import megamek.client.ui.clientGUI.boardview.sprite.Sprite;
import megamek.client.ui.util.FontHandler;
import megamek.client.ui.util.StringDrawer;
import megamek.client.ui.util.UIUtil;
import megamek.common.Player;
import megamek.common.board.BoardLocation;
import megamek.common.strategicBattleSystems.SBFElementType;
import megamek.common.strategicBattleSystems.SBFFormation;
import megamek.common.strategicBattleSystems.SBFGame;

/**
 * Sprite for an entity. Changes whenever the entity changes. Consists of an image, drawn from the Tile Manager; facing
 * and possibly secondary facing arrows; armor and internal bars; and an identification label.
 */
public class SBFFormationSprite extends Sprite {

    private static final GUIPreferences GUIP = GUIPreferences.getInstance();

    private static final int INSET = 10;

    private final SBFFormation formation;
    private final Player owner;
    private final int formationCountInHex;
    private final boolean isInfantry;

    /** True when this formation is friendly to the local player */
    private final boolean isFriendly;

    /** True when the location also contains enemy formation(s) of the local player */
    private final boolean hasEnemies;

    /** True when the location also contains friendly formation(s) of the local player */
    private final boolean hasFriendlies;

    /** True when the formation is stacked with members of its own team (friendly to the local player or not) */
    private final boolean isStackedWithTeam;

    /** The area actually covered by the icon */
    private Rectangle hitBox;

    /** Used to color the label when this unit is selected for movement etc. */
    private boolean isSelected;

    Font font = FontHandler.notoFont().deriveFont(Font.BOLD);
    Color textOutlineColor = Color.DARK_GRAY;

    public SBFFormationSprite(BoardView boardView, @Nonnull SBFFormation formation, Player owner, SBFGame game,
          int localPlayerNumber) {

        super(boardView);
        this.formation = Objects.requireNonNull(formation);
        this.owner = owner;
        BoardLocation location = formation.getPosition();
        //TODO: can we prevent errors when the position is null or doesnt exist?

        List<SBFFormation> formationsInHex = game.getActiveFormationsAt(formation.getPosition());
        formationCountInHex = formationsInHex.size();
        int friendlies = game.getFriendlyFormationsAt(location, localPlayerNumber).size();
        hasEnemies = formationCountInHex - friendlies > 0;
        hasFriendlies = friendlies > 0;
        isFriendly = game.getFriendlyFormationsAt(location, localPlayerNumber).contains(formation);
        isStackedWithTeam = (isFriendly && friendlies > 1)
              || (!isFriendly && (formationCountInHex - friendlies > 1));
        isInfantry = formation.isAnyTypeOf(SBFElementType.CI, SBFElementType.BA);

        // SBF stacking rules allow 2 friendly ground formations in a hex if one of them is infantry; otherwise exactly
        // one friendly; enemies don't count at all for stacking
        // for MM: each team may have 1 ground formation in hex and one additional infantry formation

        getBounds();
    }

    @Override
    public Rectangle getBounds() {
        bounds = new Rectangle(0, 0, bv.getHexSize().width, bv.getHexSize().height);
        Point ePos = bv.getHexLocation(formation.getPosition().coords());
        bounds.setLocation(ePos.x, ePos.y);
        hitBox = new Rectangle(bounds.x + INSET, bounds.y + INSET, bounds.width - 2 * INSET, bounds.height - 2 * INSET);
        return bounds;
    }

    @Override
    protected int getSpritePriority() {
        // When not alone in the hex, infantry paints below other formations
        return super.getSpritePriority() - (isInfantry ? 10 : 0);
    }

    @Override
    public void prepare() {
        getBounds();

        // create image for buffer
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
              .getDefaultScreenDevice()
              .getDefaultConfiguration();
        image = config.createCompatibleImage(bounds.width, bounds.height, Transparency.TRANSLUCENT);
        Graphics2D graph = (Graphics2D) image.getGraphics();
        UIUtil.setHighQualityRendering(graph);

        graph.scale(bv.getScale(), bv.getScale());
        graph.translate(42, 36);
        int yPos = 0;
        int xPos = 0;

        if (isFriendly && hasEnemies) {
            xPos = -18;
        } else if (!isFriendly && hasFriendlies) {
            xPos = 18;
        }

        if (isStackedWithTeam && isInfantry) {
            yPos = 18;
        } else if (isStackedWithTeam) {
            yPos = -12;
        }

        double scaling = 0.5;
        if (formationCountInHex == 1) {
            scaling = 0.65;
        } else if (isInfantry && isStackedWithTeam) {
            scaling = 0.45;
        }

        graph.translate(xPos, yPos);
        graph.scale(scaling, scaling);
        if (isSelected) {
            graph.setColor(Color.WHITE);
        } else if (formation.isDone()) {
            graph.setColor(Color.DARK_GRAY);
        } else {
            graph.setColor(Color.GREEN);
        }
        drawCenteredIcon(graph);
        graph.dispose();
    }

    private void drawCenteredIcon(Graphics2D graph) {
        graph.setStroke(new BasicStroke(isSelected ? 5 : 2));
        Color outlineColor = isFriendly ? GUIP.getMyUnitColor() : GUIP.getEnemyUnitColor();
        if (isSelected) {
            outlineColor = Color.WHITE;
        }

        int rectWidth = 84 - INSET;
        int rectHeight = 72 - INSET;
        graph.setColor(outlineColor);
        graph.drawRoundRect(-rectWidth / 2, -rectHeight / 2, rectWidth, rectHeight, INSET / 2, INSET / 2);
        graph.setColor(owner.getColour().getColour());
        graph.fillRoundRect(-rectWidth / 2, -rectHeight / 2, rectWidth, rectHeight, INSET / 2, INSET / 2);
        new StringDrawer(formation.getType().toString()).at(-42 + INSET, -16)
              .fontSize(16).color(Color.WHITE).outline(textOutlineColor, 2.8f).font(font).draw(graph);
        new StringDrawer("" + formation.getPointValue()).at(42-INSET, -16)
              .rightAlign().fontSize(16).color(Color.WHITE).outline(textOutlineColor, 2.8f).font(font).draw(graph);
        if (!formation.getShortName().isBlank()) {
            new StringDrawer(formation.getShortName()).at(0, 28).fontSize(20)
                  .centerX().maxWidth(72).color(Color.WHITE).outline(textOutlineColor, 2.8f).font(font).draw(graph);
        }

        int iconWidth = 84 - INSET * 5;
        int iconHeight = 72 - INSET * 5;
        //TODO: scale properly so it looks better
        graph.drawImage(owner.getCamouflage().getImage(), -iconWidth / 2, -iconHeight / 2, iconWidth, iconHeight, null);
    }

    @Override
    public boolean isInside(Point point) {
        return hitBox.contains(point.x, point.y);
    }

    /** Marks the entity as selected for movement etc., recoloring the label */
    public void setSelected(boolean status) {
        if (isSelected != status) {
            isSelected = status;
            prepare();
        }
    }

    /** Returns if the entity is marked as selected for movement etc., recoloring the label */
    public boolean getSelected() {
        return isSelected;
    }

    public SBFFormation getFormation() {
        return formation;
    }
}
