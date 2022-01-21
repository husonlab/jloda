/*
 * UndoableChangeProperty.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.undo;

import javafx.beans.property.Property;

import java.util.function.Consumer;

/**
 * An undoable property change
 * Daniel Huson, 12.2016
 */
public class UndoableChangeProperty<T> extends UndoableRedoableCommand {
    private final Property<T> property;
    private final T oldValue;
    private final T newValue;
    private final Consumer<T> updater;

    public UndoableChangeProperty(Property<T> property, T oldValue, T newValue) {
        this("", property, oldValue, newValue, null);
    }

    public UndoableChangeProperty(String name, Property<T> property, T oldValue, T newValue) {
        this(name, property, oldValue, newValue, null);
    }

    public UndoableChangeProperty(String name, Property<T> property, T oldValue, T newValue, Consumer<T> updater) {
        super(name);
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.updater = updater;
    }

    @Override
    public void undo() {
        property.setValue(oldValue);
        if (updater != null)
            updater.accept(oldValue);
    }

    @Override
    public void redo() {
        property.setValue(newValue);
        if (updater != null)
            updater.accept(newValue);
    }

    @Override
    public boolean isUndoable() {
        return oldValue != null;
    }

    @Override
    public boolean isRedoable() {
        return newValue != null;
    }
}
