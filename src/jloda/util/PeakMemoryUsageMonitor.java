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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * this class records the peak memory usage of a program
 * Daniel Huson, DATE
 */
public class PeakMemoryUsageMonitor {
    private static PeakMemoryUsageMonitor instance;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private long peak = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);

    private PeakMemoryUsageMonitor() {
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                long used = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
                if (used > peak)
                    peak = used;
            }
        }, 0, 5, SECONDS);
    }

    /**
     * start recording memory usage
     */
    public static void start() {
        if (instance == null)
            instance = new PeakMemoryUsageMonitor();
    }

    /**
     * stop recording memory usage
     */
    public static void stop() {
        if (instance != null) {
            instance.scheduler.shutdownNow();
            instance = null;
        }
    }

    /**
     * get peak usage string
     *
     * @return peak usage
     */
    public static String getPeakUsageString() {
        long available = (Runtime.getRuntime().maxMemory() / 1048576);
        if (available < 1024) {
            return String.format("%d of %dM", instance.peak, available);
        } else {
            return String.format("%.1f of %.1fG", (double) instance.peak / 1024.0, (double) available / 1024.0);
        }
    }

}
