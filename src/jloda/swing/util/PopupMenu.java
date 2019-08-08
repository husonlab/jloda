/*
 * PopupMenu.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.swing.commands.CommandManager;
import jloda.swing.commands.ICommand;
import jloda.swing.commands.TeXGenerator;
import jloda.swing.window.IPopMenuModifier;
import jloda.util.ProgramProperties;

import javax.swing.*;

/**
 * popup menu
 * Daniel Huson, 11.2010
 */
public class PopupMenu extends JPopupMenu {
    private static IPopMenuModifier menuModifier;
    private final boolean isEdgePopup;
    private final boolean isNodePopup;

    /**
     * constructor
     *
     * @param viewer
     * @param configuration
     * @param commandManager
     */
    public PopupMenu(Object viewer, String configuration, CommandManager commandManager) {
        this(viewer, configuration, commandManager, false, false, false);
    }

    /**
     * constructor
     *
     * @param viewer
     * @param configuration
     * @param commandManager
     */
    public PopupMenu(Object viewer, String configuration, CommandManager commandManager, boolean showApplicableOnly) {
        this(viewer, configuration, commandManager, showApplicableOnly, false, false);
    }

    /**
     * constructor
     *
     * @param viewer
     * @param configuration
     * @param commandManager
     */
    public PopupMenu(Object viewer, String configuration, CommandManager commandManager, boolean showApplicableOnly, boolean isNodePopup, boolean isEdgePopup) {
        super();
        this.isNodePopup = isNodePopup;
        this.isEdgePopup = isEdgePopup;
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
        if (menuModifier != null)
            menuModifier.apply(this, viewer, commandManager);
        if (ProgramProperties.get("showtex", false)) {
            System.out.println(TeXGenerator.getPopupMenuLaTeX(configuration, commandManager));
        }
        try {
            commandManager.updateEnableState();
        } catch (Exception ex) {
        }
    }

    public static IPopMenuModifier getMenuModifier() {
        return menuModifier;
    }

    public static void setMenuModifier(IPopMenuModifier menuModifier) {
        PopupMenu.menuModifier = menuModifier;
    }

    public boolean isEdgePopup() {
        return isEdgePopup;
    }

    public boolean isNodePopup() {
        return isNodePopup;
    }
}
