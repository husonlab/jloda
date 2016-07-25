/**
 * ProgressPercentage.java 
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
 * progress listener that writes percentages to the command line
 *
 * @author huson
 *         Date: 26-Jun-2004
 */
public class ProgressPercentage implements ProgressListener {
    private long steps = 0;

    private final boolean[] percentageReported = new boolean[11];
    private int nextPercentageToReport;

    private long nextThreshold = 0;
    private long tenPercent = 0;
    private long startTime = 0;

    private boolean reportedCompleted = false;

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
        percentageReported[10] = true; // sentinel
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
        for (int i = 0; i < percentageReported.length - 1; i++) // not the last entry!
            percentageReported[i] = false;
        nextThreshold = tenPercent;
        nextPercentageToReport = 1;
    }

    /**
     * sets the progress
     *
     * @param steps
     */
    public void setProgress(final long steps) {
        if (steps > nextThreshold && !percentageReported[nextPercentageToReport]) {
            System.err.print((10 * nextPercentageToReport + "% "));
            percentageReported[nextPercentageToReport] = true;
            if (nextPercentageToReport < 10)
                nextPercentageToReport++;
            nextThreshold += tenPercent;
        }
        this.steps = steps;
        if (reportedCompleted)
            reportedCompleted = false;
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
        reportedCompleted = false;
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
        if (!reportedCompleted && steps > 0)
            reportTaskCompleted();
        if (subtaskName != null)
            System.err.println(subtaskName);
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

