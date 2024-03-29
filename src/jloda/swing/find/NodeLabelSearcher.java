/*
 * NodeLabelSearcher.java Copyright (C) 2024 Daniel H. Huson
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

package jloda.swing.find;

import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloTree;
import jloda.swing.graphview.GraphView;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Class for finding and replacing node labels in a graph
 * Daniel Huson, 7.2008
 */
public class NodeLabelSearcher implements IObjectSearcher {
    private final String name;
    final Graph graph;
    final GraphView viewer;
    final Frame frame;
    protected Node current = null;

    final NodeSet toSelect;
    final NodeSet toDeselect;
    public static final String SEARCHER_NAME = "Nodes";

    /**
     * constructor
     *
	 */
    public NodeLabelSearcher(GraphView viewer) {
        this(null, SEARCHER_NAME, viewer);
    }

    /**
     * constructor
     *
	 */
    public NodeLabelSearcher(Frame frame, GraphView viewer) {
        this(frame, SEARCHER_NAME, viewer);
    }

    /**
     * constructor
     *
     * @param
	 */
    public NodeLabelSearcher(Frame frame, String name, GraphView viewer) {
        this.frame = frame;
        this.name = name;
        this.viewer = viewer;
        this.graph = viewer.getGraph();
        toSelect = new NodeSet(graph);
        toDeselect = new NodeSet(graph);
    }

    /**
     * get the parent component
     *
     * @return parent
     */
    public Component getParent() {
        return frame;
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
        return isCurrentSet() && viewer.getSelected(current);
    }

    /**
     * set selection state of current object
     *
	 */
    public void setCurrentSelected(boolean select) {
        if (current != null) {
            if (select)
                toSelect.add(current);
            else
                toDeselect.add(current);
        }
        if (select)
            viewer.setFoundNode(current);
        else
            viewer.setFoundNode(null);
    }

    /**
     * set select state of all objects
     *
	 */
    public void selectAll(boolean select) {
        viewer.selectAllNodes(select);
        viewer.repaint();
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
            return viewer.getLabel(current);
    }

    /**
     * set the label of the current object
     *
	 */
    public void setCurrentLabel(String newLabel) {
        if (current != null && !Objects.equals(newLabel, viewer.getLabel(current))) {
            if (newLabel == null || newLabel.length() == 0) {
                viewer.setLabel(current, null);
                if (viewer.getGraph() instanceof PhyloTree) {
                    viewer.getGraph().setLabel(current, null);
                }
            } else {
                viewer.setLabel(current, newLabel);
                if (viewer.getGraph() instanceof PhyloTree) {
                    viewer.getGraph().setLabel(current, newLabel);
                }

            }
            fireLabelChangedListeners(current);
        }
    }

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    public boolean isGlobalFindable() {
        return graph.getNumberOfNodes() > 0;
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    public boolean isSelectionFindable() {
        return viewer.getSelectedNodes().size() > 0;
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
        viewer.selectedNodes.addAll(toSelect);
        viewer.fireDoSelect(toSelect);
        Node v = toSelect.getLastElement();
        if (v != null) {
            final Point p = viewer.trans.w2d(viewer.getLocation(v));
            viewer.scrollRectToVisible(new Rectangle(p.x - 60, p.y - 25, 120, 50));

        }
        viewer.selectedNodes.removeAll(toDeselect);
        viewer.fireDoDeselect(toDeselect);
        toSelect.clear();
        toDeselect.clear();

        viewer.repaint();
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
    private void fireLabelChangedListeners(Node v) {
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

    /**
     * how many selected objects are there?
     *
     * @return number of objects or -1
     */
    public int numberOfSelectedObjects() {
        return viewer.getSelectedNodes().size();
    }

    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }
}
