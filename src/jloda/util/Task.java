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
 * tasks to be run in parallel on a pool of threads.
 * Use this when you want to run one of the tasks submitted to a ScheduledThreadPoolExecutor
 * in a different thread.
 * Simply call the run method. It will only start running the given runnable if it is not already running.
 * Daniel Huson, 7.2011
 */
public class Task implements Runnable {
    public enum Status {
        PENDING, RUNNING, DONE
    }

    private Status status = Status.PENDING;
    private Runnable runnable;

    /**
     * construct a new task
     */
    public Task() {
    }

    /**
     * set the runnable
     *
     * @param runnable
     */
    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * try to run the task. It will only be executed, if it has status pending.
     * If run, once completed, status is set to done.
     */
    public void run() {
        if (setStatusRun() && runnable != null) {
            try {
                runnable.run();
            } finally {
                setStatusDone();
            }
        }
    }

    /**
     * returns true, if this task has already been completed
     *
     * @return true, if done
     */
    public boolean isDone() {
        return status == Status.DONE;
    }


    /**
     * try to set the status to run
     *
     * @return true, if status was pending, else false
     */
    private boolean setStatusRun() {
        synchronized (this) {
            if (status != Status.PENDING)
                return false;
            status = Status.RUNNING;
            return true;
        }
    }

    /**
     * try to set the status to done
     *
     * @return true, if status was running
     */
    private boolean setStatusDone() {
        synchronized (this) {
            if (status != Status.RUNNING)
                return false;
            status = Status.DONE;
            return true;
        }
    }
}
