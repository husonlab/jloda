/*
 * ToolTipHelper.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.swing.util;

import jloda.fx.util.ProgramExecutorService;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Future;

/**
 * use an additional thread to compute and set the tool tip of a component
 * Daniel Huson, 5.2012
 */
public abstract class ToolTipHelper {
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
	 */
    public void mouseMoved(final Point newMousePosition) {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        future = ProgramExecutorService.getInstance().submit(() -> {
            try {
                final String toolTipText = computeToolTip(newMousePosition);
                SwingUtilities.invokeAndWait(() -> component.setToolTipText(toolTipText));

            } catch (Exception ignored) {
            }
        });
    }
}
