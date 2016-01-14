/**
 * ProgressSilent.java 
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
 * silent progress listener
 *
 * @author huson
 *         Date: 26-Jun-2004
 */
public class ProgressSilent implements ProgressListener {
    /**
     * constructor
     */
    public ProgressSilent() {
    }

    /**
     * constructor
     *
     * @param taskName
     * @param subtaskName
     */
    public ProgressSilent(final String taskName, final String subtaskName) {
    }

    /**
     * sets the steps number of steps to be done. By default, the maximum is set to 100
     *
     * @param steps
     */
    public void setMaximum(final long steps) {
    }

    /**
     * sets the progress
     *
     * @param steps
     */
    public void setProgress(final long steps) throws CanceledException {
    }

    /**
     * gets the current progress
     *
     * @return progress
     */
    public long getProgress() {
        return 0;
    }

    /**
     * closes the dialog.
     */
    public void close() {
    }

    /**
     * has user canceled?
     *
     * @throws CanceledException
     */
    public void checkForCancel() throws CanceledException {
    }

    /**
     * Sets the Task and subtask names, for use in progress bar displays
     *
     * @param taskName
     * @param subtaskName
     */
    public void setTasks(String taskName, String subtaskName) {
    }

    /**
     * Sets just the subtask
     *
     * @param subtaskName
     */
    public void setSubtask(String subtaskName) {
    }

    public void setCancelable(boolean enabled) {

    }

    public boolean isUserCancelled() {
        return false;
    }

    public void setUserCancelled(boolean userCancelled) {
    }

    public void incrementProgress() {

    }

    /**
     * is user allowed to cancel
     *
     * @return cancelable?
     */
    public boolean isCancelable() {
        return false;
    }

    public void setDebug(boolean debug) {
    }

}
