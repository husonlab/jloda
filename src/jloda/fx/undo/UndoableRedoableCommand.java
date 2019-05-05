/*
 * UndoableRedoableCommand.java Copyright (C) 2019. Daniel H. Huson
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

/**
 * A undo-redoable action
 * Daniel Huson, 12.2016
 */

abstract public class UndoableRedoableCommand {
    private String name;

    /**
     * named constructor
     *
     * @param name
     */
    public UndoableRedoableCommand(String name) {
        this.name = name;
    }

    /**
     * get name to display
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * set name to display
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * undo action
     */
    abstract public void undo();

    /**
     * redo action
     */
    abstract public void redo();

    public boolean isUndoable() {
        return true;
    }

    public boolean isRedoable() {
        return true;
    }

}

