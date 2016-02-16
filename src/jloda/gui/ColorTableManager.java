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
    private static final String DefaultColorTableName = "Fews8";
    private static final String DefaultColorTableHeatMap = "White-Green";

    private static final String[] BuiltInColorTables = {
            "Fews8;8;0x5da6dc;0xfba53a;0x60be68;0xf27db0;0xb39230;0xb376b2;0xdfd040;0xf15954;",
            "Caspian8;8;0xf64d1b;0x8633bc;0x41a744;0x747474;0x2746bc;0xff9301;0xc03150;0x2198bc;",
            "Sea9;9;0xffffdb;0xedfbb4;0xc9ecb6;0x88cfbc;0x56b7c4;0x3c90bf;0x345aa7;0x2f2b93;0x121858;",
            "Pale12;12;0xdbdada;0xf27e75;0xba7bbd;0xceedc5;0xfbf074;0xf8cbe5;0xf9b666;0xfdffb6;0x86b0d2;0x95d6c8;0xb3e46c;0xbfb8da;",
            "Pale13;13;0xe2d271;0x9ec25d;0x67c295;0x6083b2;0xc76287;0xe08473;0xf7eead;0xd5e8b7;0xd5e8b7;0xbbe6d2;0xbacee5;0xe3b4c6;0xeec8c2;",
            "Rainbow13;13;0xed1582;0xf73e43;0xee8236;0xe5ae3d;0xe5da45;0xa1e443;0x22da27;0x21d18e;0x21c8c7;0x1ba2fc;0x2346fb;0x811fd9;0x9f1cc5;",
            "Bright14;14;0xa9a8aa;0x3d9cfb;0x3b1afc;0xe0b137;0x9e00c7;0xe98032;0xa0ea3c;0x8400da;0xe3de3c;0x35e110;0xe80083;0xf13042;0x38d68c;0x3cc8c7;",
            "Retro29;29;0xf4d564;0x97141d;0xe9af6b;0x82ae92;0x356c7c;0x5c8c83;0x3a2b27;0xe28b90;0x242666;0xc2a690;0xb80614;0x35644f;0xe3a380;0xb9a253;0x72a283;0x73605b;0x94a0ad;0xf7a09d;0xe5c09e;0x4a4037;0xcec07c;0x6c80bb;0x7fa0a4;0xb9805b;0xd5c03f;0xdd802e;0x8b807f;0xc42030;0xc2603d;",
            "Pairs12Pale;12;0xe58573;0xf1c9c2;0xcb6887;0xe5b6c6;0x5a85b2;0xb7cfe5;0x60be96;0xb8e5d2;0x9fbe5f;0xd5e5b8;0xe5ce73;0xf9ebae;",
            "Pairs12;12;0x267ab2;0xa8cfe3;0x399f34;0xb4df8e;0xe11f27;0xfa9b9b;0xfe7f23;0xfcbf75;0x6a4199;0xcab3d6;0xb05a2f;0xffff9f;",
            "Yellow-Red;62;0xfdfccc;0xfdfac6;0xfdf7c0;0xfdf5ba;0xfdf2b4;0xfdefae;0xfdeda8;0xfceaa2;0xfce79c;0xfce596;0xfde290;0xfcde8a;0xfcdb84;0xfcd97e;0xfcd478;0xfcce74;0xfcc96e;0xfcc369;0xfec063;0xfbba60;0xfbb55a;0xfbaf56;0xfba953;0xfba350;0xfb9d4e;0xfb974b;0xfb9048;0xfa8a46;0xfa8344;0xfb7d41;0xfa7741;0xfa713e;0xfa6b3d;0xfb653a;0xf95e39;0xf95837;0xf95236;0xf94c34;0xf54632;0xf04030;0xf03a2e;0xea342d;0xea2e2a;0xe6282a;0xe42226;0xde1f28;0xdd1926;0xd71a28;0xd11628;0xcb132a;0xc50f2a;0xbf0c2b;0xb90a2b;0xb20a2c;0xac082a;0xa6092b;0xa0082b;0x9a082a;0x94082a;0x8e0529;0x880729;0x820629;",
            "Blue-Red;100;0x4156be;0x4158c4;0x465ec6;0x475fcc;0x4c65cc;0x4c67d2;0x516dd3;0x5370d9;0x5975d8;0x5877de;0x5e7cde;0x5f80e4;0x6584e3;0x6587e9;0x6b8be7;0x6c8eed;0x7194ef;0x7799ef;0x789bf5;0x7da1f6;0x83a7f9;0x89abf9;0x8db1fb;0x93b5fa;0x99bafd;0x9fbefd;0xa5c3fd;0xa6c2f7;0xabc8fd;0xacc7f7;0xb2cdfb;0xb2cbf5;0xb8d1f9;0xb8cef3;0xbed4f7;0xbed2f1;0xc4d7f4;0xc4d3ee;0xcad8f0;0xc9d6e8;0xcfd9e9;0xcfd8e3;0xd5dce7;0xd7dbe1;0xd8d9db;0xdeddde;0xdedad8;0xdfd7d2;0xe5dad5;0xe5d7cf;0xe5d2c9;0xebd5ca;0xe7cfc3;0xedd2c4;0xeaccbd;0xf0cfbe;0xecc8b6;0xf2cbb7;0xefc4b0;0xf5c6b1;0xf0bfaa;0xf6c0aa;0xf1bba4;0xf7bda4;0xf1b79e;0xf7b99e;0xf2b198;0xf1ab92;0xf1a78c;0xf0a187;0xef9c81;0xed967b;0xeb9076;0xe98a71;0xe8846c;0xe57e66;0xe37863;0xe6745d;0xe0725d;0xdd6c5a;0xdd6754;0xdc6150;0xd65f50;0xd8594b;0xd2574b;0xd45147;0xce4f47;0xd04943;0xca4944;0xcc433f;0xc6413f;0xc83b3a;0xc2393b;0xc43338;0xbe3339;0xc02d35;0xbe2733;0xbb2130;0xb81b2f;0xb6152e;",
            "Blue-Yellow;61;0x430956;0x440f5b;0x461560;0x461966;0x471f6b;0x482570;0x472b76;0x46317a;0x45377d;0x433d81;0x414383;0x3f4986;0x3e4f87;0x3b5589;0x385b8a;0x35618b;0x33678b;0x316d8d;0x2f738d;0x2e798d;0x2c7f8d;0x2a858c;0x298b8c;0x28918b;0x27978a;0x289d88;0x2aa386;0x2da983;0x32af7f;0x38b27c;0x3eb779;0x44ba76;0x4abd73;0x50c16f;0x56c36c;0x5cc569;0x62c766;0x68c963;0x6ecb60;0x74cd5c;0x7acf59;0x80d056;0x86d253;0x8cd350;0x92d54e;0x99d64a;0xa0d746;0xa6d944;0xacda42;0xb2db3f;0xb8db3d;0xbedc3b;0xc5de39;0xcbde38;0xd1df37;0xd7e037;0xdde137;0xe3e137;0xe9e238;0xefe33a;0xf5e43c;",
            "White-Green;53;0xffffff;0xfafdf9;0xf4fbf3;0xeff8ed;0xe9f6e7;0xe3f3e1;0xdff1db;0xdaeed5;0xd4eccf;0xd0eac9;0xcae7c3;0xc4e5bd;0xbee2b7;0xb9e0b1;0xb3deab;0xaedca5;0xa8da9f;0xa3d799;0x9ed593;0x99d38d;0x93d087;0x8fce81;0x89cc7d;0x85ca77;0x7fc771;0x79c56c;0x73c368;0x6dc165;0x68be5f;0x62bc5c;0x5cb957;0x56b653;0x50b44e;0x4ab14a;0x44af46;0x3dac41;0x37aa3b;0x31a737;0x2ba432;0x25a12d;0x1f9f28;0x1b9924;0x189323;0x188d24;0x168722;0x168124;0x147b24;0x137523;0x136f24;0x116924;0x116326;0xf5d25;0xf5727;",
            "White-Purple;82;0xebf0f2;0xf0f6f8;0xe5ecef;0xeaf3f8;0xdfe8ed;0xe4eff5;0xd9e3eb;0xdeebf3;0xd3dfe8;0xcddae4;0xd3e3ee;0xc7d6e2;0xcdddea;0xc1d1df;0xc7d9e8;0xc1d3e5;0xbbccdc;0xbbd0e3;0xb5c9db;0xb5cbe1;0xafc4d9;0xafc7df;0xa9c0d6;0xa9c3dc;0xa3bcd4;0xa3bfda;0x9db7d2;0x99b1cf;0x96abca;0x97abd0;0x94a5c8;0x909fc5;0x8e99c1;0x8b93be;0x8b8dbb;0x8b87b7;0x8d87bd;0x8b81b4;0x8c81ba;0x8b7bb2;0x8b75af;0x8d6fb1;0x8b6fab;0x8b69ae;0x8969a8;0x8b63ac;0x8863a5;0x895da7;0x875aa1;0x8854a3;0x87539d;0x884da0;0x864c9a;0x87469c;0x854696;0x874099;0x844093;0x863a95;0x843a8f;0x853490;0x82348a;0x832e8c;0x822e86;0x832887;0x822283;0x811c7e;0x7c1d78;0x7d1778;0x771573;0x72146d;0x6c1268;0x6c1868;0x660e62;0x661562;0x600e5d;0x60145d;0x5a0a57;0x5a1157;0x540752;0x540e53;0x4e074c;0xffffff;"
    };

    private static void init() {
        if (name2ColorTable.size() == 0)
            parseTables(BuiltInColorTables);
    }

    private static final Map<String, ColorTable> name2ColorTable = new TreeMap<>();

    /**
     * get a named color table
     *
     * @param name
     * @return color table
     */
    public static ColorTable getColorTable(String name) {
        init();
        if (name != null && name2ColorTable.keySet().contains(name)) {
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
     * get all names of defined tables ordered
     *
     * @return names
     */
    public static String[] getNamesOrdered() {
        init();
        ArrayList<String> list = new ArrayList<>(BuiltInColorTables.length);
        for (String aLine : BuiltInColorTables) {
            list.add(aLine.split(";")[0]);
        }
        for (String name : name2ColorTable.keySet()) {
            if (!list.contains(name))
                list.add(name);
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * get all names of defined tables ordered
     *
     * @return names
     */
    public static String[] getNamesForHeatMaps() {
        init();
        ArrayList<String> list = new ArrayList<>(BuiltInColorTables.length);
        for (String aLine : BuiltInColorTables) {
            String name = aLine.split(";")[0];
            if (name.contains("-"))
                list.add(name);
        }
        for (String name : name2ColorTable.keySet()) {
            if (!list.contains(name) && name.contains("-"))
                list.add(name);
        }
        return list.toArray(new String[list.size()]);
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
        String name = ProgramProperties.get("DefaultColorTableName", DefaultColorTableName);
        if (name2ColorTable.keySet().contains(name))
            return getColorTable(name);
        else
            return getColorTable(DefaultColorTableName);
    }

    public static void setDefaultColorTable(String name) {
        if (name2ColorTable.keySet().contains(name))
            ProgramProperties.put("DefaultColorTableName", name);

    }

    /**
     * gets the default color table
     *
     * @return default color table
     */
    public static ColorTable getDefaultColorTableHeatMap() {
        String name = ProgramProperties.get("DefaultColorTableHeatMap", DefaultColorTableHeatMap);
        if (name2ColorTable.keySet().contains(name))
            return getColorTable(name);
        else
            return getColorTable(DefaultColorTableHeatMap);
    }

    public static void setDefaultColorTableHeatMap(String name) {
        if (name2ColorTable.keySet().contains(name))
            ProgramProperties.put("DefaultColorTableHeatMap", name);

    }
}
