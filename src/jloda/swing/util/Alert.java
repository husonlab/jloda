/*
 * Alert.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.util.Basic;
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;

/**
 * show an alert window
 *
 * @author huson
 *         Date: 23-Feb-2004
 */
public class Alert {
    /**
     * create an Alert window with the given message and display it
     *
     * @param message
     */
    public Alert(String message) {
        this(null, message);
    }

    /**
     * create an Alert window with the given message and display it
     *
     * @param parent  parent window
     * @param message
     */
    public Alert(Component parent, final String message) {
        if (ProgramProperties.isUseGUI()) {
            String label;
            if (ProgramProperties.getProgramName() != null)
                label = "Alert - " + ProgramProperties.getProgramName();
            else
                label = "Alert";

            JOptionPane.showMessageDialog(parent, Basic.toMessageString(message), label, JOptionPane.ERROR_MESSAGE, ProgramProperties.getProgramIcon());
        } else
            System.err.println("Alert - " + message);
    }
}
