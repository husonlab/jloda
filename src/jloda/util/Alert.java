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

            JOptionPane.showMessageDialog(parent, Basic.toMessageString(message), label, JOptionPane.ERROR_MESSAGE);
        } else
            System.err.println("Alert - " + message);
    }
}
