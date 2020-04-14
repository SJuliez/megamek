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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import megamek.common.Terrains;
import megamek.utils.MegaMekXmlUtil;

/**
 * HexTextSettings.java Created on April 1, 2020. A storage class for settings 
 * concerning the display of terrain info texts such as "Light Woods" in the 
 * board hexes. Used by the boardview.
 *
 * @author Simon
 */
@XmlRootElement(name = "HexTextSettings")
@XmlAccessorType(XmlAccessType.NONE)
public class HexTextSettings implements Serializable {
    private static final long serialVersionUID = -1811324294581819888L;
    
    public static final String FILE_INGAME = "mmconf/HexTextInGame.xml";
    public static final String FILE_MAPED = "mmconf/HexTextMapEd.xml";
    
    public final static int HT_SEPARATOR = 1001;
    public final static int HT_LEVEL = 1002;
    public final static int HT_LIGHT = 1003;
    public final static int HT_HEAVY = 1004;
    public final static int HT_ULTRA = 1005;
    public final static int HT_OTHERS = 1006;
    
    /** The configurations for the individual terrains, containing whether to display 
     * the terrain, the color and shadow color and the text to display*/
    @XmlElement(name = "Configs")
    private ArrayList<HexTextConfig> allConfigs = new ArrayList<HexTextConfig>();

    /** The Font to use for all hex texts. */
    @XmlElement(name = "Font")
    private String font = "Sans Serif";

    /** The Font size to use for all hex texts. */
    @XmlElement(name = "FontSize")
    private int fontSize = 8;
    
    /** When true, will display terrain even when only one terrain is present, exluding the base level. */
    @XmlElement(name = "ShowSingle")
    private boolean showSingle;

    /**
     * Creates and returns an instance of HexTextSettings with basic settings similar
     * to the former MegaMek hex text style.
     */
    public static HexTextSettings getMinimalSettings() {
        HexTextSettings hts = new HexTextSettings();
        hts.addConfig(new HexTextConfig(HT_LEVEL, -1, true, "LEVEL ", "#000000", "#00000000", false));
        hts.addConfig(new HexTextConfig(Terrains.WATER, -1, true, "DEPTH ", "#000000",  "#00000000", false));
        hts.addConfig(new HexTextConfig(Terrains.BLDG_ELEV, -1, true, "HEIGHT ", "#0000FF",  "#00000000", false));
        hts.addConfig(new HexTextConfig(HT_SEPARATOR, -1, true, "\t", "#000000",  "#00000000", false));
        hts.font = "SansSerif";
        hts.fontSize = 16;
        hts.showSingle = false;
        return hts;
    }
    
    /** Loads the current ingame settings from the XML file. */
    public static HexTextSettings getInGameSettings() {
        HexTextSettings hts = getMinimalSettings();
        try(InputStream is = new FileInputStream(FILE_INGAME)) {
            HexTextSettings htsr = getInstance(is);
            if (htsr != null) {
                hts = htsr;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return hts;
    }
    
    public static void saveInGameSettings(HexTextSettings hts) {
        try(OutputStream os = new FileOutputStream(FILE_INGAME)) {
            hts.save(os);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    /** Loads the current map editor settings from the XML file. */
    public static HexTextSettings getMapEdSettings() {
        try(InputStream is = new FileInputStream(FILE_MAPED)) {
            return getInstance(is);
        } catch (IOException e) {
            e.printStackTrace();
            return getMinimalSettings();
        }
    }

    public static void saveMapEdSettings(HexTextSettings hts) {
        try(OutputStream os = new FileOutputStream(FILE_MAPED)) {
            hts.save(os);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    /** Loads the current map editor settings from the XML file. */
    public static HexTextSettings loadSettings(File file) {
        try(InputStream is = new FileInputStream(file)) {
            return getInstance(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Returns an empty HexTextSettings.
     */
    public static HexTextSettings getInstance() {
        return new HexTextSettings();
    }
    
    /** Add the HexTextConfig htc to the list of configs */
    public void addConfig(HexTextConfig htc) {
        allConfigs.add(htc);
    }

    /**
     * Load Settings. Returns a new instance of HexTextSettings with values
     * loaded from the given input stream of an XML file.
     */
    public static HexTextSettings getInstance(final InputStream is) {
        HexTextSettings hts = null;

        try {
            JAXBContext jc = JAXBContext.newInstance(HexTextSettings.class);

            Unmarshaller um = jc.createUnmarshaller();
            hts = (HexTextSettings) um.unmarshal(MegaMekXmlUtil.createSafeXmlSource(is));
        } catch (JAXBException | SAXException | ParserConfigurationException ex) {
            System.err.println("Error loading XML for map settings: " + ex.getMessage()); //$NON-NLS-1$
            ex.printStackTrace();
        }

        return hts;
    }
    
    /**
     * Save Settings to the given output stream of an XML file.
     */
    public void save(final OutputStream os) {
        try {
            JAXBContext jc = JAXBContext.newInstance(HexTextSettings.class);

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // The default header has the encoding and standalone properties
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            try {
                marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders", "<?xml version=\"1.0\"?>");
            } catch (PropertyException ex) {
                marshaller.setProperty("com.sun.xml.bind.xmlHeaders", "<?xml version=\"1.0\"?>");
            }

            JAXBElement<HexTextSettings> element = new JAXBElement<>(new QName("HexTextSettings"), HexTextSettings.class, this);

            marshaller.marshal(element, os);
        } catch (JAXBException ex) {
            System.err.println("Error writing XML for Hex Text Settings: " + ex.getMessage()); //$NON-NLS-1$
            ex.printStackTrace();
        }
    }

    // seemingly necessary for JAXB
    private HexTextSettings() {}
    
    public ArrayList<HexTextConfig> getAllConfigs() { return allConfigs; }

    public String getFont() { return font; }
    public void setFont(String f) { font = f; }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int fS) { fontSize = fS; }

    public HexTextConfig getConfig(int terrain) {
        for (HexTextConfig htc: allConfigs) {
            if (htc.getTerrain() == terrain)
                return htc;
        }
        return null;
    }

    public HexTextConfig getConfig(int terrain, int level) {
        for (HexTextConfig htc : allConfigs) {
            if ((htc.getTerrain() == terrain) && 
                    ((htc.getLevel() == -1) || htc.getLevel() == level))
                return htc;
        }
        return null;
    }
}



    
