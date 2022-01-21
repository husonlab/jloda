/*
 * GraphSearcher.java Copyright (C) 2022 Daniel H. Huson
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
import javafx.beans.property.*;
import jloda.fx.control.ItemSelectionModel;
import jloda.graph.Graph;
import jloda.graph.Node;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * graph searcher
 * Daniel Huson, 2.2020
 */
public class GraphSearcher implements IObjectSearcher<Node> {
    private final ItemSelectionModel<Node> nodeSelection;
    private Graph graph;
    private final Function<Node, String> labelGetter;
    private final BiConsumer<Node, String> labelSetter;

    private Node which;
    private final ObjectProperty<Node> found = new SimpleObjectProperty<>();

    public GraphSearcher(Graph graph, ItemSelectionModel<Node> nodeSelection, Function<Node, String> labelGetter, BiConsumer<Node, String> labelSetter) {
        this.graph = graph;
        this.nodeSelection = nodeSelection;
        this.labelGetter = labelGetter;
        this.labelSetter = labelSetter;
    }

    @Override
    public boolean gotoFirst() {
        which = graph.getFirstNode();
        return which != null;
    }

    @Override
    public boolean gotoNext() {
        if (which != null)
            which = graph.getNextNode(which);
        return which != null;
    }

    @Override
    public boolean gotoLast() {
        which = graph.getLastNode();
        return which != null;
    }

    @Override
    public boolean gotoPrevious() {
        if (which != null)
            which = graph.getPrevNode(which);
        return which != null;
    }

    @Override
    public boolean isCurrentSet() {
        return which != null;
    }

    @Override
    public boolean isCurrentSelected() {
        return which != null && nodeSelection.isSelected(which);
    }

    @Override
    public void setCurrentSelected(boolean select) {
        if (which != null) {
            final Node node = which;
            runInFXApplicationThread(() -> {
                if (select) {
                    nodeSelection.select(node);
                    found.set(node);
                } else {
                    nodeSelection.clearSelection(node);
                }
            });
        }
    }

    @Override
    public String getCurrentLabel() {
        if (which != null)
            return labelGetter.apply(which);
        else
            return null;
    }

    @Override
    public Function<String, String> getPrepareTextForReplaceFunction() {
        return null;
    }

    @Override
    public void setCurrentLabel(String newLabel) {
        if (which != null) {
            final Node node = which;
            runInFXApplicationThread(() -> labelSetter.accept(node, newLabel));
        }
    }

    @Override
    public int numberOfObjects() {
        return graph.getNumberOfNodes();
    }

    @Override
    public ReadOnlyObjectProperty<Node> foundProperty() {
        return found;
    }

    @Override
    public String getName() {
        return "Graph find";
    }

    @Override
    public ReadOnlyBooleanProperty isGlobalFindable() {
        return new SimpleBooleanProperty(true);
    }

    @Override
    public ReadOnlyBooleanProperty isSelectionFindable() {
        return new SimpleBooleanProperty(true);
    }

    @Override
    public void updateView() {
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        if (graph == null)
            throw new NullPointerException("graph");
        which = null;
        this.graph = graph;
    }

    @Override
    public boolean canFindAll() {
        return true;
    }

    @Override
    public void selectAll(boolean select) {
        runInFXApplicationThread(() -> {
            if (select)
                nodeSelection.selectItems(graph.getNodesAsList());
            else
                nodeSelection.clearSelection();
        });
    }

    private static void runInFXApplicationThread(Runnable runnable) {
        if (Platform.isFxApplicationThread())
            runnable.run();
        else
            Platform.runLater(runnable);
    }
}
