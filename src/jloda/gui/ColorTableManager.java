/*
 *  Copyright (C) 2015 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.gui;

import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * color tables
 * Daniel Huson, 1.2016
 */
public class ColorTableManager {
    private static String DefaultColorTableName = "Fews8";

    private static final String[] BuiltInColorTables = {
            "Caspian8;8;0xf64d1b;0x8633bc;0x41a744;0x747474;0x2746bc;0xff9301;0xc03150;0x2198bc;",
            DefaultColorTableName + ";8;0x5da6dc;0xfba53a;0x60be68;0xf27db0;0xb39230;0xb376b2;0xdfd040;0xf15954;",
            "Pale13;13;0xe2d271;0x9ec25d;0x67c295;0x6083b2;0xc76287;0xe08473;0xf7eead;0xd5e8b7;0xd5e8b7;0xbbe6d2;0xbacee5;0xe3b4c6;0xeec8c2;",
            "Sea9;9;0xffffdb;0xedfbb4;0xc9ecb6;0x88cfbc;0x56b7c4;0x3c90bf;0x345aa7;0x2f2b93;0x121858;",
            "Pale12;12;0xdbdada;0xf27e75;0xba7bbd;0xceedc5;0xfbf074;0xf8cbe5;0xf9b666;0xfdffb6;0x86b0d2;0x95d6c8;0xb3e46c;0xbfb8da;",
            "Rainbow13;13;0xed1582;0xf73e43;0xee8236;0xe5ae3d;0xe5da45;0xa1e443;0x22da27;0x21d18e;0x21c8c7;0x1ba2fc;0x2346fb;0x811fd9;0x9f1cc5;",
            "Bright14;14;0xa9a8aa;0x3d9cfb;0x3b1afc;0xe0b137;0x9e00c7;0xe98032;0xa0ea3c;0x8400da;0xe3de3c;0x35e110;0xe80083;0xf13042;0x38d68c;0x3cc8c7;",
            "Retro29;29;0xf4d564;0x97141d;0xe9af6b;0x82ae92;0x356c7c;0x5c8c83;0x3a2b27;0xe28b90;0x242666;0xc2a690;0xb80614;0x35644f;0xe3a380;0xb9a253;0x72a283;0x73605b;0x94a0ad;0xf7a09d;0xe5c09e;0x4a4037;0xcec07c;0x6c80bb;0x7fa0a4;0xb9805b;0xd5c03f;0xdd802e;0x8b807f;0xc42030;0xc2603d;",
            "Pairs12Pale;12;0xe58573;0xf1c9c2;0xcb6887;0xe5b6c6;0x5a85b2;0xb7cfe5;0x60be96;0xb8e5d2;0x9fbe5f;0xd5e5b8;0xe5ce73;0xf9ebae;",
            "Pairs12;12;0x267ab2;0xa8cfe3;0x399f34;0xb4df8e;0xe11f27;0xfa9b9b;0xfe7f23;0xfcbf75;0x6a4199;0xcab3d6;0xb05a2f;0xffff9f;"
    };

    private static void init() {
        if (name2ColorTable.size() == 0)
            parseTables(BuiltInColorTables);
    }

    private static final Map<String, ColorTable> name2ColorTable = new TreeMap<>();


    public static ColorTable getColorTable(String name) {
        init();
        if (name != null && name2ColorTable.keySet().contains(name)) {
            ProgramProperties.put("DefaultColorTableName", name); // change default color table name to last one used
            return name2ColorTable.get(name);
        }
        else
            return name2ColorTable.get(DefaultColorTableName);
    }

    public static int size() {
        return name2ColorTable.size();
    }

    /**
     * get all names of defined tables
     *
     * @return names
     */
    public static String[] getNames() {
        init();
        return name2ColorTable.keySet().toArray(new String[name2ColorTable.size()]);
    }

    /**
     * add a table
     * @param colorTable
     * @throws IOException
     */
    public static void addTable(ColorTable colorTable) throws IOException {
        init();
        name2ColorTable.put(colorTable.getName(), colorTable);
    }

    public static void removeTable(String name) {
        init();
        name2ColorTable.remove(name);
        saveColorTablesToProperties();
    }

    /**
     * load all color tables
     */
    public static void loadColorTablesFromProperties() {
        init();
        parseTables(ProgramProperties.get("ColorTables", ""));
    }

    /**
     * parse the definition of tables
     *
     * @param tables
     */
    private static void parseTables(String... tables) {
        int alpha = Math.max(0, Math.min(255, ProgramProperties.get("ColorAlpha", 255)));

        for (String table : tables) {
            final String[] tokens = Basic.split(table, ';');
            if (tokens.length > 0) {
                int i = 0;
                while (i < tokens.length) {
                    String name = tokens[i++];
                    int numberOfColors = Integer.valueOf(tokens[i++]);
                    final ArrayList<Color> colors = new ArrayList<>(numberOfColors);
                    for (int k = 0; k < numberOfColors; k++) {
                        Color color = new Color(Integer.decode(tokens[i++]));
                        if (alpha < 255)
                            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                        colors.add(color);
                    }
                    if (colors.size() > 0)
                        name2ColorTable.put(name, new ColorTable(name, colors));
                }
            }
        }
    }

    /**
     * save all set color tables
     */
    public static void saveColorTablesToProperties() {
        init();
        StringBuilder buf = new StringBuilder();
        for (String name : name2ColorTable.keySet()) {
            //  if (!name.equals(RandomColorsTable))
            {
                final ColorTable colorTable = name2ColorTable.get(name);
                if (colorTable != null && colorTable.size() > 0) {
                    buf.append(name).append(";");
                    buf.append(String.format("%d;", colorTable.size()));
                    for (Color color : colorTable.getColors()) {
                        buf.append("0x").append((Integer.toHexString(color.getRGB() & 0xffffff))).append(";");
                    }
                }
                ProgramProperties.put("ColorTables", buf.toString());
                System.err.println(buf.toString());
            }
        }
    }

    /**
     * gets the default color table
     *
     * @return default color table
     */
    public static ColorTable getDefaultColorTable() {
        return getColorTable(DefaultColorTableName);
    }
}
