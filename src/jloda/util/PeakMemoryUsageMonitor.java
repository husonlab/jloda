/**
 * PeakMemoryUsageMonitor.java 
 * Copyright (C) 2015 Daniel H. Huson
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
