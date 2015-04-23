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

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * use an additional thread to compute and set the tool tip of a component
 * Daniel Huson, 5.2012
 */
public abstract class ToolTipHelper {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final JComponent component;
    private Future future;

    /**
     * constructor
     *
     * @param component component to receive tooltip text
     */
    public ToolTipHelper(JComponent component) {
        this.component = component;
    }

    /**
     * override this with code for computing the tool tip text
     *
     * @return tool tip text
     */
    public abstract String computeToolTip(Point mousePosition);

    /**
     * call this whenever mouse has moved
     *
     * @param newMousePosition
     */
    public void mouseMoved(final Point newMousePosition) {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        future = executorService.submit(new Runnable() {
            public void run() {
                try {
                    final String toolTipText = computeToolTip(newMousePosition);
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            component.setToolTipText(toolTipText);
                        }
                    });

                } catch (Exception e) {
                }
            }
        });
    }

    /**
     * shut down this service
     */
    public void shutdownNow() {
        executorService.shutdownNow();
    }
}
