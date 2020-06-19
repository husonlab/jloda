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

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.ProgramProperties;


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

        controller.getInputTextArea().setText(label.getText());

        final RichTextLabel displayLabel = new RichTextLabel(label);

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
    }

}