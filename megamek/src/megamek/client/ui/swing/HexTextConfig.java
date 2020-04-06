package megamek.client.ui.swing;

import java.awt.Color;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HexTextConfig")
@XmlAccessorType(XmlAccessType.NONE)
public class HexTextConfig {

    @XmlElement(name = "TerrainID")
    private int terrain;
    
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

    public int getTerrain() { return terrain; }
    public void setTerrain(int t) { terrain = t; } 
    
    public Color getShadowColor() { return colorFromHex(hexShadowColor); }
    public void setShadowColor(Color c) { hexShadowColor = hexFromColor(c); }

    public Color getColor() { return colorFromHex(hexColor); }
    public void setColor(Color c) { hexColor = hexFromColor(c); }
    
    public boolean isDisplayed() { return display; }
    public void setDisplayed(boolean d) { display = d; }

    public String getText() { return text; }
    public void setText(String t) { text = t; }

    public boolean isBold() { return bold; }
    public void setBold(boolean b) { bold = b; }

    public HexTextConfig(int terrain, boolean display, String text, String color,
            String shadowColor, boolean bold) {
        this.terrain = terrain;
        this.display = display;
        this.text = text;
        this.hexColor = color;
        this.hexShadowColor = shadowColor;
        this.bold = bold;
    }
    
    public HexTextConfig(int terrain, boolean display, String text, Color color,
            Color shadowColor, boolean bold) {
        this.terrain = terrain;
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
        while(hex.length() < 6){
            hex = "0" + hex;
        }
        return "#" + hex + Integer.toHexString(colS.getAlpha() & 0xFF);
    }
    
}
