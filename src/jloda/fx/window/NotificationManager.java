/*
 *  NotificationManager.java Copyright (C) 2019 Daniel H. Huson
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

package jloda.fx.window;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.util.Duration;
import jloda.fx.util.ResourceManagerFX;
import jloda.util.ProgramProperties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements notifications in JavaFX,
 * Daniel Huson, 8.2019
 */
public class NotificationManager {
    public enum Mode {warning, information, confirmation, error}

    private final static int MAX_NUMBER_MESSAGES = 100;

    private final static Popup[] slot2notification = new Popup[MAX_NUMBER_MESSAGES];

    private final static Map<Popup, Integer> notification2slot = new HashMap<>();

    private final static int notificationHeight = 60;
    private final static int vGap = 4;

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
        showNotification(null, title, message, NotificationManager.Mode.information, 10000);
    }

    /**
     * show an information notification
     *
     * @param message
     */
    public static void showInformation(String message, long milliseconds) {
        showNotification(null, title, message, NotificationManager.Mode.information, milliseconds);
    }

    /**
     * show an information notification
     *
     * @param message
     */
    public static void showInformation(Stage owner, String message) {
        showNotification(owner, title, message, NotificationManager.Mode.information, 10000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showError(String message) {
        showNotification(null, title, message, NotificationManager.Mode.error, 60000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showError(Stage owner, String message) {
        showNotification(owner, title, message, NotificationManager.Mode.error, 60000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showInternalError(String message) {
        showNotification(null, title, "Internal error: " + message, NotificationManager.Mode.error, 60000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showInternalError(Stage owner, String message) {
        showNotification(owner, title, "Internal error: " + message, NotificationManager.Mode.error, 60000);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showError(String message, long milliseconds) {
        showNotification(null, title, message, NotificationManager.Mode.error, milliseconds);
    }

    /**
     * show an error notification
     *
     * @param message
     */
    public static void showError(Stage owner, String message, long milliseconds) {
        showNotification(owner, title, message, NotificationManager.Mode.error, milliseconds);
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
    public static void showWarning(Stage owner, String message) {
        showWarning(owner, message, 60000);
    }

    /**
     * show a warning notification
     *
     * @param message
     */
    public static void showWarning(Stage owner, String message, long milliseconds) {
        showNotification(owner, title, message, NotificationManager.Mode.warning, milliseconds);
    }

    /**
     * show a notification
     *
     * @param owner
     * @param title
     * @param message0
     * @param mode
     * @param milliseconds
     */
    public static void showNotification(Stage owner, String title, final String message0, final NotificationManager.Mode mode, final long milliseconds) {
        final String message = (message0.length() > maxLength + 3 ? (message0.substring(0, maxLength) + "...") : message0).replaceAll("\\s+", " ");

        if (isShowNotifications() && ProgramProperties.isUseGUI()) {
            final Window window = getWindow(owner);
            if (window != null) {
                if (title == null || title.length() == 0) {
                    title = ProgramProperties.getProgramName();
                }
                {
                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                    title += " at " + simpleDateFormat.format(System.currentTimeMillis());
                }

                final Popup notification = new Popup();

                notification.setOnHidden((e) -> {
                    slot2notification[notification2slot.get(notification)] = null;
                    notification2slot.remove(notification);
                });

                final AnchorPane anchorPane = new AnchorPane();
                notification.getContent().add(anchorPane);
                notification.setUserData(anchorPane);

                final BorderPane mainPanel = new BorderPane();
                {
                    mainPanel.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE.deriveColor(1, 1, 1, 0.8), null, null)));
                    mainPanel.setEffect(new DropShadow(3, Color.BLACK));
                    mainPanel.setMinHeight(notificationHeight);
                    mainPanel.setMaxHeight(notificationHeight);
                    mainPanel.setMinWidth(100);

                    mainPanel.setMouseTransparent(true);
                    AnchorPane.setLeftAnchor(mainPanel, 0d);
                    AnchorPane.setRightAnchor(mainPanel, 0d);
                    AnchorPane.setTopAnchor(mainPanel, 0d);
                    AnchorPane.setBottomAnchor(mainPanel, 0d);
                    anchorPane.getChildren().add(mainPanel);
                }

                {
                    final Label messageLabel = new Label(" " + message);
                    messageLabel.setFont(new Font(messageLabel.getFont().getName(), 12));
                    mainPanel.setCenter(messageLabel);
                }

                {
                    final Label titleLabel = new Label(title);
                    titleLabel.setFont(new Font(titleLabel.getFont().getName(), 10));
                    titleLabel.setMouseTransparent(true);
                    AnchorPane.setTopAnchor(titleLabel, 2d);
                    AnchorPane.setLeftAnchor(titleLabel, 10d);
                    anchorPane.getChildren().add(titleLabel);
                }

                {
                    final Button close = new Button("x");
                    close.setFont(new Font(close.getFont().getName(), 8));
                    close.setBackground(null);
                    close.setOnMousePressed((e) -> {
                        if (e.isShiftDown()) { // hide all
                            final ArrayList<Popup> all = new ArrayList<>(notification2slot.keySet());
                            for (Popup one : all) {
                                createFadeTransition(one, -1, 0, one::hide).play();
                            }
                        } else
                            createFadeTransition(notification, -1, 0, notification::hide).play();
                    });

                    close.setMinWidth(20);
                    close.setMaxWidth(20);
                    close.setMinHeight(20);
                    close.setMaxHeight(20);

                    AnchorPane.setTopAnchor(close, 2d);
                    AnchorPane.setRightAnchor(close, 2d);

                    anchorPane.getChildren().add(close);
                }

                {
                    final Image icon;
                    switch (mode) {
                        case confirmation:
                            icon = ResourceManagerFX.getIcon("dialog/dialog-confirmation.png");
                            break;
                        case warning:
                            icon = ResourceManagerFX.getIcon("dialog/dialog-warning.png");
                            break;
                        default:
                        case information:
                            icon = ResourceManagerFX.getIcon("dialog/dialog-information.png");
                            break;
                        case error:
                            icon = ResourceManagerFX.getIcon("dialog/dialog-error.png");
                            break;
                    }

                    final ImageView imageView = new ImageView(icon);
                    imageView.setFitWidth(32);
                    imageView.setFitHeight(32);
                    mainPanel.setPadding(new Insets(1, 5, 1, 5));
                    mainPanel.setLeft(new StackPane(imageView));
                }


                //notificationPopup.sizeToScene();

                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

                notification.setX(primaryScreenBounds.getMinX() + 5);
                notification.setY(primaryScreenBounds.getMaxY() - notificationHeight - vGap);

                final Transition removeAfterShowing = createFadeTransition(notification, 1, 0, notification::hide);
                removeAfterShowing.setDelay(Duration.millis(milliseconds));
                removeAfterShowing.play();

                addToShowingNotifications(notification, primaryScreenBounds.getMaxY());

                Platform.runLater(() -> {
                    notification.show(window);
                    createFadeTransition(notification, 0, 1, null).play();
                });
            }
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

    private static void addToShowingNotifications(Popup newNotification, double maxY) {
        int firstEmptySlot = 0;
        while (firstEmptySlot < MAX_NUMBER_MESSAGES && slot2notification[firstEmptySlot] != null) {
            firstEmptySlot++;
        }

        for (int i = firstEmptySlot; i > 0; i--) {
            final Popup notification = slot2notification[i - 1];
            if (notification != null) {
                slot2notification[i] = notification;
                notification2slot.put(notification, i);
                createChangeYAnimation(notification, maxY - i * (notificationHeight + vGap), maxY - (i + 1) * (notificationHeight + vGap)).play();
            }
        }
        slot2notification[0] = newNotification;
        notification2slot.put(newNotification, 0);
    }

    private static Animation createChangeYAnimation(Popup notification, double oldValue, double newValue) {
        final DoubleProperty y = new SimpleDoubleProperty();
        y.addListener((c, o, n) -> notification.setY(n.doubleValue()));
        final KeyFrame beginKeyFrame = new KeyFrame(Duration.ZERO, new KeyValue(y, oldValue));
        final KeyFrame endKeyFrame = new KeyFrame(Duration.millis(200), new KeyValue(y, newValue));
        return new Timeline(beginKeyFrame, endKeyFrame);
    }

    private static Transition createFadeTransition(Popup notification, double startValue, double endValue, Runnable runOnFinished) {
        final FadeTransition fade = new FadeTransition(Duration.millis(200), (Pane) notification.getUserData());
        if (startValue != -1)
            fade.setFromValue(startValue);
        fade.setToValue(endValue);
        if (runOnFinished != null)
            fade.setOnFinished((e) -> runOnFinished.run());
        return fade;
    }

    public static Window getWindow(Object owner) throws IllegalArgumentException {
        if (owner == null) {
            final List<Window> windows = Window.getWindows();

            for (Window window : windows) {
                if (window.isFocused() && !(window instanceof PopupWindow)) {
                    return window;
                }
            }
            for (Window window : windows) {
                if (!(window instanceof PopupWindow)) {
                    return window;
                }
            }
            return null;
        } else if (owner instanceof Window) {
            return (Window) owner;
        } else if (owner instanceof Node) {
            return ((Node) owner).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Unknown owner: " + owner.getClass());
        }
    }

    public static boolean isShowNotifications() {
        return showNotifications;
    }

    public static void setShowNotifications(boolean showNotifications) {
        NotificationManager.showNotifications = showNotifications;
        ProgramProperties.put("ShowNotifications", showNotifications);
    }

    public static int getMaxLength() {
        return maxLength;
    }

    public static void setMaxLength(int maxLength) {
        NotificationManager.maxLength = maxLength;
    }

    public static boolean isEchoToConsole() {
        return echoToConsole;
    }

    public static void setEchoToConsole(boolean echoToConsole) {
        NotificationManager.echoToConsole = echoToConsole;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        NotificationManager.title = title;
    }
}
