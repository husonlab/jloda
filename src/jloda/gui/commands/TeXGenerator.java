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

package jloda.gui.commands;

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

        String[] labels = menuDescription.toArray(new String[menuDescription.size()]);
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
