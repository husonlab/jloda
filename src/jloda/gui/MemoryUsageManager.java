/**
 * MemoryUsageManager.java 
 * Copyright (C) 2016 Daniel H. Huson
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
package jloda.gui;

import jloda.util.Basic;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * manages memory usage message, typically displayed in status bar of a window
 * Daniel Huson, 7.2011
 */
public class MemoryUsageManager {
    static private MemoryUsageManager memoryUsageManager;
    private final List<WeakReference<ChangeListener>> changeListeners;

    /**
     * constructor
     */
    private MemoryUsageManager() {
        changeListeners = new LinkedList<>();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            ChangeEvent changeEvent = new ChangeEvent(Basic.getMemoryUsageString());
                            synchronized (changeListeners) {
                                for (WeakReference<ChangeListener> weak : changeListeners) {
                                    ChangeListener listener = weak.get();
                                    if (listener != null)
                                        listener.stateChanged(changeEvent);
                                }
                            }
                        }
                    });
                } catch (InterruptedException | InvocationTargetException e) {
                    Basic.caught(e);
                }
            }
        }, 0, 5, SECONDS);
    }

    public static void addChangeListener(ChangeListener changeListener) {
        if (memoryUsageManager == null)
            memoryUsageManager = new MemoryUsageManager();
        synchronized (memoryUsageManager.changeListeners) {
            memoryUsageManager.changeListeners.add(new WeakReference<>(changeListener));
        }
    }
}
