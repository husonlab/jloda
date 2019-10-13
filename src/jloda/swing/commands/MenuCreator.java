/*
 * MenuCreator.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.swing.util.AppleSystemMenuItems;
import jloda.swing.util.ResourceManager;
import jloda.swing.util.lang.Translator;
import jloda.swing.window.IMenuModifier;
import jloda.swing.window.MenuMnemonics;
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

/**
 * class for creating and managing menus
 * Daniel Huson, 8.2006
 */
public class MenuCreator {
    public final static String MENUBAR_TAG = "MenuBar";
    static IMenuModifier menuModifer;

    private final CommandManager commandManager;
    private final Object viewer;

    /**
     * constructor
     */
    public MenuCreator(Object viewer, CommandManager commandManager) {
        this.viewer = viewer;
        this.commandManager = commandManager;
    }

    /**
     * builds a menu bar from a set of description lines.
     * Description must contain one menu bar line in the format:
     * MenuBar.menuBarLabel=item;item;item...;item, where menuBarLabel must match the
     * given name and each item is of the form Menu.menuBarLabel or simply menuBarLabel,
     *
     * @param menuBarLabel
     * @param descriptions
     * @param menuBar
     * @throws Exception
     */
    public void buildMenuBar(String menuBarLabel, Hashtable<String, String> descriptions, JMenuBar menuBar) throws Exception {
        /*
        System.err.println("Known actions:");
        for (Iterator it = actions.keySet().iterator(); it.hasNext();) {
            System.err.println(it.next());
        }
         */

        menuBarLabel = MENUBAR_TAG + "." + menuBarLabel;
        if (!descriptions.containsKey(menuBarLabel))
            throw new Exception("item not found: " + menuBarLabel);

        List<String> menus = getTokens(descriptions.get(menuBarLabel));

        for (String menuLabel : menus) {
            if (!menuLabel.startsWith("Menu."))
                menuLabel = "Menu." + menuLabel;
            if (descriptions.containsKey(menuLabel)) {
                final JMenu menu = buildMenu(menuLabel, descriptions, false);
                addSubMenus(0, menu, descriptions);
                MenuMnemonics.setMnemonics(menu);
                menuBar.add(menu);
            }
        }
    }

