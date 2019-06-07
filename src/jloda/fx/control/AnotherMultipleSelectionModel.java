/*
 * AnotherMultipleSelectionModel.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.control;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import jloda.util.Single;

import java.util.Arrays;
import java.util.Collection;

/**
 * a general purpose multiple selection model
 * Assumes that all selectable items are distinct
 * Daniel Huson, 4.2019
 *
 * @param <T> items
 */
public class AnotherMultipleSelectionModel<T> extends MultipleSelectionModel<T> {
    private final ObservableMap<T, Integer> item2index = FXCollections.observableHashMap();
    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
    private final ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();
    private final IntegerProperty selectedIndex = new SimpleIntegerProperty(-1);
    private final ObservableSet<Integer> selectedIndicesSet = FXCollections.observableSet();

    private final BooleanProperty empty = new SimpleBooleanProperty(true);

    /**
     * constructor
     * Assumes all items are distinct
     *
     * @param initialItems items
     */
    @SafeVarargs
    public AnotherMultipleSelectionModel(T... initialItems) {
        final Single<Boolean> inSelectItems = new Single<>(false);
        final Single<Boolean> inSelectIndices = new Single<>(false);

        selectedIndicesSet.addListener((SetChangeListener<Integer>) e -> {
            if (e.wasRemoved()) {
                if (!inSelectIndices.get()) {
                    inSelectIndices.set(true);
                    selectedIndices.remove(e.getElementRemoved());
                    setSelectedIndex(-1);
                    inSelectIndices.set(false);
                }
                if (!inSelectItems.get()) {
                    inSelectItems.set(true);
                    final T item = AnotherMultipleSelectionModel.this.items.get(e.getElementRemoved());
                    if (item != null)
                        selectedItems.remove(item);
                    setSelectedItem(null);
                    inSelectItems.set(false);
                }

            } else if (e.wasAdded()) {
                if (!inSelectIndices.get()) {
                    inSelectIndices.set(true);
                    final Integer index = e.getElementAdded();
                    selectedIndices.add(index);
                    setSelectedIndex(index);
                    inSelectIndices.set(false);
                }
                if (!inSelectItems.get()) {
                    inSelectItems.set(true);
                    final T item = this.items.get(e.getElementAdded());
                    if (item != null) {
                        selectedItems.add(item);
                        setSelectedItem(item);
                    }
                    inSelectItems.set(false);
                }
            }
            if (!inSelectIndices.get()) {
                inSelectItems.set(true);
                if (selectedIndices.size() == 1)
                    selectedIndex.set(selectedIndices.get(0));
                else
                    selectedIndex.set(-1);
                inSelectItems.set(false);
            }
            empty.set(selectedIndicesSet.size() == 0);

        });

        selectedItems.addListener((ListChangeListener<T>) (e) -> {
            if (!inSelectItems.get()) {
                inSelectItems.set(true);
                try {
                    while (e.next()) {
                        if (e.wasRemoved()) {
                            for (T item : e.getRemoved()) {
                                final Integer index = item2index.get(item);
                                if (index != null)
                                    selectedIndicesSet.remove(index);
                            }
                        } else if (e.wasAdded()) {
                            for (T item : e.getAddedSubList()) {
                                final Integer index = item2index.get(item);
                                if (index != null)
                                    selectedIndicesSet.add(index);
                            }
                        }
                    }

                } finally {
                    inSelectItems.set(false);
                }
            }
        });

        selectedIndices.addListener((ListChangeListener<Integer>) (e) -> {
            if (!inSelectIndices.get()) {
                inSelectIndices.set(true);
                try {
                    int lastAdded = -1;

                    while (e.next()) {
                        if (e.wasRemoved()) {
                            selectedIndicesSet.removeAll(e.getRemoved());
                            if (e.getRemoved().contains(getSelectedIndex())) {
                                setSelectedIndex(-1);
                                setSelectedItem(null);
                            }
                        } else if (e.wasAdded()) {
                            selectedIndicesSet.addAll(e.getAddedSubList());
                            lastAdded = e.getAddedSubList().get(e.getAddedSize() - 1);
                        }
                        if (lastAdded >= 0) {
                            setSelectedIndex(lastAdded);
                            setSelectedItem(getItems().get(lastAdded));
                        }
                    }
                    if (selectedIndices.size() == 1)
                        selectedIndex.set(selectedIndices.get(0));
                    else
                        selectedIndex.set(-1);

                } finally {
                    inSelectIndices.set(false);
                }
            }
        });

        setItems(initialItems);
    }

