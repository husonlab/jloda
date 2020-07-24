/*
 * SetParameterDialog.java Copyright (C) 2020. Daniel H. Huson
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

package jloda.fx.dialog;

import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;
import jloda.util.ProgramProperties;

import java.util.List;

/**
 * get an integer value
 * Daniel Huson, 1.2010
 */
public class SetParameterDialog {
    public static String apply(Stage parent, String message, List<String> values, String defaultValue) {
        final ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultValue, values);
        dialog.setTitle("Set Parameter - " + ProgramProperties.getProgramName());
        dialog.setHeaderText(message);
        dialog.setContentText("Choose the value:");

        return dialog.showAndWait().orElse(null);
    }
}
