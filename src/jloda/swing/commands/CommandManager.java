/*
 * CommandManager.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.swing.director.IDirectableViewer;
import jloda.swing.director.IDirector;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.PluginClassLoader;
import jloda.util.parse.NexusStreamParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * managers commands
 * Daniel Huson, 7.2007
 */
public class CommandManager {
    protected static final List<ICommand> globalCommands = new LinkedList<>();

    protected final IDirector dir;
    protected final List<ICommand> commands;
    protected final Map<String, ICommand> name2Command = new HashMap<>();
    protected final Map<String, ICommand> startsWith2Command = new HashMap<>();

    // these are used to update the selection state of check box menu items
    protected final Map<JMenuItem, ICommand> menuItem2Command = new HashMap<>();
    protected final Map<AbstractButton, ICommand> button2Command = new HashMap<>();

    public static final String ALT_NAME = "AltName";

    protected String undoCommand;
    protected boolean returnOnCommandNotFound = false;
    final static protected Set<String> commandsToIgnore = new HashSet<>();

    protected final Object parent;

    /**
     * construct a parser
     *
     * @param dir
     */
    public CommandManager(IDirector dir, List<ICommand> commands) {
        this.dir = dir;
        this.parent = null;
        this.commands = commands;
        for (ICommand command : commands) {
            command.setDir(dir);
        }
    }

    /**
     * construct a parser and load all commands found for the given path
     */
    public CommandManager(IDirector dir, IDirectableViewer viewer, String commandsPath) {
        this(dir, viewer, new String[]{commandsPath}, false);
    }

    /**
     * construct a parser and load all commands found for the given paths
     * @param viewer  usually an IDirectableViewer, but sometimes a JDialog
     */
    public CommandManager(IDirector dir, Object viewer, String[] commandsPaths) {
        this(dir, viewer, commandsPaths, false);
    }

    /**
     * construct a parser and load all commands found for the given path
     */
    public CommandManager(IDirector dir, IDirectableViewer viewer, String commandsPath, boolean returnOnCommandNotFound) {
        this(dir, viewer, new String[]{commandsPath}, returnOnCommandNotFound);
    }

    /**
     * construct a parser and load all commands found for the given paths
     *
     * @param viewer  usually an IDirectableViewer, but sometimes a JDialog
     */
    public CommandManager(IDirector dir, Object viewer, String[] commandsPaths, boolean returnOnCommandNotFound) {
        this.dir = dir;
        this.parent = viewer;
        this.setReturnOnCommandNotFound(returnOnCommandNotFound);
        this.commands = new LinkedList<>();

        addCommands(viewer, globalCommands, true);
        addCommands(viewer, commandsPaths);
    }

    /**
     * add more commands
     *
     * @param viewer
     * @param commandsPaths
     */
    public void addCommands(Object viewer, String[] commandsPaths) {
        final List<ICommand> commands = new LinkedList<>();
        for (String commandsPath : commandsPaths) {
            for (Object obj : PluginClassLoader.getInstances(ICommand.class,commandsPath)) {
                if (obj instanceof ICommand)
                    commands.add((ICommand) obj);
            }
        }
        addCommands(viewer, commands, false);
    }

    /**
     * add the given list of commands
     *
     * @param mustWrap commands that are defined globally but used locally must be wrapped so as to preserve the correct command manager, director and viewer
     */
    public void addCommands(Object viewer, Collection<ICommand> commands, boolean mustWrap) {
        for (final ICommand command0 : commands) {
            final ICommand command;

            if (mustWrap) {
                if (command0 instanceof ICheckBoxCommand)
                    command = new WrappedCheckBoxCommand((ICheckBoxCommand) command0);
                else
                    command = new WrappedCommand(command0);
            }
            else
                command = command0;

            command.setDir(dir);
            if (viewer instanceof IDirectableViewer)
                command.setViewer((IDirectableViewer) viewer);
            else
                command.setViewer(null);
            command.setParent(viewer);
            command.setCommandManager(this);

            String name = command.getAltName();
            if (name == null)
                name = command.getName();
            name2Command.put(name, command);

            String startsWith = command.getStartsWith();
            if (startsWith != null) {
                final ICommand prev = startsWith2Command.get(startsWith);
                if (prev != null) {
                    if (prev.getClass().isAssignableFrom(command.getClass())) // command extends prev
                    {
                    } else if (command.getClass().isAssignableFrom(prev.getClass())) // prev extends command
                    {
                        startsWith2Command.put(startsWith, command);
                    }
                } else
                    startsWith2Command.put(startsWith, command);
            }
            this.commands.add(command);
        }
    }

