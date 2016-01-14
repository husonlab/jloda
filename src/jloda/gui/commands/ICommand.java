/**
 * ICommand.java 
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

import jloda.gui.director.IDirectableViewer;
import jloda.gui.director.IDirector;
import jloda.util.parse.NexusStreamParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * basic command interface
 * Daniel Huson, 5.2010
 */
public interface ICommand {
    /**
     * set the director
     *
     * @param dir
     */
    void setDir(IDirector dir);

    /**
     * get the director
     *
     * @return
     */
    IDirector getDir();

    /**
     * set the command manager. This is required for all commands that call the "execute" method
     *
     * @param commandManager
     */
    void setCommandManager(CommandManager commandManager);

    /**
     * get the associated command manager
     *
     * @return commandManager
     */
    CommandManager getCommandManager();

    /**
     * get the name to be used as a menu label
     *
     * @return name
     */
    String getName();

    /**
     * get an alternative name used to identify this command
     *
     * @return name
     */
    String getAltName();

    /**
     * initial tokens used to identify the command
     *
     * @return first tokens
     */
    String getStartsWith();

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */
    String getDescription();

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */
    ImageIcon getIcon();

    /**
     * gets the accelerator key  to be used in menu
     *
     * @return accelerator key
     */
    javax.swing.KeyStroke getAcceleratorKey();

    /**
     * get command-line syntax. First two tokens are used to identify the command
     *
     * @return usage
     */
    String getSyntax();

    /**
     * action to be performed
     *
     * @param ev
     */
    void actionPerformed(ActionEvent ev);

    /**
     * is this a critical command that can only be executed when no other command is running?
     *
     * @return true, if critical
     */
    boolean isCritical();

    /**
     * is the command currently applicable? Used to set enable state of command
     *
     * @return true, if command can be applied
     */
    boolean isApplicable();

    /**
     * parses the given command and executes it
     *
     * @param np
     * @throws IOException
     */
    void apply(NexusStreamParser np) throws Exception;

    /**
     * gets the command needed to undo this command
     *
     * @return undo command
     */
    String getUndo();

    /**
     * sets the  viewer
     *
     * @param viewer
     */
    void setViewer(IDirectableViewer viewer);

    /**
     * gets the  viewer
     *
     * @return viewer
     */
    IDirectableViewer getViewer();


    /**
     * sets the viewer in the case that the viewer is not an  IDirectableViewer
     *
     * @param viewer
     */
    void setParent(Object viewer);

    /**
     * gets the  viewer in the case that the viewer is not an  IDirectableViewer
     *
     * @return viewer
     */
    Object getParent();

    /**
     * get the autorepeat interval. 0 means no autorepeat
     *
     * @return
     */
    int getAutoRepeatInterval();


    /**
     * set  the autorepeat interval. 0 means no autorepeat
     *
     * @param autoRepeatInterval
     */
    void setAutoRepeatInterval(int autoRepeatInterval);


    /**
     * Action to be performed in case of autorepeat
     *
     * @param ev
     */
    void actionPerformedAutoRepeat(ActionEvent ev);
}
