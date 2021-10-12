/*
 * SetSelectionModel.java Copyright (C) 2021 Daniel H. Huson
 *
 *   (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.fx.selection;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.Collection;
import java.util.HashSet;

/**
 * select model based on a set
 * Daniel Huson, 10.2021
 *
 * @param <T>
 */
public class SetSelectionModel<T> implements SelectionModel<T> {
	private final ObservableSet<T> set = FXCollections.observableSet(new HashSet<>());
	private final IntegerProperty size;

	public SetSelectionModel() {
		size = new SimpleIntegerProperty();
		size.bind(Bindings.size(set));
	}

	@Override
	public boolean select(T t) {
		return set.add(t);
	}

	@Override
	public boolean setSelected(T t, boolean select) {
		return select ? select(t) : clearSelection(t);
	}

	@Override
	public boolean selectAll(Collection<T> list) {
		return set.addAll(list);
	}

	@Override
	public void clearSelection() {
		set.clear();
	}

	@Override
	public boolean clearSelection(T t) {
		return set.remove(t);
	}

	@Override
	public boolean clearSelection(Collection<T> list) {
		return set.removeAll(list);
	}

	@Override
	public ObservableSet<T> getSelectedItems() {
		return set;
	}

	@Override
	public ReadOnlyIntegerProperty sizeProperty() {
		return size;
	}

	@Override
	public boolean isSelected(T t) {
		return set.contains(t);
	}

	@Override
	public void toggleSelection(T t) {
		if (isSelected(t))
			clearSelection(t);
		else
			select(t);
	}

	@Override
	public int size() {
		return set.size();
	}
}
