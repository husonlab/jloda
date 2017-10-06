/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package jloda.fx;


import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.MultipleSelectionModel;
import jloda.util.Basic;

import java.util.*;

/**
 * Selection model
 * Created by huson on 12/15/15.
 */
public class ASelectionModel<T> extends MultipleSelectionModel<T> {

    private final ObservableSet<Integer> selectedIndices; // the set of selected indices

    private T[] items; // need a copy of this array to map indices to objects, when required

    private int focusIndex = -1; // focus index

    private final ObservableList<Integer> unmodifiableSelectedIndices; // unmodifiable list of selected indices
    private final ObservableList<T> unmodifiableSelectedItems; // unmodifiable list of selected items

    private final ReadOnlyBooleanProperty empty;

    /**
     * Constructor
     *
     * @param items 0 or more items
     */
    @SafeVarargs
    public ASelectionModel(T... items) {
        this.items = Arrays.copyOf(items, items.length);  // use copy for safety
        selectedIndices = FXCollections.observableSet();

        // setup unmodifiable lists
        {
            // first setup observable array lists that listen for changes of the selectedIndices set
            final ObservableList<Integer> selectedIndicesAsList = FXCollections.observableArrayList();
            final ObservableList<T> selectedItems = FXCollections.observableArrayList();
            selectedIndices.addListener(new SetChangeListener<Integer>() {
                @Override
                public void onChanged(Change<? extends Integer> c) {
                    if (c.wasAdded()) {
                        selectedIndicesAsList.add(c.getElementAdded());
                        selectedItems.add(ASelectionModel.this.getItems()[c.getElementAdded()]);
                    } else if (c.wasRemoved()) {
                        selectedIndicesAsList.remove(c.getElementRemoved());
                        selectedItems.remove(ASelectionModel.this.getItems()[c.getElementRemoved()]);
                    }
                }
            });
            // wrap a unmodifiable observable list around the observable arrays lists
            unmodifiableSelectedIndices = FXCollections.unmodifiableObservableList(selectedIndicesAsList);
            unmodifiableSelectedItems = FXCollections.unmodifiableObservableList(selectedItems);
        }

        empty = (new SimpleSetProperty<>(selectedIndices).emptyProperty());
    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return unmodifiableSelectedIndices;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return unmodifiableSelectedItems;
    }

    @Override
    public void selectIndices(int index, int... indices) {
        select(index);
        for (int i : indices) {
            select(i);
        }
    }

    @Override
    public void selectAll() {
        focusIndex = -1;
        for (int index = 0; index < items.length; index++) {
            selectedIndices.add(index);
        }
    }

    @Override
    public void clearAndSelect(int index) {
        clearSelection();
        select(index);
    }

    @Override
    public void select(int index) {
        if (index >= 0 && index < items.length) {
            focusIndex = index;
            selectedIndices.add(index);
        }
    }

    @Override
    public void select(T item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(item)) {
                select(i);
                return;
            }
        }
    }

    public void selectItems(Collection<T> items) {
        for (T item : items) {
            select(item);
        }
    }

    public void clearSelection(T item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(item)) {
                clearSelection(i);
                return;
            }
        }
    }

    @Override
    public void clearSelection(int index) {
        if (index >= 0 && index < items.length) {
            selectedIndices.remove(index);
        }
    }

    @Override
    public void clearSelection() {
        focusIndex = -1;
        selectedIndices.clear();
    }

    @Override
    public boolean isSelected(int index) {
        return index >= 0 && index < items.length && selectedIndices.contains(index);
    }

    @Override
    public boolean isEmpty() {
        return empty.get();
    }

    public ReadOnlyBooleanProperty emptyProperty() {
        return empty;
    }

    @Override
    public void selectFirst() {
        if (items.length > 0) {
            select(0);
        }
    }

    @Override
    public void selectLast() {
        if (items.length > 0) {
            select(items.length - 1);
        }
    }

    @Override
    public void selectPrevious() {
        select(focusIndex - 1);
    }

    @Override
    public void selectNext() {
        select(focusIndex + 1);
    }

    /**
     * get the current array of items.
     *
     * @return items
     */
    public T[] getItems() {
        return items;
    }

    /**
     * clear selection and set list of items
     *
     * @param items
     */
    public void setItems(T... items) {
        clearSelection();
        this.items = Arrays.copyOf(items, items.length);  // use copy for safety
    }

    /**
     * clear selection and set list of items
     *
     * @param items
     */
    public void setItems(Collection<T> items) {
        clearSelection();
        this.items = Basic.toArray(items);
    }

    /**
     * clear selection and set list of items
     *
     * @param items1
     * @param items2
     */
    public void setItems(Collection<? extends T> items1, Collection<? extends T> items2) {
        clearSelection();
        final Collection<T> all = new ArrayList<>(items1.size() + items2.size());
        all.addAll(items1);
        all.addAll(items2);
        this.items = Basic.toArray(all);
    }

    /**
     * invert the current selection
     */
    public void invertSelection() {
        focusIndex = -1;
        final Set<Integer> toSelect = new HashSet<>();
        for (int index = 0; index < items.length; index++) {
            if (!selectedIndices.contains(index))
                toSelect.add(index);
        }
        selectedIndices.clear();
        selectedIndices.addAll(toSelect);
    }

    /**
     * gets the focus index or -1
     *
     * @return focus index
     */
    public int getFocusIndex() {
        return focusIndex;
    }
}
