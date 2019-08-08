/*
 * MessageWindowMenuBar.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.message;


import jloda.swing.window.MenuMnemonics;
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
