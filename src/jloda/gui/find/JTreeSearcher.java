/**
 * JTreeSearcher.java 
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
package jloda.gui.find;

import jloda.util.Basic;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;

/**
 * jTree searcher
 * Daniel Huson, 2.2012
 */

/**
 * Class for finding labels in a jTree
 * Daniel Huson, 2.2012
 */
public class JTreeSearcher implements IObjectSearcher {
    private final String name;
    final JTree jTree;
    final Frame frame;
    protected DefaultMutableTreeNode current = null;

    final Set<DefaultMutableTreeNode> toSelect;
    final Set<DefaultMutableTreeNode> toDeselect;
    public static final String SEARCHER_NAME = "Tree";

    /**
     * constructor
     *
     * @param jTree
     */
    public JTreeSearcher(JTree jTree) {
        this(null, SEARCHER_NAME, jTree);
    }

    /**
     * constructor
     *
     * @param frame
     * @param jTree
     */
    public JTreeSearcher(Frame frame, JTree jTree) {
        this(frame, SEARCHER_NAME, jTree);
    }

    /**
     * constructor
     *
     * @param
     * @param jTree
     */
    public JTreeSearcher(Frame frame, String name, JTree jTree) {
        this.frame = frame;
        this.name = name;
        this.jTree = jTree;
        toSelect = new HashSet<>();
        toDeselect = new HashSet<>();
    }

    /**
     * get the parent component
     *
     * @return parent
     */
    public Component getParent() {
        return frame;
    }

    /**
     * get the name for this type of search
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * goto the first object
     */
    public boolean gotoFirst() {
        current = (DefaultMutableTreeNode) jTree.getModel().getRoot();
        return isCurrentSet();
    }

    /**
     * goto the next object
     */
    public boolean gotoNext() {
        if (current == null)
            gotoFirst();
        else
            current = current.getNextNode();
        return isCurrentSet();
    }

    /**
     * goto the last object
     */
    public boolean gotoLast() {
        current = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) jTree.getModel().getRoot()).getLastChild();
        return isCurrentSet();
    }

    /**
     * goto the previous object
     */
    public boolean gotoPrevious() {
        if (current == null)
            gotoLast();
        else
            current = current.getPreviousNode();
        return isCurrentSet();
    }

    /**
     * is the current object selected?
     *
     * @return true, if selected
     */
    public boolean isCurrentSelected() {
        return isCurrentSet() && jTree.getSelectionModel().isPathSelected(getPath(current));
    }

    private TreePath getPath(TreeNode node) {
        java.util.List<TreeNode> list = new ArrayList<>();

        // Add all nodes to list
        while (node != null) {
            list.add(node);
            node = node.getParent();
        }
        Collections.reverse(list);

        // Convert array of nodes to TreePath
        return new TreePath(list.toArray());
    }

    /**
     * set selection state of current object
     *
     * @param select
     */
    public void setCurrentSelected(boolean select) {
        if (current != null) {
            if (select)
                toSelect.add(current);
            else
                toDeselect.add(current);
        }
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select) {
        if (select) {
            int count = jTree.getRowCount();
            TreePath[] paths = new TreePath[count];
            for (int i = 0; i < count; i++) {
                paths[i] = jTree.getPathForRow(i);
            }
            jTree.addSelectionPaths(paths);
        } else {
            jTree.clearSelection();
        }
    }

    /**
     * get the label of the current object
     *
     * @return label
     */
    public String getCurrentLabel() {
        if (current == null)
            return null;
        else
            return current.toString();
    }

    /**
     * set the label of the current object
     *
     * @param newLabel
     */
    public void setCurrentLabel(String newLabel) {
    }

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    public boolean isGlobalFindable() {
        return jTree.getComponentCount() > 0;
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    public boolean isSelectionFindable() {
        return false;
    }

    /**
     * is the current object set?
     *
     * @return true, if set
     */
    public boolean isCurrentSet() {
        return current != null;
    }

    /**
     * something has been changed or selected, update view
     */
    public void updateView() {
        // selectAll(false);
        toSelect.removeAll(toDeselect);

        TreePath[] paths = new TreePath[toSelect.size()];
        int count = 0;
        for (TreeNode node : toSelect) {
            paths[count++] = getPath(node);
        }
        jTree.addSelectionPaths(paths);

        if (current != null) {
            final TreePath path = getPath(current);
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        jTree.expandPath(path);  // this just doesn't work....
                        jTree.scrollPathToVisible(path);
                    }
                });
            } catch (Exception e) {
                Basic.caught(e);
            }
        }

        toSelect.clear();
        toDeselect.clear();
    }

    /**
     * does this searcher support find all?
     *
     * @return true, if find all supported
     */
    public boolean canFindAll() {
        return true;
    }

    /**
     * how many objects are there?
     *
     * @return number of objects or -1
     */
    public int numberOfObjects() {
        return jTree.getComponentCount();
    }

    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }
}
