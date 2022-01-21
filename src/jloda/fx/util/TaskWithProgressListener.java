/*
 * TaskWithProgressListener.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.util;

import javafx.concurrent.Task;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;

import java.util.concurrent.Callable;

/**
 * A JavaFX task with an old style progress listener
 * Daniel Huson, 1.2019
 *
 * @param <T>
 */
abstract public class TaskWithProgressListener<T> extends Task<T> implements Callable<T> {
    private long currentProgress = 0;
    private long maxProgress = 0;
    private boolean cancelable = true;
    private boolean isCanceled = false;
    private boolean debug = false;

    private ProgressListener progressListener;

    public ProgressListener getProgressListener() {
        if (progressListener == null)
            progressListener = new ProgressListener() {
                @Override
                public void setMaximum(long maxProgress) {
                    if (debug)
                        System.err.println("progress.setMaximum(" + maxProgress + ")");
                    TaskWithProgressListener.this.maxProgress = maxProgress;
                    updateProgress(currentProgress, maxProgress);
                }

                @Override
                public void setProgress(long currentProgress) throws CanceledException {
                    checkForCancel();
                    TaskWithProgressListener.this.currentProgress = currentProgress;
                    if (debug)
                        System.err.println("progress.setProgress(" + currentProgress + ")");
                    TaskWithProgressListener.this.updateProgress(currentProgress, maxProgress);
                }

                @Override
                public void setProgressIgnoreCancel(long currentProgress) {
                    TaskWithProgressListener.this.currentProgress = currentProgress;
                    if (debug)
                        System.err.println("progress.setProgress(" + currentProgress + ")");
                    TaskWithProgressListener.this.updateProgress(currentProgress, maxProgress);
                }


                @Override
                public long getProgress() {
                    return currentProgress;
                }

                @Override
                public void checkForCancel() throws CanceledException {
                    isCanceled = TaskWithProgressListener.this.isCancelled();
                    if (cancelable && isCanceled) {
                        if (debug)
                            System.err.println("progress.checkForCancel()=true");
                        throw new CanceledException();
                    }
                }

                @Override
                public void setTasks(String taskName, String subtaskName) {
                    TaskWithProgressListener.this.updateTitle(taskName.replaceAll(":$", ""));
                    TaskWithProgressListener.this.updateMessage(subtaskName != null ? subtaskName : "");
                    if (debug)
                        System.err.println("progress.setTasks(" + taskName + "," + subtaskName + ")");
                }

                @Override
                public void setSubtask(String subtaskName) {
                    TaskWithProgressListener.this.updateMessage(subtaskName != null ? subtaskName : "");
                    if (debug)
                        System.err.println("progress.setSubtask(" + subtaskName + ")");
                }

                @Override
                public void setCancelable(boolean enabled) {
                    TaskWithProgressListener.this.cancelable = enabled;
                }

                @Override
                public boolean isUserCancelled() {
                    return false;
                }

                @Override
                public void setUserCancelled(boolean userCancelled) {
                    TaskWithProgressListener.this.isCanceled = userCancelled;
                }

                @Override
                public void incrementProgress() throws CanceledException {
                    checkForCancel();
                    if (debug)
                        System.err.println("progress.incrementProgress()");
                    TaskWithProgressListener.this.updateProgress(++currentProgress, maxProgress);
                }

                @Override
                public void incrementProgressIgnoreCancel() {
                    if (debug)
                        System.err.println("progress.incrementProgress()");
                    TaskWithProgressListener.this.updateProgress(++currentProgress, maxProgress);
                }

                @Override
                public void reportTaskCompleted() {
                    if (debug)
                        System.err.println("progress.reportTaskCompleted()");
                }

                @Override
                public void close() {
                    if (debug)
                        System.err.println("progress.close()");
                }

                @Override
                public boolean isCancelable() {
                    return cancelable;
                }

                @Override
                public void setDebug(boolean debug) {
                    TaskWithProgressListener.this.debug = debug;
                }

                @Override
                public void setPause(boolean pause) {
                    System.err.println("pause: " + pause);
                }

                @Override
                public boolean getPause() {
                    return false;
                }
            };
        return progressListener;
    }
}
