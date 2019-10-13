/*
 * ListViewTypeSearcher.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.find;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import jloda.util.Single;

/**
 * adds text search capability to a list tree
 * Daniel Huson, 10.2018
 */
public class ListViewTypeSearcher {

    /**
     * setups a text-based searcher for a list tree
     *
     * @param listView
     * @param <T>
     */
    public static <T> void setup(final ListView<T> listView) {
        final Single<KeyCode> prevKeyCode = new Single<>(null);
        final Single<Integer> index = new Single<>(-1);
        final Single<String> searchString = new Single<>("");

        listView.focusedProperty().addListener((c, o, n) -> {
            if (n) {
                searchString.set("");
                index.set(-1);
            }
        });

        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (listView.getItems().size() > 0 && searchString.get().length() > 0) {
                    index.set(index.get() + 1);
                    if (index.get() >= listView.getItems().size())
                        index.set(0);

                    // search for next
                    while (index.get() < listView.getItems().size()) {
                        if (listView.getItems().get(index.get()).toString().contains(searchString.get())) {
                            listView.getSelectionModel().select(index.get());
                            listView.scrollTo(index.get());
                            break;
                        }
                        index.set(index.get() + 1);
                    }
                }

            } else if (e.getCode() == KeyCode.BACK_SPACE) {
                searchString.set("");
                if (prevKeyCode.get() == KeyCode.BACK_SPACE)
                    listView.getSelectionModel().clearSelection();
            } else {
                if (prevKeyCode.get() == KeyCode.ENTER) {
                    searchString.set("");
                }

                if (searchString.get().length() < 10000) {
                    if (e.getCode() != KeyCode.ENTER)
                        searchString.set(searchString.get() + e.getText());

                    if (searchString.get().length() == 1) {
                        listView.getSelectionModel().clearSelection();
                        index.set(-1);
                    }

                    int prevIndex = index.get();
                    index.set(prevIndex + 1);
                    while (index.get() < listView.getItems().size()) {
                        if (listView.getItems().get(index.get()).toString().contains(searchString.get())) {
                            if (prevIndex >= 0 && prevIndex < index.get())
                                listView.getSelectionModel().clearSelection(prevIndex);
                            listView.getSelectionModel().select(index.get());
                            listView.scrollTo(index.get());
                            break;
                        }
                        index.set(index.get() + 1);
                    }

                }
            }
            prevKeyCode.set(e.getCode());
        });
    }
}
