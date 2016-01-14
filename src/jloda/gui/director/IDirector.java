/**
 * IDirector.java 
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
package jloda.gui.director;

import jloda.gui.commands.CommandManager;
import jloda.util.CanceledException;

import java.awt.*;

/**
 * director interface
 * Daniel Huson, 3.2007
 */
public interface IDirector {
    // update targets
    String ALL = "ALL";
    String TITLE = "TITLE";
    String ENABLE_STATE = "enable_state";

    /**
     * execute a command
     *
     * @param command
     */
    void execute(String command);

    /**
     * execute a command using the provided command manager
     *
     * @param command
     * @param commandManager
     */
    void execute(String command, CommandManager commandManager);

    /**
     * execute a command using the provided command manager
     *
     * @param command
     * @param commandManager
     */
    void execute(String command, CommandManager commandManager, Component parent);

    /**
     * execute a command
     *
     * @param command
     */
    boolean executeImmediately(String command);

    /**
     * execute a command using the provided command manager
     *
     * @param command
     * @param commandManager
     */
    boolean executeImmediately(String command, CommandManager commandManager);

    /**
     * update viewers
     *
     * @param what update target
     */
    void notifyUpdateViewer(String what);

    /**
     * adds a viewer
     *
     * @param viewer
     */
    IDirectableViewer addViewer(IDirectableViewer viewer);

    /**
     * remove a given viewer
     *
     * @param viewer
     */
    void removeViewer(IDirectableViewer viewer);

    /**
     * get the project title
     *
     * @return title
     */
    String getTitle();

    /**
     * set the dirty flag
     *
     * @param dirty
     */
    void setDirty(boolean dirty);

    /**
     * get the dirty flag
     *
     * @return dirty
     */
    boolean getDirty();

    /**
     * set the project id
     *
     * @param id
     */
    void setID(int id);

    /**
     * get the project id
     *
     * @return id
     */
    int getID();

    /**
     * gets the main viewer associated with this director
     *
     * @return main viewer
     */
    IMainViewer getMainViewer();

    /**
     * close this director
     */
    void close() throws CanceledException;

    /**
     * tell  directed viewers to lock input
     */
    void notifyLockInput();

    /**
     * tell directed viewers to unlock input
     */
    void notifyUnlockInput();

    /**
     * returns a viewer of the given class
     *
     * @param aClass
     * @return viewer of the given class, or null
     */
    IDirectableViewer getViewerByClass(Class aClass);

    boolean isInternalDocument();

    void setInternalDocument(boolean isInternalDocument);
}
