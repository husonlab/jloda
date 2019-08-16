/*
 * EdgeLabelSearcher.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.find;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;
import jloda.graph.Graph;
import jloda.phylo.PhyloTree;
import jloda.swing.graphview.GraphView;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Class for finding and replacing edge labels in a graph
 * Daniel Huson, 7.2008
 */
public class EdgeLabelSearcher implements IObjectSearcher {
    private final Frame frame;
    private final String name;
    final Graph graph;
    final GraphView viewer;
    Edge current = null;

    final EdgeSet toSelect;
    final EdgeSet toDeselect;
    public static final String SEARCHER_NAME = "Edges";

    /**
     * constructor
     *
     * @param viewer
     */
    public EdgeLabelSearcher(GraphView viewer) {
        this(null, SEARCHER_NAME, viewer);
    }

    /**
     * constructor
     *
     * @param viewer
     */
    public EdgeLabelSearcher(Frame frame, GraphView viewer) {
        this(frame, SEARCHER_NAME, viewer);
    }

    /**
     * constructor
     *
     * @param name
     * @param viewer
     */
    public EdgeLabelSearcher(Frame frame, String name, GraphView viewer) {
        this.frame = frame;
        this.name = name;
        this.viewer = viewer;
        this.graph = viewer.getGraph();
        toSelect = new EdgeSet(graph);
        toDeselect = new EdgeSet(graph);
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
        current = graph.getFirstEdge();
        return isCurrentSet();
    }

    /**
     * goto the next object
     */
    public boolean gotoNext() {
        if (current == null)
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
        return isCurrentSet()
                && viewer.getSelected(current);
    }

    /**
     * set selection state of current object
     *
     * @param select
     */
    public void setCurrentSelected(boolean select) {
        if (current != null) {
            if (select)
                toSelect.add(current);
            else
                toDeselect.add(current);
        }
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select) {
        viewer.selectAllEdges(select);
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
     * @param newLabel
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
        return graph.getNumberOfEdges() > 0;
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    public boolean isSelectionFindable() {
        return false;
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
        viewer.selectedEdges.addAll(toSelect);
        viewer.fireDoSelect(toSelect);
        Edge edge = toSelect.getLastElement();
        if (edge != null) {
            final Point p = viewer.trans.w2d(viewer.getLocation(edge.getSource()));
            final Point q = viewer.trans.w2d(viewer.getLocation(edge.getTarget()));

            Rectangle rect = new Rectangle(p.x - 60, p.y - 25, 120, 50);
            rect.add(q);
            viewer.scrollRectToVisible(rect);
        }
        viewer.selectedEdges.removeAll(toDeselect);
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

    private final java.util.List labelChangedListeners = new LinkedList();

    /**
     * fire the label changed listener
     *
     * @param e
     */
    private void fireLabelChangedListeners(Edge e) {
        for (Object labelChangedListener : labelChangedListeners) {
            LabelChangedListener listener = (LabelChangedListener) labelChangedListener;
            listener.doLabelHasChanged(e);
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
        void doLabelHasChanged(Edge e);
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
        return viewer.getSelectedEdges().size();
    }

    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }
}
