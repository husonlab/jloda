/*
 * ClosingLastDocument.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import jloda.util.ProgramProperties;

import java.util.Optional;

/**
 * closing last document dialog
 * Daniel Huson, 3.2019
 */
public class ClosingLastDocument {

    /**
     * show the closing last document dialog
     *
     * @param stage
     * @return true, if really want to quit
     */
    public static boolean apply(Stage stage) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setResizable(true);

        alert.setTitle("Confirm Quit - " + ProgramProperties.getProgramName());
        alert.setHeaderText("Closing the last open document");
        alert.setContentText("Do you really want to quit?");
        final ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        final ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(buttonTypeCancel, buttonTypeYes);

        final Optional<ButtonType> result = alert.showAndWait();
        return result.isEmpty() || result.get() != buttonTypeCancel;
    }
}
