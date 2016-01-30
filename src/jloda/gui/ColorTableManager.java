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
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * color tables
 * Daniel Huson, 1.2016
 */
public class ColorTableManager {
    private static String DefaultColorTableName = "Standard14";

    private static final String[] BuiltInColorTables = {
            "Caspian8;8;0xf64d1b;0x8633bc;0x41a744;0x747474;0x2746bc;0xff9301;0xc03150;0x2198bc;",
            "Fews9;9;0x4d4d4d;0x5da5da;0xfaa43a;0x60bd68;0xf17cb0;0xb2912f;0xb276b2;0xdecf3f;0xf15854",
            "Pale13;13;0xe2d271;0x9ec25d;0x67c295;0x6083b2;0xc76287;0xe08473;0xf7eead;0xd5e8b7;0xd5e8b7;0xbbe6d2;0xbacee5;0xe3b4c6;0xeec8c2;",
            "Sea9;9;0xffffdb;0xedfbb4;0xc9ecb6;0x88cfbc;0x56b7c4;0x3c90bf;0x345aa7;0x2f2b93;0x121858;",
            "Faded12;12;0xdbdada;0xf27e75;0xba7bbd;0xceedc5;0xfbf074;0xf8cbe5;0xf9b666;0xfdffb6;0x86b0d2;0x95d6c8;0xb3e46c;0xbfb8da;",
            "Rainbow13;13;0x35e110;0x99ea36;0xf12a47;0x38d68f;0x3da1f8;0x3a1afd;0xe1ad36;0x9e00c7;0xe98432;0x8500da;0xdfe03b;0xe80084;0x3ccbc2;",
            DefaultColorTableName + ";14;0xa9a8aa;0x3d9cfb;0x3b1afc;0xe0b137;0x9e00c7;0xe98032;0xa0ea3c;0x8400da;0xe3de3c;0x35e110;0xe80083;0xf13042;0x38d68c;0x3cc8c7;",
            "Retro29;29;0xf4d564;0x97141d;0xe9af6b;0x82ae92;0x356c7c;0x5c8c83;0x3a2b27;0xe28b90;0x242666;0xc2a690;0xb80614;0x35644f;0xe3a380;0xb9a253;0x72a283;0x73605b;0x94a0ad;0xf7a09d;0xe5c09e;0x4a4037;0xcec07c;0x6c80bb;0x7fa0a4;0xb9805b;0xd5c03f;0xdd802e;0x8b807f;0xc42030;0xc2603d;",
            "Random97;97;0xff0000;0xff;0x8000;0xffff00;0xff00ff;0xffff;0xff1493;0xffa500;0xee82ee;0x40e0d0;0xf0e68c;0x800000;0xda70d6;0x6b8e23;" +
                    "0xcd;0xb8860b;0x808080;0xcd853f;0xf4a460;0xffb6c1;0xe9967a;0xb0c4de;0xadff2f;0x191970;0x7fffd4;0x228b22;0xba55d3;0xff6347;0xfa9a;" +
                    "0xa52a2a;0xffa07a;0xc0c0c0;0xdda0dd;0x7b68ee;0x8a2be2;0x9370db;0x87ceeb;0x7cfc00;0x6495ed;0x4682b4;0x2f4f4f;0x800080;0xff8c00;" +
                    "0xc71585;0xa9a9a9;0xdc143c;0x2e8b57;0xbdb76b;0xdeb887;0x87cefa;0x8b4513;0xff7f;0xb22222;0x696969;0xfa8072;0x4169e1;0x20b2aa;" +
                    "0xff69b4;0x6400;0x9acd32;0xded1;0xd8bfd8;0xd2b48c;0x32cd32;0x1e90ff;0x483d8b;0x8b0000;0xcd5c5c;0xadd8e6;0x6a5acd;0xdaa520;0x556b2f;" +
                    "0xff4500;0xa0522d;0xffd700;0x98fb98;0x9400d3;0xff7f50;0x4b0082;0x5f9ea0;0xd2691e;0x8b8b;0x778899;0xbfff;0x9932cc;0x8b008b;0x8dbc8f;" +
                    "0xff00;0xf08080;0xbc8f8f;0xdb7093;0x90ee90;0x48d1cc;0x66cdaa;0x7fff00;0x3cb371;0x8080;"
    };
    private static final Map<String, Collection<Color>> name2colors = new TreeMap<>();

    private static Color[] colorTable;

    private static void initialize() {
        if (colorTable == null) {
            parseTables(BuiltInColorTables);
            colorTable = name2colors.get(DefaultColorTableName).toArray(new Color[name2colors.get(DefaultColorTableName).size()]);
        }
    }

    /**
     * get the color for the given index
     *
     * @param index
     * @return color
     */
    public static Color get(int index) {
        initialize();
        return colorTable[Math.abs(index) % colorTable.length];
    }

    /**
     * get the i-th color
     *
     * @param i
     * @param alpha
     * @return color
     */
    public static Color get(int i, int alpha) {
        initialize();
        Color color = colorTable[Math.abs(i) % colorTable.length];
        if (color.getRed() > 210 && color.getGreen() > 210 && color.getBlue() > 210)
            color = color.darker();

        if (color.getAlpha() == alpha)
            return color;
        else
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static int getSize() {
        initialize();
        return colorTable.length;
    }

    /**
     * get all names of defined tables
     *
     * @return names
     */
    public static String[] getNames() {
        return name2colors.keySet().toArray(new String[name2colors.size()]);
    }

    /**
     * choose the current table
     *
     * @param name
     * @throws IOException
     */
    public static void setTable(String name) throws IOException {
        if (name2colors.containsKey(name)) {
            initialize();
            colorTable = name2colors.get(name).toArray(new Color[name2colors.get(name).size()]);
            ProgramProperties.put("ColorTableName", name);
            System.err.println("Color table set to: " + name);
        } else
            throw new IOException("Unknown color table: " + name);
    }

    /**
     * get the name of the current table
     *
     * @return table
     */
    public static String getTable() {
        return ProgramProperties.get("ColorTableName", DefaultColorTableName);
    }

    public static void addTable(String name, ArrayList<Color> colors) {
        initialize();
        name2colors.put(name, colors);
        saveColorTablesToProperties();
    }

    public static void removeTable(String name) {
        initialize();
        name2colors.remove(name);
        saveColorTablesToProperties();
    }

    /**
     * load all color tables
     */
    public static void loadColorTablesFromProperties() {
        initialize();
        parseTables(ProgramProperties.get("ColorTables", ""));
        try {
            setTable(getTable());
        } catch (IOException e) {
            Basic.caught(e);
        }
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
                        name2colors.put(name, colors);
                }
            }
        }
    }

    /**
     * save all set color tables
     */
    public static void saveColorTablesToProperties() {
        initialize();
        StringBuilder buf = new StringBuilder();
        for (String name : name2colors.keySet()) {
            //  if (!name.equals(RandomColorsTable))
            {
                final Collection<Color> colors = name2colors.get(name);
                if (colors != null && colors.size() > 0) {
                    buf.append(name).append(";");
                    buf.append(String.format("%d;", colors.size()));
                    for (Color color : colors) {
                        buf.append("0x").append((Integer.toHexString(color.getRGB() & 0xffffff))).append(";");
                    }
                }
                ProgramProperties.put("ColorTables", buf.toString());
                System.err.println(buf.toString());
            }
        }
    }
}
