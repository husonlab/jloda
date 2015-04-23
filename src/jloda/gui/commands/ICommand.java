/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    public void setDir(IDirector dir);

    /**
     * get the director
     *
     * @return
     */
    public IDirector getDir();

    /**
     * set the command manager. This is required for all commands that call the "execute" method
     *
     * @param commandManager
     */
    public void setCommandManager(CommandManager commandManager);

    /**
     * get the associated command manager
     *
     * @return commandManager
     */
    public CommandManager getCommandManager();

    /**
     * get the name to be used as a menu label
     *
     * @return name
     */
    public String getName();

    /**
     * get an alternative name used to identify this command
     *
     * @return name
     */
    public String getAltName();

    /**
     * initial tokens used to identify the command
     *
     * @return first tokens
     */
    public String getStartsWith();

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */
    public String getDescription();

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */
    public ImageIcon getIcon();

    /**
     * gets the accelerator key  to be used in menu
     *
     * @return accelerator key
     */
    public javax.swing.KeyStroke getAcceleratorKey();

    /**
     * get command-line syntax. First two tokens are used to identify the command
     *
     * @return usage
     */
    public String getSyntax();

    /**
     * action to be performed
     *
     * @param ev
     */
    public void actionPerformed(ActionEvent ev);

    /**
     * is this a critical command that can only be executed when no other command is running?
     *
     * @return true, if critical
     */
    public boolean isCritical();

    /**
     * is the command currently applicable? Used to set enable state of command
     *
     * @return true, if command can be applied
     */
    public boolean isApplicable();

    /**
     * parses the given command and executes it
     *
     * @param np
     * @throws IOException
     */
    public void apply(NexusStreamParser np) throws Exception;

    /**
     * gets the command needed to undo this command
     *
     * @return undo command
     */
    public String getUndo();

    /**
     * sets the  viewer
     *
     * @param viewer
     */
    public void setViewer(IDirectableViewer viewer);

    /**
     * gets the  viewer
     *
     * @return viewer
     */
    public IDirectableViewer getViewer();


    /**
     * sets the viewer in the case that the viewer is not an  IDirectableViewer
     *
     * @param viewer
     */
    public void setParent(Object viewer);

    /**
     * gets the  viewer in the case that the viewer is not an  IDirectableViewer
     *
     * @return viewer
     */
    public Object getParent();

    /**
     * get the autorepeat interval. 0 means no autorepeat
     *
     * @return
     */
    public int getAutoRepeatInterval();


    /**
     * set  the autorepeat interval. 0 means no autorepeat
     *
     * @param autoRepeatInterval
     */
    public void setAutoRepeatInterval(int autoRepeatInterval);


    /**
     * Action to be performed in case of autorepeat
     *
     * @param ev
     */
    public void actionPerformedAutoRepeat(ActionEvent ev);
}
