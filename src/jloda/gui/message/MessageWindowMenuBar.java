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

package jloda.gui.message;


import jloda.util.MenuMnemonics;
import jloda.util.ProgramProperties;

import javax.swing.*;


/**
 * the editor window menu bar
 *
 * @author huson
 *         Date: 17.2.04
 */
public class MessageWindowMenuBar extends JMenuBar {
    private final MessageWindow viewer;

    public MessageWindowMenuBar(MessageWindow viewer) {
        super();
        this.viewer = viewer;

        if (!ProgramProperties.isMacOS()) {
            addFileMenu();
            addEditMenu();

            for (int i = 0; i < this.getMenuCount(); i++)
                MenuMnemonics.setMnemonics(this.getMenu(i));
        } else {
            //ToDo: should be copy of other menus, with things dimmed out.
            addFileMenu();
            addEditMenu();
            for (int i = 0; i < this.getMenuCount(); i++)
                MenuMnemonics.setMnemonics(this.getMenu(i));
        }
    }

    /**
     * returns the tool bar for this simple viewer
     */
    private void addFileMenu() {
        JMenu menu = new JMenu("File");

        menu.add(viewer.getActions().getSaveFile());
        menu.addSeparator();
        menu.add(viewer.getActions().getPrintIt());
        menu.addSeparator();
        menu.add(viewer.getActions().getClose());

        add(menu);
    }

    private void addEditMenu() {
        JMenu menu = new JMenu("Edit");

        //menu.add(viewer.getActions().getUndo());
        //menu.addSeparator();
        JMenuItem menuItem = new JMenuItem(viewer.getActions().getCut());
        menuItem.setText("Cut");
        menu.add(menuItem);
        menuItem = new JMenuItem(viewer.getActions().getCopy());
        menuItem.setText("Copy");
        menu.add(menuItem);
        menuItem = new JMenuItem(viewer.getActions().getPaste());
        menuItem.setText("Paste");
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new JMenuItem(viewer.getActions().getSelectAll());
        menuItem.setText("Select All");
        menu.add(menuItem);
        menu.addSeparator();

        menu.add(viewer.getActions().getClear());
        // menu.add(viewer.getActions().gedendroscopet());

        add(menu);
    }
}
