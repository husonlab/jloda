/**
 * IDirectorListener.java 
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
/**
 * director events that viewers listen to
 * @author huson
 * 11.03
 */
package jloda.gui.director;

import jloda.util.CanceledException;

/**
 * director events that viewers listen to
 *
 * @author huson
 *         11.03
 */
public interface IDirectorListener extends IUpdateableView {
    /**
     * ask view to update itself. This is method is wrapped into a runnable object
     * and put in the swing event queue to avoid concurrent modifications.
     *
     * @param what what should be updated? Possible values: Director.ALL or Director.TITLE
     */
    void updateView(String what);

    /**
     * ask view to prevent user input
     */
    void lockUserInput();

    /**
     * ask view to allow user input
     */
    void unlockUserInput();

    /**
     * is viewer currently locked?
     *
     * @return true, if locked
     */
    boolean isLocked();

    /**
     * ask view to destroy itself
     */
    void destroyView() throws CanceledException;

    /**
     * set uptodate state
     *
     * @param flag
     */
    void setUptoDate(boolean flag);
}
