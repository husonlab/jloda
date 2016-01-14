/**
 * TextWindow.java 
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
package jloda.util;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 * a simple text window
 *
 * @author markus franz and daniel huson
 *         Date: 23-Mar-2004
 */
public class TextWindow extends JFrame {
    protected final JTextArea textArea = new JTextArea();
    protected final JScrollPane sp = new JScrollPane(textArea);
    protected boolean showing = true;
    AbstractAction saveAction;
    File lastSaveFile;
    AbstractAction closeAction;
    AbstractAction quitAction;
    AbstractAction fontSizeAction;
    private AbstractAction clear; // erase the document
    // need an instance to get default textComponent Actions
    private DefaultEditorKit kit;
    private Action cut;
    private Action copy;
    private Action paste;
    private Action selectAll;
    // private static boolean macOS = System.getProperty("mrj.version") != null;

    /**
     * constructor
     *
     * @param name name of window
     */
    public TextWindow(String name) {
        this(name, true);
    }

    /**
     * constructor
     *
     * @param name name of window
     */
    public TextWindow(String name, boolean withMenubar) {
        super(name);
        setSize(600, 210);
        textArea.setFont(new Font("Courier", Font.PLAIN, 12));


        getContentPane().add(sp);
        if (withMenubar)
            addMenus();
    }

    /**
     * gets the text area
     *
     * @return the text area
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    protected void addMenus() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic('F');
        menu.add(new JMenuItem(getSaveAction()));
        menu.addSeparator();
        menu.add(new JMenuItem(getCloseAction()));
        menu.addSeparator();
        menu.add(new JMenuItem(getQuitAction()));
        menuBar.add(menu);

        menu = new JMenu("Edit");
        menu.setMnemonic('E');
        menu.addSeparator();
        JMenuItem item = new JMenuItem(getCutAction());
        item.setText("Cut");
        menu.add(item);
        item = new JMenuItem(getCopyAction());
        item.setText("Copy");
        menu.add(item);
        item = new JMenuItem(getPasteAction());
        item.setText("Paste");
        menu.add(item);
        menu.addSeparator();
        menu.add(new JMenuItem(getClearAction()));
        menu.addSeparator();
        item = new JMenuItem(getSelectAllAction());
        item.setText("Select All");
        menu.add(item);
        menu.addSeparator();

        menu.add(new JMenuItem(getFontSizeAction()));
        menuBar.add(menu);

        setJMenuBar(menuBar);
    }

    public AbstractAction getSaveAction() {
        AbstractAction action = saveAction;
        if (action != null)
            return action;
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser chooser = new JFileChooser(lastSaveFile);
                if (chooser.showSaveDialog(null)
                        == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();

                    if (file.exists() &&
                            JOptionPane.showConfirmDialog(null,
                                    "This file already exists. " +
                                            "Would you like to overwrite the existing file?",
                                    "Save File",
                                    JOptionPane.YES_NO_OPTION) == 1)
                        return; // overwrite canceled

                    try {
                        Writer w = new FileWriter(file);
                        String text = textArea.getText();
                        w.write(text);
                        w.close();
                    } catch (Exception ex) {
                        System.err.println("Save failed: " + ex);
                    }
                    lastSaveFile = file;
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Save");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Save messages");
        action.putValue(AbstractAction.MNEMONIC_KEY, new Integer('S'));
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK));

        return saveAction = action;
    }

    public AbstractAction getCloseAction() {
        AbstractAction action = closeAction;
        if (action != null)
            return action;

        final TextWindow me = this;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                me.setVisible(false);
            }
        };
        action.putValue(AbstractAction.NAME, "Close");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Close messages");
        action.putValue(AbstractAction.MNEMONIC_KEY, new Integer('C'));
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('W', InputEvent.CTRL_MASK));
        return closeAction = action;
    }

    public AbstractAction getQuitAction() {
        AbstractAction action = quitAction;
        if (action != null)
            return action;
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        };
        action.putValue(AbstractAction.NAME, "Quit");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Quit");
        action.putValue(AbstractAction.MNEMONIC_KEY, new Integer('Q'));
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('Q', InputEvent.CTRL_MASK));

        return quitAction = action;
    }

    public AbstractAction getFontSizeAction() {
        AbstractAction action = fontSizeAction;
        if (action != null)
            return action;
        final TextWindow me = this;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                Object[] possibleValues = {"6", "7", "8", "9", "10", "12", "14", "16", "24", "32"};
                String def = "" + me.getFont().getSize();
                Object selectedValue = JOptionPane.showInputDialog(null,
                        "Font Size...", "Input",
                        JOptionPane.INFORMATION_MESSAGE, null,
                        possibleValues, def);
                if (selectedValue != null && !selectedValue.equals(def)) {
                    textArea.setFont(Font.decode("Monospaced-NORMAL-" + selectedValue));
                    textArea.repaint();
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Font Size");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Choose Font Size");
        action.putValue(AbstractAction.MNEMONIC_KEY, new Integer('F'));
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('F', InputEvent.CTRL_MASK));

        return fontSizeAction = action;
    }

    public AbstractAction getClearAction() {
        AbstractAction action = clear;
        if (action != null) return action;

        // Clear the document
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                textArea.setText("");
            }
        };
        action.putValue(AbstractAction.NAME, "Clear");
        // erase.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("quit"));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Clear document");
        return clear = action;
    }

    public Action getCutAction() {
        Action action = cut;
        if (action != null) return action;

        if (kit == null) kit = new DefaultEditorKit();

        Action[] defActions = kit.getActions();
        for (Action defAction : defActions) {
            if (defAction.getValue(Action.NAME) == DefaultEditorKit.cutAction) {
                action = defAction;
            }
        }

        if (action != null) {
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            action.putValue(Action.SHORT_DESCRIPTION, "Cut");
        }
        return cut = action;
    }

    public Action getCopyAction() {
        Action action = copy;
        if (action != null) return action;

        if (kit == null) kit = new DefaultEditorKit();

        Action[] defActions = kit.getActions();
        for (Action defAction : defActions) {

            if ((defAction.getValue(Action.NAME)).equals(DefaultEditorKit.copyAction)) {
                action = defAction;
            }
        }

        if (action != null) {
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            action.putValue(Action.SHORT_DESCRIPTION, "Copy");
        }

        return copy = action;
    }

    public Action getPasteAction() {
        Action action = paste;
        if (action != null) return action;

        if (kit == null) kit = new DefaultEditorKit();

        Action[] defActions = kit.getActions();
        for (Action defAction : defActions) {
            if (defAction.getValue(Action.NAME) == DefaultEditorKit.pasteAction) {
                action = defAction;
            }
        }

        if (action != null) {
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            action.putValue(Action.SHORT_DESCRIPTION, "Paste");
        }

        return paste = action;
    }

    public Action getSelectAllAction() {
        Action action = selectAll;
        if (action != null) return action;

        if (kit == null) kit = new DefaultEditorKit();

        Action[] defActions = kit.getActions();
        for (Action defAction : defActions) {
            if (defAction.getValue(Action.NAME) == DefaultEditorKit.selectAllAction) {
                action = defAction;
            }
        }

        if (action != null) {
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            action.putValue(Action.SHORT_DESCRIPTION, "Select All");
        }
        return selectAll = action;
    }
}
