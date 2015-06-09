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

import jloda.util.Single;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * choose a color
 * Daniel Huson, 4.2011
 */
public class ChooseColorDialog {

    /**
     * show a choose color dialog
     *
     * @param parent
     * @param title
     * @param defaultColor
     * @return color chosen or null
     */
    public static Color showChooseColorDialog(JFrame parent, String title, Color defaultColor) {
        final JColorChooser chooserPane = new JColorChooser();
        if (defaultColor != null)
            chooserPane.setColor(defaultColor);

        final Single<Color> result = new Single<>();

        ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                result.set(chooserPane.getColor());
            }
        };

        ActionListener cancelListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                result.set(null);
            }
        };

        JDialog chooser = JColorChooser.createDialog(parent, title, true, chooserPane, okListener, cancelListener);
        chooser.setVisible(true);

        return result.get();
    }
}
