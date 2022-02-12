/*
 * ProgressPercentage.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.util.progress;

import java.util.ArrayList;
import java.util.List;

/**
 * progress listener that writes percentages to the command line
 *
 * @author huson Date: 26-Jun-2004
 */
public class ProgressPercentage implements ProgressListener {

	private final List<ProgressObserver> observers = new ArrayList<>();

	private long steps = 0;

	private final boolean[] percentageReported = new boolean[11];
	private int nextPercentageToReport;

	private long nextThreshold = 0;
	private long tenPercent = 0;
	private long startTime;

	private boolean reportedCompleted;

	/**
	 * constructor
	 */
	public ProgressPercentage() {
		this(0);
	}

	/**
	 * constructor
	 *
	 */
	public ProgressPercentage(long maxSteps) {
		startTime = System.currentTimeMillis();
		percentageReported[10] = true; // sentinel
		setMaximum(maxSteps);
		reportedCompleted = false;
	}

	/**
	 * constructor
	 *
	 */
	public ProgressPercentage(final String taskName) {
		this(0);
		System.err.println(taskName);
	}

	/**
	 * constructor
	 *
	 */
	public ProgressPercentage(final String taskName, long maxSteps) {
		this(maxSteps);
		System.err.println(taskName);
	}

	/**
	 * constructor
	 *
	 */
	public ProgressPercentage(final String taskName, final String subtaskName) {
		this(0);
		System.err.println(taskName + (subtaskName != null ? " (" + subtaskName + ")" : ""));
	}

	/**
	 * sets the steps number of steps to be done. By default, the maximum is set to
	 * 100
	 *
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
	 */
	public void setProgress(final long steps) {
		setProgressIgnoreCancel(steps);
	}

	@Override
	public void setProgressIgnoreCancel(long steps) {
		if (steps > nextThreshold && !percentageReported[nextPercentageToReport]) {
			System.err.print((10 * nextPercentageToReport + "% "));
			percentageReported[nextPercentageToReport] = true;
			observers.forEach(obs -> obs.reportProgress(10 * nextPercentageToReport));
			if (nextPercentageToReport < 10)
				nextPercentageToReport++;
			nextThreshold += tenPercent;
		}
		this.steps = steps;
		if (reportedCompleted)
			reportedCompleted = false;
	}

	public void addObserver(ProgressObserver obs) {
		observers.add(obs);
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
		reportedCompleted = true;
		observers.forEach(obs -> obs.reportProgress(100));
	}

	/**
	 * report end of task
	 */
	public String getTimeString() {
		return String.format("%,.1fs", (System.currentTimeMillis() - startTime) / 1000f);
	}

	/**
	 * has user canceled?
	 *
	 */
	public void checkForCancel() {
	}

	/**
	 * Sets the Task and subtask names, for use in progress bar displays
	 *
	 */
	public void setTasks(String taskName, String subtaskName) {
		setSubtask(taskName + " " + subtaskName);
	}

	/**
	 * Sets just the subtask
	 *
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
		incrementProgressIgnoreCancel();
	}

	@Override
	public void incrementProgressIgnoreCancel() {
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

	@Override
	public void setPause(boolean pause) {

	}

	@Override
	public boolean getPause() {
		return false;
	}

}
