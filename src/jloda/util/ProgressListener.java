/**
 * ProgressListener.java 
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
package jloda.util;


/**
 * Progress listener interface
 *
 * @author huson
 *         Date: 02-Dec-2003
 */
public interface ProgressListener extends AutoCloseable {
    /**
     * set the total number of steps to be done
     *
     * @param total
     */
    void setMaximum(long total);

    /**
     * set progress
     *
     * @param current step
     */
    void setProgress(long current) throws CanceledException;

    /**
     * gets the current progress
     *
     * @return progress
     */
    long getProgress();

    void checkForCancel() throws CanceledException;

    /**
     * Sets the Task and subtask names, for use in progress bar displays
     *
     * @param taskName
     * @param subtaskName
     */
    void setTasks(String taskName, String subtaskName);

    /**
     * Sets just the subtask
     *
     * @param subtaskName
     */
    void setSubtask(String subtaskName);

    /**
     * Enable the user to cancel during this operation.
     *
     * @param enabled
     */
    void setCancelable(boolean enabled);

    boolean isUserCancelled();

    void setUserCancelled(boolean userCancelled);

    /**
     * increment progress
     */
    void incrementProgress() throws CanceledException;

    /**
     * close the progress listener
     */
    void close();

    /**
     * is user allowed to cancel
     *
     * @return cancelable?
     */
    boolean isCancelable();

    /**
     * set the debug mode
     *
     * @param debug
     */
    void setDebug(boolean debug);
}
