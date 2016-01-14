/**
 * MessageWindowActions.java 
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

import jloda.util.ResourceManager;
import jloda.util.TextPrinter;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

/**
 * actions associated with a message window
 *
 * @author huson
 *         Date: 17.2.2004
 */
public class MessageWindowActions {
    private final MessageWindow viewer;
    private final List all = new LinkedList();
    public static final String JCHECKBOX = "JCHECKBOX";
    public static final String JTEXTAREA = "JTEXTAREA";
    public static final String CRITICAL = "CRITICAL";

    public MessageWindowActions(MessageWindow viewer) {
        this.viewer = viewer;
    }

    /**
     * enable or disable critical actions
     *
     * @param flag show or hide?
     */
    public void setEnableCritical(boolean flag) {
        // because we don't want to duplicate that code
    }

    /**
     * This is where we update the enable state of all actions!
     */
    public void updateEnableState() {
    }

    /**
     * returns all actions
     *
     * @return actions
     */
    public List getAll() {
        return all;
    }

    // here we define the algorithms window specific actions:

    private AbstractAction printIt;

    /**
     * print action
     *
     * @return print action
     */
    public AbstractAction getPrintIt() {
        AbstractAction action = printIt;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {

                PrinterJob job = PrinterJob.getPrinterJob();

                TextPrinter printer = new TextPrinter(viewer.textArea.getText(), viewer.textArea.getFont());
                job.setPrintable(printer);
                // Put up the dialog box
                if (job.printDialog()) {
                    // Print the job if the user didn't cancel printing
                    try {
                        job.print();
                    } catch (Exception ex) {
                        System.err.println("Print failed: " + ex);
                    }
                }

            }
        };
        action.putValue(AbstractAction.NAME, "Print...");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Print the content");
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Print16.gif"));

