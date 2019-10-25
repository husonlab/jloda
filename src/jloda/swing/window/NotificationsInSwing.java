/*
 *  NotificationsInSwing.java Copyright (C) 2019 Daniel H. Huson
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

package jloda.swing.window;

import jloda.fx.util.ProgramExecutorService;
import jloda.swing.util.ResourceManager;
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements notifications in Swing,
 * Daniel Huson, 8.2019
 */
public class NotificationsInSwing {


    public enum Mode {warning, information, confirmation, error}

    private final static ArrayList<JFrame> activeNotificationSlots = new ArrayList<>();
    private final static Map<JFrame, Integer> frame2slot = new HashMap<>();

    private final static int notificationHeight = 60;

    private static boolean echoToConsole = true;

    private static int maxLength = 150;

    private static String title;

    private static boolean showNotifications = ProgramProperties.get("ShowNotifications", true);

    /**
     * show an information notification
     *
     * @param message
     */
    public static void showInformation(String message) {
        showNotification(title, message, Mode.information, 10000);
    }

    /**
     * show an information notification
     *
     * @param message
     */
    public static void showInformation(String message, long milliseconds) {
        showNotification(title, message, Mode.information, milliseconds);
    }

    /**
     * show an information notification
     *
     * @param message
     */
    public static void showInformation(Object parentIgnored, String message) {
        showNotification(title, message, Mode.information, 10000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showError(String message) {
        showNotification(title, message, Mode.error, 60000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showError(Object parentIgnored, String message) {
        showNotification(title, message, Mode.error, 60000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showInternalError(String message) {
        showNotification(title, "Internal error: " + message, Mode.error, 60000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showInternalError(Object parentIgnored, String message) {
        showNotification(title, "Internal error: " + message, Mode.error, 60000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showError(String message, long milliseconds) {
        showNotification(title, message, Mode.error, milliseconds);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showError(Object parentIgnored, String message, long milliseconds) {
        showNotification(title, message, Mode.error, milliseconds);
    }

    /**
     * show a warning notification
     *
     * @param message
     */
    public static void showWarning(String message) {
        showWarning(null, message);
    }

    /**
     * show a warning notification
     *
     * @param message
     */
    public static void showWarning(Object parentIgnored, String message) {
        showWarning(parentIgnored, message, 60000);
    }

    /**
     * show a warning notification
     *
     * @param message
     */
    public static void showWarning(Object parentIgnored, String message, long milliseconds) {
        showNotification(title, message, Mode.warning, milliseconds);
    }

    /**
     * show a notification
     *  @param title
     * @param message0
     * @param mode
     * @param milliseconds
     */
    public static void showNotification(String title, final String message0, final Mode mode, final long milliseconds) {
        final String message = (message0.length() > maxLength + 3 ? (message0.substring(0, maxLength) + "...") : message0);

        if (isShowNotifications() && ProgramProperties.isUseGUI()) {
            final Window activeWindow= getActiveWindow();
            if (title == null || title.length() == 0) {
                title = ProgramProperties.getProgramName();
            }
            {
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                title += " at " + simpleDateFormat.format(System.currentTimeMillis());
            }

            final JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.setOpacity(0.8f);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setBackground(new Color(0f, 0f, 0f, 1f / 3f));
            frame.setAlwaysOnTop(true);
            final JPanel mainPanel = new JPanel();
            final JLabel label = new JLabel("  " + message + "  ");
            label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 12));
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(label, BorderLayout.CENTER);

            ImageIcon icon;
            switch (mode) {
                case confirmation:
                    icon = ResourceManager.getIcon("dialog/dialog-confirmation.png");
                    break;
                case warning:
                    icon = ResourceManager.getIcon("dialog/dialog-warning.png");
                    break;
                default:
                case information:
                    icon = ResourceManager.getIcon("dialog/dialog-information.png");
                    break;
                case error:
                    icon = ResourceManager.getIcon("dialog/dialog-error.png");
                    break;
            }
            if (icon != null)
                icon = new ImageIcon(icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));

            final JPanel topPanel = new JPanel();
            topPanel.setLayout(new BorderLayout());
            final JLabel titleLabel = new JLabel(" " + title);
            titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 10));
            topPanel.add(titleLabel, BorderLayout.CENTER);
            final JButton close = new JButton(new AbstractAction("X") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.setVisible(false);
                }
            });
            close.setMaximumSize(new Dimension(12, 12));
            close.setPreferredSize(new Dimension(12, 12));
            close.setMinimumSize(new Dimension(12, 12));

            mainPanel.add(new JLabel(icon), BorderLayout.WEST);

            topPanel.add(close, BorderLayout.EAST);
            mainPanel.add(topPanel, BorderLayout.NORTH);
            mainPanel.add(Box.createVerticalStrut(notificationHeight), BorderLayout.EAST);
            mainPanel.add(Box.createHorizontalStrut(2 * notificationHeight), BorderLayout.SOUTH);

            frame.add(mainPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            frame.setLocation(5, (int) (screenSize.getHeight() - frame.getHeight() - 5));

            updateSlots();
            if (activeNotificationSlots.size() == 0)
                activeNotificationSlots.add(frame);
            else
                activeNotificationSlots.set(0, frame);

            ProgramExecutorService.getInstance().submit(() -> {
                try {
                    Thread.sleep(milliseconds);
                } catch (InterruptedException e) {
                } finally {
                    frame.setVisible(false);
                    activeNotificationSlots.set(frame2slot.get(frame), null);
                    frame2slot.remove(frame);
                }
            });
            frame.setVisible(true);
            if(activeWindow!=null)
                SwingUtilities.invokeLater(activeWindow::toFront);
        }

        if (!isShowNotifications() || isEchoToConsole()) {
            switch (mode) {
                default:
                case information: {
                    System.err.print("Info: ");
                    break;
                }
                case error: {
                    System.err.print("Error: ");
                    break;
                }
                case warning: {
                    System.err.print("Warning: ");
                    break;
                }
                case confirmation: {
                    System.err.print("Confirmed: ");
                    break;
                }
            }
            System.err.println(message);
        }
    }

    private static int findFirstEmptySlot() {
        for (int i = 0; i < activeNotificationSlots.size(); i++) {
            if (activeNotificationSlots.get(i) == null)
                return i;
        }
        return activeNotificationSlots.size();
    }

    private static void updateSlots() {
        int top = findFirstEmptySlot();
        for (int i = top; i > 0; i--) {
            final JFrame frame = activeNotificationSlots.get(i - 1);
            if (frame != null) {
                frame.setLocation((int) frame.getLocation().getX(), (int) frame.getLocation().getY() - notificationHeight - 25);
                if (i < activeNotificationSlots.size())
                    activeNotificationSlots.set(i, frame);
                else
                    activeNotificationSlots.add(frame);
                frame2slot.put(frame, i);
            }
        }
    }

    public static boolean isShowNotifications() {
        return showNotifications;
    }

    public static void setShowNotifications(boolean showNotifications) {
        NotificationsInSwing.showNotifications = showNotifications;
        ProgramProperties.put("ShowNotifications", showNotifications);
    }

    public static int getMaxLength() {
        return maxLength;
    }

    public static void setMaxLength(int maxLength) {
        NotificationsInSwing.maxLength = maxLength;
    }

    public static boolean isEchoToConsole() {
        return echoToConsole;
    }

    public static void setEchoToConsole(boolean echoToConsole) {
        NotificationsInSwing.echoToConsole = echoToConsole;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        NotificationsInSwing.title = title;
    }

    public static Window getActiveWindow() {
        Window windows[] = Window.getWindows();
        for (Window w:windows) {
            if (w.isActive()) {
                return w;
            }
        }
        return null;
    }
}
