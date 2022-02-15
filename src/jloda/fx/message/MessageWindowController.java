/*
 * MessageWindowController.java Copyright (C) 2022 Daniel H. Huson
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


import javafx.fxml.FXML;
import javafx.scene.control.*;
import jloda.fx.window.SplashScreen;
import jloda.util.ProgramProperties;

import java.time.Duration;

/**
 * message window controller
 * Daniel Huson, 6.2018
 */
public class MessageWindowController {
	@FXML
	private MenuBar menuBar;

	@FXML
	private Menu fileMenu;

	@FXML
	private Menu editMenu;

	@FXML
	private Menu helpMenu;

	@FXML
	private MenuItem saveAsMenuItem;

	@FXML
	private MenuItem closeMenuItem;

	@FXML
	private MenuItem copyMenuItem;

	@FXML
	private MenuItem clearMenuItem;

	@FXML
	private MenuItem selectAllMenuItem;

	@FXML
	private MenuItem selectNoneMenuItem;

	@FXML
	private MenuItem aboutMenuItem;

	@FXML
	private TextArea textArea;

	@FXML
	private ButtonBar buttonBar;

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public MenuItem getSaveAsMenuItem() {
		return saveAsMenuItem;
	}

	public MenuItem getCloseMenuItem() {
		return closeMenuItem;
	}

	public MenuItem getCopyMenuItem() {
		return copyMenuItem;
	}

	public MenuItem getClearMenuItem() {
		return clearMenuItem;
	}

	public MenuItem getSelectAllMenuItem() {
		return selectAllMenuItem;
	}

	public MenuItem getSelectNoneMenuItem() {
		return selectNoneMenuItem;
	}

	public MenuItem getAboutMenuItem() {
		return aboutMenuItem;
	}

	public TextArea getTextArea() {
		return textArea;
	}

	public ButtonBar getButtonBar() {
		return buttonBar;
	}

	@FXML
	void initialize() {
		// if we are running on MacOS, put the specific menu items in the right places
		if (ProgramProperties.isMacOS()) {
			getMenuBar().setUseSystemMenuBar(true);
			//fileMenu.getItems().remove(getQuitMenuItem());
			helpMenu.getItems().remove(getAboutMenuItem());
		} else {
			getAboutMenuItem().setOnAction((e) -> SplashScreen.showSplash(Duration.ofMinutes(1)));
		}
	}
}
