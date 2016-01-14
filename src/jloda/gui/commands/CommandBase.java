/**
 * CommandBase.java 
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
package jloda.gui.commands;

import jloda.gui.director.IDirectableViewer;
import jloda.gui.director.IDirector;
import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;

/**
 * Base class for commands
 * Daniel Huson, 5.2010
 */
public abstract class CommandBase {
    private boolean selected = false;
    private IDirector dir;
    private IDirectableViewer theViewer;
    private Object theParent;
    private Timer autoRepeatTimer;
    private int autoRepeatInterval = 0; // if >0, will autorepeat with given number of milliseconds
    private CommandManager commandManager;

    /**
     * constructor
     */
    public CommandBase() {
    }

    /**
     * constructor
     * @param commandManager
     */
    public CommandBase(CommandManager commandManager) {
        setCommandManager(commandManager);
        setDir(commandManager.getDir());
        setParent(commandManager.getParent());
        if (commandManager.getParent() instanceof IDirectableViewer)
            setViewer((IDirectableViewer) commandManager.getParent());
    }

    /**
     * set the director
     *
     * @param dir
     */
    public void setDir(IDirector dir) {
        this.dir = dir;
    }

    /**
     * get the director
     *
     * @return dir
     */
    public IDirector getDir() {
        return dir;
    }

    /**
     * set the command manager. This is required for all commands that call the "execute" method
     *
     * @param commandManager
     */
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * get the command manager
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * get the viewer
     */
    public IDirectableViewer getViewer() {
        return theViewer;
    }

    /**
     * set the viewer
     */
    public void setViewer(IDirectableViewer viewer) {
        this.theViewer = viewer;
    }

    /**
     * sets the viewer in the case that the viewer is not an  IDirectableViewer
     *
     * @param viewer
     */
    public void setParent(Object viewer) {
        theParent = viewer;
    }

    /**
     * gets the  viewer in the case that the viewer is not an  IDirectableViewer
     *
     * @return viewer
     */
    public Object getParent() {
        return theParent;
    }

    /**
     * get an alternative name used to identify this command
     *
     * @return name
     */
    public String getAltName() {
        return null;
    }

    /**
     * parses the given command and executes it
     *
     * @param np
     * @throws java.io.IOException
     */
    abstract public void apply(NexusStreamParser np) throws Exception;

    /**
     * parses the given command and executes it
     *
     * @param command
     * @throws java.io.IOException
     */
    public void apply(String command) throws Exception {
        apply(new NexusStreamParser(new StringReader(command)));
    }

    /**
     * get command-line usage description
     *
     * @return usage
     */
    abstract public String getSyntax();

    /**
     * initial tokens used to identify the command
     *
     * @return first tokens
     */
    public String getStartsWith() {
        String syntax = getSyntax();
        if (syntax == null)
            return null;
        else {
            NexusStreamParser np = new NexusStreamParser(new StringReader(syntax));
            np.setSquareBracketsSurroundComments(false);

            String startsWith = "";
            String token;
            try {
                while (np.peekNextToken() != NexusStreamParser.TT_EOF) {
                    token = np.getWordRespectCase();
                    if (token == null || token.startsWith("[") || token.startsWith("{") || token.startsWith("<") || token.equals(";"))
                        break;
                    startsWith += " " + token;
                }
                return startsWith;
            } catch (IOException e) {
                Basic.caught(e);
                return null;
            }
        }
    }

    /**
     * execute a command in a separate thread
     *
     * @param command
     */
    public void execute(String command) {

        if (getViewer() != null) {
            dir.execute(command, commandManager, getViewer().getFrame());
        } else
            dir.execute(command, commandManager);
    }

    /**
     * execute a command in the current thread
     *
     * @param command
     */
    public void executeImmediately(String command) {
        dir.executeImmediately(command, commandManager);
    }

    /**
     * set the selected status
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * is selected?
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * action to be performed
     *
     * @param ev
     */
    abstract public void actionPerformed(ActionEvent ev);

    /**
     * get the autorepeat interval. 0 means no autorepeat
     *
     * @return autorepeat interval
     */
    public int getAutoRepeatInterval() {
        return autoRepeatInterval;
    }

    /**
     * set  the autorepeat interval. 0 means no autorepeat
     *
     * @param autoRepeatInterval
     */
    public void setAutoRepeatInterval(int autoRepeatInterval) {
        this.autoRepeatInterval = autoRepeatInterval;
    }

    /**
     * Action to be performed in case of autorepeat
     *
     * @param ev
     */
    public void actionPerformedAutoRepeat(ActionEvent ev) {
        actionPerformed(ev);
        if (getAutoRepeatInterval() > 0) {
            if (autoRepeatTimer == null) {
                autoRepeatTimer = new Timer(getAutoRepeatInterval(), new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        CommandBase.this.actionPerformed(null);
                    }
                });
                autoRepeatTimer.setRepeats(true);
                JComponent component = (JComponent) ev.getSource();
                component.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        //System.err.println("pressed");
                        if (!autoRepeatTimer.isRunning()) autoRepeatTimer.start();
                    }

                    public void mouseReleased(MouseEvent e) {
                        //System.err.println("released");
                        if (autoRepeatTimer.isRunning()) autoRepeatTimer.stop();
                    }
                });
            }
        }
    }

    /**
     * gets the command needed to undo this command
     *
     * @return undo command
     */
    public String getUndo() {
        return null;
    }
}
