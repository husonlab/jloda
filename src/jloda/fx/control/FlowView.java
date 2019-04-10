/*
 *  Copyright (C) 2018. Daniel H. Huson
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

/*
 *  Copyright (C) 2019. Daniel H. Huson
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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * simple implementation of a flow view
 * Uses virtualization
 * Also allows background production of nodes. This is useful if computing a node for an item takes time
 * Daniel Huson, 4.2019
 */
public class FlowView<T> extends Pane implements Closeable {
    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final Map<T, Node> item2node = new HashMap<>();
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

    /**
     * constructor
     *
     * @param nodeProducer
     */
    public FlowView(final Function<T, Node> nodeProducer, boolean backgroundProduceNodes) {
        listView = new ListView<>();
        listView.setOrientation(Orientation.VERTICAL);
        listView.prefWidthProperty().bind(widthProperty());
        listView.prefHeightProperty().bind(heightProperty());
        getChildren().add(listView);
        listView.setSelectionModel(new EmptyMultipleSelectionModel<>());

        listView.setCellFactory(v -> new ListCell<ArrayList<T>>() {
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

        blockSize.addListener((c, o, n) -> {
            recomputeBlocks(items, true);
        });

        if (backgroundProduceNodes) {
            backgroundComputeNodes();
        }

        selectedItemListener = (observable, oldValue, newValue) -> {
            if (true) { // doesn't work very well
                if (item2node.get(newValue) != null && item2node.get(newValue).getParent().getUserData() instanceof ArrayList) {
                    final ArrayList<T> block = (ArrayList<T>) item2node.get(newValue).getParent().getUserData();
                    listView.scrollTo(block);
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

        ArrayList<T> array = new ArrayList<>(getBlockSize());
        for (T item : items) {
            array.add(item);
            if (array.size() == getBlockSize()) {
                listView.getItems().add(array);
                array = new ArrayList<>(getBlockSize());
            }
        }
        if (array.size() > 0)
            listView.getItems().add(array);
    }

    private ExecutorService executorService;

    /**
     * background computes snapshots
     */
    private void backgroundComputeNodes() {
        executorService = Executors.newSingleThreadExecutor();

        nodeProducerQueue.addListener((ListChangeListener<T>) (e) -> {
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
                                Platform.runLater(() ->
                                {
                                    synchronized (item2node) {
                                        if (!item2node.containsKey(item)) {
                                            final Node snapshot = nodeProducer.apply(item);
                                            item2node.put(item, snapshot);
                                        }
                                    }
                                });
                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * close the flow view. Shuts down the background thread, if used
     */
    public void close() {
        if (executorService != null)
            executorService.shutdownNow();
        setSelectionModel(null);
    }

    public ObservableList<T> getItems() {
        return items;
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
}
