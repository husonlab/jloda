/*
 * SelectionModel.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.selection;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.ObservableSet;

import java.util.Collection;

/**
 * a select model that does not require an array
 * Daniel Huson, 10.2021
 *
 * @param <T>
 */
public interface SelectionModel<T> {
	boolean select(T t);

	boolean setSelected(T t, boolean select);

	boolean selectAll(Collection<? extends T> list);

	void clearSelection();

	boolean clearSelection(T t);

	boolean clearSelection(Collection<? extends T> list);

	ObservableSet<T> getSelectedItems();

	T getSelectedItem();

	ReadOnlyIntegerProperty sizeProperty();

	int size();

	boolean isSelected(T t);

	void toggleSelection(T t);
}
