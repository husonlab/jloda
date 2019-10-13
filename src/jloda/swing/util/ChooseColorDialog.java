/*
 * ChooseColorDialog.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.util;

import jloda.util.Single;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * choose a color
 * Daniel Huson, 4.2011
 */
public class ChooseColorDialog {
    public final static JColorChooser colorChooser = new JColorChooser();

    /**
     * show a choose color dialog
     *
     * @param parent
     * @param title
     * @param defaultColor
     * @return color chosen or null
     */
    public static Color showChooseColorDialog(JFrame parent, String title, Color defaultColor) {
        if (defaultColor != null)
            colorChooser.setColor(defaultColor);

        final Single<Color> result = new Single<>();

        final ActionListener okListener = actionEvent -> result.set(colorChooser.getColor());

        final ActionListener cancelListener = actionEvent -> result.set(null);

        final JDialog chooser = JColorChooser.createDialog(parent, title, true, colorChooser, okListener, cancelListener);

        chooser.setVisible(true);

        return result.get();
    }
}
