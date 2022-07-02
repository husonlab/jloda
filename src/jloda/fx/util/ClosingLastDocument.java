/*
 * ClosingLastDocument.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import jloda.fx.window.MainWindowManager;
import jloda.util.ProgramProperties;

/**
 * closing last document dialog
 * Daniel Huson, 3.2019
 */
public class ClosingLastDocument {
    /**
     * show the closing last document dialog
     *
     * @return true, if really want to quit
     */
    public static boolean apply(Stage stage) {
        if (!ProgramProperties.isConfirmQuit())
            return true;
        else {
            final var alert = new Alert(Alert.AlertType.CONFIRMATION);
            if (MainWindowManager.isUseDarkTheme()) {
                alert.getDialogPane().getScene().getWindow().getScene().getStylesheets().add("jloda/resources/css/dark.css");
            }
            alert.initOwner(stage);
            alert.setResizable(true);

            alert.setTitle("Confirm Quit - " + ProgramProperties.getProgramName());
            alert.setHeaderText("Closing the last open document");
            alert.setContentText("Do you really want to quit?");

            final ButtonType yesAndNeverAskAgainType = new ButtonType("Yes, never ask again");

            final ButtonType cancelType = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            final ButtonType yesType = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(yesAndNeverAskAgainType, cancelType, yesType);

            final var result = alert.showAndWait();
            if (result.isPresent() && result.get() == yesAndNeverAskAgainType) {
                ProgramProperties.setConfirmQuit(false);
            }
            return result.isEmpty() || result.get() != cancelType;
        }
    }
}
