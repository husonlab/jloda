/**
 * ProgressDialog.java
 * Copyright (C) 2016 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.gui;

import javafx.application.Platform;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgramProperties;
import jloda.util.ProgressListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Stack;

/**
 * A progress bar dialog that updates via the swing event queue
 *
 * @author huson
 *         Date: 02-Dec-2003
 */
public class ProgressDialog implements ProgressListener {
    static private long delayInMilliseconds = 2000;// wait two seconds before opening progress bar
    static private final int BITS = 30; // used to shift long values to int ones
    private long startTime = System.currentTimeMillis();
    private JDialog dialog;
    private boolean closed = false;
    private boolean visible = false;
    private JProgressBar progressBar;
    boolean userCancelled;
    private JLabel taskLabel = new JLabel();
    private JButton cancelButton;
    private boolean closeOnCancel = true;
    private String task;
    private String subtask;
    private boolean debug = false;

    private long maxProgess = 100;
    private long currentProgress = -1;
    private boolean shiftedDown = false;

    private StatusBar frameStatusBar = null;
    private JPanel statusBarPanel = null;

    private final Component owner;

    private boolean cancelable = true;

    /**
     * Constructs a Progress Dialog with a given task name and subtask name. The dialog is embedded into
     * the given frame. If frame = null then the dialog will appear as a separate window.
     *
     * @param taskName
     * @param subtaskName
     * @param owner
     */
    public ProgressDialog(final String taskName, final String subtaskName, final Component owner) {
        this.owner = owner;
        setup(taskName, subtaskName, delayInMilliseconds);
        checkTimeAndShow();
        if (dialog != null)
            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public ProgressDialog(final String taskName, final String subtaskName, final Component owner, final long delayInMillisec) {
        this.owner = owner;
        setup(taskName, subtaskName, delayInMillisec);
        checkTimeAndShow();
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * sets up Progress Dialog with a given task name and subtask name. The dialog is embedded into
     * the given frame. If frame = null then the dialog will appear as a separate window.
     *  @param taskName
     * @param subtaskName
     */
    private void setup(final String taskName, final String subtaskName, final long delayInMillisec) {
        run(new Runnable() {
            public void run() {
                frameStatusBar = findStatusBar(owner);

                userCancelled = false;
                delayInMilliseconds = delayInMillisec;
// the label:
                taskLabel = new JLabel();
                task = taskName;
                subtask = subtaskName;
                updateTaskLabel();

// the progress bar:
                progressBar = new JProgressBar(0, 150);
                progressBar.setValue(-1);
                progressBar.setIndeterminate(true);
                progressBar.setStringPainted(false);
                if (ProgramProperties.isMacOS()) { //On the mac - make like the standard p bar
                    Dimension d = progressBar.getPreferredSize();
                    d.height = 10;
                    progressBar.setPreferredSize(d);
                    d = progressBar.getMaximumSize();
                    d.height = 10;
                    progressBar.setMaximumSize(d);
                }

// the cancel button:
                cancelButton = new JButton();
                resetCancelButtonText();
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            setUserCancelled(true);
                            checkForCancel();
                        } catch (CanceledException e1) {
                        }
                    }
                });

                if (!isCancelable())
                    cancelButton.setEnabled(false);

                if (frameStatusBar != null) { // window appears to have a status bar that can be used for the progress bar
                    statusBarPanel = new JPanel();
                    statusBarPanel.setLayout(new BorderLayout());

                    progressBar.setPreferredSize(new Dimension(300, 10));
                    statusBarPanel.add(progressBar, BorderLayout.CENTER);

                    cancelButton.setPreferredSize(new Dimension(60, 14));
                    cancelButton.setMinimumSize(new Dimension(60, 14));
                    cancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
                    cancelButton.setBorder(BorderFactory.createEtchedBorder());
                    statusBarPanel.add(cancelButton, BorderLayout.EAST);
                } else { // no status bar for a program bar, show a window
                    final JFrame parent = (owner instanceof JFrame ? (JFrame) owner : null);
                    dialog = new JDialog(parent, "Progress...");
                    dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                    if (!ProgramProperties.isMacOS()) { // none mac progress dialog:
                        final GridBagLayout gridBag = new GridBagLayout();
                        final JPanel pane = new JPanel(gridBag);
                        pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                        GridBagConstraints c = new GridBagConstraints();

                        c.anchor = GridBagConstraints.CENTER;
                        c.fill = GridBagConstraints.HORIZONTAL;
                        c.weightx = 3;
                        c.weighty = 1;
                        c.gridx = 1;
                        c.gridy = 0;
                        c.gridwidth = 3;
                        c.gridheight = 1;
                        pane.add(taskLabel, c);

                        c.anchor = GridBagConstraints.CENTER;
                        c.fill = GridBagConstraints.NONE;
                        c.weightx = 1;
                        c.weighty = 5;
                        c.gridx = 1;
                        c.gridy = 1;
                        c.gridwidth = 3;
                        c.gridheight = 1;
                        pane.add(progressBar, c);

                        c.anchor = GridBagConstraints.CENTER;
                        c.weightx = 1;
                        c.weighty = 1;
                        c.gridx = 1;
                        c.gridy = 2;
                        c.gridwidth = 1;
                        c.gridheight = 1;
                        pane.add(cancelButton, c);

                        dialog.getContentPane().add(pane);
                        dialog.setSize(new Dimension(550, 120));
                    } else {  // mac os progress dialog:
                        final JPanel contentPane = new JPanel(new BorderLayout());
                        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//Progress Bar and cancel button.
                        JPanel barpane = new JPanel();
                        barpane.setLayout(new BoxLayout(barpane, BoxLayout.LINE_AXIS));
                        barpane.add(progressBar);

                        barpane.add(cancelButton);

                        JPanel taskPanel = new JPanel();
                        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.PAGE_AXIS));
                        taskPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
                        taskPanel.add(taskLabel);
                        taskPanel.add(Box.createHorizontalGlue());

                        //Put everything into the content pane
                        contentPane.add(barpane, BorderLayout.PAGE_START);
                        contentPane.add(taskPanel, BorderLayout.LINE_START);
                        dialog.setContentPane(contentPane);
                        dialog.setSize(new Dimension(550, 120));
                    }

                    if (dialog.getParent() != null) {
                        int x = dialog.getParent().getX();
                        int y = dialog.getParent().getY();
                        int dx = dialog.getParent().getWidth() - dialog.getWidth();
                        int dy = dialog.getParent().getHeight() - dialog.getHeight();
                        x += dx / 2;
                        y += dy / 2;

                        dialog.setLocation(x, y);
                    }
                    //dialog.setVisible(true);  //open once delay has passed
                }
            }
        });
    }

    /**
     * determine whether given component contains a statusbar
     *
     * @param component
     * @return statusbar or null
     */
    private static StatusBar findStatusBar(Component component) {
        if (component instanceof Container) {
            Container frame = (Container) component;
            final Stack<Component> stack = new Stack<>();
            stack.addAll(Arrays.asList(frame.getComponents()));
            while (stack.size() > 0) {
                Component c = stack.pop();
                if (c instanceof StatusBar)
                    return (StatusBar) c;
                else if (c instanceof Container)
                    stack.addAll(Arrays.asList(((Container) c).getComponents()));
            }
        }
        return null;
    }


    /**
     * sets the steps number of steps to be done. This can be done in the event dispatch thread
     *
     * @param steps
     */
    public void setMaximum(final long steps) {
        startTime = System.currentTimeMillis();

        shiftedDown = (steps > (1 << BITS));

        maxProgess = steps;
        checkTimeAndShow();

        if (progressBar != null && maxProgess != progressBar.getMaximum()) {
            run(new Runnable() {
                public void run() {
                    progressBar.setMaximum((int) (shiftedDown ? steps >>> BITS : steps));
                }
            });
        }
    }

    /**
     * sets the progress. If a negative value is given, sets the progress bar to indeterminate mode
     *
     * @param steps
     */
    public void setProgress(final long steps) throws CanceledException {
        if (steps != currentProgress) {
            currentProgress = steps;
            checkForCancel();

            if (progressBar != null && currentProgress != progressBar.getValue()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (currentProgress < 0) {
                            progressBar.setIndeterminate(true);
                            progressBar.setString(null);
                        } else {
                            progressBar.setIndeterminate(false);
                            progressBar.setValue((int) (shiftedDown ? steps >>> BITS : steps));
                        }
                    }
                });
            }
        }
    }

    /**
     * gets the current progress
     *
     * @return progress
     */
    public long getProgress() {
        return currentProgress;
    }

    /**
     * increment the progress
     *
     * @throws CanceledException
     */
    public void incrementProgress() throws CanceledException {
        if (currentProgress == -1)
            currentProgress = 1;
        else
            currentProgress++;
        checkForCancel();

        if (progressBar != null && currentProgress != progressBar.getValue()) {
            run(new Runnable() {
                public void run() {
                    progressBar.setValue((int) (shiftedDown ? currentProgress >>> BITS : currentProgress));
                }
            });
        }
    }

    /**
     * closes the dialog.
     */
    public void close() {
        run(new Runnable() {
            public void run() {
                if (!closed) {
                    if (statusBarPanel != null) {
                        frameStatusBar.setExternalPanel1(null, false);
                        frameStatusBar.setComponent2(statusBarPanel, false);
                        statusBarPanel = null;
                    }
                    if (dialog != null) {
                        dialog.setVisible(false);
                        dialog.dispose();
                        dialog = null;
                    }
                    closed = true;
                    visible = false;
                }
            }
        });
    }

    /**
     * has user canceled?
     *
     * @throws CanceledException
     */
    public void checkForCancel() throws CanceledException {
        checkTimeAndShow();

        if (this.userCancelled) {
            //dialog.setVisible(false);
            if (closeOnCancel)
                close();

            throw new CanceledException();
        }
    }

    /**
     * sets the subtask name
     *
     * @param subtaskName
     * @throws CanceledException
     */
    public void setSubtask(String subtaskName) {
        checkTimeAndShow();

        if ((subtaskName == null && subtask != null) || (subtaskName != null && (subtask == null || !subtask.equals(subtaskName)))) {
            subtask = subtaskName;

            run(new Runnable() {
                public void run() {
                    updateTaskLabel();
                }
            });
        }
    }


    /**
     * Sets the task name (first description, printed in bold)  and subtask
     *
     * @param taskName
     * @param subtaskName
     * @throws CanceledException
     */
    public void setTasks(String taskName, String subtaskName) {
        checkTimeAndShow();

        if ((taskName == null && task != null) || (taskName != null && (task == null || !task.equals(taskName)))
                || (subtaskName == null && subtask != null) || (subtaskName != null && (subtask == null || !subtask.equals(subtaskName)))) {
            task = taskName;
            subtask = subtaskName;
            run(new Runnable() {
                public void run() {
                    updateTaskLabel();
                }
            });
        }
    }

    private void updateTaskLabel() {
        String label = "<html><p style=\"font-size:" + (statusBarPanel != null ? "10pt" : "12pt") + ";\">";
        if (this.task != null)
            label += "<b>" + this.task + "</b>";
        if (this.task != null && this.subtask != null)
            label += ": ";
        if (this.subtask != null)
            label += this.subtask;
        label += "</font></p>";
        if (statusBarPanel != null) {
            frameStatusBar.setExternalPanel1(new JLabel(label), true);
            statusBarPanel.setToolTipText(label);
        } else
            taskLabel.setText(label);
    }

    public boolean isUserCancelled() {
        return userCancelled;
    }

    public void setUserCancelled(boolean userCancelled) {
        this.userCancelled = userCancelled;
    }

    private void checkTimeAndShow() {
        try {
            if (!closed && !visible && System.currentTimeMillis() - startTime > delayInMilliseconds) {
                show();
            }
        } catch (Exception ex) {
        }
    }

    /**
     * show the progress bar
     */
    public void show() {
        if (!visible) {
            run(new Runnable() {
                    public void run() {
                        if (owner != null && owner instanceof Window) {
                            // ((Window) owner).toFront(); // this causes weird effects
                        }
                        if (progressBar != null) {
                            updateTaskLabel();
                            progressBar.setMaximum((int) (shiftedDown ? maxProgess >>> BITS : maxProgess));
                            if (currentProgress < 0) {
                                progressBar.setIndeterminate(true);
                                progressBar.setString(null);
                            } else {
                                progressBar.setIndeterminate(false);
                                progressBar.setValue((int) (shiftedDown ? currentProgress >>> BITS : currentProgress));
                            }
                        }
                        if (statusBarPanel != null) {
                            frameStatusBar.setComponent2(statusBarPanel, !closed);
                        } else if (dialog != null) {
                            dialog.setVisible(true);
                        }
                        visible = true;
                    }
            });
        }
    }

    /**
     * run a task either directly, if in swing thread, or later, if FX thread, or invoke or wait, otherwise
     *
     * @param runnable
     */
    private static void run(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread())
            runnable.run();
        else if (true || Platform.isFxApplicationThread())
            SwingUtilities.invokeLater(runnable);
        else // todo: this may lead to FX vs Swing deadlock. But not using this cases Inspector window to appear below current window
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (Exception e) {
                Basic.caught(e);
            }
    }

    public static long getDelayInMilliseconds() {
        return delayInMilliseconds;
    }

    public static void setDelayInMilliseconds(long delayInMilliseconds) {
        ProgressDialog.delayInMilliseconds = delayInMilliseconds;
    }

    /**
     * in debug mode, report tasks and subtasks to stderr, too
     *
     * @return verbose mode
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * in debug mode, report tasks and subtasks to stderr, too
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * is user allowed to cancel?
     *
     * @param cancelable
     */
    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        if (cancelButton != null)
            cancelButton.setEnabled(cancelable);
    }

    /**
     * is user allowed to cancel
     *
     * @return cancelable?
     */
    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelButtonText(String text) {
        cancelButton.setText(text);
    }

    public void resetCancelButtonText() {
        if (ProgramProperties.isMacOS())
            cancelButton.setText("Stop");
        else
            cancelButton.setText("Cancel");
    }

    public boolean isCloseOnCancel() {
        return closeOnCancel;
    }

    public void setCloseOnCancel(boolean closeOnCancel) {
        this.closeOnCancel = closeOnCancel;
    }
}
