/**
 * TableSearcher.java 
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

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * table searcher
 * Daniel Huson, 7.2008
 */
public class TableSearcher implements IObjectSearcher {
    private final Component parent;
    private final JTable table;
    private final String name;

    private int row = 0;
    private int col = 0;


    /**
     * constructor
     */
    public TableSearcher(Component parent, String name, JTable table) {
        this.parent = parent;
        this.name = name;
        this.table = table;
    }

    /**
     * goto the first object
     *
     * @return true, if successful
     */
    public boolean gotoFirst() {
        row = col = 0;
        return true;
    }

    /**
     * goto the next object
     *
     * @return true, if successful
     */
    public boolean gotoNext() {
        if (col + 1 < table.getColumnCount()) {
            col++;
            return true;
        } else if (row + 1 < table.getRowCount()) {
            col = 0;
            row++;
            return true;
        } else {
            row = 0;
            col = -1;
            selectAll(false);
            return false;
        }
    }

    /**
     * goto the last object
     *
     * @return true, if successful
     */
    public boolean gotoLast() {
        if (table.getRowCount() > 0 && table.getColumnCount() > 0) {
            row = table.getRowCount() - 1;
            col = table.getColumnCount() - 1;
            return true;
        } else {
            row = col = 0;
            return false;
        }
    }

    /**
     * goto the previous object
     *
     * @return true, if successful
     */
    public boolean gotoPrevious() {
        if (col > 0) {
            col--;
            return true;
        } else if (row > 0) {
            col = table.getColumnCount() - 1;
            row--;
            return true;
        } else {
            row = table.getRowCount() - 1;
            col = table.getColumnCount();
            selectAll(false);
            return false;
        }
    }

    /**
     * is the current object set?
     *
     * @return true, if set
     */
    public boolean isCurrentSet() {
        return row < table.getRowCount() && col < table.getColumnCount();
    }

    /**
     * is the current object selected?
     *
     * @return true, if selected
     */
    public boolean isCurrentSelected() {
        return isCurrentSet() && table.isCellSelected(row, col);
    }

    /**
     * set selection state of current object
     *
     * @param select
     */
    public void setCurrentSelected(boolean select) {
        if (isCurrentSet()) {
            table.setRowSelectionInterval(row, row);
            table.setColumnSelectionInterval(col, col);
        }
    }

    /**
     * get the label of the current object
     *
     * @return label
     */
    public String getCurrentLabel() {
        if (isCurrentSet())
            return table.getValueAt(row, col).toString();
        else
            return "";
    }

    /**
     * set the label of the current object
     *
     * @param newLabel
     */
    public void setCurrentLabel(String newLabel) {
        if (isCurrentSet())
            table.setValueAt(newLabel, row, col);
    }

    /**
     * how many objects are there?
     *
     * @return number of objects or -1
     */
    public int numberOfObjects() {
        return table.getRowCount() * table.getColumnCount();
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
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    public boolean isGlobalFindable() {
        return numberOfObjects() > 0;
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    public boolean isSelectionFindable() {
        return table.getSelectedRowCount() > 0;
    }

    /**
     * something has been changed or selected, update view
     */
    public void updateView() {
    }

    /**
     * does this searcher support find all?
     *
     * @return true, if find all supported
     */
    public boolean canFindAll() {
        return false;
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select) {
        if (select)
            table.selectAll();
        else
            table.clearSelection();
    }

    /**
     * get the parent component
     *
     * @return parent
     */
    public Component getParent() {
        return parent;
    }

    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }
}
