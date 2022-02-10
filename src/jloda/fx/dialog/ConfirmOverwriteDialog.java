/*
 * ConfirmOverwriteDialog.java Copyright (C) 2022 Daniel H. Huson
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
package jloda.fx.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import jloda.fx.window.MainWindowManager;
import jloda.util.FileUtils;

import java.util.Optional;

/**
 * confirm overwrite dialog
 * Daniel Huson, 1.2020
 */
public class ConfirmOverwriteDialog {
    public enum Result {yes, all, no, cancel}

    public static Result apply(Stage owner, String fileName) {
		if (!FileUtils.fileExistsAndIsNonEmpty(fileName))
			return Result.yes;

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

		if (MainWindowManager.isUseDarkTheme()) {
			alert.getDialogPane().getScene().getWindow().getScene().getStylesheets().add("jloda/resources/css/dark.css");
		}

		alert.initOwner(owner);
		alert.setTitle("File exists");
		alert.setHeaderText("The file '" + FileUtils.getFileNameWithoutPath(fileName) + "' already exists.");
		alert.setContentText("Overwrite the existing file?");
		ButtonType buttonTypeYes = new ButtonType("Yes");
		ButtonType buttonTypeYesAll = new ButtonType("Yes to all");
		ButtonType buttonTypeNo = new ButtonType("No");
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeNo, buttonTypeYes, buttonTypeYesAll, buttonTypeCancel);

		final Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == buttonTypeYes)
                return Result.yes;
            else if (result.get() == buttonTypeNo)
                return Result.no;
            else if (result.get() == buttonTypeYesAll)
                return Result.all;
        }
        return Result.cancel;
    }
}
