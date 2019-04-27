/*
 * NotificationManager.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import jloda.swing.util.ProgramProperties;
import jloda.swing.util.ResourceManager;
import org.controlsfx.control.Notifications;

import java.net.URL;

public class NotificationManager {
    public enum Mode {warning, information, confirmation, error}

    private static boolean isFXInitialized = false;

    private static String title = null;
    private static boolean useDarkStyle = true;
    private static Pos position = Pos.TOP_RIGHT;

    private static boolean echoToConsole = true;

    private static int maxLength = 150;

    private static boolean showNotifications = ProgramProperties.get("ShowNotifications", true);


    /**
     * show an information notation
     *
     * @param message
     */
    public static void showInformation(String message) {
        showNotification(title, message, Mode.information, Pos.BOTTOM_LEFT, 10000);
    }

    /**
     * show an information notation
     *
     * @param message
     */
    public static void showInformation(String message, long milliseconds) {
        showNotification(title, message, Mode.information, Pos.BOTTOM_LEFT, milliseconds);
    }

    /**
     * show an information notation
     *
     * @param message
     */
    public static void showInformation(Object parentIgnored, String message) {
        showNotification(title, message, Mode.information, Pos.BOTTOM_LEFT, 10000);
    }

    /**
     * show an error notation
     *
     * @param message
     */
    public static void showError(String message) {
        showNotification(title, message, Mode.error, Pos.BOTTOM_LEFT, 60000);
    }

    /**
     * show an error notation
     *
     * @param message
     */
    public static void showError(Object parentIgnored, String message) {
        showNotification(title, message, Mode.error, Pos.BOTTOM_LEFT, 60000);
    }

    /**
     * show an error notation
     *
     * @param message
     */
    public static void showInternalError(String message) {
        showNotification(title, "Internal error: " + message, Mode.error, Pos.BOTTOM_LEFT, 60000);
    }

    /**
     * show an error notation
     *
     * @param message
     */
    public static void showInternalError(Object parentIgnored, String message) {
        showNotification(title, "Internal error: " + message, Mode.error, Pos.BOTTOM_LEFT, 60000);
    }

    /**
     * show an error notation
     *
     * @param message
     */
    public static void showError(String message, long milliseconds) {
        showNotification(title, message, Mode.error, Pos.BOTTOM_LEFT, milliseconds);
    }

    /**
     * show an error notation
     *
     * @param message
     */
    public static void showError(Object parentIgnored, String message, long milliseconds) {
        showNotification(title, message, Mode.error, Pos.BOTTOM_LEFT, milliseconds);
    }

    /**
     * show a warning notation
     *
     * @param message
     */
    public static void showWarning(String message) {
        showWarning(null, message);
    }

    /**
     * show a warning notation
     *
     * @param message
     */
    public static void showWarning(Object parentIgnored, String message) {
        showWarning(parentIgnored, message, 60000);
    }

    /**
     * show a warning notation
     *
     * @param message
     */
    public static void showWarning(Object parentIgnored, String message, long milliseconds) {
        showNotification(title, message, Mode.warning, Pos.BOTTOM_LEFT, milliseconds);
    }

    /**
     * show a notification
     *
     * @param title
     * @param message0
     * @param mode
     * @param position
     * @param milliseconds
     */
    public static void showNotification(final String title, final String message0, final Mode mode, final Pos position, final long milliseconds) {
        final String message = (message0.length() > maxLength + 3 ? (message0.substring(0, maxLength) + "...") : message0);

        if (isShowNotifications() && ProgramProperties.isUseGUI()) {
            Platform.runLater(new Runnable() {
                public void run() {
                    final Notifications notification = Notifications.create();
                    if (isUseDarkStyle())
                        notification.darkStyle();
                    notification.title(title).text(message).hideAfter(new Duration(milliseconds)).position(position);

                    final ImageView imageView;
                    switch (mode) {
                        default:
                        case information: {
                            imageView = new ImageView(Notifications.class.getResource("/org/controlsfx/dialog/dialog-information.png").toExternalForm());
                            break;
                        }
                        case error: {
                            imageView = new ImageView(Notifications.class.getResource("/org/controlsfx/dialog/dialog-error.png").toExternalForm());
                            break;
                        }
                        case warning: {
                            imageView = new ImageView(Notifications.class.getResource("/org/controlsfx/dialog/dialog-warning.png").toExternalForm());
                            break;
                        }
                        case confirmation: {
                            imageView = new ImageView(Notifications.class.getResource("/org/controlsfx/dialog/dialog-confim.png").toExternalForm());
                            break;
                        }
                    }
                    imageView.setFitHeight(16);
                    imageView.setFitWidth(16);
                    notification.graphic(imageView);
                    notification.show();
                }
            });
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

    public static boolean isUseDarkStyle() {
        return useDarkStyle;
    }

    public static void setUseDarkStyle(boolean useDarkStyle) {
        NotificationManager.useDarkStyle = useDarkStyle;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        NotificationManager.title = title;
    }

    public static Pos getPosition() {
        return position;
    }

    public static void setPosition(Pos position) {
        NotificationManager.position = position;
    }

    public static int getMaxLength() {
        return maxLength;
    }

    public static void setMaxLength(int maxLength) {
        NotificationManager.maxLength = maxLength;
    }

    /**
     * get the style sheet URL
     *
     * @return
     */
    public static String getControlStylesheetURL() {
        final URL url = ResourceManager.getCssURL("notificationpopup.css");
        if (url != null) {
            return url.toExternalForm();
        }
        return null;
    }

    public static boolean isEchoToConsole() {
        return echoToConsole;
    }

    public static void setEchoToConsole(boolean echoToConsole) {
        NotificationManager.echoToConsole = echoToConsole;
    }

    public static boolean isShowNotifications() {
        return showNotifications;
    }

    public static void setShowNotifications(boolean showNotifications) {
        NotificationManager.showNotifications = showNotifications;
        ProgramProperties.put("ShowNotifications", showNotifications);
    }
}