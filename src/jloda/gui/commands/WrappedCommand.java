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

/**
 * a wrapped command
 * This is used for globally defined commands to ensure that they are given the correct dir, parent and viewer on execution
 * Daniel Huson, 4.2015
 */
public class WrappedCommand implements ICommand {
    protected final ICommand command;
    private CommandManager commandManager;
    private IDirector dir;
    private Object parent;
    private IDirectableViewer viewer;

    /**
     * constructor
     *
     * @param command
     */
    public WrappedCommand(ICommand command) {
        this.command = command;
    }

    /**
     * set the director
     *
     * @param dir
     */
    @Override
    public void setDir(IDirector dir) {
        this.dir = dir;
        command.setDir(dir);
    }

    /**
     * get the director
     *
     * @return
     */
    @Override
    public IDirector getDir() {
        return dir;
    }

    /**
     * set the command manager. This is required for all commands that call the "execute" method
     *
     * @param commandManager
     */
    @Override
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * get the associated command manager
     *
     * @return commandManager
     */
    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * get the name to be used as a menu label
     *
     * @return name
     */
    @Override
    public String getName() {
        return command.getName();
    }

    /**
     * get an alternative name used to identify this command
     *
     * @return name
     */
    @Override
    public String getAltName() {
        return command.getAltName();
    }

    /**
     * initial tokens used to identify the command
     *
     * @return first tokens
     */
    @Override
    public String getStartsWith() {
        return command.getStartsWith();
    }

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */
    @Override
    public String getDescription() {
        return command.getDescription();
    }

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */
    @Override
    public ImageIcon getIcon() {
        return command.getIcon();
    }

    /**
     * gets the accelerator key  to be used in menu
     *
     * @return accelerator key
     */
    @Override
    public KeyStroke getAcceleratorKey() {
        return command.getAcceleratorKey();
    }

    /**
     * get command-line syntax. First two tokens are used to identify the command
     *
     * @return usage
     */
    @Override
    public String getSyntax() {
        return command.getSyntax();
    }

    /**
     * action to be performed
     *
     * @param ev
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        synchronized (command) {
            command.setCommandManager(commandManager);
            command.setViewer(viewer);
            command.setDir(dir);
            command.setParent(parent);
            command.actionPerformed(ev);
        }
    }

    /**
     * is this a critical command that can only be executed when no other command is running?
     *
     * @return true, if critical
     */
    @Override
    public boolean isCritical() {
        return command.isCritical();
    }

    /**
     * is the command currently applicable? Used to set enable state of command
     *
     * @return true, if command can be applied
     */
    @Override
    public boolean isApplicable() {
        synchronized (command) {
            command.setCommandManager(commandManager);
            command.setViewer(viewer);
            command.setDir(dir);
            command.setParent(parent);
            return command.isApplicable();
        }
    }

    /**
     * parses the given command and executes it
     *
     * @param np
     * @throws Exception
     */
    @Override
    public void apply(NexusStreamParser np) throws Exception {
        synchronized (command) {
            command.setCommandManager(commandManager);
            command.setViewer(viewer);
            command.setDir(dir);
            command.setParent(parent);
            command.apply(np);
        }
    }

    /**
     * gets the command needed to undo this command
     *
     * @return undo command
     */
    @Override
    public String getUndo() {
        return command.getUndo();
    }

    /**
     * sets the  viewer
     *
     * @param viewer
     */
    @Override
    public void setViewer(IDirectableViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * gets the  viewer
     *
     * @return viewer
     */
    @Override
    public IDirectableViewer getViewer() {
        return viewer;
    }

    /**
     * sets the viewer in the case that the viewer is not an  IDirectableViewer
     *
     * @param parent
     */
    @Override
    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * gets the  viewer in the case that the viewer is not an  IDirectableViewer
     *
     * @return viewer
     */
    @Override
    public Object getParent() {
        return parent;
    }

    /**
     * get the autorepeat interval. 0 means no autorepeat
     *
     * @return
     */
    @Override
    public int getAutoRepeatInterval() {
        return command.getAutoRepeatInterval();
    }

    /**
     * set  the autorepeat interval. 0 means no autorepeat
     *
     * @param autoRepeatInterval
     */
    @Override
    public void setAutoRepeatInterval(int autoRepeatInterval) {
        command.setAutoRepeatInterval(autoRepeatInterval);
    }

    /**
     * Action to be performed in case of autorepeat
     *
     * @param ev
     */
    @Override
    public void actionPerformedAutoRepeat(ActionEvent ev) {
        synchronized (command) {
            command.setCommandManager(commandManager);
            command.setViewer(viewer);
            command.setDir(dir);
            command.setParent(parent);
            command.actionPerformedAutoRepeat(ev);
        }
    }
}
