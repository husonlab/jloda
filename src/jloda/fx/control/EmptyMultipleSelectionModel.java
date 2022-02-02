/*
 * EmptyMultipleSelectionModel.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;

import java.util.Arrays;
import java.util.Collection;

/**
 * empty selection mode
 * Daniel Huson
 *
 * @param <T> items
 */
public class EmptyMultipleSelectionModel<T> extends MultipleSelectionModel<T> {
    private final ObservableList<T> selectedItems = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    /**
     * constructor
     * Assumes all items are distinct
     *
     * @param items items
     */
    @SafeVarargs
    public EmptyMultipleSelectionModel(T... items) {
    }

    /**
     * set the set of items. Indices refer to the ordering defined in the collection.
     * Assumes all items are distinct
     *
	 */
    public void setItems(Collection<? extends T> items) {
    }

    /**
     * set the set of items. Indices refer to the ordering defined in the collection.
     * Assumes all items are distinct
     *
	 */
    @SafeVarargs
    public final void setItems(T... items) {
        setItems(Arrays.asList(items));
    }


    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public void selectIndices(int index, int... indices) {
    }

    @Override
    public void selectAll() {
    }

    @Override
    public void clearAndSelect(int index) {
    }

    @Override
    public void select(int index) {
    }

    @Override
    public void select(T item) {
    }

    @Override
    public void clearSelection(int index) {
    }

    public void clearSelection(T item) {
    }

    @Override
    public void clearSelection() {

    }

    @Override
    public boolean isSelected(int index) {
        return false;
    }

    public boolean isSelected(T item) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void selectPrevious() {
    }

    @Override
    public void selectNext() {
    }

    @Override
    public void selectFirst() {
    }

    @Override
    public void selectLast() {
    }

    public void invertSelection() {
    }

    public void clearSelection(Collection<? extends T> items) {
    }

    public void selectItems(Collection<? extends T> items) {
    }

    public BooleanProperty emptyProperty() {
        return new SimpleBooleanProperty(true);
    }

    public ObservableList<T> getItems() {
        return selectedItems;
    }
}
