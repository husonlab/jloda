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
        changeListeners = new LinkedList<WeakReference<ChangeListener>>();

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
                } catch (InterruptedException e) {
                    Basic.caught(e);
                } catch (InvocationTargetException e) {
                    Basic.caught(e);
                }
            }
        }, 0, 5, SECONDS);
    }

    public static void addChangeListener(ChangeListener changeListener) {
        if (memoryUsageManager == null)
            memoryUsageManager = new MemoryUsageManager();
        synchronized (memoryUsageManager.changeListeners) {
            memoryUsageManager.changeListeners.add(new WeakReference<ChangeListener>(changeListener));
        }
    }
}
