/*
 * EdgeLabelSearcher.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import jloda.fx.control.ItemSelectionModel;
import jloda.graph.Edge;
import jloda.graph.Graph;
import jloda.phylo.PhyloSplitsGraph;

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;

/**
 * Class for finding and replacing edge labels in a labeled graph
 * Daniel Huson, 7.2008, 1.2018
 */
public class EdgeLabelSearcher implements IObjectSearcher<Edge> {
    private final String name;
    private Graph graph;
    private final ItemSelectionModel<Edge> edgeSelectionModel;
    private Edge current = null;

    private final ObjectProperty<Edge> found = new SimpleObjectProperty<>();

    private final BooleanProperty globalFindable = new SimpleBooleanProperty();
    private final BooleanProperty selectionReplaceable = new SimpleBooleanProperty();

    public static final String SEARCHER_NAME = "Edges";

    /**
     * constructor
     *
     * @param
	 */
    public EdgeLabelSearcher(PhyloSplitsGraph graph, ItemSelectionModel<Edge> edgeSelectionModel) {
        this(SEARCHER_NAME, graph, edgeSelectionModel);
    }

    /**
     * constructor
     *
     * @param
	 */
    public EdgeLabelSearcher(String name, Graph graph, ItemSelectionModel<Edge> edgeSelectionModel) {
        this.graph = graph;
        this.name = name;
        this.edgeSelectionModel = edgeSelectionModel;
        globalFindable.set(true); // todo: should listen for changes of graph
        selectionReplaceable.bind(Bindings.isNotEmpty(edgeSelectionModel.getSelectedItems()));
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
        current = graph.getFirstEdge();
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
        current = graph.getLastEdge();
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
        return isCurrentSet() && edgeSelectionModel.isSelected(current);
    }

    /**
     * set selection state of current object
     *
	 */
    public void setCurrentSelected(boolean select) {
        if (current != null) {
            final Edge toSelect = current;
            Platform.runLater(() -> {
                if (select) {
                    edgeSelectionModel.select(toSelect);
                    found.set(toSelect);
                } else {
                    edgeSelectionModel.clearSelection(toSelect);
                    found.set(null);
                }
            });
        }
    }

    /**
     * set select state of all objects
     *
	 */
    public void selectAll(boolean select) {
        Platform.runLater(() -> {
            if (select)
                edgeSelectionModel.selectItems(graph.getEdgesAsList());
            else
                edgeSelectionModel.clearSelection();
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

    @Override
    public Function<String, String> getPrepareTextForReplaceFunction() {
        return null;
    }

    /**
     * set the label of the current object
     *
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
	 */
    private void fireLabelChangedListeners(Edge v) {
        for (LabelChangedListener listener : labelChangedListeners) {
            listener.doLabelHasChanged(v);
        }
    }

    /**
     * add a label changed listener
     *
	 */
    public void addLabelChangedListener(LabelChangedListener listener) {
        labelChangedListeners.add(listener);
    }

    /**
     * remove a label changed listener
     *
	 */
    public void removeLabelChangedListener(LabelChangedListener listener) {
        labelChangedListeners.remove(listener);
    }

    /**
     * label changed listener
     */
    public interface LabelChangedListener {
        void doLabelHasChanged(Edge v);
    }

    /**
     * how many objects are there?
     *
     * @return number of objects or -1
     */
    public int numberOfObjects() {
        return graph.getNumberOfEdges();
    }

    /**
     * how many selected objects are there?
     *
     * @return number of objects or -1
     */
    public int numberOfSelectedObjects() {
        return edgeSelectionModel.size();
    }

    public Edge getFound() {
        return found.get();
    }

    public ReadOnlyObjectProperty<Edge> foundProperty() {
        return found;
    }

    public ItemSelectionModel<Edge> getSelectionModel() {
        return edgeSelectionModel;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
