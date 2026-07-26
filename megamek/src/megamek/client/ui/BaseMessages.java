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

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import megamek.MegaMek;

/**
 * This class can be used to get a resource bundle (i18n texts), using the language set in MM's preferences. To
 * construct an instance for a specific class, use one of the static factories of(). A single get() method is used to
 * obtain localized strings which can be used with and without additional arguments. When additional arguments are
 * present, MessageFormat is used for formatting, i.e. {0} formatting is used and advanced ChoiceFormatting may be used.
 * Note that an (empty) _en.properties file is necessary to avoid errors with language file choice.
 */
@SuppressWarnings("unused") // Utility class
public final class BaseMessages {

    private final ResourceBundle resourceBundle;

    /**
     * Returns a resource bundle for the given object. The class name of the object is used to determine the bundle
     * name. So, when used in megamek.client.ui.SomeDialog, resources/megamek/client/ui/SomeDialog.properties (and its
     * other language equivalents) is used. Note that when used in a superclass, the bundle name will use the class name
     * of the subclass.
     *
     * @param object An object (usually a dialog or similar class object)
     *
     * @return The resource bundle for i18n texts
     *
     * @throws MissingResourceException – if no resource bundle for the object can be found
     */
    public static BaseMessages of(Object object) {
        return of(object.getClass());
    }

    /**
     * Returns a resource bundle for the given class. The class name is used to determine the bundle name.
     *
     * @param clazz The class to search the bundle for (usually a dialog or similar class)
     *
     * @return The resource bundle for i18n texts
     *
     * @throws MissingResourceException – if no resource bundle for the class can be found
     */
    public static BaseMessages of(Class<?> clazz) {
        return of(clazz.getName());
    }

    /**
     * Returns a resource bundle for the given bundle name.
     *
     * @param bundleName The bundle's name ("megamek.client.ui.somedialog")
     *
     * @return The resource bundle for i18n texts
     *
     * @throws MissingResourceException – if no resource bundle for the specified name can be found
     */
    public static BaseMessages of(String bundleName) {
        return new BaseMessages(bundleName);
    }

    private BaseMessages(String bundleName) {
        resourceBundle = ResourceBundle.getBundle(bundleName, MegaMek.getMMOptions().getLocale());
    }

    /**
     * Retrieves the string for the given key from this resource bundle or one of its parents. Additional parameters are
     * applied using MessageFormat (so the resource string should use {x} formatting and it can use advanced
     * ChoiceFormat formatting). This method can be used without giving additional parameters for strings that don't
     * contain placeholders. Note that all exceptions are caught and, when one occurs, "!!! key !!!" is returned.
     *
     * @param key  The resource key
     * @param args Additional info to insert for placeholders
     *
     * @return The formatted resource bundle i18n string
     */
    public String get(String key, Object... args) {
        try {
            String message = resourceBundle.getString(key);
            if (args.length == 0) {
                // avoid MessageFormat when unnecessary as it may mangle apostrophs
                return message;
            }
            return MessageFormat.format(message, args);
        } catch (Exception ex) {
            return "!!! %s !!!".formatted(key);
        }
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}
