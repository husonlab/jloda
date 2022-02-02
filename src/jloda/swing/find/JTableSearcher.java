/*
 * JTableSearcher.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.swing.find;

import jloda.util.Pair;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for finding labels in a JTable
 * Daniel Huson, 2.2012
 */
public class JTableSearcher implements IObjectSearcher {
    private final String name;
    final JTable table;
    final Frame frame;
    protected final Pair<Integer, Integer> current = new Pair<>(-1, -1);

    final Set<Pair<Integer, Integer>> toSelect;
    final Set<Pair<Integer, Integer>> toDeselect;
    public static final String SEARCHER_NAME = "JTable";

    /**
     * constructor
     *
	 */
    public JTableSearcher(JTable table) {
        this(null, SEARCHER_NAME, table);
    }

    /**
     * constructor
     *
	 */
    public JTableSearcher(Frame frame, JTable table) {
        this(frame, SEARCHER_NAME, table);
    }

    /**
     * constructor
     *
     * @param
	 */
    public JTableSearcher(Frame frame, String name, JTable table) {
        this.frame = frame;
        this.name = name;
        this.table = table;
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
        current.setFirst(0);
        current.setSecond(0);
        boolean tried; //  did we try a cell? If yes and the column has a non zero width, then we use it
        do {
            tried = false;
            if (current.getSecond() < table.getModel().getColumnCount() - 1) {
                current.setSecond(current.getSecond() + 1);
                tried = true;
            } else if (current.getFirst() < table.getModel().getRowCount() - 1) {
                current.setFirst(current.getFirst() + 1);
                current.setSecond(0);
                tried = true;
            }
            if (tried) {
                TableColumnModel model = table.getColumnModel();
                if (model.getColumn(current.getSecond()).getMaxWidth() > 0)
                    break;
            }
        }
        while (tried);

        if (!tried) {
            current.setFirst(0);
            current.setSecond(0);
        }
        return isCurrentSet();
    }

    /**
     * goto the next object
     */
    public boolean gotoNext() {
        if (isCurrentSet()) {
            boolean tried; //  did we try a cell? If yes and the column has a non zero width, then we use it
            do {
                tried = false;
                if (current.getSecond() < table.getModel().getColumnCount() - 1) {
                    current.setSecond(current.getSecond() + 1);
                    tried = true;
                } else if (current.getFirst() < table.getModel().getRowCount() - 1) {
                    current.setFirst(current.getFirst() + 1);
                    current.setSecond(0);
                    tried = true;
                }
                if (tried) {
                    TableColumnModel model = table.getColumnModel();
                    if (model.getColumn(current.getSecond()).getMaxWidth() > 0)
                        break;
                }
            }
            while (tried);

            if (!tried) {
                current.setFirst(-1);
                current.setSecond(-1);
            }
        } else
            gotoFirst();
        return isCurrentSet();
    }

    /**
     * goto the last object
     */
    public boolean gotoLast() {
        current.setFirst(table.getModel().getRowCount() - 1);
        current.setSecond(table.getModel().getColumnCount() - 1);

        return isCurrentSet();
    }

    /**
     * goto the previous object
     */
    public boolean gotoPrevious() {
        if (isCurrentSet()) {
            if (current.getSecond() > 0)
                current.setSecond(current.getSecond() - 1);
            else if (current.getFirst() > 0) {
                current.setFirst(current.getFirst() - 1);
                current.setSecond(table.getModel().getColumnCount() - 1);
            } else {
                current.setFirst(-1);
                current.setSecond(-1);
            }
        } else
            gotoLast();
        return isCurrentSet();
    }

    /**
     * is the current object selected?
     *
     * @return true, if selected
     */
    public boolean isCurrentSelected() {
        return isCurrentSet() && table.isCellSelected(current.getFirst(), table.getSelectedColumn());
    }

    /**
     * set selection state of current object
     *
	 */
    public void setCurrentSelected(boolean select) {
        if (select)
            toSelect.add(new Pair<>(current.getFirst(), current.getSecond()));
        else
            toDeselect.add(new Pair<>(current.getFirst(), current.getSecond()));
    }

    /**
     * set select state of all objects
     *
	 */
    public void selectAll(boolean select) {
        if (select) {
            table.selectAll();
        } else {
            table.clearSelection();
        }
    }

    /**
     * get the label of the current object
     *
     * @return label
     */
    public String getCurrentLabel() {
        if (!isCurrentSet())
            return null;
        else
            return table.getModel().getValueAt(current.getFirst(), current.getSecond()).toString();
    }

    /**
     * set the label of the current object
     *
	 */
    public void setCurrentLabel(String newLabel) {
    }

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    public boolean isGlobalFindable() {
        return table.getComponentCount() > 0;
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
        return current.getFirst() >= 0 && current.getFirst() < table.getModel().getRowCount() && current.getSecond() >= 0 && current.getSecond() < table.getModel().getColumnCount();
    }

    /**
     * something has been changed or selected, update tree
     */
    public void updateView() {

        for (Pair<Integer, Integer> pair : toDeselect) {
            if (table.isCellSelected(pair.getFirst(), pair.getSecond()))
                table.changeSelection(pair.getFirst(), pair.getSecond(), true, false);
        }

        for (Pair<Integer, Integer> pair : toSelect) {
            if (!table.isCellSelected(pair.getFirst(), pair.getSecond())) {
                table.changeSelection(pair.getFirst(), pair.getSecond(), true, false);
            }
        }

        /*
        if (isCurrentSet()) {
            Rectangle rect = table.getCellRect(current.getFirst(), current.getSecond(), true);
            table.scrollRectToVisible(rect);
        }
        */

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
        return table.getModel().getRowCount() * table.getModel().getColumnCount();
    }

    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }
}
