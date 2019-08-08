/*
 * InfoMessage.java Copyright (C) 2019. Daniel H. Huson
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
 *
 */

package jloda.swing.util;

import jloda.util.Basic;
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;

/**
 * show an info window
 *
 * @author Daniel Huson, 6.2019
 */
public class InfoMessage {

    public InfoMessage(String message) {
        this(null, message);
    }

    public InfoMessage(Component parent, String message) {
        this(null, message, false);
    }

    public InfoMessage(Component parent, final String message, boolean echoToConsole) {
        if (ProgramProperties.isUseGUI()) {
            String label;
            if (ProgramProperties.getProgramName() != null)
                label = "Info - " + ProgramProperties.getProgramName();
            else
                label = "Info";

            JOptionPane.showMessageDialog(parent, Basic.toMessageString(message), label, JOptionPane.INFORMATION_MESSAGE, ProgramProperties.getProgramIcon());
            if (echoToConsole)
                System.err.println("Info - " + message);
        } else
            System.err.println("Info - " + message);
    }
}
