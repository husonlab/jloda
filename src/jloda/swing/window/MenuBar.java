/*
 * MenuBar.java Copyright (C) 2023 Daniel H. Huson
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

package jloda.swing.window;

import jloda.swing.commands.CommandManager;
import jloda.swing.commands.MenuCreator;
import jloda.swing.util.ResourceManager;
import jloda.util.Basic;
import jloda.util.PropertiesListListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * makes the menu bar
 * Daniel Huson, 1.2007
 */
public class MenuBar extends JMenuBar {
    private final JMenu windowMenu;
    private JMenu recentFilesMenu;
    static int numberFixedWindowMenuItems = 0;
    private PropertiesListListener recentFilesListener;

    /**
     * creates the window menu bar
     *
	 */
    public MenuBar(Object viewer, MenuConfiguration configuration, CommandManager commandManager) {
        MenuCreator menuCreator = new MenuCreator(viewer, commandManager);
        try {
            menuCreator.buildMenuBar("main", configuration, this);
        } catch (Exception e) {
            Basic.caught(e);
            throw new RuntimeException("Failed to build menus: " + e);
        }
        windowMenu = MenuCreator.findMenu("Window", this, false);
        if (windowMenu != null)
            numberFixedWindowMenuItems = windowMenu.getItemCount();
        JMenu recentFilesMenu = MenuCreator.findMenu("Open Recent", this, true);
        if (recentFilesMenu != null)
            setupRecentFilesMenu(commandManager, recentFilesMenu);
    }

    /**
     * setup the recent files menu
     */
    private void setupRecentFilesMenu(final CommandManager commandManager, final JMenu recentFilesMenu) {
        this.recentFilesMenu = recentFilesMenu;
        recentFilesMenu.setIcon(ResourceManager.getIcon("sun/Open16.gif"));

        recentFilesListener = new PropertiesListListener() {
            public boolean isInterested(String name) {
                return name != null && name.equals("RecentFiles");
            }

            public void hasChanged(List<String> recentFileNames) {
                recentFilesMenu.removeAll();
                for (String fileName : recentFileNames) {
                    recentFilesMenu.add(createOpenRecentFileAction(commandManager, fileName));
                    recentFilesMenu.setEnabled(recentFilesMenu.getItemCount() > 0);
                }
            }
        };
    }

    /**
     * gets a recent files listener  used by the menu to listener for changes to the recent files menu
     *
     * @return listener
     */
    public PropertiesListListener getRecentFilesListener() {
        return recentFilesListener;
    }

    /**
     * gets the windows menu
     *
     * @return windows menu
     */
    public JMenu getWindowMenu() {
        return windowMenu;
    }

    private AbstractAction createOpenRecentFileAction(final CommandManager commandManager, final String recentFileName) {
        final String displayName;
        if (recentFileName.length() <= 40)
            displayName = recentFileName;
        else
            displayName = "..." + recentFileName.substring(recentFileName.length() - 35);

        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                commandManager.getDir().execute("open file='" + recentFileName + "';", commandManager);
            }
        };
        action.putValue(AbstractAction.NAME, displayName);
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/Open16.gif"));

        return action;
    }

    /**
     * turn all recent file menu items on or off
     *
	 */
    public void setEnableRecentFileMenuItems(boolean state) {
        if (recentFilesMenu != null)
            for (Component component : recentFilesMenu.getMenuComponents()) {
                if (component instanceof JMenuItem) {
                    component.setEnabled(state);
                }
            }
    }

    /**
     * find the named top-level menu
     *
     * @return menu or null
     */
    public JMenu findMenu(String name) {
        for (int i = 0; i < getMenuCount(); i++) {
            JMenu menu = getMenu(i);
            if (menu.getText().equals(name))
                return menu;
        }
        return null;
    }
}
