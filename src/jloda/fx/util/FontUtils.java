/*
 * FontUtils.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.fx.util;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import jloda.util.Basic;

/**
 * some font utilities
 * Daniel Huson, 3.2018
 */
public class FontUtils {
    /**
     * get the weight of a font
     *
     * @param font
     * @return the weight
     */
    public static FontWeight getWeight(Font font) {
        return getWeight(font.getStyle());
    }

    /**
     * get the posture
     *
     * @param font
     * @return posture
     */
    public static FontPosture getPosture(Font font) {
        return getPosture(font.getStyle());
    }

    /**
     * guess the weight
     *
     * @param style
     * @return weight
     */
    public static FontWeight getWeight(String style) {
        for (String word : style.split("\\s+")) {
            FontWeight weight = FontWeight.findByName(word.toUpperCase());
            if (weight != null)
                return weight;
        }
        return FontWeight.NORMAL;
    }

    /**
     * get the posture
     *
     * @param style
     * @return posture
     */
    public static FontPosture getPosture(String style) {
        for (String word : style.split("\\s+")) {
            FontPosture posture = FontPosture.findByName(word.toUpperCase());
            if (posture != null)
                return posture;
        }
        return FontPosture.REGULAR;
    }

    /**
     * creates a font for the given family, style and size
     *
     * @param family
     * @param style
     * @param size
     * @return new font
     */
    public static Font font(String family, String style, double size) {
        return Font.font(family, getWeight(style), getPosture(style), size);
    }

    /**
     * creates the same font with a different size
     *
     * @param font
     * @param size
     * @return new font of given size
     */
    public static Font font(Font font, double size) {
        if (size > 0 && size != font.getSize())
            return Font.font(font.getFamily(), getWeight(font), getPosture(font), size);
        else
            return font;
    }

    /**
     * write font as comma-separated string family,style,size
     *
     * @param font
     * @return font name as string
     */
    public static String toString(Font font) {
        return (font.getFamily() + "," + font.getStyle() + "," + font.getSize() + " ").replaceAll("\\.0* ", "");
    }

    /**
     * get value font from string in format family,style,size
     *
     * @param fontString
     * @return font
     */
    public static Font valueOf(String fontString) {
        final String[] tokens = fontString.split(",");
        if (tokens.length == 3 && Basic.isDouble(tokens[2]))
            return font(tokens[0], tokens[1], Basic.parseDouble(tokens[2]));
        else
            return null;
    }
}
