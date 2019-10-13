/*
 * FlowView.java Copyright (C) 2019. Daniel H. Huson
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
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * simple implementation of a flow tree
 * Uses virtualization
 * Also allows background production of nodes. This is useful if computing a node for an item takes time
 * Daniel Huson, 4.2019
 */
public class FlowView<T> extends Pane implements Closeable {
    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final ObservableMap<T, Node> item2node = FXCollections.observableHashMap();
    private final Function<T, Node> nodeProducer;
    private final ObservableList<T> nodeProducerQueue = FXCollections.observableArrayList();

    private final IntegerProperty blockSize = new SimpleIntegerProperty(60);
    private final DoubleProperty Vgap = new SimpleDoubleProperty(0);
    private final DoubleProperty Hgap = new SimpleDoubleProperty(0);

    private final Background emptyBackground = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));

    private final ListView<ArrayList<T>> listView;

    private final IntegerProperty size = new SimpleIntegerProperty(0);

    private MultipleSelectionModel<T> selectionModel;
    private final ChangeListener<T> selectedItemListener;

    private final BooleanProperty scrollToSelection = new SimpleBooleanProperty(false);

    private final BooleanProperty precomputeSnapshots = new SimpleBooleanProperty(false);
    private ListChangeListener<T> listChangeListener;
    private ExecutorService executorService;

    /**
     * constructor
     *
     * @param nodeProducer
     */
    public FlowView(final Function<T, Node> nodeProducer) {
        listView = new ListView<>();
        listView.setOrientation(Orientation.VERTICAL);
        listView.prefWidthProperty().bind(widthProperty());
        listView.prefHeightProperty().bind(heightProperty());
        getChildren().add(listView);
        listView.setSelectionModel(new EmptyMultipleSelectionModel<>());
        listView.setOnKeyReleased((e) -> {
        });
        listView.setOnKeyPressed((e) -> {
            if (selectionModel != null) {
                if (e.getCode() == KeyCode.LEFT) {
                    if (selectionModel.getSelectedIndex() == -1)
                        selectionModel.selectLast();
                    else if (selectionModel.getSelectedIndex() > 0)
                        selectionModel.selectPrevious();
                } else if (e.getCode() == KeyCode.RIGHT) {
                    if (selectionModel.getSelectedIndex() == -1)
                        selectionModel.selectFirst();
                    else if (selectionModel.getSelectedIndex() < size())
                        selectionModel.selectNext();
                } else if (e.getCode() == KeyCode.UP) {
                    if (selectionModel.getSelectedIndex() <= getBlockSize())
                        selectionModel.selectFirst();
                    else
                        selectionModel.clearAndSelect(selectionModel.getSelectedIndex() - getBlockSize());
                } else if (e.getCode() == KeyCode.DOWN) {
                    if (selectionModel.getSelectedIndex() >= size() - getBlockSize())
                        selectionModel.selectLast();
                    else
                        selectionModel.clearAndSelect(selectionModel.getSelectedIndex() + getBlockSize());
                }
                e.consume();
            }
        });
        listView.setFocusTraversable(false);

        listView.setCellFactory(v -> new ListCell<>() {
            {
                setStyle(String.format("-fx-padding: %.0fpx %.0fpx %.0fpx %.0fpx;", 0.5 * getVgap(), getHgap(), 0.5 * getVgap(), getHgap()));
            }

            @Override
            protected void updateItem(ArrayList<T> block, boolean empty) {
                super.updateItem(block, empty);
                super.setBackground(emptyBackground);
                if (empty || block == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    final FlowPane flowPane = new FlowPane();
                    flowPane.setOnKeyPressed((e) -> {
                    });
                    flowPane.setOnKeyReleased((e) -> {
                    });

                    flowPane.setHgap(getHgap());
                    flowPane.setVgap(getVgap());
                    flowPane.setBackground(emptyBackground);
                    for (T item : block) {
                        if (!item2node.containsKey(item)) {
                            final Node snapshot = nodeProducer.apply(item);
                            item2node.put(item, snapshot);
                            flowPane.getChildren().add(snapshot);
                        } else
                            flowPane.getChildren().add(item2node.get(item));
                    }
                    flowPane.setUserData(block);
                    setGraphic(flowPane);
                }
            }
        });

        this.nodeProducer = nodeProducer;

        items.addListener((ListChangeListener<T>) (c) -> {
            boolean mustRelayoutAll = false;
            while (c.next()) {
                if (c.wasPermutated() || c.wasReplaced()) {
                    mustRelayoutAll = true;
                } else if (c.wasRemoved()) {
                    item2node.keySet().removeAll(c.getRemoved());
                    mustRelayoutAll = true;
                } else if (c.wasAdded()) {
                    if (nodeProducer != null)
                        nodeProducerQueue.addAll(c.getAddedSubList());
                    if (!mustRelayoutAll)
                        recomputeBlocks(c.getAddedSubList(), false);
                }
            }
            if (mustRelayoutAll) { // todo: need to improve this
                listView.getItems().clear();
                recomputeBlocks(items, true);
            }
            size.set(items.size());
        });

        blockSize.addListener((c, o, n) -> recomputeBlocks(items, true));

        precomputeSnapshots.addListener((c, o, n) -> precomputeSnapshots(n));

        selectedItemListener = (c, o, n) -> {
            if (n != null && isScrollToSelection()) { // doesn't work very well
                final Node node = item2node.get(n);
                if (node != null) {
                    final Node subFlowPaneContainingNode = node.getParent();
                    if (subFlowPaneContainingNode == null)
                        System.err.println("parentOfNode==null");

                    if (subFlowPaneContainingNode != null && subFlowPaneContainingNode.getUserData() instanceof ArrayList) {
                        final ArrayList<T> block = (ArrayList<T>) subFlowPaneContainingNode.getUserData();
                        listView.scrollTo(block);
                    }
                }
            }
        };

        setSelectionModel(new AnotherMultipleSelectionModel<>());
    }

    /**
     * recompute the blocks
     */
    private void recomputeBlocks(Collection<? extends T> items, boolean clear) {
        if (clear) {
            listView.getItems().clear();
        }

        ArrayList<T> block = new ArrayList<>(getBlockSize());
        for (T item : items) {
            block.add(item);
            if (block.size() == getBlockSize()) {
                listView.getItems().add(block);
                block = new ArrayList<>(getBlockSize());
            }
        }
        if (block.size() > 0)
            listView.getItems().add(block);
    }


    private void precomputeSnapshots(boolean precompute) {
        if (precompute) {
            if (executorService == null) {
                executorService = Executors.newSingleThreadExecutor();
                listChangeListener = (e) -> {
                    while (e.next()) {
                        if (e.wasAdded()) {
                            for (T item : e.getAddedSubList()) {
                                if (!executorService.isShutdown()) {
                                    executorService.submit(() -> {
                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException ex) {
                                            return; // if tab is closed, this is where we exit the loop
                                        }
                                        if (isPrecomputeSnapshots()) {
                                            Platform.runLater(() ->
                                            {
                                                synchronized (item2node) {
                                                    if (!item2node.containsKey(item)) {
                                                        final Node snapshot = nodeProducer.apply(item);
                                                        item2node.put(item, snapshot);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                };
            }
            nodeProducerQueue.addListener(listChangeListener);
        }
    }

    /**
     * close the flow tree. Shuts down the background thread, if used
     */
    public void close() {
        if (executorService != null)
            executorService.shutdownNow();
        setSelectionModel(null);
    }

    public ObservableList<T> getItems() {
        return items;
    }

    public Map<T, Node> getItemNodeMap() {
        return new ReadOnlyMapWrapper<>(item2node);
    }

    public int getBlockSize() {
        return blockSize.get();
    }

    public IntegerProperty blockSizeProperty() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize.set(blockSize);
    }

    public double getVgap() {
        return Vgap.get();
    }

    public DoubleProperty vgapProperty() {
        return Vgap;
    }

    public void setVgap(double vgap) {
        this.Vgap.set(vgap);
    }

    public double getHgap() {
        return Hgap.get();
    }

    public DoubleProperty hgapProperty() {
        return Hgap;
    }

    public void setHgap(double hgap) {
        this.Hgap.set(hgap);
    }

    public Node getNode(T item) {
        return item2node.get(item);
    }

    public int size() {
        return size.get();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size;
    }

    public MultipleSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    public void setSelectionModel(MultipleSelectionModel<T> selectionModel) {
        if (this.selectionModel != null) {
            this.selectionModel.selectedItemProperty().removeListener(selectedItemListener);
        }
        this.selectionModel = selectionModel;
        if (selectionModel != null)
            this.selectionModel.selectedItemProperty().addListener(selectedItemListener);
    }

    public boolean isScrollToSelection() {
        return scrollToSelection.get();
    }

    /**
     * scroll to selection? Doesn't work well when block size is so big that flow pane wraps around
     *
     * @return
     */
    public BooleanProperty scrollToSelectionProperty() {
        return scrollToSelection;
    }

    public void setScrollToSelection(boolean scrollToSelection) {
        this.scrollToSelection.set(scrollToSelection);
    }

    public boolean isPrecomputeSnapshots() {
        return precomputeSnapshots.get();
    }

    public BooleanProperty precomputeSnapshotsProperty() {
        return precomputeSnapshots;
    }

    public void setPrecomputeSnapshots(boolean precomputeSnapshots) {
        this.precomputeSnapshots.set(precomputeSnapshots);
    }
}
