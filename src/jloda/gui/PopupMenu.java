/**
 * PopupMenu.java 
 * Copyright (C) 2016 Daniel H. Huson
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
