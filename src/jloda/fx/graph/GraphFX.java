/*
 *  GraphFX.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.graph;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.graph.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * provides observable list of nodes and adjacentEdges, and label properties
 * Daniel Huson, 1.20020
 */
public class GraphFX<G extends Graph> {
    private G graph;
    private final ObservableList<Node> nodeList = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<Node> readOnlyNodeList = new ReadOnlyListWrapper<>(nodeList);

    private final ObservableList<Edge> edgeList = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<Edge> readOnlyEdgeList = new ReadOnlyListWrapper<>(edgeList);
    private GraphUpdateListener graphUpdateListener;

    private final BooleanProperty empty = new SimpleBooleanProperty(true);

    private NodeArray<StringProperty> node2LabelProperty;
    private EdgeArray<StringProperty> edge2LabelProperty;

    private final AtomicBoolean updatingProperties = new AtomicBoolean(false);

    public GraphFX() {
    }

    public GraphFX(G graph) {
        setGraph(graph);
    }

    public G getGraph() {
        return graph;
    }

    public void setGraph(G graph) {
        if (this.graph != null && graphUpdateListener != null) {
            this.graph.removeGraphUpdateListener(graphUpdateListener);
        }

        if (graph != null) {
            graphUpdateListener = new GraphUpdateAdapter() {
                @Override
                public void newNode(Node v) {
                    Platform.runLater(() -> {
                        try {
                            nodeList.add(v);
                        } catch (NotOwnerException ignored) {
                        }
                    });
                }

                @Override
                public void deleteNode(Node v) {
                    Platform.runLater(() -> {
                        try {
                            nodeList.remove(v);
                        } catch (NotOwnerException ignored) {
                        }
                    });
                }

                @Override
                public void newEdge(Edge e) {
                    Platform.runLater(() -> {
                        try {
                            edgeList.add(e);
                        } catch (NotOwnerException ignored) {
                        }
                    });
                }

                @Override
                public void deleteEdge(Edge e) {
                    Platform.runLater(() -> {
                        try {
                            edgeList.remove(e);
                        } catch (NotOwnerException ignored) {
                        }
                    });
                }

                @Override
                public void nodeLabelChanged(Node v, String newLabel) {
                    try {
                        StringProperty stringProperty = node2LabelProperty.get(v);
                        if (stringProperty != null) {
                            Platform.runLater(() -> stringProperty.set(newLabel));
                        }
                    } catch (NotOwnerException ignored) {
                    }
                }

                @Override
                public void edgeLabelChanged(Edge e, String newLabel) {
                    try {
                        StringProperty stringProperty = edge2LabelProperty.get(e);
                        if (stringProperty != null) {
                            Platform.runLater(() -> stringProperty.set(newLabel));
                        }
                    } catch (NotOwnerException ignored) {
                    }
                }
            };
            graph.addGraphUpdateListener(graphUpdateListener);
            node2LabelProperty = new NodeArray<>(graph);
            edge2LabelProperty = new EdgeArray<>(graph);
        } else
            node2LabelProperty = null;

        empty.bind(Bindings.isEmpty(nodeList));

        this.graph = graph;
    }

    public ObservableList<Node> getNodeList() {
        return readOnlyNodeList;
    }

    public ObservableList<Edge> getEdgeList() {
        return readOnlyEdgeList;
    }

    public StringProperty nodeLabelProperty(Node v) {
        StringProperty stringProperty = node2LabelProperty.get(v);
        if (stringProperty == null) {
            stringProperty = new SimpleStringProperty(graph.getLabel(v));
            node2LabelProperty.put(v, stringProperty);
        }
        return stringProperty;
    }

    public StringProperty edgeLabelProperty(Edge e) {
        StringProperty stringProperty = edge2LabelProperty.get(e);
        if (stringProperty == null) {
            stringProperty = new SimpleStringProperty(graph.getLabel(e));
            edge2LabelProperty.put(e, stringProperty);
        }
        return stringProperty;
    }

    public boolean isEmpty() {
        return empty.get();
    }

    public ReadOnlyBooleanProperty emptyProperty() {
        return empty;
    }

    public boolean isUpdatingProperties() {
        return updatingProperties.get();
    }

    public void setUpdatingProperties(boolean updating) {
        updatingProperties.set(updating);
    }

    public boolean isNotUpdatingPropertiesAndSet() {
        return updatingProperties.compareAndSet(false, true);
    }
}