        all.add(action);
        return printIt = action;
    }

    private AbstractAction close;

    /**
     * close this viewer
     *
     * @return close action
     */
    public AbstractAction getClose() {
        AbstractAction action = close;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                viewer.setVisible(false);
            }
        };
        action.putValue(AbstractAction.NAME, "Close");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        action.putValue(AbstractAction.MNEMONIC_KEY, new Integer('C'));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Close this viewer");
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("Close16.gif"));
        // close is critical because we can't easily kill the worker thread

        all.add(action);
        return close = action;
    }

    static File lastSaveFile = new File(System.getProperty("user.dir"));
    private AbstractAction saveFile;

    public AbstractAction getSaveFile() {
        AbstractAction action = saveFile;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser chooser = new JFileChooser(lastSaveFile);
                if (chooser.showSaveDialog(viewer.getFrame())
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
                        String text = viewer.textArea.getText();
                        w.write(text);
                        w.close();
                    } catch (Exception ex) {
                        System.err.println("Save failed: " + ex);
                    }
                    lastSaveFile = file;
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Save As...");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('S',
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Save16.gif"));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Save as text file");

        all.add(action);
        return saveFile = action;
    }

    AbstractAction input;

    AbstractAction getInput() {
        if (input != null)
            return input;
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
            }
        };
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Messages");
        all.add(action);
        return input = action;
    }

    AbstractAction clear;

    public AbstractAction getClear() {
        if (clear != null)
            return clear;
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                viewer.textArea.setText("");
            }
        };
        action.putValue(AbstractAction.NAME, "Clear");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Clear the messages");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        all.add(action);
        return clear = action;
    }

    ////// basic textComponent Actions
    private AbstractAction undo;

    /**
     * undo action
     */
    public AbstractAction getUndo(final UndoManager undoManager) {
        AbstractAction action = undo;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    undoManager.undo();
                } catch (CannotUndoException ex) {
                }
                updateUndoRedo(undoManager);
            }
        };
        action.setEnabled(false);

        action.putValue(AbstractAction.NAME, "Undo");
        action.putValue(AbstractAction.MNEMONIC_KEY, new Integer('U'));
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        // quit.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("quit"));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Undo");

        action.putValue(CRITICAL, Boolean.TRUE);

        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Undo16.gif"));

        //all.add(action);
        return undo = action;
    }//End of getUndo

    /**
     * updates the undo action
     */
    public void updateUndoRedo(UndoManager undoManager) {
        if (undoManager.canUndo()) {
            undo.setEnabled(true);
        } else {
            undo.setEnabled(false);
        }
        if (undoManager.canRedo()) {
            redo.setEnabled(true);
        } else {
            redo.setEnabled(false);
        }
    }

    private AbstractAction redo;

    /**
     * redo action
     */
    public AbstractAction getRedo(final UndoManager undoManager) {
        AbstractAction action = redo;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    undoManager.redo();
                } catch (CannotRedoException ex) {
                }
                updateUndoRedo(undoManager);
            }
        };
        action.setEnabled(false);

        action.putValue(AbstractAction.NAME, "Redo");
        action.putValue(AbstractAction.MNEMONIC_KEY, new Integer('R'));
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | java.awt.event.InputEvent.SHIFT_MASK));
        // quit.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("quit"));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Redo");
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Redo16.gif"));

        action.putValue(CRITICAL, Boolean.TRUE);
        //all.add(action);
        return redo = action;

    }//End of getRedo

    // need an instance to get default textComponent Actions
    private DefaultEditorKit kit;


    private Action cut;

    public Action getCut() {
        Action action = cut;
        if (action != null) return action;

        if (kit == null) kit = new DefaultEditorKit();

        Action[] defActions = kit.getActions();
        for (Action defAction : defActions) {
            if (defAction.getValue(Action.NAME) == DefaultEditorKit.cutAction) {
                action = defAction;
            }
        }
        action.putValue(AbstractAction.MNEMONIC_KEY, (int) 'T');
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        action.putValue(Action.SHORT_DESCRIPTION, "Cut");

        action.putValue(CRITICAL, Boolean.TRUE);

        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Cut16.gif"));

        all.add(action);
        return cut = action;
    }

    private Action copy;

    public Action getCopy() {
        Action action = copy;
        if (action != null) return action;

        if (kit == null) kit = new DefaultEditorKit();

        Action[] defActions = kit.getActions();
        for (Action defAction : defActions) {

            if ((defAction.getValue(Action.NAME)).equals(DefaultEditorKit.copyAction)) {
                action = defAction;
            }
        }
        action.putValue(AbstractAction.MNEMONIC_KEY, (int) 'C');
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        action.putValue(Action.SHORT_DESCRIPTION, "Copy");
        action.putValue(CRITICAL, Boolean.TRUE);

        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Copy16.gif"));

        all.add(action);
        return copy = action;
    }

    private Action paste;

    public Action getPaste() {
        Action action = paste;
        if (action != null) return action;

        if (kit == null) kit = new DefaultEditorKit();

        Action[] defActions = kit.getActions();
        for (Action defAction : defActions) {
            if (defAction.getValue(Action.NAME) == DefaultEditorKit.pasteAction) {
                action = defAction;
            }
        }
        action.putValue(AbstractAction.MNEMONIC_KEY, (int) 'P');
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        action.putValue(Action.SHORT_DESCRIPTION, "Paste");
        action.putValue(CRITICAL, Boolean.TRUE);

        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Paste16.gif"));

        all.add(action);
        return paste = action;
    }

    private Action selectAll;

    public Action getSelectAll() {
        Action action = selectAll;
        if (action != null) return action;

        if (kit == null) kit = new DefaultEditorKit();

        Action[] defActions = kit.getActions();
        for (Action defAction : defActions) {
            if (defAction.getValue(Action.NAME) == DefaultEditorKit.selectAllAction) {
                action = defAction;
            }
        }
        action.putValue(AbstractAction.MNEMONIC_KEY, (int) 'A');
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        action.putValue(Action.SHORT_DESCRIPTION, "Select All");

        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return selectAll = action;
    }

}
