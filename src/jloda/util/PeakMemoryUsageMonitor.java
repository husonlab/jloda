/*
 * PeakMemoryUsageMonitor.java Copyright (C) 2022 Daniel H. Huson
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

import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * this class records the peak memory usage of a program
 * Daniel Huson, 5.2015
 */
public class PeakMemoryUsageMonitor {
    private static long start;
    private static long peak;

    /**
     * start recording memory and time
     */
    public static void start() {
       if(start==0L) {
           Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
               long used = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
               if (used > peak)
                   peak = used;
           }, 0, 5, SECONDS);
       }
        peak = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
        start = System.currentTimeMillis();
    }

    /**
     * report the recorded memory and time to stderr
     */
    public static void report() {
        System.err.println("Total time:  " + PeakMemoryUsageMonitor.getSecondsSinceStartString());
        System.err.println("Peak memory: " + PeakMemoryUsageMonitor.getPeakUsageString());
    }

    /**
     * get peak usage string
     *
     * @return peak usage
     */
    public static String getPeakUsageString() {
        long available = (Runtime.getRuntime().maxMemory() / 1048576);
        if (available < 1024) {
            return String.format("%d of %dM", peak, available);
        } else {
            return StringUtils.removeTrailingZerosAfterDot(String.format("%.1f of %.1f", (double)peak / 1024.0, (double) available / 1024.0)) + "G";
        }
    }

    /**
     * get number of elapsed seconds since start
     *
     * @return seconds since start
     */
    public static String getSecondsSinceStartString() {
        return String.format("%,.1fs", (System.currentTimeMillis() - start) / 1000.0);
    }
}
