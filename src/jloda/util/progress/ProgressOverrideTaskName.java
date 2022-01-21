/*
 * ProgressOverrideTaskName.java Copyright (C) 2022 Daniel H. Huson
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

public class ProgressOverrideTaskName implements ProgressListener {
	private final ProgressListener progress;
	private final String taskName;

	public ProgressOverrideTaskName(ProgressListener progress, String taskName) {
		this.progress = progress;
		this.taskName = taskName;
	}

	@Override
	public void setMaximum(long total) {
		progress.setMaximum(total);
	}

	@Override
	public void setProgress(long current) throws CanceledException {
		progress.setProgress(current);

	}

	@Override
	public void setProgressIgnoreCancel(long current) {
		progress.setProgressIgnoreCancel(current);
	}

	@Override
	public long getProgress() {
		return progress.getProgress();
	}

	@Override
	public void checkForCancel() throws CanceledException {
		progress.checkForCancel();
	}

	@Override
	public void setTasks(String taskName, String subtaskName) {
		progress.setTasks(this.taskName, subtaskName);
	}

	@Override
	public void setSubtask(String subtaskName) {
		progress.setSubtask(subtaskName);
	}

	@Override
	public void setCancelable(boolean enabled) {
		progress.setCancelable(enabled);
	}

	@Override
	public boolean isUserCancelled() {
		return progress.isUserCancelled();
	}

	@Override
	public void setUserCancelled(boolean userCancelled) {
		progress.setUserCancelled(userCancelled);
	}

	@Override
	public void incrementProgress() throws CanceledException {
		progress.incrementProgress();
	}

	@Override
	public void incrementProgressIgnoreCancel() {
		progress.incrementProgressIgnoreCancel();
	}

	@Override
	public void close() {
		progress.close();
	}

	@Override
	public boolean isCancelable() {
		return progress.isCancelable();
	}

	@Override
	public void reportTaskCompleted() {
		progress.reportTaskCompleted();
	}

	@Override
	public void setDebug(boolean debug) {
		progress.setDebug(debug);
	}

	@Override
	public void setPause(boolean pause) {
		progress.setPause(pause);
	}

	@Override
	public boolean getPause() {
		return progress.getPause();
	}
}
