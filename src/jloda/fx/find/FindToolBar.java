/*
 * FindToolBar.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jloda.fx.util.ExtendedFXMLLoader;

import java.util.ArrayList;

/**
 * find (and replace) tool bar
 * Daniel Huson, 1.2018
 */
public class FindToolBar extends VBox {
    private final FindToolBarController controller;
    private final SearchManager searchManager;

    private final BooleanProperty showFindToolBar = new SimpleBooleanProperty(false);
    private final BooleanProperty showReplaceToolBar = new SimpleBooleanProperty(false);
    private final BooleanProperty canFindAgain = new SimpleBooleanProperty(false);

    private final ObservableList<ISearcher> searchers = FXCollections.observableArrayList();
    private final ToggleGroup searcherButtonsToggleGroup = new ToggleGroup();

    /**
     * constructor
     *
     * @param searcher
     */
    public FindToolBar(ISearcher searcher, ISearcher... additional) {
        final ExtendedFXMLLoader<FindToolBarController> extendedFXMLLoader = new ExtendedFXMLLoader<>(FindToolBar.class);
        controller = extendedFXMLLoader.getController();

        setStyle("-fx-border-color: lightgray;");

        showFindToolBarProperty().addListener((c, o, n) -> {
            if (getChildren().contains(controller.getReplaceToolBar()))
                setShowReplaceToolBar(false);
            if (n) {
                if (!getChildren().contains(controller.getAnchorPane()))
                    getChildren().add(controller.getAnchorPane());
                Platform.runLater(() -> {
                    controller.getSearchComboBox().requestFocus();
                    controller.getSearchComboBox().getEditor().selectAll();
                });
            } else {
                getChildren().remove(controller.getAnchorPane());
                cancel();
            }
        });

        showReplaceToolBarProperty().addListener((c, o, n) -> {
            if (n) {
                setShowFindToolBar(true);
                if (!getChildren().contains(controller.getReplaceToolBar()))
                    getChildren().add(controller.getReplaceToolBar());
            } else
                getChildren().remove(controller.getReplaceToolBar());
        });


        searchManager = new SearchManager();
        searchManager.searchTextProperty().bind(controller.getSearchComboBox().getEditor().textProperty());
        searchManager.replaceTextProperty().bind(controller.getReplaceComboBox().getEditor().textProperty());
        searchManager.setSearcher(searcher);

        canFindAgain.bind(searchManager.searchTextProperty().isNotEmpty().and(showFindToolBar));

        controller.getLabel().textProperty().bind(searchManager.messageProperty());

        controller.getSearchComboBox().setButtonCell(new ListCell<>());

        searchManager.messageProperty().addListener((c, o, n) -> {
            final Color color;
            if (n.startsWith("No"))
                color = Color.LIGHTPINK.deriveColor(1, 0.5, 1, 1);
            else if (n.startsWith("Found"))
                color = Color.PALEGREEN.deriveColor(1, 0.5, 1, 1);
            else
                color = Color.WHITE;
            controller.getSearchComboBox().getEditor().setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
        });

        // add entered stuff to list
        controller.getSearchComboBox().valueProperty().addListener((c, o, n) -> {
            if (n != null && n.length() > 0) {
                ArrayList<String> toDelete = new ArrayList<>();
                for (String item : controller.getSearchComboBox().getItems()) {
                    if (item != null && item.length() > 0) {
                        if (n.equals(item))
                            return; // already present
                        if (n.startsWith(item))
                            toDelete.add(item);
                    }
                }
                if (toDelete.size() > 0)
                    controller.getSearchComboBox().getItems().removeAll(toDelete);
                controller.getSearchComboBox().getItems().add(0, n);
                controller.getSearchComboBox().getSelectionModel().select(0);
            }
        });

        // add entered stuff to list
        controller.getReplaceComboBox().valueProperty().addListener((c, o, n) -> {
            if (n != null && n.length() > 0) {
                ArrayList<String> toDelete = new ArrayList<>();
                for (String item : controller.getReplaceComboBox().getItems()) {
                    if (item != null && item.length() > 0) {
                        if (n.equals(item))
                            return; // already present
                        if (n.startsWith(item))
                            toDelete.add(item);
                    }
                }
                if (toDelete.size() > 0)
                    controller.getReplaceComboBox().getItems().removeAll(toDelete);
                controller.getReplaceComboBox().getItems().add(0, n);
                controller.getReplaceComboBox().getSelectionModel().select(0);
            }
        });

        searchManager.caseSensitiveOptionProperty().bind(controller.getCaseSensitiveCheckBox().selectedProperty());
        searchManager.wholeWordsOnlyOptionProperty().bind(controller.getWholeWordsOnlyCheckBox().selectedProperty());
        searchManager.regularExpressionsOptionProperty().bind(controller.getRegExCheckBox().selectedProperty());

        controller.getRegExCheckBox().disableProperty().bind(searchManager.disabledProperty());
        controller.getWholeWordsOnlyCheckBox().disableProperty().bind(searchManager.disabledProperty());
        controller.getCaseSensitiveCheckBox().disableProperty().bind(searchManager.disabledProperty());


        BooleanProperty inSearch = new SimpleBooleanProperty(false);

        controller.getSearchComboBox().setOnAction((e) -> {
            if (!inSearch.get()) {
                if (searchManager.getSearchText().length() > 0) {
                    try {
                        inSearch.set(true);
                        controller.getSearchComboBox().setValue(searchManager.getSearchText());
                        searchManager.findFirst();
                    } finally {
                        inSearch.set(false);
                    }
                }
            }
        });

        controller.getFindButton().disableProperty().bind(searchManager.disabledProperty().or(searchManager.searchTextProperty().isEmpty()));
        controller.getFindButton().setOnAction((e) -> {
            if (!inSearch.get()) {
                try {
                    inSearch.set(true);
                    controller.getSearchComboBox().setValue(searchManager.getSearchText());
                    searchManager.findFirst();
                } finally {
                    inSearch.set(false);
                }
            }
        });

        controller.getNextButton().disableProperty().bind(searchManager.disabledProperty().or(searchManager.searchTextProperty().isEmpty()));
        controller.getNextButton().setOnAction((e) -> {
            if (!inSearch.get()) {
                try {
                    inSearch.set(true);
                    controller.getSearchComboBox().setValue(searchManager.getSearchText());
                    searchManager.findNext();
                } finally {
                    inSearch.set(false);
                }
            }
        });

        controller.getAllButton().disableProperty().bind(searchManager.disabledProperty().or(searchManager.searchTextProperty().isEmpty()).or(searchManager.canFindAllProperty().not()));
        controller.getAllButton().setOnAction((e) -> {
            if (!inSearch.get()) {
                try {
                    inSearch.set(true);
                    controller.getSearchComboBox().setValue(searchManager.getSearchText());
                    searchManager.findAll();
                } finally {
                    inSearch.set(false);
                }
            }
        });

        controller.getReplaceButton().disableProperty().bind(searchManager.disabledProperty().or(searchManager.searchTextProperty().isEmpty()));
        controller.getReplaceButton().setOnAction((e) -> {
            if (!inSearch.get()) {
                try {
                    inSearch.set(true);
                    controller.getSearchComboBox().setValue(searchManager.getSearchText());
                    searchManager.findAndReplace();
                } finally {
                    inSearch.set(false);
                }
            }
        });

        controller.getReplaceAllButton().disableProperty().bind(searchManager.disabledProperty().or(searchManager.searchTextProperty().isEmpty()));
        controller.getReplaceAllButton().setOnAction((e) -> {
            if (!inSearch.get()) {
                try {
                    inSearch.set(true);
                    controller.getSearchComboBox().setValue(searchManager.getSearchText());
                    searchManager.replaceAll();
                } finally {
                    inSearch.set(false);
                }
            }
        });

        controller.getInSelectionOnlyCheckBox().disableProperty().bind(searchManager.disabledProperty().or(controller.getReplaceButton().disabledProperty()).or(searchManager.canReplaceInSelectionProperty().not()));
        controller.getInSelectionOnlyCheckBox().selectedProperty().addListener((c, o, n) -> searchManager.setGlobalScope(!n));

        controller.getCloseButton().setOnAction((e) -> setShowFindToolBar(false));

        for (ISearcher another : additional) {
            addSearcher(another);
        }
    }

