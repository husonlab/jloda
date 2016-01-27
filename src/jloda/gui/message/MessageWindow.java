/**
 * MessageWindow.java 
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
package jloda.gui.message;


import jloda.gui.find.SearchManager;
import jloda.util.Alert;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

/**
 * message window
 *
 * @author huson & Franz
 *         17.2.2004
 */
public class MessageWindow {
    private final MessageWindowActions actions;
    private final JFrame frame;
    private boolean toConsoleWhenHidden = true;
    private static MessageWindow instance;
    final public static String SEARCHER_NAME = "Messages";

    public JTextArea textArea = null;

    /**
     * sets up the message window
     *
     * @param icon
     * @param title
     * @param parent
     */
    public MessageWindow(ImageIcon icon, String title, Component parent) {
        this(icon, title, parent, true);
    }

    /**
     * sets up the message window
     *
     * @param icon
     * @param title
     * @param parent
     * @param visible
     */
    public MessageWindow(ImageIcon icon, String title, Component parent, boolean visible) {
        if (getInstance() != null)
            new Alert("Internal error, multiple instances of MessageWindow");
        else
            setInstance(this);

        actions = new MessageWindowActions(this);
        MessageWindowMenuBar menuBar = new MessageWindowMenuBar(this);

        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(actions.getClear());
        popupMenu.add(actions.getCopy());

        frame = new JFrame();
        if (icon != null)
            frame.setIconImage(icon.getImage());
        frame.setJMenuBar(menuBar);
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setTitle(title);

        frame.getContentPane().add(getPanel());
        try {
            if (parent != null) {
                Point location = parent.getLocation();
                frame.setSize(new Dimension((int) Math.min(600.0, parent.getSize().getWidth()), 200));
                frame.setLocation(location.x, location.y + Math.min(600, (int) parent.getSize().getHeight()));
            }
        } catch (Exception ex) {
            frame.setLocationRelativeTo(parent);
        }

        if (visible) {
            frame.setVisible(true);
            startCapturingOutput();
        }

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                if (toConsoleWhenHidden)
                    stopCapturingOutput();
                frame.setVisible(false);
            }

            public void windowActivated(WindowEvent windowEvent) {
                startCapturingOutput();
                SearchManager searchManager = SearchManager.getInstance();
                if (searchManager != null)
                    searchManager.chooseTargetForFrame(getTextArea());
            }

