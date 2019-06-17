/*
 * AppleSystemMenuItems.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.util;


import javax.swing.*;
import java.awt.*;

/**
 * Set apple system menu items
 * Daniel Huson, 3.2014
 */
public class AppleSystemMenuItems {
    /**
     * sets the quit menu action
     *
     * @param action
     * @return true if set
     */
    public static boolean setQuitAction(final Action action) {
        final Desktop desktop = Desktop.getDesktop();
        if (desktop != null) {
            desktop.setQuitHandler((e, r) -> {
                action.actionPerformed(null);
                r.cancelQuit();
            });
            return true;
        } else
            return false;
    }

    /**
     * set the about menu action
     *
     * @param action
     * @return true if set
     */
    public static boolean setAboutAction(final Action action) {
        final Desktop desktop = Desktop.getDesktop();
        if (desktop != null) {
            desktop.setAboutHandler((e) -> action.actionPerformed(null));
            return true;
        } else
            return false;
    }

    /**
     * set the preferences menu action
     *
     * @param action
     * @return true if set
     */
    public static boolean setPreferencesAction(final Action action) {
        final Desktop desktop = Desktop.getDesktop();
        if (desktop != null) {
            desktop.setPreferencesHandler((e) -> action.actionPerformed(null));
            return true;
        } else
            return false;
    }
}
