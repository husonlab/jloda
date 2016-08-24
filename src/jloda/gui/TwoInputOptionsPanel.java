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

import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;

/**
 * two input options panel
 * Created by huson on 8/24/16.
 */
public class TwoInputOptionsPanel<T, S> {

    /**
     * show a two value input dialog
     *
     * @param title
     * @param label1
     * @param value1
     * @param label2
     * @param value2
     * @return true, if not canceled
     */
    public static String[] show(Component parent, String title, String label1, String value1, String toolTip1, String label2, String value2, String toolTip2) {
        final JTextField field1 = new JTextField(8);
        field1.setText(value1);
        field1.setToolTipText(toolTip1);

        final JLabel jLabel1 = new JLabel(label1 + ":  ");
        jLabel1.setToolTipText(toolTip1);

        final JTextField field2 = new JTextField(8);
        field2.setText(value2);
        field2.setToolTipText(toolTip2);

        final JLabel jLabel2 = new JLabel(label2 + ":  ");
        jLabel2.setToolTipText(toolTip2);

        final JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(2, 2));
        myPanel.add(jLabel1);
        myPanel.add(field1);
        myPanel.add(jLabel2);
        myPanel.add(field2);

        final int result = JOptionPane.showConfirmDialog(parent, myPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                ProgramProperties.getProgramIcon());
        if (result == JOptionPane.OK_OPTION) {
            return new String[]{field1.getText(), field2.getText()};
        } else
            return null;
    }
}
