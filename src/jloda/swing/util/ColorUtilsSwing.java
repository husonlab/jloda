/*
 * Colors.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.swing.util;

import jloda.util.AColor;

import java.awt.*;

/**
 * color utilies for swing (awt) colors
 * Daniel Huson, 11.2011, 10.2022
 */
public class ColorUtilsSwing {


	/**
	 * gets color as 'r g b' or 'r g b a' string  or string "null"
	 *
	 * @return r g b a
	 */
	public static String toString3Int(Color color) {
		if (color == null)
			return "null";
		final StringBuilder buf = new StringBuilder().append(color.getRed()).append(" ").append(color.getGreen()).append(" ").append(color.getBlue());
		if (color.getAlpha() < 255)
			buf.append(" ").append(color.getAlpha());
		return buf.toString();
	}

	/**
	 * gets a color as a background color
	 *
	 * @return color
	 */
	static public String getBackgroundColorHTML(Color color) {
		return String.format("<font bgcolor=#%x>", (color.getRGB() & 0xFFFFFF));
	}

	public static AColor convert(Color color) {
		return new AColor(color.getRGB());
	}

	public static Color convert(AColor color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
}
