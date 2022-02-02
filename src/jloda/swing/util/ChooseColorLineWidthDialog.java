/*
 * ChooseColorLineWidthDialog.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.util.NumberUtils;
import jloda.util.Pair;
import jloda.util.Single;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * choose a color and a line width
 * Daniel Huson, 2.2019
 */
public class ChooseColorLineWidthDialog {
    public final static JColorChooser colorChooser = new JColorChooser();

    /**
     * show a choose color dialog
     *
     * @return color chosen or null
     */
    public static Pair<Integer, Color> showDialog(JFrame parent, String title, int defaultLineWidth, Color defaultColor) {

        final Single<Integer> resultLineWidth = new Single<>();


        final JDialog chooser = new JDialog(parent, title, true);
        chooser.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        chooser.setLocationRelativeTo(parent);
        chooser.setSize(500, 500);

        final JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        chooser.getContentPane().add(main);
        final JPanel top = new JPanel();
        top.add(new Label("Line width:    "));
        final JTextField lineWidthTextField = new JTextField("" + defaultLineWidth);
        lineWidthTextField.setPreferredSize(new Dimension(50, 20));
        top.add(lineWidthTextField);
        main.add(top, BorderLayout.NORTH);

        if (defaultColor != null)
            colorChooser.setColor(defaultColor);

        final Single<Color> resultColor = new Single<>();


        main.add(colorChooser, BorderLayout.CENTER);

        final JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.setBorder(BorderFactory.createEtchedBorder());
        bottom.add(Box.createHorizontalGlue());

        final JButton cancelButton = new JButton();
        cancelButton.setAction(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent actionEvent) {
                resultColor.set(null);
                chooser.setVisible(false);
            }
        });
        bottom.add(cancelButton);

        final JButton applyButton = new JButton();
        applyButton.setAction(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent actionEvent) {
                resultLineWidth.set(NumberUtils.parseInt(lineWidthTextField.getText()));
                resultColor.set(colorChooser.getColor());
                chooser.setVisible(false);
            }
        });
        bottom.add(applyButton);
        main.add(bottom, BorderLayout.SOUTH);

        chooser.setVisible(true);

        if (resultColor.get() == null)
            return null;
        else
            return new Pair<>(resultLineWidth.get(), resultColor.get());
    }
}