    /**
     * execute
     *
     * @param commandString
     * @throws IOException
     * @throws CanceledException
     */
    public void execute(String commandString) throws IOException, CanceledException {
        commandString = Basic.protectBackSlashes(commandString);  // need this for windows paths
        NexusStreamParser np = new NexusStreamParser(new StringReader(commandString));
        execute(np);
    }

    /**
     * execute a stream of commands
     *
     * @param np
     */
    public void execute(NexusStreamParser np) throws CanceledException, IOException {
        while (np.peekNextToken() != NexusStreamParser.TT_EOF) {
            if (np.peekMatchIgnoreCase(";")) {
                np.matchIgnoreCase(";"); // skip empty command
            } else {
                boolean found = false;
                for (ICommand command : startsWith2Command.values()) {
                    // System.err.println("trying " + Basic.getShortName(command.getClass()));
                    if (command.getStartsWith() != null && np.peekMatchIgnoreCase(command.getStartsWith())) {
                        try {
                            if (command.getName() != null && command.getName().equals("Undo"))
                                undoCommand = null;
                            else
                                undoCommand = command.getUndo();
                            command.apply(np);
                        } catch (CanceledException e) {
                            // System.err.println("USER canceled");
                            throw e;
                        } catch (Exception e) {
                            Basic.caught(e);
                            System.err.println("Command usage: " + command.getSyntax() + " - " + command.getDescription());
                            throw new IOException(e.getMessage());
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (returnOnCommandNotFound) {
                        String command = np.getWordRespectCase();
                        System.err.println("Failed to parse command: " + command + " " + Basic.toString(np.getTokensRespectCase(null, ";"), " "));
                        String similar = getUsageStartsWith(command);
                        if (similar.length() > 0) {
                            System.err.println("Similar commands:");
                            System.err.print(similar);
                        }
                        return;
                    } else
                        System.err.println("Failed to parse command: '" + np.getWordRespectCase() + "'");
                }
            }
        }
    }

    /**
     * get the named command
     *
     * @param name
     * @return command
     */
    public ICommand getCommand(String name) {
        return name2Command.get(name);
    }

    /**
     * enable or disable all critical actions
     *
     * @param on
     */
    public void setEnableCritical(boolean on) {
        /*
         * update selection state of all menu items
         */
        for (JMenuItem menuItem : menuItem2Command.keySet()) {
            ICommand command = menuItem2Command.get(menuItem);
            if (command != null && command.isCritical()) {
                menuItem.setEnabled(on && command.isApplicable());
            }
        }

        /*
         * update selection state of all check boxes
         */
        for (AbstractButton button : button2Command.keySet()) {
            ICommand command = button2Command.get(button);
            if (command != null && command.isCritical()) {
                button.setEnabled(on && command.isApplicable());
                button.getAction().setEnabled(button.isEnabled());
            }
        }
    }

    /**
     * update the enable state
     */
    public void updateEnableState() {
        /**
         * update selection state of all menu items
         */
        try {
            for (JMenuItem menuItem : menuItem2Command.keySet()) {
                ICommand command = menuItem2Command.get(menuItem);
                if (command != null) {
                    menuItem.setEnabled(command.isApplicable());
                    if (command instanceof ICheckBoxCommand)
                        menuItem.setSelected(((ICheckBoxCommand) command).isSelected());
                }
            }

            /**
             * update selection state of all check boxes
             */
            for (AbstractButton button : button2Command.keySet()) {
                ICommand command = button2Command.get(button);
                if (button.getAction() != null)
                    button.getAction().setEnabled(command.isApplicable());
                else
                    button.setEnabled(command.isApplicable());
                if (command instanceof ICheckBoxCommand) {
                    button.setSelected(((ICheckBoxCommand) command).isSelected());
                    if (((ICheckBoxCommand) command).isSelected())
                        button.setBorder(BorderFactory.createBevelBorder(1));
                    else
                        button.setBorder(BorderFactory.createEtchedBorder());
                    button.repaint();

                }
            }
            } catch (Exception ex) {
            Basic.caught(ex);
        }
    }

    /**
     * update the enable state
     */
    public void updateEnableState(String commandName) {
        for (JMenuItem menuItem : menuItem2Command.keySet()) {
            ICommand command = menuItem2Command.get(menuItem);
            if (command.getName().equals(commandName)) {
                menuItem.setEnabled(command.isApplicable());
                if (command instanceof ICheckBoxCommand)
                    menuItem.setSelected(((ICheckBoxCommand) command).isSelected());
            }
        }

        /**
         * update selection state of all check boxes
         */
        for (AbstractButton button : button2Command.keySet()) {
            ICommand command = button2Command.get(button);
            if (command.getName().equals(commandName)) {
                if (button.getAction() != null)
                    button.getAction().setEnabled(command.isApplicable());
                else
                    button.setEnabled(command.isApplicable());
                if (command instanceof ICheckBoxCommand) {
                    button.setSelected(((ICheckBoxCommand) command).isSelected());
                    if (((ICheckBoxCommand) command).isSelected())
                        button.setBorder(BorderFactory.createBevelBorder(1));
                    else
                        button.setBorder(BorderFactory.createEtchedBorder());
                }
            }
        }
    }

    /**
     * gets the usage of all commands, ordered by menu
     *
     * @param menuBar
     * @return usage
     */
    public String getUsage(JMenuBar menuBar) {
        StringBuilder buf = new StringBuilder();
        Set<ICommand> seen = new HashSet<>();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);

            List<JMenu> subMenus = getUsageMenu("menu", menu, buf, seen);
            while (subMenus.size() > 0) {
                menu = subMenus.remove(0);
                subMenus.addAll(getUsageMenu("sub-menu", menu, buf, seen));
            }
        }

        final Set<ICommand> additionalCommands = new HashSet<>(commands);
        additionalCommands.removeAll(seen);
        if (additionalCommands.size() > 0) {
            buf.append("Additional commands:\n");
            SortedSet<String> lines = new TreeSet<>();
            for (ICommand command : additionalCommands) {
                String syntax = command.getSyntax();
                if (syntax != null) {
                    String description = command.getDescription();
                    if (description != null) {
                        if (Basic.getLastLine(syntax).length() + description.length() < 100)
                            lines.add(syntax + " - " + description + "\n");
                        else
                            lines.add(syntax + "\n\t- " + description + "\n");
                    }
                }
            }
            for (String line : lines)
                buf.append(line);
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * writes the description of a menu and returns all hierachical menus below it
     *
     * @param menu
     * @param buf
     * @param seen
     * @return list of sub menus
     */
    private List<JMenu> getUsageMenu(String label, JMenu menu, StringBuilder buf, Set<ICommand> seen) {
        List<JMenu> subMenus = new LinkedList<>();

        if (menu.getText().equals("Open Recent"))
            return subMenus;
        buf.append(menu.getText()).append(" ").append(label).append(":\n");

        for (int j = 0; j < menu.getItemCount(); j++) {
            JMenuItem item = menu.getItem(j);
            if (item != null) {
                if (item instanceof JMenu) {
                    subMenus.add((JMenu) item);
                } else {
                    Action action = item.getAction();
                    String name = null;
                    if (action != null) {
                        name = (String) action.getValue(ALT_NAME);
                        if (name == null)
                            name = (String) action.getValue(AbstractAction.NAME);
                    }
                    if (name == null)
                        name = item.getText();
                    ICommand command = getCommand(name);
                    if (command != null && command.getSyntax() != null) {
                        String syntax = command.getSyntax();
                        String description = command.getDescription();
                        if (Basic.getLastLine(syntax).length() + description.length() < 100)
                            buf.append(syntax).append(" - ").append(description).append("\n");
                        else
                            buf.append(syntax).append("\n\t- ").append(description).append("\n");

                        seen.add(command);
                    }
                }
            }
        }
        buf.append("\n");
        return subMenus;
    }


    /**
     * gets the usage of all commands
     *
     * @return usage
     */
    public String getUsage() {
        return getUsage((Set<String>) null);
    }

    /**
     * gets the usage of all commands
     *
     * @param keywords set of keywords, if not null or empty, at least one of these words must appear in the syntax or description
     * @return usage
     */
    public String getUsage(Set<String> keywords) {
        SortedSet<String> lines = new TreeSet<>();
        for (ICommand command : commands) {
            if (command.getSyntax() != null) {
                boolean ok = keywords == null || keywords.size() == 0;
                if (!ok) {
                    for (String keyword : keywords) {
                        if (command.getSyntax().toLowerCase().contains(keyword.toLowerCase())) {
                            ok = true;
                            break;
                        }
                        if (command.getDescription() != null && command.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                            ok = true;
                            break;
                        }
                    }
                }
                if (ok)
                    lines.add(command.getSyntax() + " - " + command.getDescription() + "\n");
            }
        }
        StringBuilder buf = new StringBuilder();

        for (String line : lines) {
            buf.append(line);
        }
        return buf.toString();
    }

    /**
     * gets the usage of all commands
     *
     * @return usage
     */
    public String getUsageStartsWith(String name) {
        final SortedSet<String> lines = new TreeSet<>();
        for (ICommand command : commands) {
            String syntaxString = command.getSyntax();
            if (syntaxString != null && syntaxString.trim().toLowerCase().startsWith(name))
                lines.add(syntaxString + " - " + command.getDescription() + "\n");
        }
        StringBuilder buf = new StringBuilder();

        for (String line : lines) {
            buf.append(line);
        }
        return buf.toString();
    }


    /**
     * gets the list of all commands
     *
     * @return all commands
     */
    public List<ICommand> getAllCommands() {
        return commands;
    }

    public String getUndoCommand() {
        return undoCommand;
    }

    /**
     * get a menu item for the named command
     *
     * @param commandName
     * @return menu item
     */
    public JMenuItem getJMenuItem(String commandName) {
        final ICommand command = getCommand(commandName);
        if (command != null) {
            final JMenuItem item = getJMenuItem(command);
            if (item != null) {
                item.setEnabled(command.isApplicable());
                return item;
            }
        }
        return null;
    }

    /**
     * get a menu item for the named command
     *
     * @param commandName
     * @param enabled
     * @return menu item
     */
    public JMenuItem getJMenuItem(String commandName, boolean enabled) {
        ICommand command = getCommand(commandName);
        JMenuItem item = getJMenuItem(command);
        if (item != null)
            item.setEnabled(enabled && command.isApplicable());
        return item;
    }

    /**
     * creates a menu item for the given command
     *
     * @param command
     * @return menu item
     */
    public JMenuItem getJMenuItem(final ICommand command) {
        if (command == null) {
            JMenuItem nullItem = new JMenuItem("Null");
            nullItem.setEnabled(false);
            return nullItem;
        }
        if (command instanceof ICheckBoxCommand) {
            final ICheckBoxCommand checkBoxCommand = (ICheckBoxCommand) command;

            final JCheckBoxMenuItem cbox = new JCheckBoxMenuItem();
            AbstractAction action = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    checkBoxCommand.setSelected(cbox.isSelected());
                    if (command.getAutoRepeatInterval() > 0)
                        command.actionPerformedAutoRepeat(actionEvent);
                    else
                        command.actionPerformed(actionEvent);
                }
            };
            action.putValue(AbstractAction.NAME, command.getName());
            action.putValue(ALT_NAME, command.getAltName());
            if (command.getDescription() != null)
                action.putValue(AbstractAction.SHORT_DESCRIPTION, command.getDescription());
            if (command.getIcon() != null)
                action.putValue(AbstractAction.SMALL_ICON, command.getIcon());
            if (command.getAcceleratorKey() != null)
                action.putValue(AbstractAction.ACCELERATOR_KEY, command.getAcceleratorKey());
            cbox.setAction(action);
            cbox.setSelected(checkBoxCommand.isSelected());
            menuItem2Command.put(cbox, checkBoxCommand);
            return cbox;
        } else {
            AbstractAction action = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    if (command.getAutoRepeatInterval() > 0)
                        command.actionPerformedAutoRepeat(actionEvent);
                    else
                        command.actionPerformed(actionEvent);
                }
            };
            action.putValue(AbstractAction.NAME, command.getName());
            action.putValue(ALT_NAME, command.getAltName());
            if (command.getDescription() != null)
                action.putValue(AbstractAction.SHORT_DESCRIPTION, command.getDescription());
            if (command.getIcon() != null)
                action.putValue(AbstractAction.SMALL_ICON, command.getIcon());
            if (command.getAcceleratorKey() != null)
                action.putValue(AbstractAction.ACCELERATOR_KEY, command.getAcceleratorKey());
            JMenuItem menuItem = new JMenuItem(action);
            menuItem2Command.put(menuItem, command);
            return menuItem;
        }
    }

    /**
     * creates a button for the command
     *
     * @param commandName
     * @return button
     */
    public AbstractButton getButton(String commandName) {
        return getButton(commandName, true);
    }

    private static boolean warned = false;

    /**
     * creates a button for the command
     *
     * @param commandName
     * @param enabled
     * @return button
     */
    public AbstractButton getButton(String commandName, boolean enabled) {
        AbstractButton button = getButton(getCommand(commandName));
        button.setEnabled(enabled);
        if (button.getText() != null && button.getText().equals("Null")) {
            System.err.println("Failed to create button for command '" + commandName + "'");
            if (!warned) {
                warned = true;
                System.err.println("Table of known commands:");
                for (String name : name2Command.keySet()) {
                    System.err.print(" '" + name + "'");
                }
                System.err.println();
            }
        }
        return button;
    }

    /**
     * creates a button for the command
     *
     * @param command
     * @return button
     */
    public AbstractButton getButtonForToolBar(final ICommand command) {
        if (command == null) {
            JButton nullButton = new JButton("Null");
            nullButton.setEnabled(false);
            return nullButton;
        }
        if (command instanceof ICheckBoxCommand) {
            final ICheckBoxCommand checkBoxCommand = (ICheckBoxCommand) command;

            final JButton cbox = new JButton();
            AbstractAction action = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    checkBoxCommand.setSelected(cbox.isSelected());
                    if (cbox.isEnabled()) {
                        if (command.getAutoRepeatInterval() > 0)
                            command.actionPerformedAutoRepeat(actionEvent);
                        else
                            command.actionPerformed(actionEvent);
                    }
                }
            };
            action.putValue(AbstractAction.NAME, command.getName());
            action.putValue(ALT_NAME, command.getAltName());
            if (command.getDescription() != null)
                action.putValue(AbstractAction.SHORT_DESCRIPTION, command.getDescription());
            if (command.getIcon() != null)
                action.putValue(AbstractAction.SMALL_ICON, command.getIcon());
            if (command.getAcceleratorKey() != null)
                action.putValue(AbstractAction.ACCELERATOR_KEY, command.getAcceleratorKey());
            cbox.setAction(action);
            cbox.setSelected(checkBoxCommand.isSelected());
            button2Command.put(cbox, checkBoxCommand);
            if (cbox.getIcon() != null)
                cbox.setText(null);
            return cbox;
        } else
            return getButton(command);
    }

    /**
     * creates a button for the command
     *
     * @param command
     * @return button
     */
    public AbstractButton getButton(final ICommand command) {
        if (command == null) {
            JButton nullButton = new JButton("Null");
            nullButton.setEnabled(false);
            return nullButton;
        }
        if (command instanceof ICheckBoxCommand) {
            final ICheckBoxCommand checkBoxCommand = (ICheckBoxCommand) command;

            final JCheckBox cbox = new JCheckBox();
            AbstractAction action = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    checkBoxCommand.setSelected(cbox.isSelected());
                    if (cbox.isEnabled()) {
                        if (command.getAutoRepeatInterval() > 0)
                            command.actionPerformedAutoRepeat(actionEvent);
                        else
                            command.actionPerformed(actionEvent);
                    }
                }
            };
            action.putValue(AbstractAction.NAME, command.getName());
            action.putValue(ALT_NAME, command.getAltName());
            if (command.getDescription() != null)
                action.putValue(AbstractAction.SHORT_DESCRIPTION, command.getDescription());
            if (command.getIcon() != null)
                action.putValue(AbstractAction.SMALL_ICON, command.getIcon());
            if (command.getAcceleratorKey() != null)
                action.putValue(AbstractAction.ACCELERATOR_KEY, command.getAcceleratorKey());
            cbox.setAction(action);
            cbox.setSelected(checkBoxCommand.isSelected());
            button2Command.put(cbox, checkBoxCommand);
            if (cbox.getIcon() != null)
                cbox.setText(null);
            return cbox;
        } else {
            final JButton button = new JButton();
            AbstractAction action = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    if (button.isEnabled()) {
                        if (command.getAutoRepeatInterval() > 0)
                            command.actionPerformedAutoRepeat(actionEvent);
                        else
                            command.actionPerformed(actionEvent);
                    }
                }
            };
            action.putValue(AbstractAction.NAME, command.getName());
            action.putValue(ALT_NAME, command.getAltName());
            if (command.getDescription() != null)
                action.putValue(AbstractAction.SHORT_DESCRIPTION, command.getDescription());
            if (command.getIcon() != null)
                action.putValue(AbstractAction.SMALL_ICON, command.getIcon());
            if (command.getAcceleratorKey() != null)
                action.putValue(AbstractAction.ACCELERATOR_KEY, command.getAcceleratorKey());
            button.setAction(action);
            if (button.getIcon() != null)
                button.setText(null);
            button2Command.put(button, command);
            return button;
        }
    }

    /**
     * creates a button for the command
     *
     * @param command
     * @return button
     */
    public AbstractButton getRadioButton(final ICommand command) {
        if (command == null) {
            JButton nullButton = new JButton("Null");
            nullButton.setEnabled(false);
            return nullButton;
        }
        if (command instanceof ICheckBoxCommand) {
            final ICheckBoxCommand checkBoxCommand = (ICheckBoxCommand) command;

            final JRadioButton cbox = new JRadioButton();
            AbstractAction action = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    checkBoxCommand.setSelected(cbox.isSelected());
                    if (cbox.isEnabled()) {
                        if (command.getAutoRepeatInterval() > 0)
                            command.actionPerformedAutoRepeat(actionEvent);
                        else
                            command.actionPerformed(actionEvent);
                    }
                }
            };
            action.putValue(AbstractAction.NAME, command.getName());
            action.putValue(ALT_NAME, command.getAltName());
            if (command.getDescription() != null)
                action.putValue(AbstractAction.SHORT_DESCRIPTION, command.getDescription());
            if (command.getIcon() != null)
                action.putValue(AbstractAction.SMALL_ICON, command.getIcon());
            if (command.getAcceleratorKey() != null)
                action.putValue(AbstractAction.ACCELERATOR_KEY, command.getAcceleratorKey());
            cbox.setAction(action);
            cbox.setSelected(checkBoxCommand.isSelected());
            button2Command.put(cbox, checkBoxCommand);
            if (cbox.getIcon() != null)
                cbox.setText(null);
            return cbox;
        } else
            return null;
    }

    /**
     * get the director
     *
     * @return
     */
    public IDirector getDir() {
        return dir;
    }

    /**
     * creates an action for the command. Note that this action is not controlled by any
     * command manager, in particular its enable state is never updated.
     * Only use for state-free commands such as Quit
     *
     * @return action object
     */
    public static AbstractAction createAction(final ICommand command) {
        if (command == null) {
            AbstractAction nullAction = new AbstractAction("Null") {
                public void actionPerformed(ActionEvent actionEvent) {
                }
            };
            nullAction.setEnabled(false);
            return nullAction;
        }

        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                command.actionPerformed(actionEvent);
            }
        };
        action.putValue(AbstractAction.NAME, command.getName());
        action.putValue(ALT_NAME, command.getAltName());
        if (command.getDescription() != null)
            action.putValue(AbstractAction.SHORT_DESCRIPTION, command.getDescription());
        if (command.getIcon() != null)
            action.putValue(AbstractAction.SMALL_ICON, command.getIcon());
        if (command.getAcceleratorKey() != null)
            action.putValue(AbstractAction.ACCELERATOR_KEY, command.getAcceleratorKey());
        return action;
    }

    /**
     * should execute command return when unable to match a token?
     *
     * @return
     */
    public boolean isReturnOnCommandNotFound() {
        return returnOnCommandNotFound;
    }

    public void setReturnOnCommandNotFound(boolean returnOnCommandNotFound) {
        this.returnOnCommandNotFound = returnOnCommandNotFound;
    }

    /**
     * contains set of command names that are all silently ignored when building a menu
     *
     * @return list of commands to ignore
     */
    public static Set<String> getCommandsToIgnore() {
        return commandsToIgnore;
    }

    /**
     * gets the parent viewer for this command parser
     *
     * @return parent, either IDirectableViewer or   JDialog
     */
    public Object getParent() {
        return parent;
    }

    /**
     * gets the list of global commands. These commands are added to all command managers
     *
     * @return global commands
     */
    public static List<ICommand> getGlobalCommands() {
        return globalCommands;
    }
}
