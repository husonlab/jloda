/*
 * CompositeCommand.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * composite command
 * Daniel Huson, 1.2018
 */
public class CompositeCommand extends UndoableRedoableCommand {
    private final ArrayList<UndoableRedoableCommand> commands = new ArrayList<>();

    public CompositeCommand(String name, UndoableRedoableCommand... commands) {
        super(name);
        add(commands);
    }

    public void add(UndoableRedoableCommand... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }

    @Override
    public void undo() {
		CollectionUtils.reverse(commands).forEach(UndoableRedoableCommand::undo); // undo in backward order
    }

    @Override
    public void redo() {
        commands.forEach(UndoableRedoableCommand::redo);
    }

    @Override
    public boolean isUndoable() {
        final Optional<UndoableRedoableCommand> notUndoable = commands.stream().filter(c -> !c.isUndoable()).findAny();
        if (notUndoable.isPresent())
            return false;
        else
            return commands.size() > 0;
    }

    @Override
    public boolean isRedoable() {
        final Optional<UndoableRedoableCommand> notRedoable = commands.stream().filter(c -> !c.isRedoable()).findAny();
        if (notRedoable.isPresent())
            return false;
        else
            return commands.size() > 0;
    }
}
