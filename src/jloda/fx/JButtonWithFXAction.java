/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package jloda.fx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javax.swing.*;

/**
 * a swing button that calls an FX event handler
 * Created by huson on 2/17/17.
 */
public class JButtonWithFXAction extends JButton {
    /**
     * constructor
     *
     * @param name
     * @param eventHandler
     */
    public JButtonWithFXAction(String name, final EventHandler<ActionEvent> eventHandler) {
        this.setAction(new AbstractAction(name) {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        eventHandler.handle(new ActionEvent(this, null));
                    }
                });
            }
        });
    }

}
