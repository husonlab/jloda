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
