/*
 * UndoableChangePropertyPair.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.beans.property.Property;

/**
 * An undoable property change
 * Daniel Huson, 12.2016
 */
public class UndoableChangePropertyPair<S, T> extends UndoableRedoableCommand {
    private final Property<S> property1;
    private final S oldValue1;
    private S newValue1;
    private final Property<T> property2;
    private final T oldValue2;
    private T newValue2;

    /**
     * constructor
     */
    public UndoableChangePropertyPair(Property<S> property1, S oldValue1, S newValue1, Property<T> property2, T oldValue2, T newValue2) {
        this("", property1, oldValue1, newValue1, property2, oldValue2, newValue2);
    }

    /**
     * constructor
     *
     * @param name
     * @param property1
     * @param oldValue1
     * @param newValue1
     * @param property2
     * @param oldValue2
     * @param newValue2
     */
    public UndoableChangePropertyPair(String name, Property<S> property1, S oldValue1, S newValue1, Property<T> property2, T oldValue2, T newValue2) {
        super(name);
        this.property1 = property1;
        this.oldValue1 = oldValue1;
        this.newValue1 = newValue1;
        this.property2 = property2;
        this.oldValue2 = oldValue2;
        this.newValue2 = newValue2;

    }

    @Override
    public void undo() {
        property1.setValue(oldValue1);
        property2.setValue(oldValue2);
    }

    @Override
    public void redo() {
        property1.setValue(newValue1);
        property2.setValue(newValue2);
    }

    public S getNewValue1() {
        return newValue1;
    }

    public void setNewValue1(S newValue1) {
        this.newValue1 = newValue1;
    }

    public T getNewValue2() {
        return newValue2;
    }

    public void setNewValue2(T newValue2) {
        this.newValue2 = newValue2;
    }
}
