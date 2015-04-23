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
