/*
 * ItemSelectionModel.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.fx.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import java.util.Collection;
import java.util.TreeSet;

/**
 * simple item-based selection  model
 *
 * @param <T> Daniel Huson, 2015
 */
public class ItemSelectionModel<T extends Comparable<?>> {
    private final ObservableSet<T> selectedItemSet = FXCollections.observableSet(new TreeSet<T>());

    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
    private final ObservableList<T> selectedItemsUnmodifiable = FXCollections.unmodifiableObservableList(selectedItems);
    private final IntegerProperty size = new SimpleIntegerProperty(0);
    private final BooleanProperty empty = new SimpleBooleanProperty(true);

    public ItemSelectionModel() {
        size.bind(Bindings.size(selectedItemSet));
        empty.bind(sizeProperty().isEqualTo(0));

        selectedItemSet.addListener((SetChangeListener<T>) c -> {
            if (c.wasRemoved()) {
                selectedItems.remove(c.getElementRemoved());
            } else if (c.wasAdded()) {
                selectedItems.add(c.getElementAdded());
            }
        });
    }


    public ObservableList<T> getSelectedItemsUnmodifiable() {
        return selectedItemsUnmodifiable;
    }

    public void clearAndSelect(T item) {
        clearSelection();
        select(item);
    }

    public void select(T item) {
        selectedItemSet.add(item);
    }

    public void selectAll(Collection<T> items) {
        selectedItemSet.addAll(items);
    }

    public void clearSelection(T item) {
        selectedItemSet.remove(item);
    }

    public void clearSelectionAll(Collection<? extends T> items) {
        selectedItemSet.removeAll(items);
    }

    public void clearSelection() {
        selectedItemSet.clear();
    }

    public void toggleSelection(T item) {
        if (selectedItemSet.contains(item))
            clearSelection(item);
        else
            select(item);
    }

    public void toggleSelection(Collection<T> items) {
        items.forEach(this::toggleSelection);
    }

    public boolean isSelected(T item) {
        return selectedItemSet.contains(item);
    }

    public boolean isEmpty() {
        return empty.get();
    }

    public ReadOnlyBooleanProperty emptyProperty() {
        return empty;
    }

    public int size() {
        return size.get();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size;
    }
}