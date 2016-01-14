/**
 * IDirectableViewer.java 
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

import javax.swing.*;

/**
 * A directable viewer is one that listens to the director updates and informs
 * on the uptodate status
 *
 * @author huson
 *         Date: 26-Nov-2003
 */
public interface IDirectableViewer extends IDirectorListener {
    /**
     * is viewer uptodate?
     *
     * @return uptodate
     */
    boolean isUptoDate();

    /**
     * return the frame associated with the viewer
     *
     * @return frame
     */
    JFrame getFrame();

    /**
     * gets the title
     *
     * @return title
     */
    String getTitle();

    /**
     * gets the associated command manager
     *
     * @return command manager
     */
    CommandManager getCommandManager();


    /**
     * get the name of the class
     *
     * @return class name
     */
    String getClassName();
}
