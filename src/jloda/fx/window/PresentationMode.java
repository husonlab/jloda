/*
 *  PresentationMode.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.scene.control.CheckMenuItem;

/**
 * setup presentation mode menu item
 */
public class PresentationMode {
	public static void setupPresentationModeMenuItem(IMainWindow window, CheckMenuItem menuItem) {
		menuItem.selectedProperty().addListener((c, o, n) -> {
			if (n) {
				window.getStage().setFullScreen(true);
				if (!window.getStage().getScene().getStylesheets().contains("jloda/resources/css/presentation.css"))
					window.getStage().getScene().getStylesheets().add("jloda/resources/css/presentation.css");
			} else
				window.getStage().getScene().getStylesheets().remove("jloda/resources/css/presentation.css");

		});
	}
}
