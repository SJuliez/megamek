/*  
* MegaMek - Copyright (C) 2020 - The MegaMek Team  
*  
* This program is free software; you can redistribute it and/or modify it under  
* the terms of the GNU General Public License as published by the Free Software  
* Foundation; either version 2 of the License, or (at your option) any later  
* version.  
*  
* This program is distributed in the hope that it will be useful, but WITHOUT  
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS  
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more  
* details.  
*/

package megamek.client.ui.swing;

import java.awt.Color;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * HexTextConfig.java Created on April 1, 2020. Used as a subclass of {@link HexTextSettings}
 * to store info for a single terrain.
 *
 * @author Simon
 */
@XmlRootElement(name = "HexTextConfig")
@XmlAccessorType(XmlAccessType.NONE)
public class HexTextConfig {

    @XmlElement(name = "TerrainID")
    private int terrain;
    
    @XmlElement(name = "TerrainLevel")
    private int level;
    
    @XmlElement(name = "IsDisplayed")
    private boolean display;
    
    @XmlElement(name = "TextinHex")
    private String text;
    
    @XmlElement(name = "TextColor")
    private String hexColor;
    
    @XmlElement(name = "ShadowColor")
    private String hexShadowColor;
    
    @XmlElement(name = "BoldFace")
    private boolean bold;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Color getShadowColor() { 
        return colorFromHex(hexShadowColor); 
    }

    public void setShadowColor(Color c) { 
        hexShadowColor = hexFromColor(c); 
    }

    public Color getColor() { 
        return colorFromHex(hexColor); 
    }

    public void setColor(Color c) { 
        hexColor = hexFromColor(c); 
    }

    public int getTerrain() {
        return terrain;
    }

    public void setTerrain(int terrain) {
        this.terrain = terrain;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public HexTextConfig(int terrain, int level, boolean display, String text, String color,
            String shadowColor, boolean bold) {
        this.terrain = terrain;
        this.level = level;
        this.display = display;
        this.text = text;
        this.hexColor = color;
        this.hexShadowColor = shadowColor;
        this.bold = bold;
    }
    
    public HexTextConfig(int terrain, int level, boolean display, String text, Color color,
            Color shadowColor, boolean bold) {
        this.terrain = terrain;
        this.level = level;
        this.display = display;
        this.text = text;
        this.hexColor = hexFromColor(color);
        this.hexShadowColor = hexFromColor(shadowColor);
        this.bold = bold;
    }
    
    // seemingly necessary for JAXB
    public HexTextConfig() {}
    
    /** Converts #FFFFFFFF RGBA String colS to a Color. */
    private static Color colorFromHex(String colS) {
        if (colS.length() == 7) {    // RGBA is always stored, but in case the XML file is edited
            return new Color(
                    Integer.valueOf( colS.substring( 1, 3 ), 16 ),
                    Integer.valueOf( colS.substring( 3, 5 ), 16 ),
                    Integer.valueOf( colS.substring( 5 ), 16 ) );
        } else {
            return new Color(
                    Integer.valueOf( colS.substring( 1, 3 ), 16 ),
                    Integer.valueOf( colS.substring( 3, 5 ), 16 ),
                    Integer.valueOf( colS.substring( 5, 7 ), 16 ),
                    Integer.valueOf( colS.substring( 7 ), 16 )
                    );
        }
    }
    
    /** Converts Color colS to a #FFFFFFFF RGBA String. */
    private static String hexFromColor(Color colS) {
        String hex = Integer.toHexString(colS.getRGB() & 0xffffff);
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        return "#" + hex + Integer.toHexString(colS.getAlpha() & 0xFF);
    }
    
}
