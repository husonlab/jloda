/*
 * GraphSearcher.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

/*
 * GraphSearcher.java Copyright (C) 2020. Algorithms in Bioinformatics, University of Tuebingen
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
 *
 */

package jloda.fx.find;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import jloda.fx.control.ItemSelectionModel;
import jloda.fx.control.ZoomableScrollPane;
import jloda.graph.Graph;
import jloda.graph.Node;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * graph searcher
 * Daniel Huson, 2.2020
 */
public class GraphSearcher implements IObjectSearcher<Node> {
    private final ZoomableScrollPane scrollPane;
    private final ItemSelectionModel<Node> nodeSelection;
    private final Graph graph;
    private final Function<Node, Label> labelGetter;
    private final BiConsumer<Node, String> labelSetter;
    private Node which;
    private final ObjectProperty<Node> found = new SimpleObjectProperty<>();

    public GraphSearcher(ZoomableScrollPane scrollPane, Graph graph, ItemSelectionModel<Node> nodeSelection, Function<Node, Label> labelGetter, BiConsumer<Node, String> labelSetter) {
        this.scrollPane = scrollPane;
        this.graph = graph;
        this.nodeSelection = nodeSelection;
        this.labelGetter = labelGetter;
        if (labelSetter != null)
            this.labelSetter = labelSetter;
        else
            this.labelSetter = (v, t) -> labelGetter.apply(v).setText(t);
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
                } else {
                    nodeSelection.clearSelection(node);
                }
            });
        }
    }

    @Override
    public String getCurrentLabel() {
        if (which != null)
            return labelGetter.apply(which).getText();
        else
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
    public MultipleSelectionModel<Node> getSelectionModel() {
        return null;
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
        return new SimpleBooleanProperty(false);
    }

    @Override
    public void updateView() {
        if (which != null) {
            final Node node = which;
            runInFXApplicationThread(() -> scrollPane.ensureVisible(labelGetter.apply(node)));
        }
    }

    @Override
    public boolean canFindAll() {
        return true;
    }

    @Override
    public void selectAll(boolean select) {
        runInFXApplicationThread(() -> {
            if (select)
                nodeSelection.getSelectedItems().addAll(graph.getNodesAsSet());
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
