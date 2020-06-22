/*
 * LabelEditor.java Copyright (C) 2020. Daniel H. Huson
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
 *
 */

package jloda.fx.label;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.BasicFX;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.ProgramProperties;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;


/**
 * label editor
 * Daniel Huson, 6.2020
 */
public class EditLabelDialog extends Dialog<String> {
    private final EditLabelDialogController controller;

    public EditLabelDialog(Stage owner, RichTextLabel label) {

        final ExtendedFXMLLoader<EditLabelDialogController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        setTitle("Label Editor - " + ProgramProperties.getProgramName());
        setHeaderText("Set label (using HTML tags to style, if desired)");
        initOwner(owner);
        setResizable(true);
        getDialogPane().setContent(extendedFXMLLoader.getRoot());
        initModality(Modality.WINDOW_MODAL);

        controller.getInputTextArea().setText(label.getText().replaceAll("<n>", "\n"));

        final RichTextLabel displayLabel = new RichTextLabel(label);

        if (false) // todo: implement background removal here
            displayLabel.getChildren().addListener((ListChangeListener<Node>) c -> {
                while (c.next()) {
                    for (Node node : c.getAddedSubList()) {
                        if (node instanceof ImageView) {
                            final ImageView imageView = (ImageView) node;
                            imageView.setOnContextMenuRequested(z -> {
                                final MenuItem removeWhiteBackground = new MenuItem("Remove White Background");
                                removeWhiteBackground.setOnAction(d -> {
                                    Executors.newSingleThreadExecutor().submit(() -> {
                                        try {
                                            final Image image = imageView.getImage(); // todo: remove background
                                            Platform.runLater(() -> imageView.setImage(image));
                                        } catch (Exception ex) {
                                            System.err.println(ex);
                                        }
                                    });
                                });
                                final ContextMenu menu = new ContextMenu(removeWhiteBackground);
                                menu.show(imageView, z.getScreenX(), z.getScreenY());
                            });
                        }
                    }
                }
            });
        // trigger listener:
        displayLabel.setText("");
        displayLabel.setText(label.getText());

        controller.getInputTextArea().textProperty().addListener((c, o, n) -> {
            displayLabel.setText(n.replaceAll("[\n\r]", " "));
        });

        controller.getPreviewStackPane().getChildren().add(displayLabel);

        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType buttonTypeApply = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);

        setResultConverter((dialogButton) -> {
            if (dialogButton == buttonTypeApply)
                return displayLabel.getText();
            else
                return null;
        });

        getDialogPane().getButtonTypes().setAll(buttonTypeCancel, buttonTypeApply);

        controller.getClearHTMLButton().setOnAction(z -> controller.getInputTextArea().setText(displayLabel.getRawText()));

        controller.getInputTextArea().setOnDragOver(this::handleDragOver);
        controller.getInputTextArea().setOnDragDropped(this::handleDragDropped);
        controller.getInputTextArea().setOnDragExited(event -> controller.getInputTextArea().setEffect(null));
    }

    private void handleDragOver(DragEvent e) {
        final Dragboard db = e.getDragboard();

        boolean canDrop = false;
        if (db.hasFiles()) {
            for (File file : db.getFiles()) {
                if (BasicFX.acceptableImageFormat(file.getName())) {
                    canDrop = true;
                    break;
                }
            }
        }
        if (!canDrop && db.hasString() && db.getString().startsWith("http") && BasicFX.acceptableImageFormat(db.getString())) {
            canDrop = true;
        }
        if (canDrop) {
            controller.getInputTextArea().setEffect(new DropShadow());
            e.acceptTransferModes(TransferMode.COPY);
        } else {
            e.consume();
        }
    }

    private void handleDragDropped(DragEvent e) {
        final TextArea inputTextArea = controller.getInputTextArea();

        final Dragboard db = e.getDragboard();

        System.err.println(db);
        System.err.println(db.getString());
        System.err.println(db.getImage());

        boolean success = false;
        if (db.hasFiles()) {
            success = true;

            final List<File> files = db.getFiles();
            for (File file : files) {
                if (BasicFX.acceptableImageFormat(file.getName())) {
                    System.err.println(file.getAbsolutePath());
                    if (file.getName().startsWith("http") || file.getName().startsWith("file:")) {
                        inputTextArea.setText(inputTextArea.getText() + String.format(" <img src=\"%s\" height=64>", file.getPath()));
                    } else
                        inputTextArea.setText(inputTextArea.getText() + String.format(" <img src=\"file:%s\" height=64>", file.getAbsolutePath()));
                }
            }
        } else if (db.hasString() && db.getString().startsWith("http") && BasicFX.acceptableImageFormat(db.getString())) {
            inputTextArea.setText(inputTextArea.getText() + String.format(" <img src=\"%s\" height=64>", db.getString()));
        }
        e.setDropCompleted(success);
        e.consume();
    }

}