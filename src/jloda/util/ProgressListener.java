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
