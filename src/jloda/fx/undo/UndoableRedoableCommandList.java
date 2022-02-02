/*
 * UndoableRedoableCommandList.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.ArrayList;
import java.util.Collection;

/**
 * A list of  undoable/redoable  commands
 * Daniel Huson, 6.2017
 */
public class UndoableRedoableCommandList extends UndoableRedoableCommand {
    private final ArrayList<UndoableRedoableCommand> list = new ArrayList<>();

    /**
     * constructor
     */
    public UndoableRedoableCommandList() {
        super("");
    }

    /**
     * constructor
     *
	 */
    public UndoableRedoableCommandList(String name) {
        super(name);
    }

    /**
     * constructor
     *
	 */
    public UndoableRedoableCommandList(String name, Collection<? extends UndoableRedoableCommand> list) {
        super(name);
        this.list.addAll(list);
    }

    public void add(UndoableRedoableCommand property) {
        list.add(property);
    }

    public  <T>  void add (Property<T> property, T oldValue, T newValue) {
        add(new UndoableChangeProperty<>(property, oldValue, newValue));
    }

    public void add (Runnable undo,Runnable redo) {
        add(UndoableRedoableCommand.create("",undo,redo));
    }

    public int size() {
        return list.size();
    }

    public ArrayList<UndoableRedoableCommand> getList() {
        return list;
    }

    @Override
    public void undo() {
        for (var command : list) {
            if(command.isUndoable())
            command.undo();
        }
    }

    @Override
    public void redo() {
        for (var command : list) {
            if(command.isRedoable())
                command.redo();
        }
    }

    @Override
    public boolean isUndoable() {
        return list.stream().anyMatch(UndoableRedoableCommand::isUndoable);
    }

    @Override
    public boolean isRedoable() {
        return list.stream().anyMatch(UndoableRedoableCommand::isRedoable);
    }
}
