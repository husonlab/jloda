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
}
