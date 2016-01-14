/**
 * MultiLineCellRenderer.java 
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
package jloda.util;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * MultiLine Cell Renderer for the JTree to display more than one line
 *
 * @author daniel huson, 2010
 */
public class MultiLineCellRenderer implements TreeCellRenderer {
    public final static String GRAY = "<font color=#a0a0a0>";
    public final static String RED = "<font color=#ff0000>";
    public final static String BLUE = "<font color=#0000ff>";
    public final static String GREEN = "<font color=#00ff00>";

    /**
     * create a multi-line renderer
     */
    public MultiLineCellRenderer() {
    }

    /**
     * get the tree cell render component
     *
     * @param tree
     * @param value
     * @param isSelected
     * @param expanded
     * @param leaf
     * @param row
     * @param hasFocus
     * @return component
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);

        JEditorPane textPane = new JEditorPane();
        textPane.setEditable(false);

        textPane.setContentType("text/html");
        String text = Basic.trimEmptyLines(stringValue);
        if (text.contains("\n") && text.indexOf("\n") < text.length() - 1) // more than one line:
        {
            text = Basic.trimEmptyLines(text).replaceAll("\t", "&#9;");
            text = "<html><pre><font face=\"monospace\" size=\"3\">" + text + "</font></pre></html>";
        } else // only one line:
            text = "<html><font face=\"monospace\" size=\"3\">" + text + "</font></html>";

        textPane.setText(text);
        textPane.setEnabled(tree.isEnabled());
        if (isSelected) {
            textPane.setBackground(ProgramProperties.SELECTION_COLOR);
        } else {
            textPane.setBackground(UIManager.getColor("Tree.textBackground"));
        }
        textPane.revalidate();
        return textPane;
    }
}

