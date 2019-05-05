/*
 * UndoableChangeList.java Copyright (C) 2019. Daniel H. Huson
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

import java.util.ArrayList;
import java.util.Collection;

/**
 * A list of  undoable property changes
 * Daniel HUson, 6.2017
 */
public class UndoableChangeList<T> extends UndoableRedoableCommand {
    private final ArrayList<UndoableRedoableCommand> list = new ArrayList<>();

    /**
     * constructor
     */
    public UndoableChangeList() {
        super("");
    }

    /**
     * constructor
     *
     * @param name
     */
    public UndoableChangeList(String name) {
        super(name);
    }

    /**
     * constructor
     *
     * @param name
     */
    public UndoableChangeList(String name, Collection<UndoableRedoableCommand> list) {
        super(name);
        this.list.addAll(list);
    }


    public void add(UndoableRedoableCommand property) {
        list.add(property);
    }

    public int size() {
        return list.size();
    }

    public ArrayList<UndoableRedoableCommand> getList() {
        return list;
    }

    @Override
    public void undo() {
        for (UndoableRedoableCommand change : list) {
            change.undo();
        }
    }

    @Override
    public void redo() {
        for (UndoableRedoableCommand change : list) {
            change.redo();
        }
    }
}
