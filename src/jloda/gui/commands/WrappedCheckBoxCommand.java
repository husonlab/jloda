/**
 * WrappedCheckBoxCommand.java 
 * Copyright (C) 2016 Daniel H. Huson
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
package jloda.gui.commands;

/**
 * a wrapped command
 * This is used for globally defined commands to ensure that they are given the correct dir, parent and viewer on execution
 * Daniel Huson, 4.2015
 */
public class WrappedCheckBoxCommand extends WrappedCommand implements ICheckBoxCommand {

    /**
     * constructor
     *
     * @param command
     */
    public WrappedCheckBoxCommand(ICheckBoxCommand command) {
        super(command);
    }

    /**
     * this is currently selected?
     *
     * @return selected
     */
    @Override
    public boolean isSelected() {
        return ((ICheckBoxCommand) command).isSelected();
    }

    /**
     * set the selected status of this command
     *
     * @param selected
     */
    @Override
    public void setSelected(boolean selected) {
        ((ICheckBoxCommand) command).setSelected(selected);
    }
}
