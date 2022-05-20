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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;

public class PresentationMode {
	private final static BooleanProperty presentationMode = new SimpleBooleanProperty(false);

	static {
		presentationMode.addListener(e -> {
			for (var window : MainWindowManager.getInstance().getMainWindows()) {
				window.getStage().setFullScreen(true);
				ensurePresentationMode(window.getStage());
				for (var aux : MainWindowManager.getInstance().getAuxiliaryWindows(window)) {
					ensurePresentationMode(aux);
				}
			}
		});
	}

	public static void ensurePresentationMode(Stage stage) {
		if (isPresentationMode())
			stage.getScene().getStylesheets().add("jloda/resources/css/presentation.css");
		else
			stage.getScene().getStylesheets().remove("jloda/resources/css/presentation.css");
	}

	public static void setPresentationMode(boolean presentationMode) {
		PresentationMode.presentationMode.set(presentationMode);
	}

	public static boolean isPresentationMode() {
		return presentationMode.get();
	}

	public static BooleanProperty presentationModeProperty() {
		return presentationMode;
	}
}
