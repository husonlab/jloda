/*
 * ColorSchemeManager.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.util;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

public class ColorSchemeManager {
    private final ObservableMap<String, ObservableList<Color>> name2ColorSchemes = FXCollections.observableMap(new TreeMap<>());
    private final StringProperty lastColorScheme = new SimpleStringProperty("Retro29");

    public static String[] BuiltInColorTables = {
            "Caspian8;8;0Xf64d1b;0X8633bc;0X41a744;0X747474;0X2746bc;0Xff9301;0Xc03150;0X2198bc;" +
                    "Fews8;8;0X5da6dc;0Xfba53a;0X60be68;0Xf27db0;0Xb39230;0Xb376b2;0Xdfd040;0Xf15954;" +
                    "Pairs12;12;0X267ab2;0Xa8cfe3;0X399f34;0Xb4df8e;0Xe11f27;0Xfa9b9b;0Xfe7f23;0Xfcbf75;0X6a4199;0Xcab3d6;0Xb05a2f;0Xffff9f;" +
                    "Pale12;12;0Xdbdada;0Xf27e75;0Xba7bbd;0Xceedc5;0Xfbf074;0Xf8cbe5;0Xf9b666;0Xfdffb6;0X86b0d2;0X95d6c8;0Xb3e46c;0Xbfb8da;" +
                    "Rainbow13;13;0Xed1582;0Xf73e43;0Xee8236;0Xe5ae3d;0Xe5da45;0Xa1e443;0X22da27;0X21d18e;0X21c8c7;0X1ba2fc;0X2346fb;0X811fd9;0X9f1cc5;" +
                    "Retro29;29;0Xf4d564;0X97141d;0Xe9af6b;0X82ae92;0X356c7c;0X5c8c83;0X3a2b27;0Xe28b90;0X242666;0Xc2a690;0Xb80614;0X35644f;0Xe3a380;0Xb9a253;0X72a283;0X73605b;0X94a0ad;0Xf7a09d;0Xe5c09e;0X4a4037;0Xcec07c;0X6c80bb;0X7fa0a4;0Xb9805b;0Xd5c03f;0Xdd802e;0X8b807f;0Xc42030;0Xc2603d;" +
                    "Sea9;9;0Xffffdb;0Xedfbb4;0Xc9ecb6;0X88cfbc;0X56b7c4;0X3c90bf;0X345aa7;0X2f2b93;0X121858;" +
                    "White-Green;160;0xfdfefd;0xfbfdfb;0xfafdf9;0xf8fcf7;0xf6fcf5;0xf4fbf3;0xf2faf1;0xf0f9ef;0xeff8ed;0xedf7eb;0xebf7e9;0xe9f6e7;0xe8f5e5;0xe6f4e3;0xe4f4e2;0xe2f3e0;" +
                    "0xe0f2de;0xdff1db;0xddf0d9;0xdbefd7;0xdaeed5;0xd8edd3;0xd6edd1;0xd4eccf;0xd3ebcd;0xd1eacb;0xd0eac9;0xcee9c7;0xcce8c5;0xcae7c3;0xc8e7c2;0xc7e6c0;0xc5e6be;" +
                    "0xc3e5bc;0xc1e4bb;0xc0e3b9;0xbee2b7;0xbce2b5;0xbae1b2;0xb8e0b0;0xb6dfae;0xb5deac;0xb3deab;0xb1dda9;0xb0dda7;0xaedca5;0xacdba3;0xaadaa1;0xa8da9f;0xa7d99d;" +
                    "0xa5d89b;0xa3d799;0xa1d697;0x9fd695;0x9ed593;0x9dd491;0x9bd48f;0x99d38d;0x97d28b;0x95d189;0x93d087;0x91cf85;0x8fce82;0x8dcd80;0x8bcd7e;0x89cc7d;0x87cb7b;" +
                    "0x86cb79;0x85ca77;0x83ca75;0x81c873;0x7fc771;0x7dc770;0x7bc66e;0x79c56c;0x77c46b;0x75c46a;0x73c368;0x71c267;0x6fc266;0x6dc165;0x6cc063;0x6abf61;0x68be5f;" +
                    "0x66be5f;0x64bd5d;0x62bc5c;0x60bb5b;0x5fba59;0x5db958;0x5bb856;0x59b755;0x57b754;0x55b653;0x53b651;0x51b550;0x50b44e;0x4eb24c;0x4cb24b;0x4ab14a;0x48b149;" +
                    "0x47b047;0x45af46;0x43af45;0x42ae43;0x40ad42;0x3dac41;0x3cac3f;0x3aab3e;0x39aa3c;0x37aa3b;0x35a93b;0x34a839;0x32a838;0x30a736;0x2fa535;0x2ca533;0x2aa431;" +
                    "0x29a230;0x27a22f;0x25a12d;0x23a02c;0x229f2a;0x209f29;0x1e9e27;0x1d9c26;0x1b9c25;0x1b9a24;0x1a9824;0x199623;0x199422;0x189223;0x199024;0x188e24;0x188c24;" +
                    "0x178a23;0x168822;0x178624;0x168424;0x168224;0x158024;0x147f22;0x147d23;0x147b24;0x157924;0x157725;0x137624;0x127423;0x117223;0x127023;0x136f25;0x126d25;" +
                    "0x126b25;0x116924;0x106723;0x106523;0x106525;0x116326;0x106126;0x105f25;",
            "Blue-Red;203;0x4156be;0x4055c2;0x4459c2;0x4259c6;0x465dc5;0x455dc9;0x4961c9;0x4860cd;0x4b64ce;0x4c67d2;0x4e68ce;0x4f6bd3;0x506cd7;0x536ed3;0x5471d8;0x5874d7;0x5674dc;" +
                    "0x5a77db;0x5978e0;0x5d7bdd;0x5c7ce2;0x607fdf;0x5f80e4;0x6483e2;0x6384e7;0x6588e9;0x6787e5;0x698ae9;0x6c8eeb;0x6e91ef;0x7091ea;0x7195f1;0x7395ed;0x7598f1;" +
                    "0x789bf5;0x799bf0;0x7b9ff6;0x7d9ff1;0x7fa2f5;0x82a6f9;0x83a5f4;0x86a9fa;0x86a9f5;0x8aadfb;0x8aacf6;0x8db1fb;0x8eb0f7;0x91b5fc;0x92b4f8;0x95b8fc;0x96b6f8;" +
                    "0x99bafd;0x9ab9f8;0x9dbdfd;0x9ebcf8;0xa1c0fd;0xa2bff8;0xa5c3fd;0xa6c3f8;0xa9c6fd;0xaac5f7;0xadcafc;0xaec9f6;0xb1ccfb;0xb2cbf5;0xb5cffa;0xb6cdf4;0xb9d2f9;" +
                    "0xbad1f5;0xbbd0f1;0xbed4f7;0xbfd4f3;0xbfd2ef;0xc3d6f4;0xc3d2ed;0xc7d6f2;0xc8d6ee;0xc7d5ea;0xccdaee;0xcbd6e8;0xcedaea;0xced7e4;0xd2dbe9;0xd3dae5;0xd2d8e1;" +
                    "0xd7dde5;0xd7dbe1;0xd6d9dd;0xdbdee1;0xdbdbdd;0xdad8d9;0xdfdcdc;0xdedad8;0xded7d4;0xe2dcd8;0xe2d9d4;0xe0d6d0;0xe6dad3;0xe5d7cf;0xe4d4cb;0xe9d8cf;0xe8d5cb;" +
                    "0xe6d1c7;0xead3c7;0xe7cfc3;0xebd1c3;0xe9cdbf;0xedcfbf;0xebccbb;0xefcdbb;0xecc8b6;0xf0cab7;0xeec5b2;0xf2c7b3;0xefc2ae;0xf3c4af;0xf0bfaa;0xf4c2ab;0xf1bca6;" +
                    "0xf5bea7;0xf2baa2;0xf7bda3;0xf1b79e;0xf6b99f;0xf5b59c;0xf2b198;0xf7b398;0xf2ad94;0xf6af95;0xf1aa90;0xf6ac91;0xf6a98d;0xf1a78c;0xf0a389;0xf6a589;0xf0a085;" +
                    "0xf4a185;0xef9c81;0xf49e81;0xf39a7e;0xee987d;0xf2967b;0xed947a;0xf19277;0xeb9076;0xef8e73;0xea8c73;0xee8a70;0xe9886f;0xed866d;0xe8846c;0xec8269;0xe68069;" +
                    "0xea7e66;0xe47c65;0xe97a64;0xe37863;0xe77660;0xe1755f;0xe5725d;0xe0705c;0xe46e5a;0xdd6c5a;0xe16a57;0xdb6857;0xdf6654;0xda6553;0xdc6150;0xd86151;0xda5d4f;" +
                    "0xd55d4f;0xd8594b;0xd3594c;0xd65549;0xd1554a;0xd45147;0xcf5148;0xd24d46;0xcd4d46;0xd04943;0xca4944;0xcd4540;0xc84541;0xc9413e;0xc93d3b;0xc53e3d;0xc5393a;" +
                    "0xc1383b;0xc53538;0xbf3439;0xc23037;0xbd3038;0xc02c35;0xbb2b35;0xbe2733;0xb92533;0xbb2130;0xb72132;0xba1d30;0xb51d31;0xb7192f;0xb31830;0xb4142e;"
    };

    private final LongProperty update = new SimpleLongProperty(0);

    private static ColorSchemeManager instance;

    public static ColorSchemeManager getInstance() {
        if (instance == null)
            instance = new ColorSchemeManager();
        return instance;
    }

    private ColorSchemeManager() {
        parseTables(ProgramProperties.get("ColorSchemes", BuiltInColorTables));
    }

    /**
     * parse the definition of tables
     *
     * @param tables
     */
    public void parseTables(String... tables) {
        int alpha = Math.max(0, Math.min(255, ProgramProperties.get("ColorAlpha", 255)));

        for (String table : tables) {
            final String[] tokens = Basic.split(table, ';');
            if (tokens.length > 0) {
                int i = 0;
                while (i < tokens.length) {
                    String name = tokens[i++];
                    int numberOfColors = Integer.valueOf(tokens[i++]);
                    final ObservableList<Color> colors = FXCollections.observableArrayList();
                    for (int k = 0; k < numberOfColors; k++) {
                        Color color = Color.web(tokens[i++]);
                        if (alpha < 255)
                            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                        colors.add(color);
                    }
                    if (colors.size() > 0 && !name2ColorSchemes.containsKey(name)) {
                        name2ColorSchemes.put(name, colors);
                    }
                }
            }
        }
    }

    public String writeTables() {
        final StringBuilder buf = new StringBuilder();
        for (String name : name2ColorSchemes.keySet()) {
            buf.append(String.format("%s;%d;", name.replaceAll(";", "_"), name2ColorSchemes.get(name).size()));
            for (Color color : name2ColorSchemes.get(name)) {
                buf.append(String.format("0X%02x%02x%02x;", (int) (255 * color.getRed()),
                        (int) (255 * color.getGreen()),
                        (int) (255 * color.getBlue())));

            }
        }
        return buf.toString();
    }

    public ObservableList<Color> getColorScheme(String name) {
        lastColorScheme.set(name);
        return name2ColorSchemes.get(name);
    }

    public void setColorScheme(String name, ObservableList<Color> colors) {
        name2ColorSchemes.put(name, colors);
        ProgramProperties.put("ColorSchemes", writeTables());
        update.set(update.get() + 1);
    }

    public String getLastColorScheme() {
        return lastColorScheme.get();
    }

    public ReadOnlyStringProperty lastColorSchemeProperty() {
        return lastColorScheme;
    }

    public Collection<String> getNames() {
        return new TreeSet<>(name2ColorSchemes.keySet());
    }

    public ObservableMap<String, ObservableList<Color>> getName2ColorSchemes() {
        return name2ColorSchemes;
    }
}
