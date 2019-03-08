/*
 *  Copyright (C) 2019 Daniel H. Huson
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


import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import jloda.util.Basic;

import java.util.*;

/**
 * Selection model
 * Created by huson on 12/15/15.
 */
public class ASelectionModel<T> extends MultipleSelectionModel<T> {
    private enum Update {ClearAndAdd, Add, Remove, RemoveAll, SelectAll}

    private final BitSet selectedIndicesBits = new BitSet();
    private final ObservableList<Integer> selectedIndicesList = FXCollections.observableArrayList();

    private T[] items; // need a copy of this array to map indices to objects, when required

    private int focusIndex = -1; // focus index

    private final ObservableList<Integer> unmodifiableSelectedIndices; // unmodifiable list of selected indices
    private final ObservableList<T> unmodifiableSelectedItems; // unmodifiable list of selected items

    private final BooleanProperty empty = new SimpleBooleanProperty(true);

    private final BooleanProperty canSelectAll = new SimpleBooleanProperty(false);
    private final BooleanProperty canSelectNone = new SimpleBooleanProperty(false);

    /**
     * Constructor
     *
     * @param items 0 or more items
     */
    @SafeVarargs
    public ASelectionModel(T... items) {
        this.items = Arrays.copyOf(items, items.length);  // use copy for safety

        // setup unmodifiable lists
        {
            // first setup observable array lists that listen for changes of the selectedIndices set
            final ObservableList<T> selectedItems = FXCollections.observableArrayList();

            selectedIndicesList.addListener(new ListChangeListener<Integer>() {
                @Override
                public void onChanged(Change<? extends Integer> c) {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            final ArrayList<T> set = new ArrayList<>(c.getAddedSize());
                            for (int i : c.getAddedSubList()) {
                                set.add(ASelectionModel.this.items[i]);
                            }
                            selectedItems.addAll(set);
                        } else if (c.wasRemoved()) {
                            final ArrayList<T> set = new ArrayList<>(c.getRemovedSize());
                            for (int i : c.getRemoved()) {
                                set.add(ASelectionModel.this.items[i]);
                            }
                            selectedItems.removeAll(set);
                        }
                        canSelectAll.set(ASelectionModel.this.items.length > 0 && selectedItems.size() < ASelectionModel.this.items.length);
                        canSelectNone.set(ASelectionModel.this.items.length > 0 && selectedItems.size() > 0);

                    }

                }
            });
            // wrap a unmodifiable observable list around the observable arrays lists
            unmodifiableSelectedIndices = FXCollections.unmodifiableObservableList(selectedIndicesList);
            unmodifiableSelectedItems = FXCollections.unmodifiableObservableList(selectedItems);
        }
        empty.bind(Bindings.size(selectedIndicesList).isEqualTo(0));
    }

    /**
     * update the selection
     *
     * @param update
     * @param additional
     */
    private void update(Update update, int... additional) {
        final BitSet selection = new BitSet();
        for (int i : additional) {
            selection.set(i);
        }
        update(update, selection);
    }

    /**
     * update the selection
     *
     * @param update
     * @param selection
     */
    private void update(Update update, BitSet selection) {
        synchronized (selectedIndicesList) {
            switch (update) {
                case RemoveAll: {
                    selectedIndicesBits.clear();
                    selectedIndicesList.clear();
                    break;
                }
                case SelectAll: {
                    selectedIndicesBits.set(1, items.length);
                    final ArrayList<Integer> set = new ArrayList<>(selection.cardinality());
                    for (int i = 0; i < items.length; i++) {
                        set.add(i);
                    }
                    selectedIndicesList.setAll(set);
                    break;
                }
                case Remove: {
                    final ArrayList<Integer> set = new ArrayList<>(selection.cardinality());
                    for (int i = 0; i < items.length; i++) {
                        if (selection.get(i) && selectedIndicesBits.get(i))
                            set.add(i);
                    }
                    if (set.size() > 0) {
                        selectedIndicesBits.andNot(selection);
                        selectedIndicesList.removeAll(set);
                    }
                    break;
                }
                case ClearAndAdd:
                    selectedIndicesBits.clear();
                    selectedIndicesList.clear();
                case Add: {
                    final ArrayList<Integer> set = new ArrayList<>(selection.cardinality());
                    for (int i = 0; i < items.length; i++) {
                        if (selection.get(i) && !selectedIndicesBits.get(i))
                            set.add(i);
                    }
                    if (set.size() > 0) {
                        selectedIndicesBits.or(selection);
                        selectedIndicesList.addAll(set);
                    }
                    break;
                }
            }
        }
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
        final BitSet toSelect = new BitSet();
        toSelect.set(index);
        select(index);
        for (int i : indices) {
            toSelect.set(i);
        }
        update(Update.Add, toSelect);
    }

    @Override
    public void selectAll() {
        focusIndex = -1;
        update(Update.SelectAll);
    }

    @Override
    public void clearAndSelect(int index) {
        update(Update.ClearAndAdd, index);
    }

    @Override
    public void select(int index) {
        if (index >= 0 && index < items.length && !selectedIndicesBits.get(index)) {
            focusIndex = index;
            update(Update.Add, index);
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

    public void selectItems(java.util.Collection<T> collection) {
        final Set<T> set = (collection instanceof Set ? (Set) collection : new HashSet<T>(collection));
        final BitSet toSelect = new BitSet();
        for (int i = 0; i < items.length; i++) {
            if (set.contains(items[i])) {
                toSelect.set(i);
            }
        }
        update(Update.Add, toSelect);
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
        if (index >= 0 && selectedIndicesBits.get(index)) {
            update(Update.Remove,index);
        }
    }

    @Override
    public void clearSelection() {
        focusIndex = -1;
        update(Update.RemoveAll);
    }

    @Override
    public boolean isSelected(int index) {
        return index >= 0 && selectedIndicesBits.get(index);
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
        this.items = Arrays.copyOf(items, items.length);// use copy for safety
        canSelectAll.set(true);
        canSelectNone.set(false);
    }

    /**
     * clear selection and set list of items
     *
     * @param items
     */
    public void setItems(Collection<T> items) {
        clearSelection();
        this.items = Basic.toArray(items);
        canSelectAll.set(true);
        canSelectNone.set(false);
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
        final BitSet selection = new BitSet();
        for (int i = 0; i < items.length; i++) {
            selection.set(i, !selectedIndicesBits.get(i));
        }
        update(Update.ClearAndAdd, selection);
    }

    /**
     * gets the focus index or -1
     *
     * @return focus index
     */
    public int getFocusIndex() {
        return focusIndex;
    }

    public boolean isCanSelectAll() {
        return canSelectAll.get();
    }

    public ReadOnlyBooleanProperty canSelectAllProperty() {
        return canSelectAll;
    }

    public boolean isCanSelectNone() {
        return canSelectNone.get();
    }

    public ReadOnlyBooleanProperty canSelectNoneProperty() {
        return canSelectNone;
    }
}