    /**
     * set the set of items. Indices refer to the ordering defined in the collection.
     * Assumes all items are distinct
     *
     * @param items
     */
    public void setItems(Collection<? extends T> items) {
        selectedIndicesSet.clear();
        this.items.setAll(items);
        item2index.clear();
        int index = 0;
        for (T item : items) {
            item2index.put(item, index++);
        }
    }

    /**
     * set the set of items. Indices refer to the ordering defined in the collection.
     * Assumes all items are distinct
     *
     * @param items
     */
    @SafeVarargs
    public final void setItems(T... items) {
        setItems(Arrays.asList(items));
    }


    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return selectedIndices;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return selectedItems;
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
        if (getSelectionMode() == SelectionMode.MULTIPLE) {
            for (int i = 0; i < items.size(); i++)
                selectedIndicesSet.add(i);
        }
    }

    @Override
    public void clearAndSelect(int index) {
        clearSelection();
        select(index);
    }

    @Override
    public void select(int index) {
        if (getSelectionMode() == SelectionMode.SINGLE)
            selectedIndicesSet.clear();

        if (index >= 0 && index < items.size()) {
            selectedIndicesSet.add(index);
        }
    }

    @Override
    public void select(T item) {
        final Integer index = item2index.get(item);
        if (index != null)
            select(index);
    }

    @Override
    public void clearSelection(int index) {
        if (index >= 0 && index < items.size()) {
            selectedIndicesSet.remove(index);
        }
    }

    public void clearSelection(T item) {
        final Integer index = item2index.get(item);
        if (index != null)
            clearSelection(index);
    }

    @Override
    public void clearSelection() {
        selectedIndicesSet.clear();
    }

    @Override
    public boolean isSelected(int index) {
        if (index >= 0 && index < items.size())
            return selectedIndicesSet.contains(index);
        else
            return false;
    }

    public boolean isSelected(T item) {
        final Integer index = item2index.get(item);
        if (index != null)
            return isSelected(index);
        else
            return false;
    }

    @Override
    public boolean isEmpty() {
        return selectedIndicesSet.size() == 0;
    }

    @Override
    public void selectPrevious() {
        if (selectedIndex.get() > 0) {
            clearAndSelect(selectedIndex.get() - 1);
        }
    }

    @Override
    public void selectNext() {
        if (selectedIndex.get() >= 0 && selectedIndex.get() + 1 < items.size()) {
            clearAndSelect(selectedIndex.get() + 1);
        }
    }

    @Override
    public void selectFirst() {
        if (items.size() > 0)
            clearAndSelect(0);
    }

    @Override
    public void selectLast() {
        if (items.size() > 0)
            clearAndSelect(items.size() - 1);
    }

    public void invertSelection() {
        if (getSelectionMode() == SelectionMode.MULTIPLE) {
            for (int i = 0; i < items.size(); i++) {
                if (!isSelected(i))
                    select(i);
                else
                    clearSelection(i);
            }
        }
    }

    public void clearSelection(Collection<? extends T> items) {
        for (T item : items) {
            final Integer index = item2index.get(item);
            if (index != null)
                clearSelection(index);
        }
    }


    public void selectItems(Collection<? extends T> items) {
        for (T item : items) {
            final Integer index = item2index.get(item);
            if (index != null)
                select(index);
        }
    }

    public BooleanProperty emptyProperty() {
        return empty;
    }

    public ObservableList<T> getItems() {
        return new ReadOnlyListWrapper<>(items);
    }
}
