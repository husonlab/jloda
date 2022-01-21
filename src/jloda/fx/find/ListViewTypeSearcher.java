/*
 * ListViewTypeSearcher.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.find;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

/**
 * adds text search capability to a list view
 * Daniel Huson, 10.2018
 */
public class ListViewTypeSearcher<T> {
	private final Service<Boolean> clearService;
	private int index = 0;
	private String searchString = "";


	public ListViewTypeSearcher(final ListView<T> listView) {
		clearService = new Service<>() {
			@Override
			protected Task<Boolean> createTask() {
				return new Task<>() {
					@Override
					protected Boolean call() {
						try {
							Thread.sleep(1000);
							searchString = "";
							index = 0;
						} catch (InterruptedException ignored) {
						}
						return false;
					}
				};
			}
		};
		listView.setOnKeyPressed(e -> {
			if (e.getText().length() > 0) {
				clearService.restart();
				searchString += e.getText().toLowerCase();
				if (listView.getItems().size() > 0 && searchString.length() > 0) {
					if (index >= listView.getItems().size())
						index = 0;

					// search for next
					while (index < listView.getItems().size()) {
						if (listView.getItems().get(index).toString().toLowerCase().contains(searchString)) {
							listView.getSelectionModel().clearAndSelect(index);
							listView.scrollTo(index);
							break;
						}
						index += 1;
					}
				}
			} else if (e.getCode() == KeyCode.BACK_SPACE) {
				if (searchString.length() > 0) {
					searchString = "";
				} else {
					listView.getSelectionModel().clearSelection();
					index = 0;
				}
			}
		});

	}

	/**
	 * setups a text-based searcher for a list tree
	 */
	public static <T> ListViewTypeSearcher setup(final ListView<T> listView) {
		return new ListViewTypeSearcher<>(listView);
	}
}
