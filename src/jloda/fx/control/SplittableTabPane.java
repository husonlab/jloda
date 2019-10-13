/*
 * SplittableTabPane.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.control;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * a splittable tab pane
 * Daniel Huson, 4.2019
 */
public class SplittableTabPane extends Pane {

    private final ObjectProperty<TabPane> focusedTabPane = new SimpleObjectProperty<>();
    private final ASingleSelectionModel<Tab> selectionModel = new ASingleSelectionModel<>();

    private ObservableList<Tab> tabs = FXCollections.observableArrayList();

    private ObservableMap<TabPane, SplitPane> tabPane2ParentSplitPane = FXCollections.observableHashMap();

    private static final String TAB_DRAG_KEY = "tab";
    private final ObjectProperty<Tab> draggingTab = new SimpleObjectProperty<>();

    private final IntegerProperty size = new SimpleIntegerProperty(0);

    private final BooleanProperty allowUndock = new SimpleBooleanProperty(true);

    private final ArrayList<AuxiliaryWindow> auxiliaryWindows = new ArrayList<>();

    /**
     * constructor
     */
    public SplittableTabPane() {
        final TabPane tabPane = createTabPane();
        tabPane.prefWidthProperty().bind(widthProperty());
        tabPane.prefHeightProperty().bind(heightProperty());

        final SplitPane rootSplitPane = new SplitPane();
        setupSplitPane(rootSplitPane, tabPane);

        tabPane2ParentSplitPane.put(tabPane, rootSplitPane);

        rootSplitPane.setOrientation(Orientation.HORIZONTAL);
        getChildren().add(rootSplitPane);

        tabs.addListener((ListChangeListener<Tab>) (c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Tab tab : c.getAddedSubList()) {
                        TabPane target;
                        if (getFocusedTabPane() != null)
                            target = getFocusedTabPane();
                        else {
                            target = findATabPane(rootSplitPane.getItems());
                        }
                        if (target == null) {
                            target = createTabPane();
                            target.prefWidthProperty().bind(widthProperty());
                            target.prefHeightProperty().bind(heightProperty());
                            tabPane2ParentSplitPane.put(target, rootSplitPane);
                            rootSplitPane.getItems().add(target);
                        }
                        moveTab(tab, null, target);
                        setupDrag(tab);
                    }
                } else if (c.wasRemoved()) {
                    for (Tab tab : c.getRemoved()) {
                        // check whether in auxiliary window:
                        boolean gone = false;
                        for (AuxiliaryWindow auxiliaryWindow : auxiliaryWindows) {
                            if (auxiliaryWindow.getTab() == tab) {
                                auxiliaryWindow.getStage().close();
                                gone = true;
                                break;
                            }
                        }
                        if (!gone)
                            moveTab(tab, tab.getTabPane(), null);
                        if (tab instanceof Closeable) {
                            try {
                                ((Closeable) tab).close();
                            } catch (IOException e) {
                                Basic.caught(e);
                            }
                        }
                    }
                }
            }
            selectionModel.setItems(tabs);
            size.set(tabs.size());
        });

        rootSplitPane.getItems().addListener((InvalidationListener) (e) -> {
            if (rootSplitPane.getItems().size() == 0) {
                Platform.runLater(() -> {
                    TabPane target = createTabPane();
                    target.prefWidthProperty().bind(widthProperty());
                    target.prefHeightProperty().bind(heightProperty());
                    tabPane2ParentSplitPane.put(target, rootSplitPane);
                    rootSplitPane.getItems().add(target);
                });
            }
        });

        selectionModel.selectedItemProperty().addListener((c, o, n) -> {
            if (n != null && n.getTabPane() != null)
                n.getTabPane().getSelectionModel().select(n);
            setFocusedTabPane(n != null ? n.getTabPane() : findATabPane(rootSplitPane.getItems()));
            //System.err.println("Selected: " + n);
        });

        /*
        focusedTabPane.addListener((c, o, n) -> {
            System.err.println("Focus: " + o + " -> " + n);
        });
        */

        tabPane.requestFocus();
    }

    public ASingleSelectionModel<Tab> getSelectionModel() {
        return selectionModel;
    }

    public ObservableList<Tab> getTabs() {
        return tabs;
    }

    public TabPane getFocusedTabPane() {
        return focusedTabPane.get();
    }

    public ObjectProperty<TabPane> focusedTabPaneProperty() {
        return focusedTabPane;
    }

    public void setFocusedTabPane(TabPane focusedTabPane) {
        this.focusedTabPane.set(focusedTabPane);
        if (focusedTabPane != null)
            focusedTabPane.requestFocus();
    }

    /**
     * the number of tabs
     *
     * @return size
     */
    public int size() {
        return size.get();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size;
    }

    /**
     * is undocking of tabs into auxiliary windows allowed
     *
     * @return true, if undocking is allowed
     */
    public boolean isAllowUndock() {
        return allowUndock.get();
    }

    public BooleanProperty allowUndockProperty() {
        return allowUndock;
    }

    public void setAllowUndock(boolean allowUndock) {
        this.allowUndock.set(allowUndock);
    }

    /**
     * redock all undocked tabs
     */
    public void redockAll() {
        for (AuxiliaryWindow auxiliaryWindow : auxiliaryWindows) {
            moveTab(auxiliaryWindow.getTab(), null, getFocusedTabPane());
            auxiliaryWindow.getStage().close();
        }
    }

    private void moveTab(Tab tab, TabPane oldTabPane, TabPane newTabPane) {
        moveTab(tab, oldTabPane, newTabPane, -1);
    }

    /**
     * move tab to other tab pane and update context menu accordingly
     *
     * @param tab
     * @param oldTabPane if not null, removed from here
     * @param newTabPane if not null, added to here
     * @param index      index to add at, or -1
     */
    private void moveTab(Tab tab, TabPane oldTabPane, TabPane newTabPane, int index) {
        if (oldTabPane != null) {
            final SplitPane oldSplitPane = tabPane2ParentSplitPane.get(oldTabPane);
            final int oldIndex = oldTabPane.getTabs().indexOf(tab);
            if (oldIndex >= 0) {
                oldTabPane.getTabs().remove(tab);
                if (oldTabPane.getTabs().size() == 0 && oldSplitPane != null) {
                    oldSplitPane.getItems().remove(oldTabPane);
                    if (getFocusedTabPane() == oldTabPane) {
                        Platform.runLater(() -> {
                            if (tabPane2ParentSplitPane.size() > 0)
                                setFocusedTabPane(tabPane2ParentSplitPane.keySet().iterator().next());
                        });
                    }
                    tabPane2ParentSplitPane.remove(oldTabPane);
                }
                if (newTabPane == null) {
                    if (oldTabPane.getTabs().size() > 0) {
                        Platform.runLater(() -> {
                            final int i = Math.min(oldIndex, oldTabPane.getTabs().size() - 1);
                            if (i >= 0)
                                selectionModel.select(i);
                        });
                    }
                }
            }
        }

        if (newTabPane != null) {
            if (index >= 0 && index < newTabPane.getTabs().size())
                newTabPane.getTabs().add(index, tab);
            else
                newTabPane.getTabs().add(tab);
            setupMenu(tab, newTabPane, tabPane2ParentSplitPane.get(newTabPane));
            Platform.runLater(() -> {
                newTabPane.getSelectionModel().select(tab);
                selectionModel.select(tab);
                setFocusedTabPane(newTabPane);
            });
        }
    }

    /**
     * split the given tab pane in the given orientation and move the tab into the new tab pane
     *
     * @param orientation
     * @param tab
     * @param tabPane
     */
    private void split(Orientation orientation, Tab tab, TabPane tabPane) {
        final SplitPane parentSplitPane = tabPane2ParentSplitPane.get(tabPane);

        if (parentSplitPane.getItems().size() == 1 && parentSplitPane.getOrientation() != orientation)
            parentSplitPane.setOrientation(orientation);

        if (parentSplitPane.getOrientation() == orientation) { // desired split has same orientation as parent split parent, add
            final TabPane newTabPane = createTabPane();
            final double[] dividers = addDivider(parentSplitPane.getDividerPositions());
            parentSplitPane.getItems().add(newTabPane);
            parentSplitPane.setDividerPositions(dividers);
            tabPane2ParentSplitPane.put(newTabPane, parentSplitPane);
            moveTab(tab, tabPane, newTabPane);
        } else { // change of orientation, create new split pane

            final SplitPane splitPane = new SplitPane();
            splitPane.setOrientation(orientation);
            final IntegerProperty splitPaneSize = new SimpleIntegerProperty();
            splitPaneSize.bind(Bindings.size(splitPane.getItems()));

            splitPaneSize.addListener((c, o, n) -> {
                if (o.intValue() > 0 && n.intValue() == 0) {
                    parentSplitPane.getItems().remove(splitPane);
                } else if (o.intValue() > 1 && n.intValue() == 1) {
                    final Node lastNode = splitPane.getItems().get(0);
                    final int index = parentSplitPane.getItems().indexOf(splitPane);
                    if (index != -1) {
                        parentSplitPane.getItems().set(index, lastNode);
                    } else {
                        final double[] dividers = addDivider(parentSplitPane.getDividerPositions());
                        parentSplitPane.getItems().add(lastNode);
                        parentSplitPane.setDividerPositions(dividers);
                    }
                    parentSplitPane.getItems().remove(splitPane);
                    if (lastNode instanceof TabPane)
                        tabPane2ParentSplitPane.put((TabPane) lastNode, parentSplitPane);
                }
            });

            final int indexInParentSplitPane = parentSplitPane.getItems().indexOf(tabPane);
            parentSplitPane.getItems().set(indexInParentSplitPane, splitPane);

            final TabPane newTabPane = createTabPane();
            setupSplitPane(splitPane, tabPane, newTabPane);

            tabPane2ParentSplitPane.put(tabPane, splitPane);
            tabPane2ParentSplitPane.put(newTabPane, splitPane);

            moveToOpposite(tab, tabPane);

            final double[] dividers = parentSplitPane.getDividerPositions();
            parentSplitPane.setDividerPositions(dividers);
        }
    }

    /**
     * add a new divider
     */
    private static double[] addDivider(double[] oldDividers) {
        if (oldDividers.length == 0)
            return new double[]{0.5};
        else {
            final double[] dividers = new double[oldDividers.length + 1];
            System.arraycopy(oldDividers, 0, dividers, 0, oldDividers.length);
            dividers[oldDividers.length] = 0.5 * (1 + oldDividers[oldDividers.length - 1]);
            return dividers;
        }
    }

    /**
     * move a tab to the opposite tab pane
     *
     * @param tab     to be moved
     * @param tabPane the pane from which the tab will be moved
     */
    private void moveToOpposite(Tab tab, TabPane tabPane) {
        final SplitPane parentSplitPane = tabPane2ParentSplitPane.get(tabPane);
        if (parentSplitPane.getItems().size() == 1) { // there is no opposite, need to split
            split(parentSplitPane.getOrientation(), tab, tabPane);
        } else {
            final int index = parentSplitPane.getItems().indexOf(tabPane);
            if (index != -1) {
                // try to find another tab in this split pane:
                for (int i = parentSplitPane.getItems().size() - 1; i >= 0; i--) {
                    if (i != index && parentSplitPane.getItems().get(i) instanceof TabPane) {
                        final TabPane target = (TabPane) (parentSplitPane.getItems().get(i));
                        if (target != null) {
                            moveTab(tab, tabPane, target);
                            return;
                        }
                    }
                }
                // find a tab in other contained split pane:
                for (int i = parentSplitPane.getItems().size() - 1; i >= 0; i--) {
                    if (i != index) {
                        final TabPane target = findATabPane(Collections.singleton(parentSplitPane.getItems().get(i)));
                        if (target != null) {
                            moveTab(tab, tabPane, target);
                            return;
                        }
                    }
                }
            }
        }
    }

    private TabPane findATabPane(Collection<Node> nodes) {
        for (Node node : nodes) {
            if (node instanceof TabPane)
                return (TabPane) node;
            else if (node instanceof SplitPane) {
                final TabPane result = findATabPane(((SplitPane) node).getItems());
                if (result != null)
                    return result;
            }
        }
        return null;
    }

    /**
     * setup the menu when inserting a tab
     *
     * @param tab
     * @param tabPane
     * @param splitPane
     */
    private void setupMenu(Tab tab, TabPane tabPane, SplitPane splitPane) {
        final ArrayList<MenuItem> menuItems = new ArrayList<>();

        if (tab.isClosable()) {
            final MenuItem close = new MenuItem("Close");
            close.setOnAction((e) -> {
                if (tab.isClosable())
                    tabs.remove(tab);
            });
            close.disableProperty().bind(Bindings.size(tabs).isEqualTo(0).or(tab.closableProperty().not()));
            menuItems.add(close);

            final MenuItem closeOthers = new MenuItem("Close Others");
            closeOthers.setOnAction((e) -> {
                for (Tab aTab : tabPane.getTabs()) {
                    if (aTab != tab && aTab.isClosable())
                        Platform.runLater(() -> tabs.remove(aTab));
                }
            });
            closeOthers.disableProperty().bind(Bindings.size(tabPane.getTabs()).lessThan(2).or(tab.closableProperty().not()));
            menuItems.add(closeOthers);

            final MenuItem closeAll = new MenuItem("Close All");
            closeAll.setOnAction((e) -> {
                for (Tab aTab : tabPane.getTabs()) {
                    if (aTab.isClosable())
                        Platform.runLater(() -> tabs.remove(aTab));
                }
            });
            closeAll.disableProperty().bind(tab.closableProperty().not());
            menuItems.add(closeAll);

            tab.setClosable(true);
            tab.setOnCloseRequest((e) -> close.getOnAction().handle(null));
            menuItems.add(new SeparatorMenuItem());
        }

        if (isAllowUndock()) {
            final MenuItem redock = new MenuItem("Redock");
            redock.setOnAction((e) -> moveTab(tab, null, getFocusedTabPane()));
            // we don't show the redock menu item because the undocked window doesn't have a tab

            final MenuItem undock = new MenuItem("Undock");
            undock.setOnAction((e) -> {
                final double x = tabPane.localToScreen(0, 0).getX();
                final double y = tabPane.localToScreen(0, 0).getY();
                moveTab(tab, tabPane, null);
                final AuxiliaryWindow auxiliaryWindow = createAuxiliaryWindow(tab, x, y, tabPane.getWidth(), tabPane.getHeight() - 20, redock);
                auxiliaryWindow.getStage().setOnCloseRequest((z) -> {
                    redock.getOnAction().handle(null);
                    auxiliaryWindow.getStage().hide();
                    auxiliaryWindows.remove(auxiliaryWindow);
                });
                auxiliaryWindow.getStage().show();
                auxiliaryWindows.add(auxiliaryWindow);
            });
            undock.disableProperty().bind(allowUndock.not().or(sizeProperty().lessThan(2)));
            menuItems.add(undock);
            menuItems.add(new SeparatorMenuItem());
        }

        final MenuItem splitVertically = new MenuItem("Split Vertically");
        splitVertically.setOnAction((e) -> split(Orientation.HORIZONTAL, tab, tabPane));
        splitVertically.disableProperty().bind(Bindings.size(tabPane.getTabs()).lessThan(2));
        menuItems.add(splitVertically);

        final MenuItem splitHorizontally = new MenuItem("Split Horizontally");
        splitHorizontally.setOnAction((e) -> split(Orientation.VERTICAL, tab, tabPane));
        splitHorizontally.disableProperty().bind(Bindings.size(tabPane.getTabs()).lessThan(2));
        menuItems.add(splitHorizontally);

        menuItems.add(new SeparatorMenuItem());

        final MenuItem moveToOpposite = new MenuItem("Move to Opposite");
        moveToOpposite.setOnAction((e) -> moveToOpposite(tab, tab.getTabPane()));
        moveToOpposite.disableProperty().bind(Bindings.size(tabPane.getTabs()).lessThan(1));
        menuItems.add(moveToOpposite);

        menuItems.add(new SeparatorMenuItem());

        final MenuItem changeSplitOrientation = new MenuItem("Change Split Orientation");
        changeSplitOrientation.setOnAction((e) -> splitPane.setOrientation(splitPane.getOrientation() == Orientation.VERTICAL ? Orientation.HORIZONTAL : Orientation.VERTICAL));
        menuItems.add(changeSplitOrientation);

        final ArrayList<MenuItem> existingItems;
        if (tab.getContextMenu() != null) {
            existingItems = new ArrayList<>(tab.getContextMenu().getItems());
            int index = findMenuItem(changeSplitOrientation.getText(), existingItems);
            while (index >= 0)
                existingItems.remove(index--);
        } else
            existingItems = null;
        tab.setContextMenu(new ContextMenu(menuItems.toArray(new MenuItem[0])));

        if (existingItems != null && existingItems.size() > 0) {
            if (!(existingItems.get(0) instanceof SeparatorMenuItem)) {
                tab.getContextMenu().getItems().add(new SeparatorMenuItem());
            }
            tab.getContextMenu().getItems().addAll(existingItems);
        }
    }

    /**
     * find the first occurrence of a menu item with the given text
     *
     * @param text
     * @param menuItems
     * @return index or -1
     */
    private static int findMenuItem(String text, Collection<MenuItem> menuItems) {
        int i = 0;
        for (MenuItem item : menuItems) {
            if (text.equals(item.getText()))
                return i;
            i++;
        }
        return -1;
    }

    private void setupSplitPane(SplitPane splitPane, TabPane... tabPanes) {
        splitPane.getItems().setAll(tabPanes);
        splitPane.prefWidthProperty().bind(widthProperty());
        splitPane.prefHeightProperty().bind(heightProperty());

        final double[] positions = new double[tabPanes.length - 1];
        for (int i = 1; i < tabPanes.length; i++) {
            positions[i - 1] = i * (1.0 / tabPanes.length);
        }
        splitPane.setDividerPositions(positions);
    }

    /**
     * create a tab pane
     *
     * @return new tab pane
     */
    private TabPane createTabPane() {
        final TabPane tabPane = new TabPane();
        tabPane.getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            if (n == null)
                selectionModel.clearSelection();
            else
                selectionModel.select(n);
        });
        tabPane.focusedProperty().addListener((c, o, n) -> {
            if (n) {
                if (tabPane.getSelectionModel().isEmpty())
                    selectionModel.clearSelection();
                else
                    selectionModel.select(tabPane.getSelectionModel().getSelectedItem());
            }
        });
        setupDrop(tabPane);
        return tabPane;
    }

    private void setupDrag(Tab tab) {
        final Label label = new Label(tab.getText());
        label.setGraphic(tab.getGraphic());
        tab.setText("");
        tab.setGraphic(label);
        tab.textProperty().addListener((c, o, n) -> {
            if (n.length() > 0) {
                label.setText(n);
                Platform.runLater(() -> tab.setText(""));
            }
        });
        tab.graphicProperty().addListener((c, o, n) -> {
            if (n != label) {
                label.setGraphic(n);
                Platform.runLater(() -> tab.setGraphic(label));
            }
        });
        label.setOnDragOver(event -> {
            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()
                    && TAB_DRAG_KEY.equals(dragboard.getString()) && draggingTab.get() != null) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });

        label.setOnDragDetected(event -> {
            Dragboard dragboard = label.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(TAB_DRAG_KEY);
            dragboard.setContent(clipboardContent);
            draggingTab.set(tab);
            event.consume();
        });

        label.setOnDragDropped((event -> {
            final int index = tab.getTabPane().getTabs().indexOf(tab);

            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString() && TAB_DRAG_KEY.equals(dragboard.getString()) && draggingTab.get() != null) {
                final Tab draggedTab = draggingTab.get();

                if (draggedTab.getTabPane() == tab.getTabPane() && index != tab.getTabPane().getTabs().indexOf(draggedTab)) {
                    final TabPane tabPane = draggedTab.getTabPane();
                    tabPane.getTabs().remove(draggedTab);
                    tabPane.getTabs().add(index, draggedTab);
                    tabPane.getSelectionModel().select(draggedTab);
                } else {
                    moveTab(draggedTab, draggedTab.getTabPane(), tab.getTabPane(), index);
                }
                event.setDropCompleted(true);
                draggingTab.set(null);
                event.consume();
            }
        }));
    }

    private void setupDrop(TabPane tabPane) {
        tabPane.setOnDragOver(event -> {
            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()
                    && TAB_DRAG_KEY.equals(dragboard.getString()) && draggingTab.get() != null && draggingTab.get().getTabPane() != tabPane) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });

        tabPane.setOnDragDropped(event -> {
            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString() && TAB_DRAG_KEY.equals(dragboard.getString()) && draggingTab.get() != null && draggingTab.get().getTabPane() != tabPane) {
                final Tab tab = draggingTab.get();

                moveTab(tab, tab.getTabPane(), tabPane);
                event.setDropCompleted(true);
                draggingTab.set(null);
                event.consume();
            }
        });
    }

    private AuxiliaryWindow createAuxiliaryWindow(final Tab tab, double screenX, double screenY, double width, double height, MenuItem redock) {
        final StackPane root = new StackPane();
        tab.setContextMenu(new ContextMenu(redock));

        root.getChildren().add(tab.getContent());
        //root.getChildren().add(new TabPane(tab)); // need to disable drop targets for this to work

        final Stage stage = new Stage();
        if (tab.getText().length() > 0)
            stage.setTitle(tab.getText());
        else if (tab.getGraphic() instanceof Labeled)
            stage.setTitle(((Labeled) tab.getGraphic()).getText());
        else
            stage.setTitle("Untitled");
        stage.getIcons().addAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(new Scene(root, width, height));
        stage.sizeToScene();
        stage.setX(screenX);
        stage.setY(screenY);
        return new AuxiliaryWindow(stage, tab);
    }

    private static class AuxiliaryWindow {
        final private Stage stage;
        final private Tab tab;

        AuxiliaryWindow(Stage stage, Tab tab) {
            this.stage = stage;
            this.tab = tab;
        }

        public Stage getStage() {
            return stage;
        }

        public Tab getTab() {
            return tab;
        }
    }
}
