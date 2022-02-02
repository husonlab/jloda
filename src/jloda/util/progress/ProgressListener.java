/*
 * ProgressListener.java Copyright (C) 2022 Daniel H. Huson
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


import jloda.util.CanceledException;

/**
 * Progress listener interface
 *
 * @author huson
 * Date: 02-Dec-2003
 */
public interface ProgressListener extends AutoCloseable {
	/**
	 * set the total number of steps to be done
	 *
	 */
	void setMaximum(long total);

	/**
	 * set progress
	 *
	 * @param current step
	 */
	void setProgress(long current) throws CanceledException;

	/**
	 * set progress, ignoring any cancel exception
	 *
	 * @param current step
	 */
	void setProgressIgnoreCancel(long current);


	/**
	 * gets the current progress
	 *
	 * @return progress
	 */
	long getProgress();

	default void addObserver(ProgressObserver obs) {

	}

	void checkForCancel() throws CanceledException;

	/**
	 * Sets the Task and subtask names, for use in progress bar displays
	 *
	 */
	void setTasks(String taskName, String subtaskName);

	/**
	 * Sets just the subtask
	 *
	 */
	void setSubtask(String subtaskName);

	/**
	 * Enable the user to cancel during this operation.
	 *
	 */
	void setCancelable(boolean enabled);

	boolean isUserCancelled();

	void setUserCancelled(boolean userCancelled);

	/**
	 * increment progress
	 */
	void incrementProgress() throws CanceledException;

	/**
	 * increment progress, ignoring any cancel exception
	 */
	void incrementProgressIgnoreCancel();


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
	 *
	 */
	void reportTaskCompleted();

	/**
	 * set the debug mode
	 *
	 */
	void setDebug(boolean debug);

	/**
	 * calculation has been paused
	 *
	 */
	void setPause(boolean pause);

	/**
	 * has calculation been paused?
	 *
	 */
	boolean getPause();

}
