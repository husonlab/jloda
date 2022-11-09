/*
 * MessageWindow.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.message;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.ProgramProperties;
import jloda.fx.window.IMainWindow;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class MessageWindow {
    private static MessageWindow instance;
    private static final BooleanProperty visible = new SimpleBooleanProperty(false);

    private final MessageWindowController controller;
    private final Stage stage;

    private final PrintStream printStream;

    private MessageWindow() {
        final ExtendedFXMLLoader<MessageWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();
        stage = new Stage();
        stage.getIcons().setAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        //stage.sizeToScene();
        final IMainWindow parentMainWindow = MainWindowManager.getInstance().getLastFocusedMainWindow();
        if (parentMainWindow != null) {
            stage.setX(parentMainWindow.getStage().getX());
            stage.setY(parentMainWindow.getStage().getY() + parentMainWindow.getStage().getHeight() - 10);
            stage.setWidth(Math.max(stage.getScene().getWidth(), parentMainWindow.getStage().getWidth()));
            stage.setHeight(150);
        }
        stage.setTitle("Message Window - " + ProgramProperties.getProgramName());
        stage.setOnCloseRequest((c) -> setVisible(false));

        printStream = new PrintStreamForTextArea(controller.getTextArea());

        controller.getCloseMenuItem().setOnAction((e) -> setVisible(false));
        controller.getSaveAsMenuItem().setOnAction((e) -> saveAs());

        controller.getClearMenuItem().setOnAction((e) -> controller.getTextArea().clear());
        controller.getClearMenuItem().disableProperty().bind(controller.getTextArea().textProperty().isEmpty());

        controller.getSelectAllMenuItem().setOnAction((e) -> controller.getTextArea().selectAll());
        controller.getSelectAllMenuItem().disableProperty().bind(controller.getTextArea().textProperty().isEmpty());

        controller.getSelectNoneMenuItem().setOnAction((e) -> controller.getTextArea().selectRange(0, 0));
        controller.getSelectNoneMenuItem().disableProperty().bind(controller.getTextArea().textProperty().isEmpty());

        controller.getCopyMenuItem().setOnAction((e) -> controller.getTextArea().copy());
        controller.getCopyMenuItem().disableProperty().bind(controller.getTextArea().selectedTextProperty().isEmpty());

        controller.getClearMenuItem().setOnAction((e) -> controller.getTextArea().clear());
        controller.getClearMenuItem().disableProperty().bind(controller.getTextArea().textProperty().isEmpty());

        visible.bind(stage.showingProperty());
        setVisible(true);
    }

    public static MessageWindow getInstance() {
        if (instance == null)
            instance = new MessageWindow();
        return instance;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            if (!stage.isShowing()) {
                {
                    stage.show();
                    Basic.restoreSystemOut(printStream);
                    Basic.restoreSystemErr(printStream);
                }
                stage.toFront();
                stage.getIcons().setAll(ProgramProperties.getProgramIconsFX()); // seem to need to refresh these
            } else if (!stage.isFocused()) {
                stage.toFront();
            }
        } else { // is showing and has focus, hide
            if (stage.isShowing()) {
                Basic.restoreSystemOut();
                Basic.restoreSystemErr();
                stage.hide();
            }
        }
    }

    public boolean isVisible() {
        return visible.get();
    }

    public static BooleanProperty visibleProperty() {
        return visible;
    }

    public void append(TextArea textArea, String string) {
        Platform.runLater(() -> {
            textArea.setText(textArea.getText() + string);
            textArea.positionCaret(textArea.getText().length());
        });
    }

    public void saveAs() {

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SplitsTree6 messages");

        final File previousFile = new File(ProgramProperties.get("SaveMessagesFile", "messages.txt"));

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text", "*.txt"));
        fileChooser.setInitialFileName(previousFile.getPath());
        final File selectedFile = fileChooser.showSaveDialog(MainWindowManager.getInstance().getLastFocusedMainWindow().getStage());
        if (selectedFile != null) {
            ProgramProperties.put("SaveMessagesFile", selectedFile.getPath());
            try (FileWriter w = new FileWriter(selectedFile)) {
                w.write(controller.getTextArea().getText());

            } catch (IOException e) {
                NotificationManager.showError("Save messages failed: " + e.getMessage());
            }
        }
    }
}
