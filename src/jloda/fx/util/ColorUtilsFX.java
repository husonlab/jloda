/*
 * ColorUtilsFX.java Copyright (C) 2022. Daniel H. Huson
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

import javafx.scene.paint.Color;
import jloda.util.AColor;

import java.util.Random;

/**
 * utilities for FX colors
 * Daniel Huson, 10.2022
 */
public class ColorUtilsFX {
	public static boolean isColor(String text) {
		if (text.equals("random"))
			return true;
		{
			try {
				javafx.scene.paint.Color.web(text);
				return true;
			} catch (Exception ignored) {
				return false;
			}
		}
	}

	public static Color parseColor(String text) {
		if (text.equals("random")) {
			var random = new Random();
			return new Color(random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
		} else
			return Color.web(text);
	}

	public static String toStringCSS(Color color) {
		return color.toString().replace("0x", "#");
	}

	public static AColor convert(Color color) {
		return new AColor((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity());
	}

	public static Color convert(AColor color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
}
