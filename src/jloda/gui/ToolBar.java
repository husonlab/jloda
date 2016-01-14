/**
 * ToolBar.java 
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
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * tool bar generator
 * Daniel Huson, 16.2010
 */
public class ToolBar extends JToolBar {
    static private IToolBarModifier toolBarModifier;

    /**
     * construct a tool bar using the given configuation
     * Example:  New...;Save...;|;Print...;|;Select All;
     * To add a button with text label, tooltip and popup menu, use this syntax:
     * {;label(tooltip);command1;command2;command3;};
     *
     * @param configuration
     * @param commandManager
     * @throws Exception
     */
    public ToolBar(String configuration, CommandManager commandManager) {
        super();
        this.setRollover(true);
        this.setBorder(BorderFactory.createEtchedBorder());
        this.setFloatable(false);
        this.setLayout(new WrapLayout(FlowLayout.LEFT, 2, 2));

        String[] tokens = configuration.split(";");

        JPopupMenu popupMenu = null; // not null when in creation of popup menu
        boolean needToAddPopupMenu = false;

        for (String token : tokens) {
            switch (token) {
                case "|":
                    if (popupMenu != null)
                        popupMenu.addSeparator();
                    else
                        addSeparator(new Dimension(5, 10));
                    break;
                case "{":
                    if (popupMenu == null) {
                        popupMenu = new JPopupMenu();
                        needToAddPopupMenu = true;
                    } else
                        System.err.println("Warning: nested popup menu in toolbar detected, not implemented");
                    break;
                case "}":
                    popupMenu = null;
                    needToAddPopupMenu = false;
                    break;
                default:
                    if (CommandManager.getCommandsToIgnore().contains(token)) {
                        if (needToAddPopupMenu) // this popup menu is disabled
                        {
                            popupMenu = null;
                            needToAddPopupMenu = false;
                        }
                        continue;
                    }

                    if (needToAddPopupMenu) {
                        String tooltip = null;
                        int a = token.indexOf("(");
                        int b = token.indexOf(")");
                        if (a != -1 && b > a + 1) {
                            tooltip = token.substring(a + 1, b);
                            token = token.substring(0, a);
                        }
                        final JButton button = new JButton(token);
                        Basic.changeFontSize(button, 10);

                        if (tooltip != null)
                            button.setToolTipText(tooltip);
                        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(2, 0, 2, 0)));
                        final JPopupMenu popup = popupMenu;
                        button.addMouseListener(new MouseAdapter() {
                            public void mousePressed(MouseEvent e) {
                                popup.show(e.getComponent(), e.getX(), e.getY());
                            }
                        });
                        add(button);
                        needToAddPopupMenu = false;
                        continue;
                    }

                    ICommand command = commandManager.getCommand(token);
                    if (command == null) {
                        JLabel label = new JLabel("(" + token + ")");
                        label.setBorder(BorderFactory.createEmptyBorder());
                        label.setEnabled(false);
                        add(label);
                    } else if (popupMenu != null) {
                        popupMenu.add(commandManager.getJMenuItem(command));
                    } else {
                        AbstractButton button = commandManager.getButtonForToolBar(command);
                        //button = new JToggleButton(button.getAction());
                        //button.setText(null);
                        button.setBorder(BorderFactory.createEtchedBorder());
                        add(button);
                    }
                    break;
            }
        }
        if (toolBarModifier != null)
            toolBarModifier.apply(this, commandManager);

        if (ProgramProperties.get("showtex", false)) {
            System.out.println(TeXGenerator.getToolBarLaTeX(configuration, commandManager));
        }
    }

    public static IToolBarModifier getToolBarModifier() {
        return toolBarModifier;
    }

    public static void setToolBarModifier(IToolBarModifier toolBarModifier) {
        ToolBar.toolBarModifier = toolBarModifier;
    }

}
