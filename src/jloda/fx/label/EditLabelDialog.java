/*
 * EditLabelDialog.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.label;

import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.BasicFX;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.HTMLConvert;
import jloda.util.ProgramProperties;

import java.io.File;
import java.util.List;
import java.util.TreeSet;


/**
 * label editor
 * Daniel Huson, 6.2020
 */
public class EditLabelDialog extends Dialog<String> {
    private final EditLabelDialogController controller;
    private final RichTextLabel displayLabel;

    public EditLabelDialog(Stage owner, RichTextLabel label) {
        final ExtendedFXMLLoader<EditLabelDialogController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        setTitle("Label Editor - " + ProgramProperties.getProgramName());
        setHeaderText("Set label. Use HTML tags to style. Drag-and-drop images.");
        initOwner(owner);
        setResizable(true);
        getDialogPane().setContent(extendedFXMLLoader.getRoot());
        initModality(Modality.WINDOW_MODAL);

        displayLabel = (label != null ? new RichTextLabel(label) : new RichTextLabel());
        displayLabel.setScale(1.0);
        controller.getInputTextArea().setText(displayLabel.getText().replaceAll("<br>", "\n"));

        controller.getSupportedHTMLTextArea().setText(RichTextLabel.getSupportedHTMLTags());

        // trigger listener:
        final String tmp = displayLabel.getText();
        displayLabel.setText("???");
        displayLabel.setText(tmp);

        controller.getInputTextArea().textProperty().addListener((c, o, n) -> displayLabel.setText(n.replaceAll("[\n\r]", "<br>")));

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

        for (var character : new TreeSet<>(HTMLConvert.characterHtmlMap.keySet())) {
            var button = new Button(String.valueOf(character));
            button.setPrefSize(Button.USE_COMPUTED_SIZE, 16);
            //button.setEllipsisString("");
            button.setStyle("-fx-background-color: transparent;-fx-border-color: transparent;");
            button.setOnAction(e -> {
                var content = new ClipboardContent();
                content.putString(HTMLConvert.characterHtmlMap.get(character));
                Clipboard.getSystemClipboard().setContent(content);
                controller.getInputTextArea().paste();
            });
            controller.getSpecialCharactersFlowPane().getChildren().add(button);
        }
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

        boolean success = false;
        if (db.hasFiles()) {
            success = true;

            final List<File> files = db.getFiles();
            for (File file : files) {
                if (BasicFX.acceptableImageFormat(file.getName())) {
                    if (file.getName().startsWith("http") || file.getName().startsWith("file:")) {
                        inputTextArea.setText(inputTextArea.getText() + String.format(" <img src=\"%s\" height=%.1f>", file.getPath(), Math.max(12, displayLabel.getFontSize())));
                    } else
                        inputTextArea.setText(inputTextArea.getText() + String.format(" <img src=\"file://%s\" height=%.1f>", file.getAbsolutePath(), Math.max(12, displayLabel.getFontSize())));
                }
            }
        } else if (db.hasString() && db.getString().startsWith("http") && BasicFX.acceptableImageFormat(db.getString())) {
            inputTextArea.setText(inputTextArea.getText() + String.format(" <img src=\"%s\" height=64>", db.getString()));
        }
        e.setDropCompleted(success);
        e.consume();
    }
}