/*
 *  MenuConfiguration.java Copyright (C) 2019 Daniel H. Huson
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

package jloda.swing.window;

import java.util.Hashtable;

/**
 * menu configurator
 * Daniel Huson, 5.2010
 */
public class MenuConfiguration extends Hashtable<String, String> {
    /**
     * Put the menu bar configuration.
     * Example: "File;Edit;Select;Options;Tree;View;Window;"
     *
     * @param menuNames
     */
    public void defineMenuBar(String menuNames) {
        put("MenuBar.main", menuNames);

    }

    /**
     * Configure a menu.
     * Example: "Select", "All Panels;No Panels;Invert Panels;|;"
     *
     * @param name
     * @param menuItemNames
     */
    public void defineMenu(String name, String menuItemNames) {
        put("Menu." + name, name + ";" + menuItemNames);
    }

    /**
     * gets the menu bar description
     *
     * @return
     */
    public String getMenuBar() {
        return get("MenuBar.main");
    }

    /**
     * gets a menu description
     *
     * @param name
     * @return
     */
    public String getMenu(String name) {
        return get(name).replace("name;", "");
    }
}
