/*
 * ASingleSelectionModel.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;

import java.util.Collection;

/**
 * a single selection model
 * Daniel Huson, 4.2019
 *
 * @param <T>
 */
public class ASingleSelectionModel<T> extends SingleSelectionModel<T> {
    private final ObservableList<T> items = FXCollections.observableArrayList();

    public ASingleSelectionModel() {
        items.addListener((InvalidationListener) (e) -> clearSelection());
    }

    public ASingleSelectionModel(Collection<T> items) {
        this();
        setItems(items);
    }

    @SafeVarargs
    public ASingleSelectionModel(T... items) {
        setItems(items);
    }

    public void setItems(Collection<T> items) {
        clearSelection();
        this.items.setAll(items);
    }

    @SafeVarargs
    public final void setItems(T... items) {
        this.items.setAll(items);
    }

    public ObservableList<T> getItems() {
        return items;
    }

    @Override
    protected T getModelItem(int index) {
        if (index >= 0 && index < items.size())
            return items.get(index);
        else
            return null;
    }

    @Override
    protected int getItemCount() {
        return items.size();
    }
}

