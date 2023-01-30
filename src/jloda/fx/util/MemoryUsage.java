/*
 * MemoryUsage.java Copyright (C) 2023 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jloda.util.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * monitors memory usage
 * Daniel Huson, 1.2018
 */
public class MemoryUsage {
    private static MemoryUsage instance;

    private final StringProperty memoryUsageString;

    private final LongProperty currentMemoryUsage;
    private final LongProperty peakMemoryUsage;

    private final LongProperty availableMemory;

    private MemoryUsage() {
        memoryUsageString = new SimpleStringProperty();
        currentMemoryUsage = new SimpleLongProperty(0);
        peakMemoryUsage = new SimpleLongProperty(0);
        availableMemory = new SimpleLongProperty(Runtime.getRuntime().maxMemory() / 1048576);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            long usage = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
            if (usage != currentMemoryUsage.get()) {
                Platform.runLater(() -> {
                    currentMemoryUsage.set(usage);
                    if (currentMemoryUsage.get() > peakMemoryUsage.get())
                        peakMemoryUsage.set(currentMemoryUsage.get());
                    if (availableMemory.get() < 1024)
						memoryUsageString.set("%d of %dM".formatted(currentMemoryUsage.get(), availableMemory.get()));
                    else
                        memoryUsageString.set(StringUtils.removeTrailingZerosAfterDot("%.1f",currentMemoryUsage.get() / 1024.0)+" of "+
                                              StringUtils.removeTrailingZerosAfterDot("%.1f",availableMemory.get() / 1024.0) + "G");
                });
            }
        }, 0, 5, SECONDS);
    }

    public static MemoryUsage getInstance() {
        if (instance == null)
            instance = new MemoryUsage();
        return instance;
    }

    public LongProperty currentMemoryUsageProperty() {
        return currentMemoryUsage;
    }

    public LongProperty availableMemoryProperty() {
        return availableMemory;
    }

    public LongProperty peakMemoryUsageProperty() {
        return peakMemoryUsage;
    }

    public StringProperty memoryUsageStringProperty() {
        return memoryUsageString;
    }

    public static String getMaxMemoryString() {
        var total=Runtime.getRuntime().maxMemory()/1048576L;
        if (total < 1024L)
            return "%dM".formatted(total);
        else
            return StringUtils.removeTrailingZerosAfterDot("%.1f",total / 1024.0)+"G";
    }
}
