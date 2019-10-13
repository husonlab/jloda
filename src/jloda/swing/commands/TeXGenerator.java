/*
 * TeXGenerator.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.commands;

import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * generate LaTeX description of menus
 * Daniel Huson, 11.2010
 */
public class TeXGenerator {
    /**
     * Get LaTeX description of menus
     * Format:
     * Menu.menuLabel=name;item;item;...;item;  where  name is menu name
     * and item is either the menuLabel of an action, | to indicate a separator
     * or @menuLabel to indicate menuLabel name of a submenu
     *
     * @param menuBarLayout
     * @param menusConfigurations
     * @return menu description in LaTeX
     * @throws Exception
     */
    public static String getMenuLaTeX(CommandManager commandManager, String menuBarLayout, Hashtable<String, String> menusConfigurations) throws Exception {
        StringWriter w = new StringWriter();

        if (!menuBarLayout.startsWith("Menu."))
            menuBarLayout = "Menu." + menuBarLayout;
        String description = menusConfigurations.get(menuBarLayout);
        if (description == null)
            return null;
        List<String> menuDescription = MenuCreator.getTokens(description);
        if (menuDescription.size() == 0)
            return null;
        Iterator it = menuDescription.iterator();
        String menuName = (String) it.next();

        w.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
        w.write("\\mysubsection{The " + menuName + " menu}\n");
        w.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n");

        w.write("The \\pmenu{" + menuName + "} menu contains the following items:\n\n");
        w.write("\\begin{itemize}\n");

        String[] labels = menuDescription.toArray(new String[0]);
        for (int i = 1; i < labels.length; i++) {
            String name = labels[i];

            if (name.startsWith("@")) {
                w.write("\\item The \\pmenuitem{" + menuName + "}{" + name.substring(1) + "} submenu.\n");
            } else if (name.equals("|")) {
                // separator
            } else {
                ICommand command = commandManager.getCommand(name);
                if (command != null) {
                    name = command.getName(); // label might have been altName...
                    boolean notMac = name.equals("Quit") || name.equals("About") || name.equals("About...") || name.equals("Preferences") || name.equals("Preferences...");
                    name = name.replaceAll("_", "-");
                    String des = command.getDescription();
                    w.write("\\item The \\pmenuitem{" + menuName + "}{" + name + "} item: " + (des != null ? des.replaceAll("_", "-") : " NONE") +
                            (notMac ? " (Windows and Linux only)" : "") + ".\n");
                }
            }
        }
        w.write("\\end{itemize}\n\n");
        return w.toString();
    }

    /**
     * get a laTeX description of a tool bar
     *
     * @param configuration
     * @param commandManager
     * @return LaTeX
     */
    public static String getToolBarLaTeX(String configuration, CommandManager commandManager) {
        StringWriter w = new StringWriter();

        if (configuration != null) {
            w.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
            w.write("\\mysubsection{The Toolbar}\n");
            w.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n");

            w.write("The toolbar contains the following items:\n\n");
            w.write("\\begin{itemize}\n");

            String[] tokens = configuration.split(";");
            for (String name : tokens) {
                if (name.equals("|")) {
                    // separator
                } else {
                    ICommand command = commandManager.getCommand(name);
                    if (command != null) {
                        name = command.getName(); // label might have been altName...
                        w.write("\\item The \\pbutton{" + name + "} item: " + command.getDescription().replaceAll("_", "-") + ".\n");
                    }
                }
            }
            w.write("\\end{itemize}\n\n");
        }
        return w.toString();
    }

    /**
     * get a laTeX description of a tool bar
     *
     * @param configuration
     * @param commandManager
     * @return LaTeX
     */
    public static String getPopupMenuLaTeX(String configuration, CommandManager commandManager) {
        StringWriter w = new StringWriter();

        if (configuration != null) {
            w.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
            w.write("\\mysubsection{The Popup Menu}\n");
            w.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n");

            w.write("The popup menu contains the following items:\n\n");
            w.write("\\begin{itemize}\n");

            String[] tokens = configuration.split(";");
            for (String name : tokens) {
                if (name.equals("|")) {
                    // separator
                } else {
                    ICommand command = commandManager.getCommand(name);
                    if (command != null) {
                        name = command.getName(); // label might have been altName...
                        w.write("\\item The \\ppopupmenuitem{WHICH?}{" + name + "} item: " + command.getDescription().replaceAll("_", "-") + ".\n");
                    }
                }
            }
            w.write("\\end{itemize}\n\n");
        }
        return w.toString();
    }
}
