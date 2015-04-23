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
 * progress listener that writes percentages to the command line
 *
 * @author huson
 *         Date: 26-Jun-2004
 */
public class ProgressPercentage implements ProgressListener {
    private long steps = 0;

    private final boolean[] reported = new boolean[11];
    private int nextPercentageToReport;

    private long nextThreshold = 0;
    private long tenPercent = 0;
    private long startTime = 0;

    /**
     * constructor
     */
    public ProgressPercentage() {
        this(0);
    }

    /**
     * constructor
     * @param maxSteps
     */
    public ProgressPercentage(long maxSteps) {
        startTime = System.currentTimeMillis();
        reported[10] = true; // sentinel
        setMaximum(maxSteps);
    }

    /**
     * constructor
     * @param taskName
     */
    public ProgressPercentage(final String taskName) {
        this(0);
        System.err.println(taskName);
    }

    /**
     * constructor
     * @param taskName
     * @param maxSteps
     */
    public ProgressPercentage(final String taskName, long maxSteps) {
        this(maxSteps);
        System.err.println(taskName);
    }

    /**
     * constructor
     *
     * @param taskName
     * @param subtaskName
     */
    public ProgressPercentage(final String taskName, final String subtaskName) {
        this(0);
        System.err.println(taskName + (subtaskName != null ? " (" + subtaskName + ")" : ""));
    }

    /**
     * sets the steps number of steps to be done. By default, the maximum is set to 100
     *
     * @param maxSteps
     */
    public void setMaximum(final long maxSteps) {
        tenPercent = maxSteps / 10;
        nextThreshold = tenPercent;
        nextPercentageToReport = 1;
    }

    /**
     * sets the progress
     *
     * @param steps
     */
    public void setProgress(final long steps) {
        this.steps = steps;

        if (steps > nextThreshold && !reported[nextPercentageToReport]) {
            System.err.print((10 * nextPercentageToReport + "% "));
            reported[nextPercentageToReport] = true;
            if (nextPercentageToReport < 10)
                nextPercentageToReport++;
            nextThreshold += tenPercent;
        }
    }

    /**
     * gets the current progress
     *
     * @return progress
     */
    public long getProgress() {
        return steps;
    }

    /**
     * closes the dialog.
     */
    public void close() {
        reportTaskCompleted();
    }

    /**
     * report end of task
     */
    public void reportTaskCompleted() {
        System.err.println("100% (" + getTimeString() + ")");
        startTime = System.currentTimeMillis();
    }

    /**
     * report end of task
     */
    public String getTimeString() {
        return String.format("%.1fs", (System.currentTimeMillis() - startTime) / 1000.0);
    }

    /**
     * has user canceled?
     *
     * @throws CanceledException
     */
    public void checkForCancel() {
    }

    /**
     * Sets the Task and subtask names, for use in progress bar displays
     *
     * @param taskName
     * @param subtaskName
     */
    public void setTasks(String taskName, String subtaskName) {
        // if (taskName != null)
        //    System.err.println(taskName + (subtaskName != null ? (": " + subtaskName) : ""));
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
        setProgress(steps + 1);
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

