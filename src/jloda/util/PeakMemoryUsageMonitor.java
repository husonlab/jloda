/*
 * PeakMemoryUsageMonitor.java Copyright (C) 2019. Daniel H. Huson
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

import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * this class records the peak memory usage of a program
 * Daniel Huson, 5.2015
 */
public class PeakMemoryUsageMonitor {
    private static PeakMemoryUsageMonitor instance;
    private final long start;
    private long peak = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000);

    /**
     * constructor
     */
    private PeakMemoryUsageMonitor() {
        start = System.currentTimeMillis();
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            long used = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000);
            if (used > peak)
                peak = used;
        }, 0, 5, SECONDS);
    }

    private static PeakMemoryUsageMonitor getInstance() {
        if (instance == null) {
            instance = new PeakMemoryUsageMonitor();
        }
        return instance;
    }

    /**
     * start recording memory and time
     */
    public static void start() {
        getInstance();
    }

    /**
     * report the recorded memory and time
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
        long available = (Runtime.getRuntime().maxMemory() / 1000000);
        if (available < 1024) {
            return String.format("%d of %dM", getInstance().peak, available);
        } else {
            return String.format("%.1f of %.1fG", (double) getInstance().peak / 1000.0, (double) available / 1000.0);
        }
    }

    /**
     * get number of elapsed seconds since start
     *
     * @return seconds since start
     */
    public static String getSecondsSinceStartString() {
        return ((System.currentTimeMillis() - getInstance().start) / 1000) + "s";
    }

}
