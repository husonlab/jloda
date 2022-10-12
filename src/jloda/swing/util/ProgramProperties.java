/*
 * ProgramProperties.java Copyright (C) 2022. Daniel H. Huson
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
 *
 */

package jloda.swing.util;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ProgramProperties extends jloda.util.ProgramProperties {

	public static final Color SELECTION_COLOR = new Color(252, 208, 102);
	public static final Color SELECTION_COLOR_DARKER = new Color(210, 190, 95);
	public static final Color SELECTION_COLOR_ADDITIONAL_TEXT = new Color(93, 155, 206);
	private static final ArrayList<ImageIcon> programIcons = new ArrayList<>();
	static private final Font defaultFont = new Font("Arial", Font.PLAIN, 12);
	public static PageFormat pageFormat = null;


	public static ArrayList<ImageIcon> getProgramIcons() {
		return programIcons;
	}

	public static void setProgramIcons(Collection<ImageIcon> icons) {
		programIcons.clear();
		for (ImageIcon icon : icons) {
			if (icon != null)
				programIcons.add(icon);
		}
	}

	public static ArrayList<Image> getProgramIconImages() {
		final ArrayList<Image> images = new ArrayList<>();
		for (ImageIcon icon : getProgramIcons()) {
			images.add(icon.getImage());
		}
		return images;
	}

	/**
	 * gets the program icon
	 *
	 * @return program icon
	 */
	public static ImageIcon getProgramIcon() {
		ImageIcon result = null;
		for (ImageIcon imageIcon : getProgramIcons()) {
			if (result == null || imageIcon.getIconHeight() < 128 && imageIcon.getIconHeight() > result.getIconHeight()) // 64 preferred
				result = imageIcon;
		}
		return result;
	}

	public static PageFormat getPageFormat() {
		return pageFormat;
	}

	public static void setPageFormat(PageFormat pageFormat) {
		ProgramProperties.pageFormat = pageFormat;
	}

	/**
	 * put a property
	 */
	public static void put(String key, Font value) {
		put(key, value.getFamily(), value.getStyle(), value.getSize());
	}

	/**
	 * put a property
	 */
	public static void put(String key, String family, Integer style0, Integer size0) {
		Font def = get(key, defaultFont);
		String name;
		if (family == null)
			name = def.getFamily();
		else
			name = family;
		int style;
		style = Objects.requireNonNullElseGet(style0, def::getStyle);
		int size;
		size = Objects.requireNonNullElseGet(size0, def::getSize);

		switch (style) {
			case Font.BOLD + Font.ITALIC -> name += "-BOLDITALIC";
			case Font.BOLD -> name += "-BOLD";
			case Font.ITALIC -> name += "-ITALIC";
			case Font.PLAIN -> name += "-PLAIN";
		}
		name += "-" + size;
		props.setProperty(key, name);
	}

	/**
	 * gets a font property
	 *
	 * @return font or default
	 */
	public static Font get(String name, Font def) {
		String value = (String) props.get(name);
		if (value == null)
			return def;
		else {
			value = value.replaceAll(" ", "\\ ");
			return Font.decode(value);
		}
	}

	/**
	 * gets a color property
	 *
	 * @return set property or default
	 */
	public static Color get(Object name, Color def) {
		String value = (String) props.get(name);
		if (value == null || value.equalsIgnoreCase("null"))
			return def;
		else
			return Color.decode(value);
	}

	/**
	 * put a property
	 */
	public static void put(String key, Color value) {
		if (value == null)
			props.setProperty(key, "null");
		else
			props.setProperty(key, "" + value.getRGB());
	}
}