    public boolean isShowFindToolBar() {
        return showFindToolBar.get();
    }

    public BooleanProperty showFindToolBarProperty() {
        return showFindToolBar;
    }

    public void setShowFindToolBar(boolean showFindToolBar) {
        this.showFindToolBar.set(showFindToolBar);
    }

    public boolean getShowReplaceToolBar() {
        return showReplaceToolBar.get();
    }

    public BooleanProperty showReplaceToolBarProperty() {
        return showReplaceToolBar;
    }

    public void setShowReplaceToolBar(boolean showReplaceToolBar) {
        this.showReplaceToolBar.set(showReplaceToolBar);
    }

    public boolean isCanFindAgain() {
        return canFindAgain.get();
    }

    /**
     * can find again be performed?
     *
     * @return true if findable text present
     */
    public ReadOnlyBooleanProperty canFindAgainProperty() {
        return canFindAgain;
    }

    /**
     * perform find again
     */
    public void findAgain() {
        controller.getNextButton().fire();
    }

    public void cancel() {
        searchManager.cancel();
    }

    /**
     * add the searcher to the list of available searchers. It will be represented by a toggle button
     *
     * @param other
     */
    public void addSearcher(ISearcher other) {
        if (searchers.size() == 0) {
            final ISearcher searcher = searchManager.getSearcher();
            // make sure the current searcher is present:
            final ToggleButton searcherButton = new ToggleButton(searcher.getName());
            searcherButton.setStyle("-fx-font-size: 10");
            searcherButton.setToggleGroup(searcherButtonsToggleGroup);
            searcherButton.setOnAction((e) -> searchManager.setSearcher(searcher));
            searcherButton.setSelected(true);
            controller.getToolBar().getItems().add(controller.getToolBar().getItems().size() - 2, new Separator(Orientation.VERTICAL));
            controller.getToolBar().getItems().add(controller.getToolBar().getItems().size() - 2, searcherButton);
            searchers.add(searcher);
        }

        if (!searchers.contains(other)) {
            final ToggleButton searcherButton = new ToggleButton(other.getName());
            searcherButton.setStyle("-fx-font-size: 10");
            searcherButton.setToggleGroup(searcherButtonsToggleGroup);
            searcherButton.setOnAction((e) -> searchManager.setSearcher(other));
            controller.getToolBar().getItems().add(controller.getToolBar().getItems().size() - 2, searcherButton);
            searchers.add(other);
        }
    }

    /**
     * set the searcher to use, first adding it, if necessary
     *
     * @param searcher
     */
    public void setSearcher(ISearcher searcher) {
        addSearcher(searcher);
        searchManager.setSearcher(searcher);
    }
}
