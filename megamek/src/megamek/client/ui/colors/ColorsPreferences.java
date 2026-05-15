package megamek.client.ui.colors;

import megamek.client.ui.clientGUI.GUIPreferences;
import megamek.client.ui.util.PlayerColour;
import megamek.common.preference.PreferenceManager;
import megamek.common.preference.PreferenceStoreProxy;

import java.awt.Color;

public class ColorsPreferences extends PreferenceStoreProxy {

    protected static ColorsPreferences instance = new ColorsPreferences();

    public static ColorsPreferences getInstance() {
        return instance;
    }

    protected ColorsPreferences() {
        store = PreferenceManager.getInstance().getPreferenceStore("Colors", getClass().getName(),
                    "megamek.client.ui.colors.ColorsPreferences");
    }

    public Color getColor(String name) {
        final String text = store.getString(name);
        final Color color = GUIPreferences.parseRGB(text);
        return (color == null) ? PlayerColour.parseFromString(text).getColour() : color;
    }

    public void setColor(String name, Color c) {
        store.setValue(name, getColorString(c));
    }

    protected String getColorString(Color colour) {
        return colour.getRed() + " " + colour.getGreen() + " " + colour.getBlue();
    }

    @Override
    public String[] getAdvancedProperties() {
        return new String[0];
    }
}
