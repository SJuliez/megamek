/*
 * Copyright (c) 2023 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.common;

public enum ERAS {
    AOW("Age of War", 0, 9),
    SL("Star League", 2571, 10),
    ESW("Early Succession Wars", 2781, 11),
    LSW("Late Succession Wars", 2901, 256),
    CLI("Clan Invasion", 3050, 13),
    CW("Civil War", 3062, 247),
    JHD("Jihad", 3068, 14),
    EREP("Early Republic", 3081, 15),
    LREP("Late Republic", 3101, 254),
    DARK("Dark Age", 3131, 16),
    ILC("IlClan", 3151, 257);

    private final String name;
    private final int startYear;
    private final int mulLinkId;

    ERAS(String name, int begin, int mulLinkId) {
        this.name = name;
        this.startYear = begin;
        this.mulLinkId = mulLinkId;
    }

    public String getName() {
        return name;
    }

    public int getStartYear() {
        return startYear;
    }

    public static ERAS getEra(int year) {
        if (year < SL.startYear) {
            return AOW;
        } else if (year < ESW.startYear) {
            return SL;
        } else if (year < LSW.startYear) {
            return ESW;
        } else if (year < CLI.startYear) {
            return LSW;
        } else if (year < CW.startYear) {
            return CLI;
        } else if (year < JHD.startYear) {
            return CW;
        } else if (year < EREP.startYear) {
            return JHD;
        } else if (year < LREP.startYear) {
            return EREP;
        } else if (year < DARK.startYear) {
            return LREP;
        } else if (year < ILC.startYear) {
            return DARK;
        } else {
            return ILC;
        }
    }

    public int getMulLinkId() {
        return mulLinkId;
    }
}
