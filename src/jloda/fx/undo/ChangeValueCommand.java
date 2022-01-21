/*
 * ChangeValueCommand.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.function.Consumer;

/**
 * change value command
 * Daniel Huson, 10.2019
 *
 * @param <T>
 */
public class ChangeValueCommand<T> extends UndoableRedoableCommand {
    private final Runnable undo;
    private final Runnable redo;
    private final long eventId;

    public ChangeValueCommand(String name, T oldValue, T newValue, Consumer<T> changer) {
        this(name, System.currentTimeMillis(), oldValue, newValue, changer);
    }

    public ChangeValueCommand(String name, long eventId, T oldValue, T newValue, Consumer<T> changer) {
        super(name);
        this.eventId = eventId;
        undo = () -> changer.accept(oldValue);
        redo = () -> changer.accept(newValue);
        // System.err.println(name + " " + eventId + " " + oldValue + " -> " + newValue);
    }

    @Override
    public void undo() {
        undo.run();
    }

    @Override
    public void redo() {
        redo.run();
    }

    public long getEventId() {
        return eventId;
    }
}
