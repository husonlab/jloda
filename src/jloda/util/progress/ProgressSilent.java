/*
 * ProgressSilent.java Copyright (C) 2022 Daniel H. Huson
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

/**
 * silent progress listener
 *
 * @author huson
 * Date: 26-Jun-2004
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
	 */
	public ProgressSilent(final String taskName, final String subtaskName) {
	}

	/**
	 * sets the steps number of steps to be done. By default, the maximum is set to 100
	 *
	 */
	public void setMaximum(final long steps) {
	}

	/**
	 * sets the progress
	 *
	 */
	public void setProgress(final long steps) {
	}

	/**
	 * gets the current progress
	 *
	 * @return progress
	 */
	public long getProgress() {
		return 0;
	}


	@Override
	public void reportTaskCompleted() {
	}

	/**
	 * closes the dialog.
	 */
	public void close() {
	}

	/**
	 * has user canceled?
	 */
	public void checkForCancel() {
	}

	/**
	 * Sets the Task and subtask names, for use in progress bar displays
	 *
	 */
	public void setTasks(String taskName, String subtaskName) {
	}

	/**
	 * Sets just the subtask
	 *
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

	@Override
	public void setProgressIgnoreCancel(long current) {

	}

	@Override
	public void incrementProgressIgnoreCancel() {

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
