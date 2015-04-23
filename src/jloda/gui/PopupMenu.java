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

import jloda.gui.commands.CommandManager;
import jloda.gui.commands.ICommand;
import jloda.gui.commands.TeXGenerator;
import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;

import javax.swing.*;

/**
 * popup menu
 * Daniel Huson, 11.2010
 */
public class PopupMenu extends JPopupMenu {
    /**
     * constructor
     *
     * @param configuration
     * @param commandManager
     */
    public PopupMenu(String configuration, CommandManager commandManager) {
        this(configuration, commandManager, false);
    }

    /**
     * constructor
     *
     * @param configuration
     * @param commandManager
     */
    public PopupMenu(String configuration, CommandManager commandManager, boolean showApplicableOnly) {
        super();
        if (configuration != null && configuration.length() > 0) {
            String[] tokens = configuration.split(";");

            for (String token : tokens) {
                if (token.equals("|")) {
                    addSeparator();
                } else {
                    JMenuItem menuItem;
                    ICommand command = commandManager.getCommand(token);
                    if (command == null) {
                        if (showApplicableOnly)
                            continue;
                        menuItem = new JMenuItem(token + "#");
                        menuItem.setEnabled(false);
                        add(menuItem);
                    } else {
                        if (CommandManager.getCommandsToIgnore().contains(command.getName()))
                            continue;
                        if (showApplicableOnly && !command.isApplicable())
                            continue;
                        menuItem = commandManager.getJMenuItem(command);
                    }
                    if (menuItem.getIcon() == null)
                        menuItem.setIcon(ResourceManager.getIcon("Empty16.gif"));
                    add(menuItem);
                }
            }
        }
        if (ProgramProperties.get("showtex", false)) {
            System.out.println(TeXGenerator.getPopupMenuLaTeX(configuration, commandManager));
        }
        try {
            commandManager.updateEnableState();
        } catch (Exception ex) {
        }
    }
}
