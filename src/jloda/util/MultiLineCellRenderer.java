/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            textPane.setBackground(UIManager.getColor("Tree.selectionBackground"));
        } else {
            textPane.setBackground(UIManager.getColor("Tree.textBackground"));
        }
        textPane.revalidate();
        return textPane;
    }
}

