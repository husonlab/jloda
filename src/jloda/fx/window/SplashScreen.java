/*
 * SplashScreen.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.window;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.util.ResourceManagerFX;

import java.time.Duration;

/**
 * shows a splash screen
 * Daniel Huson, 3.2019
 */
public class SplashScreen {
    private final Stage stage;

    private static String versionString;
    private static Image image;
    private static SplashScreen instance;

    private static double scale = 0.5;
    private static double fitHeight = 0.0;

    private static Point2D labelAnchor = new Point2D(20, 20);

    public static SplashScreen getInstance() {
        if (instance == null)
            instance = new SplashScreen(null);
        return instance;
    }

    /**
     * constructor
     */
    private SplashScreen(Image image) {
        stage = new Stage(StageStyle.UNDECORATED);
        stage.setResizable(false);

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane);
        stage.setScene(scene);

        if (image != null) {
            final ImageView imageView = new ImageView(image);
            if (getFitHeight() > 0) {
                setScale(getFitHeight() / image.getHeight());
            }
            imageView.setScaleX(getScale());
            imageView.setScaleY(getScale());
            imageView.setEffect(new DropShadow());
            stackPane.getChildren().add(imageView);
            stage.setWidth(imageView.getScaleX() * image.getWidth() + 2);
            stage.setHeight(imageView.getScaleY() * image.getHeight() + 2);
        } else {
            stage.setWidth(500);
            stage.setHeight(50);
        }

        if (getVersionString() != null) {
            AnchorPane anchorPane = new AnchorPane();
            final Label label = new Label(getVersionString());
            anchorPane.getChildren().add(label);
            AnchorPane.setTopAnchor(label, labelAnchor.getY());
            AnchorPane.setLeftAnchor(label, labelAnchor.getX());
            stackPane.getChildren().add(anchorPane);
        }

        scene.setOnMouseClicked(e -> stage.hide());

        stage.focusedProperty().addListener((c, o, n) -> {
            if (!n)
                stage.hide();
        });
    }

    public static void setImageResourceName(String name) {
        image = ResourceManagerFX.getImage(name);
    }

    public static String getVersionString() {
        return versionString;
    }

    public static void setVersionString(String versionString) {
        SplashScreen.versionString = versionString;
    }

    public static void showSplash(Duration duration) {
        if (instance == null)
            instance = new SplashScreen(image);

        Platform.runLater(() -> {
            // center:
            final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            instance.stage.setX((screenBounds.getWidth() - instance.stage.getWidth()) / 2);
            instance.stage.setY((screenBounds.getHeight() - instance.stage.getHeight()) / 2);
            instance.stage.show();
            ProgramExecutorService.getInstance().submit(() -> {
                try {
                    Thread.sleep(duration.toMillis());
                } catch (InterruptedException ignored) {
                } finally {
                    Platform.runLater(instance.stage::hide);
                }
            });
        });
    }

    public static Point2D getLabelAnchor() {
        return labelAnchor;
    }

    public static void setLabelAnchor(Point2D labelAnchor) {
        SplashScreen.labelAnchor = labelAnchor;
    }

    public static double getScale() {
        return SplashScreen.scale;
    }

    public static void setScale(double scale) {
        SplashScreen.scale = scale;
    }

    public static double getFitHeight() {
        return SplashScreen.fitHeight;
    }

    public static void setFitHeight(double fitHeight) {
        SplashScreen.fitHeight = fitHeight;
    }
}
