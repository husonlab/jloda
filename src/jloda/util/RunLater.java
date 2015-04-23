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
        Runnable myRunnable = new Runnable() {
            public void run() {
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
            }
        };
        Thread worker = new Thread(myRunnable);
        worker.setDaemon(true);
        worker.setPriority(Thread.currentThread().getPriority() - 1);
        worker.start();

        System.err.println("Out:");
    }
}
