/*
 * CompositeCommand.java Copyright (C) 2019. Daniel H. Huson
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
 * composite command
 * Daniel Huson, 1.2018
 */
public class CompositeCommand extends UndoableRedoableCommand {
    private final UndoableRedoableCommand[] commands;

    public CompositeCommand(String name, UndoableRedoableCommand... commands) {
        super(name);
        this.commands = commands;
    }

    @Override
    public void undo() {
        if (true) {
            for (int i = commands.length - 1; i >= 0; i--) { // undo in backward order
                UndoableRedoableCommand command = commands[i];
                command.undo();
            }
        } else {
            for (UndoableRedoableCommand command : commands) {
                command.undo();
            }
        }
    }

    @Override
    public void redo() {
        for (UndoableRedoableCommand command : commands) {
            command.redo();
        }
    }

    @Override
    public boolean isUndoable() {
        for (UndoableRedoableCommand command : commands) {
            if (!command.isUndoable())
                return false;
        }
        return commands.length > 0;
    }

    @Override
    public boolean isRedoable() {
        for (UndoableRedoableCommand command : commands) {
            if (!command.isRedoable())
                return false;
        }
        return commands.length > 0;
    }
}
