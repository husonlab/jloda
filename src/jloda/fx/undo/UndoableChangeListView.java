/*
 * UndoableChangeListView.java Copyright (C) 2019. Daniel H. Huson
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
public class UndoableChangeListView<T> extends UndoableRedoableCommand {
    private final ArrayList<T> items;

    private final List<T> prevItems;

    private final ListView<T> listView;

    /**
     * constructor
     *
     * @param name      display name
     * @param listView  list of items
     * @param prevItems previous value of  list of items
     */
    public UndoableChangeListView(String name, ListView<T> listView, List<T> prevItems) {
        super(name);
        this.listView = listView;
        this.items = new ArrayList<>(listView.getItems());
        this.prevItems = prevItems;
    }

    /**
     * get items in list A
     *
     * @return items in list A
     */
    public ArrayList<T> getItems() {
        return items;
    }


    @Override
    public void undo() {
        listView.getItems().setAll(prevItems);
    }

    @Override
    public void redo() {
        listView.getItems().setAll(items);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof UndoableChangeListView) {
            final UndoableChangeListView that = (UndoableChangeListView) other;
            return this.items.equals(that.items);
        }
        return false;
    }
}
