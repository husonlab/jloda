/*
 * UndoableChangeListViews2.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.undo;

import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * undoable change for a pair of list views (such as active/inactive taxa or trees)
 * Daniel Huson, 12.2016
 */
public class UndoableChangeListViews2<T> extends UndoableRedoableCommand {
    private final ArrayList<T> itemsA;
    private final ArrayList<T> itemsB;

    private final List<T> prevItemsA;
    private final List<T> prevItemsB;

    private final ListView<T> listViewA;
    private final ListView<T> listViewB;

    /**
     * constructor
     *
     * @param name       display name
     * @param listViewA  first list of items
     * @param prevItemsA previous value of first list of items
     * @param listViewB  second list of items
     * @param prevItemsB previous value of second list of items
     */
    public UndoableChangeListViews2(String name, ListView<T> listViewA, List<T> prevItemsA, ListView<T> listViewB, List<T> prevItemsB) {
        super(name);
        this.listViewA = listViewA;
        this.itemsA = new ArrayList<>(listViewA.getItems());
        this.prevItemsA = prevItemsA;
        this.listViewB = listViewB;
        this.itemsB = new ArrayList<>(listViewB.getItems());
        this.prevItemsB = prevItemsB;
    }

    /**
     * get items in list A. Use this  for access to previous items for next change
     *
     * @return items in list A
     */
    public ArrayList<T> getItemsA() {
        return itemsA;
    }

    /**
     * get list of items in list B. Use this  for access to previous items for next change
     *
     * @return items in list B
     */
    public ArrayList<T> getItemsB() {
        return itemsB;
    }

    @Override
    public void undo() {
        listViewA.getItems().setAll(prevItemsA);
        listViewB.getItems().setAll(prevItemsB);
    }

    @Override
    public void redo() {
        listViewA.getItems().setAll(itemsA);
        listViewB.getItems().setAll(itemsB);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof UndoableChangeListViews2) {
            final UndoableChangeListViews2 that = (UndoableChangeListViews2) other;
            return this.itemsA.equals(that.itemsA) && this.itemsB.equals(that.itemsB);
        }
        return false;
    }
}
