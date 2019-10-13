/*
 * SplashScreen.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
    public final Stage stage;

    private static String versionString;
    private static SplashScreen instance;

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
            imageView.setScaleX(0.5);
            imageView.setScaleY(0.5);
            stackPane.getChildren().add(imageView);
            stage.setWidth(imageView.getScaleX() * image.getWidth());
            stage.setHeight(imageView.getScaleY() * image.getHeight());
        } else {
            stage.setWidth(500);
            stage.setHeight(50);
        }

        if (getVersionString() != null) {
            AnchorPane anchorPane = new AnchorPane();
            final Label label = new Label(getVersionString());
            anchorPane.getChildren().add(label);
            AnchorPane.setTopAnchor(label, 20.0);
            AnchorPane.setLeftAnchor(label, 20.0);
            stackPane.getChildren().add(anchorPane);
        }

        scene.setOnMouseClicked(e -> stage.hide());

        stage.focusedProperty().addListener((c, o, n) -> {
            if (!n)
                stage.hide();
        });
    }

    public static void setImageResourceName(String name) {
        final Image image = ResourceManagerFX.getImage(name);
        instance = new SplashScreen(image);
    }

    public static String getVersionString() {
        return versionString;
    }

    public static void setVersionString(String versionString) {
        SplashScreen.versionString = versionString;
    }

    public void showSplash(Duration duration) {
        Platform.runLater(() -> {
            // center:
            final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
            stage.show();
            ProgramExecutorService.getInstance().submit(() -> {
                try {
                    Thread.sleep(duration.toMillis());
                } catch (InterruptedException e) {
                } finally {
                    Platform.runLater(stage::hide);
                }
            });
        });
    }
}
