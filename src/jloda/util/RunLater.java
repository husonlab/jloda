/*
 * RunLater.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.util;

/**
 * run a method later
 * Daniel Huson, 5.2011
 */
public class RunLater {
    /**
     * wait the given amount of milli-seconds and then call the run method of the runnable object
     *
     * @param waitMilliSeconds
     * @param runnable
     */
    public void apply(final long waitMilliSeconds, final Runnable runnable) {

        System.err.println("In:");
        Runnable myRunnable = () -> {
            long startTime = System.currentTimeMillis();
            long endTime = startTime + waitMilliSeconds;

            while (System.currentTimeMillis() < endTime) {
                // Still within time threshold, wait a little longer
                try {
                    Thread.sleep(100L);  // Sleep 100 milliseconds
                    if (!Thread.currentThread().isAlive()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    // Someone woke us up during sleep, that's OK
                }
            }
            System.err.println("Run:");
            runnable.run();
        };
        Thread worker = new Thread(myRunnable);
        worker.setDaemon(true);
        worker.setPriority(Thread.currentThread().getPriority() - 1);
        worker.start();

        System.err.println("Out:");
    }
}