    /**
     * builds a menu from a description.
     * Format:
     * Menu.menuLabel=name;item;item;...;item;  where  name is menu name
     * and item is either the menuLabel of an action, | to indicate a separator
     * or @menuLabel to indicate menuLabel name of a submenu
     *
     * @param menuBarConfiguration
     * @param menusConfigurations
     * @param addEmptyIcon
     * @return menu
     * @throws Exception
     */
    private JMenu buildMenu(String menuBarConfiguration, Hashtable<String, String> menusConfigurations, boolean addEmptyIcon) throws Exception {
        if (!menuBarConfiguration.startsWith("Menu."))
            menuBarConfiguration = "Menu." + menuBarConfiguration;
        String description = menusConfigurations.get(menuBarConfiguration);
        if (description == null)
            return null;
        List<String> menuDescription = getTokens(description);
        if (menuDescription.size() == 0)
            return null;
        boolean skipNextSeparator = false;  // avoid double separators
        Iterator it = menuDescription.iterator();
        String menuName = (String) it.next();
        JMenu menu = new JMenu(Translator.get(menuName));
        if (addEmptyIcon)
            menu.setIcon(ResourceManager.getIcon("Empty16.gif"));
        String[] labels = menuDescription.toArray(new String[0]);
        for (int i = 1; i < labels.length; i++) {
            String label = labels[i];
            if (i == labels.length - 2 && label.equals("|") && labels[i + 1].equals("Quit"))
                skipNextSeparator = true; // avoid separator at bottom of File menu in mac version

            if (skipNextSeparator && label.equals("|")) {
                skipNextSeparator = false;
                continue;
            }
            skipNextSeparator = false;

            if (label.startsWith("@")) {
                JMenu subMenu = new JMenu(Translator.get(label));
                subMenu.setIcon(ResourceManager.getIcon("Empty16.gif"));
                menu.add(subMenu);
            } else if (label.equals("|")) {
                menu.addSeparator();
                skipNextSeparator = true;
            } else {
                if (CommandManager.getCommandsToIgnore().contains(label))
                    continue;
                final ICommand command = commandManager.getCommand(label);
                if (command != null) {
                    label = command.getName(); // label might have been altName...
                    if (CommandManager.getCommandsToIgnore().contains(label))
                        continue;
                    boolean done = false;
                    if (ProgramProperties.isMacOS()) {
                        switch (label) {
                            case "Quit": {
                                if (AppleSystemMenuItems.setQuitAction(createAction(command))) {
                                    if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) == null) {
                                        skipNextSeparator = true;
                                    }
                                    done = true;
                                }
                                break;
                            }
                            case "About":
                            case "About...": {
                                if (AppleSystemMenuItems.setAboutAction(createAction(command))) {
                                    if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) == null) {
                                        skipNextSeparator = true;
                                    }
                                    done = true;
                                }
                                break;
                            }
                            case "Preferences":
                            case "Preferences...": {
                                if (AppleSystemMenuItems.setPreferencesAction(createAction(command))) {
                                    if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) == null) {
                                        skipNextSeparator = true;
                                    }
                                    done = true;
                                }
                                break;
                            }
                        }
                    }
                    if (!done) {
                        final JMenuItem menuItem = commandManager.getJMenuItem(command);
                        menuItem.setText(Translator.get(label));
                        menuItem.setToolTipText(command.getDescription());
                        menu.add(menuItem);
                        // always add empty icon, if non is given
                        if (menuItem.getIcon() == null)
                            menuItem.setIcon(ResourceManager.getIcon("Empty16.gif"));
                    }
                } else {
                    final JMenuItem menuItem = new JMenuItem(label + " #");
                    menuItem.setIcon(ResourceManager.getIcon("Empty16.gif"));
                    menu.add(menuItem);
                    menu.getItem(menu.getItemCount() - 1).setEnabled(false);
                }
            }
        }
        if (menuModifer != null)
            menuModifer.apply(menu, viewer, commandManager);
        if (ProgramProperties.get("showtex", false)) {
            System.out.println(TeXGenerator.getMenuLaTeX(commandManager, menuBarConfiguration, menusConfigurations));
        }

        return menu;
    }

    /**
     * adds submenus to a menu
     *
     * @param depth
     * @param menu
     * @param descriptions
     * @throws Exception
     */
    private void addSubMenus(int depth, JMenu menu, Hashtable<String, String> descriptions) throws Exception {
        if (depth > 5)
            throw new Exception("Submenus: too deep: " + depth);
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item != null && item.getText() != null && item.getText().startsWith("@")) {
                String name = item.getText().substring(1);
                item.setText(name);
                JMenu subMenu = buildMenu(name, descriptions, true);
                if (subMenu != null) {
                    addSubMenus(depth + 1, subMenu, descriptions);
                    menu.remove(i);
                    menu.add(subMenu, i);
                }
            }
        }
    }

    /**
     * find named menu
     *
     * @param name
     * @param menuBar
     * @param mayBeSubmenu also search for sub menu
     * @return menu or null
     */
    public static JMenu findMenu(String name, JMenuBar menuBar, boolean mayBeSubmenu) {
        name = Translator.get(name, false);
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu result = findMenu(name, menuBar.getMenu(i), mayBeSubmenu);
            if (result != null)
                return result;
        }
        return null;
    }

    /**
     * searches for menu by name
     *
     * @param name
     * @param menu
     * @param mayBeSubmenu
     * @return menu or null
     */
    public static JMenu findMenu(String name, JMenu menu, boolean mayBeSubmenu) {
        name = Translator.get(name, false);
        // System.err.println("TEXT: " + menu.getText());
        if (menu.getText().equals(name))
            return menu;
        if (mayBeSubmenu) {
            for (int j = 0; j < menu.getItemCount(); j++) {
                JMenuItem item = menu.getItem(j);
                if (item != null) {
                    Component comp = item.getComponent();
                    if (comp instanceof JMenu) {
                        JMenu result = findMenu(name, (JMenu) comp, true);
                        if (result != null)
                            return result;
                    }
                }
            }
        }
        return null;
    }


    /**
     * get the list of tokens in a description
     *
     * @param str
     * @return list of tokens
     * @throws Exception
     */
    static public List<String> getTokens(String str) throws Exception {
        try {
            int pos = str.indexOf("=");
            str = str.substring(pos + 1).trim();
            StringTokenizer tokenizer = new StringTokenizer(str, ";");
            List<String> result = new LinkedList<>();
            while (tokenizer.hasMoreTokens())
                result.add(tokenizer.nextToken());
            return result;
        } catch (Exception ex) {
            throw new Exception("failed to parse description-line: <" + str + ">: " + ex);
        }
    }

    /**
     * create an action for the given command
     *
     * @param command
     * @return action
     */
    private AbstractAction createAction(final ICommand command) {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (command.getAutoRepeatInterval() > 0)
                    command.actionPerformedAutoRepeat(actionEvent);
                else
                    command.actionPerformed(actionEvent);
            }
        };
        action.putValue(AbstractAction.NAME, command.getName());
        if (command.getDescription() != null)
            action.putValue(AbstractAction.SHORT_DESCRIPTION, command.getDescription());
        if (command.getIcon() != null)
            action.putValue(AbstractAction.SMALL_ICON, command.getIcon());
        if (command.getAcceleratorKey() != null)
            action.putValue(AbstractAction.ACCELERATOR_KEY, command.getAcceleratorKey());
        return action;
    }

    /**
     * if set, the menu modifier is applied to each menu after it is built
     *
     * @param menuModifier
     */
    public static void setMenuModifier(IMenuModifier menuModifier) {
        menuModifer = menuModifier;
    }

}