            public void windowOpened(WindowEvent event) {
                startCapturingOutput();
            }
        });

        frame.addMouseListener(new MouseAdapter() {
            /**
             * Invoked when a mouse button has been pressed on a component.
             */
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    popupMenu.show(frame, e.getX(), e.getY());
            }

            /**
             * Invoked when a mouse button has been released on a component.
             */
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    popupMenu.show(frame, e.getX(), e.getY());
            }
        });

    }

    /**
     * sets the title
     *
     * @param title
     */
    public void setTitle(String title) {
        frame.setTitle(title);
    }

    /**
     * returns the actions object associated with the window
     *
     * @return actions
     */
    public MessageWindowActions getActions() {
        return actions;
    }


    /**
     * returns the frame of the window
     */
    public JFrame getFrame() {
        return frame;
    }

    private JPanel panel = null;

    /**
     * gets the content pane
     *
     * @return the content pane
     */
    private JPanel getPanel() {
        if (panel != null)
            return panel;
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        //Insets insets = new Insets(1, 5, 1, 5);
        //constraints.insets = insets;

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;

        textArea = new JTextArea();
        textArea.setFont(new Font("Courier", Font.PLAIN, 12));
        textArea.setSelectionColor(ProgramProperties.SELECTION_COLOR);

        AbstractAction action = getActions().getInput();
        action.putValue(MessageWindowActions.JTEXTAREA, textArea);
        textArea.setToolTipText((String) action.getValue(AbstractAction.SHORT_DESCRIPTION));

        JScrollPane scrollP = new JScrollPane(textArea);

        // ((JTextArea) comp).addPropertyChangeListener(action);
        // textArea.setToolTipText((String) action.getValue(AbstractAction.SHORT_DESCRIPTION));
        panel.add(scrollP, constraints);

        return panel;
    }

    final PrintStream systemOut = System.out;
    final PrintStream systemErr = System.err;
    PrintStream printStream = null;

    /**
     * start capturing all output to standard out and err
     */
    public void startCapturingOutput() {
        if (printStream == null) {
            //this is the trick: overload everything in PrintStream
            //and redirect anything sent to this to the text box
            printStream = new PrintStream(System.out) {
                public void println(String x) {
                    textArea.append(x + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void print(String x) {
                    textArea.append(x);
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void println(Object x) {
                    textArea.append(x + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void print(Object x) {
                    textArea.append("" + x);
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void println(boolean x) {
                    textArea.append(x + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void print(boolean x) {
                    textArea.append("" + x);
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void println(int x) {
                    textArea.append(x + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void print(int x) {
                    textArea.append("" + x);
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void println(float x) {
                    textArea.append(x + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void print(float x) {
                    textArea.append("" + x);
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void println(char x) {
                    textArea.append(x + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void print(char x) {
                    textArea.append("" + x);
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void println(double x) {
                    textArea.append(x + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void print(double x) {
                    textArea.append("" + x);
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void println(char[] x) {
                    textArea.append(Basic.toString(x) + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void print(char[] x) {
                    textArea.append(Basic.toString(x));
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void println(long x) {
                    textArea.append(x + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void print(long x) {
                    textArea.append("" + x);
                    textArea.setCaretPosition(textArea.getText().length());
                }

                public void write(byte[] buf, int off, int len) {
                    for (int i = 0; i < len; i++)
                        write(buf[off + i]);
                }

                public void write(byte b) {
                    print((char) b);
                }

                public void setError() {
                }

                public boolean checkError() {
                    return false;
                }

                public void flush() {
                }
            };
        }

        String collected = Basic.stopCollectingStdErr();
        if (collected.length() > 0)
            printStream.print(collected);
        System.setOut(printStream);
        System.setErr(printStream);
    }

    /**
     * stop capturing output to standard out and err
     */
    public void stopCapturingOutput() {
        System.setOut(systemOut);
        System.setErr(systemErr);
    }

    /**
     * gets the title of this viewer
     *
     * @return title
     */
    public String getTitle() {
        return frame.getTitle();
    }

    /**
     * show or hide message window
     *
     * @param visible
     */
    public void setVisible(boolean visible) {
        if (visible) {
            frame.setVisible(true);
            startCapturingOutput();
        } else {
            stopCapturingOutput();
            frame.setVisible(false);
            if (frame.getDefaultCloseOperation() == WindowConstants.EXIT_ON_CLOSE)
                System.exit(0);
        }
    }

    /**
     * is visible?
     */
    public boolean isVisible() {
        return frame.isVisible();
    }

    /**
     * gets the text area
     *
     * @return text area
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    /**
     * send all messages to console when window hidden?
     *
     * @return true, if messages should go to console, when hidden
     */
    public boolean isToConsoleWhenHidden() {
        return toConsoleWhenHidden;
    }

    /**
     * send all messages to console when window is hidden?
     *
     * @param toConsoleWhenHidden
     */
    public void setToConsoleWhenHidden(boolean toConsoleWhenHidden) {
        this.toConsoleWhenHidden = toConsoleWhenHidden;
        if (!frame.isVisible()) {
            if (toConsoleWhenHidden)
                stopCapturingOutput();
            else
                startCapturingOutput();
        }
    }

    /**
     * gets the instance of the message window, if it already exists.
     * WIll return nul, if  not yet set
     *
     * @return message window or null
     */
    public static MessageWindow getInstance() {
        return instance;
    }

    /**
     * sets the instance
     *
     * @param instance
     */
    public static void setInstance(MessageWindow instance) {
        MessageWindow.instance = instance;
    }

    /**
     * add an item to a menu
     *
     * @param action
     */
    public void addToMenu(String menuName, AbstractAction action) {
        if (action != null) {
            JMenuBar bar = frame.getJMenuBar();
            for (int i = 0; i < bar.getMenuCount(); i++) {
                if (bar.getMenu(i).getText() != null && bar.getMenu(i).getText().startsWith(menuName)) {
                    JMenu menu = bar.getMenu(i);
                    if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1).getText().length() > 0)
                        menu.addSeparator();
                    menu.add(action);
                    return;
                }
            }
        }
    }

    /**
     * add an button to a menu
     *
     * @param item
     */
    public void addToMenu(String menuName, JMenuItem item) {
        if (item != null) {
            JMenuBar bar = frame.getJMenuBar();
            for (int i = 0; i < bar.getMenuCount(); i++) {
                if (bar.getMenu(i).getText() != null && bar.getMenu(i).getText().startsWith(menuName)) {
                    JMenu menu = bar.getMenu(i);
                    if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1).getText().length() > 0)
                        menu.addSeparator();
                    menu.add(item);
                    return;
                }
            }
        }
    }
}
