/*
 * NodeLabelSearcher.java Copyright (C) 2019. Daniel H. Huson
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
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.control.MultipleSelectionModel;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.phylo.PhyloSplitsGraph;

import java.util.LinkedList;
import java.util.Objects;

/**
 * Class for finding and replacing node labels in a labeled graph
 * Daniel Huson, 7.2008, 1.2018
 */
public class NodeLabelSearcher implements IObjectSearcher<Node> {
    private final String name;
    private Graph graph;
    private final AMultipleSelectionModel<Node> nodeSelectionModel;
    private Node current = null;

    private final ObjectProperty<Node> found = new SimpleObjectProperty<>();

    private final BooleanProperty globalFindable = new SimpleBooleanProperty();
    private final BooleanProperty selectionReplaceable = new SimpleBooleanProperty();

    public static final String SEARCHER_NAME = "Nodes";

    /**
     * constructor
     *
     * @param
     * @param graph
     */
    public NodeLabelSearcher(PhyloSplitsGraph graph, AMultipleSelectionModel<Node> nodeSelectionModel) {
        this(SEARCHER_NAME, graph, nodeSelectionModel);
    }

    /**
     * constructor
     *
     * @param
     * @param graph
     */
    public NodeLabelSearcher(String name, Graph graph, AMultipleSelectionModel<Node> nodeSelectionModel) {
        this.graph = graph;
        this.name = name;
        this.nodeSelectionModel = nodeSelectionModel;
        globalFindable.set(true); // todo: should listen for graphs of graph
        selectionReplaceable.bind(Bindings.isNotEmpty(nodeSelectionModel.getSelectedItems()));
    }

    /**
     * get the name for this type of search
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * goto the first object
     */
    public boolean gotoFirst() {
        current = graph.getFirstNode();
        return isCurrentSet();
    }

    /**
     * goto the next object
     */
    public boolean gotoNext() {
        if (current == null || current.getOwner() == null)
            gotoFirst();
        else
            current = current.getNext();
        return isCurrentSet();
    }

    /**
     * goto the last object
     */
    public boolean gotoLast() {
        current = graph.getLastNode();
        return isCurrentSet();
    }

    /**
     * goto the previous object
     */
    public boolean gotoPrevious() {
        if (current == null)
            gotoLast();
        else
            current = current.getPrev();
        return isCurrentSet();
    }

    /**
     * is the current object selected?
     *
     * @return true, if selected
     */
    public boolean isCurrentSelected() {
        return isCurrentSet() && nodeSelectionModel.getSelectedItems().contains(current);
    }

    /**
     * set selection state of current object
     *
     * @param select
     */
    public void setCurrentSelected(boolean select) {
        if (current != null) {
            final Node toSelect = current;
            Platform.runLater(() -> {
                if (select) {
                    nodeSelectionModel.select(toSelect);
                    found.set(toSelect);
                } else {
                    nodeSelectionModel.clearSelection(toSelect);
                    found.set(null);
                }
            });
        }
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select) {
        Platform.runLater(() -> {
            if (select)
                nodeSelectionModel.selectAll();
            else
                nodeSelectionModel.clearSelection();
        });
    }

    /**
     * get the label of the current object
     *
     * @return label
     */
    public String getCurrentLabel() {
        if (current == null)
            return null;
        else
            return graph.getLabel(current);
    }

    /**
     * set the label of the current object
     *
     * @param newLabel
     */
    public void setCurrentLabel(String newLabel) {
        if (current != null && !Objects.equals(newLabel, graph.getLabel(current))) {
            if (newLabel == null || newLabel.length() == 0) {
                graph.setLabel(current, null);
            } else {
                graph.setLabel(current, newLabel);
            }
            fireLabelChangedListeners(current);
        }
    }

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    public ReadOnlyBooleanProperty isGlobalFindable() {
        return globalFindable;
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    public ReadOnlyBooleanProperty isSelectionFindable() {
        return selectionReplaceable;
    }

    /**
     * is the current object set?
     *
     * @return true, if set
     */
    public boolean isCurrentSet() {
        return current != null;
    }

    /**
     * something has been changed or selected, update tree
     */
    public void updateView() {
    }

    /**
     * does this searcher support find all?
     *
     * @return true, if find all supported
     */
    public boolean canFindAll() {
        return true;
    }

    private final java.util.List<LabelChangedListener> labelChangedListeners = new LinkedList<>();

    /**
     * fire the label changed listener
     *
     * @param v
     */
    private void fireLabelChangedListeners(Node v) {
        for (LabelChangedListener listener : labelChangedListeners) {
            listener.doLabelHasChanged(v);
        }
    }

    /**
     * add a label changed listener
     *
     * @param listener
     */
    public void addLabelChangedListener(LabelChangedListener listener) {
        labelChangedListeners.add(listener);
    }

    /**
     * remove a label changed listener
     *
     * @param listener
     */
    public void removeLabelChangedListener(LabelChangedListener listener) {
        labelChangedListeners.remove(listener);
    }

    /**
     * label changed listener
     */
    public interface LabelChangedListener {
        void doLabelHasChanged(Node v);
    }

    /**
     * how many objects are there?
     *
     * @return number of objects or -1
     */
    public int numberOfObjects() {
        return graph.getNumberOfNodes();
    }


    public Node getFound() {
        return found.get();
    }

    public ReadOnlyObjectProperty<Node> foundProperty() {
        return found;
    }

    @Override
    public MultipleSelectionModel<Node> getSelectionModel() {
        return nodeSelectionModel;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
