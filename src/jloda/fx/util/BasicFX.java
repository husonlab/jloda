/*
 * BasicFX.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * basic stuff for FX
 * Daniel Huson, 3.2019
 */
public class BasicFX {
    /**
     * go to given line and given col
     *
     * @param lineNumber
     * @param col        if col<=1 or col>line length, will select the whole line, else selects line starting at given col
     */
    public static void gotoAndSelectLine(TextArea textArea, long lineNumber, int col) {
        if (col < 0)
            col = 0;
        else if (col > 0)
            col--; // because col is 1-based

        lineNumber = Math.max(1, lineNumber);
        final String text = textArea.getText();
        int start = 0;
        for (int i = 1; i < lineNumber; i++) {
            start = text.indexOf('\n', start + 1);
            if (start == -1) {
                System.err.println("No such line number: " + lineNumber);
                return;
            }
        }
        start++;
        if (start < text.length()) {
            int end = text.indexOf('\n', start);
            if (end == -1)
                end = text.length();
            if (start + col < end)
                start = start + col;
            textArea.requestFocus();
            textArea.selectRange(start, end);
        }
    }

    /**
     * get all children, including all Group nodes and their children
     *
     * @param children
     * @return recursively get all children
     */
    public static Collection<? extends Node> getAllChildrenRecursively(Collection<Node> children) {
        final ArrayList<Node> all = new ArrayList<>();
        final Queue<Node> stack = new LinkedList<>(children);
        while (stack.size() > 0) {
            final Node node = stack.remove();
            all.add(node);
            if (node instanceof Group) {
                stack.addAll(((Group) node).getChildren());
            }
        }
        return all;
    }
}
