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

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.util.ResourceManagerFX;
import jloda.util.ProgramProperties;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implements notifications in JavaFX,
 * Daniel Huson, 8.2019
 */
public class NotificationManager {
    public enum Mode {warning, information, confirmation, error}

    private final static ArrayList<PopupWindow> activeNotificationSlots = new ArrayList<>();
    private final static Map<PopupWindow, Integer> stage2slot = new HashMap<>();

    private final static int minNotificationSize = 60;

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
     * @param owner
     * @param title
     * @param message0
     * @param mode
     * @param milliseconds
     */
    public static void showNotification(Stage owner, String title, final String message0, final NotificationManager.Mode mode, final long milliseconds) {
        final String message = (message0.length() > maxLength + 3 ? (message0.substring(0, maxLength) + "...") : message0);

        if (isShowNotifications() && ProgramProperties.isUseGUI()) {
            final Window window = getWindow(getWindow(owner));
            if (window != null) {
                if (title == null || title.length() == 0) {
                    title = ProgramProperties.getProgramName();
                }
                {
                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                    title += " at " + simpleDateFormat.format(System.currentTimeMillis());
                }

                final Popup notificationPopup = new Popup();
                notificationPopup.setOnHidden((e) -> {
                });

                final BorderPane mainPanel = new BorderPane();
                mainPanel.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE.deriveColor(1, 1, 1, 0.8), null, null)));
                mainPanel.setEffect(new DropShadow(2, Color.GRAY));

                final Label label = new Label(" " + message);
                label.setFont(new Font(label.getFont().getName(), 14));

                mainPanel.setCenter(label);

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

                final BorderPane topPanel = new BorderPane();
                topPanel.setPadding(new Insets(0, 0, 0, 20));

                topPanel.setLeft(new Label(title));
                final Button close = new Button("x");
                close.setFont(new Font(close.getFont().getName(), 8));
                close.setBackground(null);
                close.setOnAction((e) -> notificationPopup.hide());

                close.setMinWidth(20);
                close.setMaxWidth(20);
                close.setMinHeight(20);
                close.setMaxHeight(20);

                final ImageView imageView = new ImageView(icon);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                mainPanel.setPadding(new Insets(1, 5, 1, 5));
                mainPanel.setLeft(new StackPane(imageView));

                topPanel.setRight(close);
                mainPanel.setTop(topPanel);

                mainPanel.setMinSize(minNotificationSize, minNotificationSize);
                mainPanel.setMaxHeight(minNotificationSize);

                notificationPopup.getContent().add(mainPanel);
                notificationPopup.sizeToScene();

                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

                notificationPopup.setX(primaryScreenBounds.getMinX() + 20);
                notificationPopup.setY(primaryScreenBounds.getMaxY());

                updateSlots();
                if (activeNotificationSlots.size() == 0)
                    activeNotificationSlots.add(notificationPopup);
                else
                    activeNotificationSlots.set(0, notificationPopup);
                stage2slot.put(notificationPopup, 0);

                notificationPopup.show(window);
                changeY(notificationPopup, primaryScreenBounds.getMaxY() - minNotificationSize - 20);
                createHideTimeline(notificationPopup, mainPanel, milliseconds).play();
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
            final PopupWindow stage = activeNotificationSlots.get(i - 1);
            if (stage != null) {
                changeY(stage, stage.getY() - minNotificationSize - 10);
                if (i < activeNotificationSlots.size())
                    activeNotificationSlots.set(i, stage);
                else
                    activeNotificationSlots.add(stage);
                stage2slot.put(stage, i);
            }
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

    private static void changeY(PopupWindow stage, double newValue) {
        final long millisecond = 500;
        ProgramExecutorService.getInstance().submit(() -> {
                    final double delta = 100 * (newValue - stage.getY()) / millisecond;
                    for (long time = 0; time < millisecond; time += 100) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                        stage.setY(stage.getY() + delta);
                    }
                }
        );
    }

    public static Window getWindow(Object owner) throws IllegalArgumentException {
        if (owner == null) {
            List<Window> windows = Window.getWindows();
            Iterator it = windows.iterator();

            Window window;
            do {
                if (!it.hasNext()) {
                    return null;
                }

                window = (Window) it.next();
            } while (!window.isFocused() || window instanceof PopupWindow);

            return window;
        } else if (owner instanceof Window) {
            return (Window) owner;
        } else if (owner instanceof Node) {
            return ((Node) owner).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Unknown owner: " + owner.getClass());
        }
    }

    private static Timeline createHideTimeline(Popup notificationPopup, final Pane pane, long milliseconds) {
        KeyValue fadeOutBegin = new KeyValue(pane.opacityProperty(), 1.0D);
        KeyValue fadeOutEnd = new KeyValue(pane.opacityProperty(), 0.0D);
        KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
        KeyFrame kfEnd = new KeyFrame(Duration.millis(500.0D), fadeOutEnd);
        Timeline timeline = new Timeline(kfBegin, kfEnd);
        timeline.setDelay(Duration.millis(milliseconds));
        timeline.setOnFinished((e) -> {
            notificationPopup.hide();
            activeNotificationSlots.set(stage2slot.get(notificationPopup), null);
            stage2slot.remove(notificationPopup);
        });
        return timeline;
    }
}
