/**
 * PaletteManager.java 
 * Copyright (C) 2016 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package jloda.gui;

import jloda.util.Alert;
import jloda.util.ProgramProperties;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.List;

/**
 * manges colors used in comparisons
 * Daniel Huson, 5.2009
 */
public class PaletteManager {
    private static Color[] colors;

    /**
     * get the i-th color
     *
     * @param i
     * @return color
     */
    public static Color get(int i) {
        if (colors == null)
            loadColors();
        return colors[Math.abs(i) % colors.length];
    }

    /**
     * get the i-th color
     *
     * @param i
     * @param alpha
     * @return color
     */
    public static Color get(int i, int alpha) {
        if (colors == null)
            loadColors();
        Color color = colors[Math.abs(i) % colors.length];
        if (color.getRed() > 210 && color.getGreen() > 210 && color.getBlue() > 210)
            color = color.darker();

        if (color.getAlpha() == alpha)
            return color;
        else
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * get the number of colors
     *
     * @return number of colors
     */
    public static int getSize() {
        if (colors == null)
            loadColors();
        return colors.length;
    }

    /**
     * get the list of all colors
     *
     * @return colors
     */
    public static List<Color> getColors() {
        if (colors == null)
            loadColors();
        return Arrays.asList(colors);
    }

    /**
     * set the palette to the given colors
     *
     * @param colors
     */
    public static void setColors(Collection<Color> colors) {
        PaletteManager.colors = colors.toArray(new Color[colors.size()]);
        StringBuilder buf = new StringBuilder();
        for (Color color : colors) {
            buf.append(" 0x").append(Integer.toHexString(0x00FFFFFF & color.getRGB()));

        }
        ProgramProperties.put("ColorPalette", buf.toString());
        // int alpha = ProgramProperties.get("ColorAlpha", 200);
    }

    /**
     * load or reload colors
     */
    public static void loadColors() {
        int alpha = ProgramProperties.get("ColorAlpha", 255);
        java.util.List<Color> list = loadPaletteFromString(ProgramProperties.get("ColorPalette", ""));
        if (list.size() == 0)
            list = getDefaultPalette();
        colors = list.toArray(new Color[list.size()]);
        if (alpha < 255) {
            for (int i = 0; i < colors.length; i++) {
                colors[i] = new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), alpha);
            }
        }
    }

    /**
     * load a color palette from a string
     *
     * @param string
     * @return colors in file
     */
    static public java.util.List<Color> loadPaletteFromString(String string) {
        java.util.List<Color> result = new LinkedList<>();

        try {
            StringTokenizer st = new StringTokenizer(string);
            while (st.hasMoreTokens()) {
                result.add(Color.decode(st.nextToken()));
            }
        } catch (Exception ex) {
            new Alert("Load color palette failed: " + ex);
        }
        return result;
    }

    /**
     * get the default palette
     *
     * @return default palette
     */
    static public java.util.List<Color> getDefaultPalette() {
        java.util.List<Color> colors = new LinkedList<>();

        colors.add(Color.decode("0xFF0000")); // Red
        colors.add(Color.decode("0x0000FF")); // Blue
        colors.add(Color.decode("0x008000")); // Green
        colors.add(Color.decode("0xFFFF00")); // Yellow
        colors.add(Color.decode("0xFF00FF")); // Magenta
        colors.add(Color.decode("0x00FFFF")); // Cyan
        colors.add(Color.decode("0xFF1493")); // Deep pink        
        colors.add(Color.decode("0xFFA500")); // Orange

        colors.add(Color.decode("0xEE82EE")); // Violet
        colors.add(Color.decode("0x40E0D0")); // Turquoise
        colors.add(Color.decode("0xF0E68C")); // Khaki
        colors.add(Color.decode("0x800000")); // Maroon
        colors.add(Color.decode("0xDA70D6")); // Orchid
        colors.add(Color.decode("0x6B8E23")); // Olivedrab
        colors.add(Color.decode("0x0000CD")); // Mediumblue
        colors.add(Color.decode("0xB8860B")); // Darkgoldenrod
        colors.add(Color.decode("0x808080")); // Gray
        colors.add(Color.decode("0xCD853F")); // Peru
        colors.add(Color.decode("0xF4A460")); // Sandy brown
        colors.add(Color.decode("0xFFB6C1")); // Lightpink
        colors.add(Color.decode("0xE9967A")); // Dark salmon
        colors.add(Color.decode("0xB0C4DE")); // Lightsteel blue
        colors.add(Color.decode("0xADFF2F")); // Green yellow
        colors.add(Color.decode("0x191970")); // Midnightblue
        colors.add(Color.decode("0x7FFFD4")); // Aquamarine
        colors.add(Color.decode("0x228B22")); // Forestgreen
        colors.add(Color.decode("0xBA55D3")); // Medium orchid
        colors.add(Color.decode("0xFF6347")); // Tomato
        colors.add(Color.decode("0x00FA9A")); // Medium springgreen
        colors.add(Color.decode("0xA52A2A")); // Brown
        colors.add(Color.decode("0xFFA07A")); // Lightsalmon
        colors.add(Color.decode("0xC0C0C0")); // Silver
        colors.add(Color.decode("0xDDA0DD")); // Plum
        colors.add(Color.decode("0x7B68EE")); // Medium slate blue
        colors.add(Color.decode("0x8A2BE2")); // Blue violet
        colors.add(Color.decode("0x9370DB")); // Medium purple
        colors.add(Color.decode("0x87CEEB")); // Skyblue
        colors.add(Color.decode("0x7CFC00")); // Lawngreen
        colors.add(Color.decode("0x6495ED")); // Cornflower blue
        colors.add(Color.decode("0x4682B4")); // Steelblue
        colors.add(Color.decode("0x2F4F4F")); // Darkslategray
        colors.add(Color.decode("0x800080")); // Purple
        colors.add(Color.decode("0xFF8C00")); // Darkorange
        colors.add(Color.decode("0xC71585")); // Mediumvioletred
        colors.add(Color.decode("0xA9A9A9")); // Dark gray
        colors.add(Color.decode("0xDC143C")); // Crimson
        colors.add(Color.decode("0x2E8B57")); // Seagreen
        colors.add(Color.decode("0xBDB76B")); // Dark khaki
        colors.add(Color.decode("0xDEB887")); // Burlywood
        colors.add(Color.decode("0x87CEFA")); // Lightsky blue
        colors.add(Color.decode("0x8B4513")); // Saddlebrown
        colors.add(Color.decode("0x00FF7F")); // Springgreen
        colors.add(Color.decode("0xB22222")); // Firebrick
        colors.add(Color.decode("0x696969")); // Dimgray
        colors.add(Color.decode("0xFA8072")); // Salmon
        colors.add(Color.decode("0x4169E1")); // Royal blue
        colors.add(Color.decode("0x20B2AA")); // Lightseagreen
        colors.add(Color.decode("0xFF69B4")); // Hotpink
        colors.add(Color.decode("0x006400")); // Darkgreen
        colors.add(Color.decode("0x9ACD32")); // Yellow green
        colors.add(Color.decode("0x00DED1")); // Dark turquoise
        colors.add(Color.decode("0xD8BFD8")); // Thistle
        colors.add(Color.decode("0xD2B48C")); // Tan
        colors.add(Color.decode("0x32CD32")); // Limegreen
        colors.add(Color.decode("0x1E90FF")); // Dodger blue
        colors.add(Color.decode("0x483D8B")); // Darkslateblue
        colors.add(Color.decode("0x8B0000")); // Darkred
        colors.add(Color.decode("0xCD5C5C")); // Indianred
        colors.add(Color.decode("0xADD8E6")); // Lightblue
        colors.add(Color.decode("0x6A5ACD")); // Slate blue
        colors.add(Color.decode("0xDAA520")); // Goldenrod
        colors.add(Color.decode("0x556B2F")); // Darkolivegreen
        colors.add(Color.decode("0xFF4500")); // Orangered
        colors.add(Color.decode("0xA0522D")); // Sienna
        colors.add(Color.decode("0xFFD700")); // Gold
        colors.add(Color.decode("0x98FB98")); // Palegreen
        colors.add(Color.decode("0x9400D3")); // Darkviolet
        colors.add(Color.decode("0xFF7F50")); // Coral
        colors.add(Color.decode("0x4B0082")); // Indigo
        colors.add(Color.decode("0x5F9EA0")); // Cadet blue
        colors.add(Color.decode("0xD2691E")); // Chocolate
        colors.add(Color.decode("0x008B8B")); // Darkcyan
        colors.add(Color.decode("0x778899")); // Light slate gray
        colors.add(Color.decode("0x00BFFF")); // Deep sky blue
        colors.add(Color.decode("0x9932CC")); // Dark orchid
        colors.add(Color.decode("0x8B008B")); // Darkmagenta
        colors.add(Color.decode("0x8DBC8F")); // Dark seagreen
        colors.add(Color.decode("0x00FF00")); // Lime
        colors.add(Color.decode("0xF08080")); // Light coral
        colors.add(Color.decode("0xBC8F8F")); // Rosy brown
        colors.add(Color.decode("0xDB7093")); // Pale violetred
        colors.add(Color.decode("0x90EE90")); // Lightgreen
        colors.add(Color.decode("0x48D1CC")); // Medium turquoise
        colors.add(Color.decode("0x66CDAA")); // Medium aquamarine
        colors.add(Color.decode("0x7FFF00")); // Chartreuse
        colors.add(Color.decode("0x3CB371")); // Mediumseagreen
        colors.add(Color.decode("0x008080")); // Teal
        return colors;
    }

    /**
     * load a color palette from a file
     *
     * @param fileName
     */
    static public void loadPaletteFromFile(String fileName) {
        List<Color> result = new LinkedList<>();
        try {
            BufferedReader r = new BufferedReader(new FileReader(fileName));

            String aLine;
            while ((aLine = r.readLine()) != null) {
                if (aLine.length() == 0 || aLine.startsWith("#"))
                    continue;
                result.add(Color.decode(aLine));
            }
            r.close();
        } catch (Exception ex) {
            new Alert("Load color palette failed: " + ex);
        }
        setColors(result);
        System.err.println("Colors loaded: " + colors.length);
    }

    /**
     * exports the colors to a file
     *
     * @param fileName
     */
    static public void exportPaletteToFile(String fileName) {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(fileName));
            w.write("# Color palette created by jloda\n");
            w.write("# Number of colors=" + colors.length + "\n");
            for (Color color : colors) {
                w.write("0x" + Integer.toHexString(0x00FFFFFF & color.getRGB()) + "\n");
            }
            w.close();
        } catch (Exception ex) {
            new Alert("Save color palette FAILED: " + ex);
        }
    }
}
