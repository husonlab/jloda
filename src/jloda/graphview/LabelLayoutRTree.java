/**
 * LabelLayoutRTree.java 
 * Copyright (C) 2016 Daniel H. Huson
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
package jloda.graphview;

import jloda.graph.Node;
import jloda.util.Basic;
import jloda.util.RTree;

import java.awt.*;

/**
 * layout labels using an RTree
 * Daniel Huson, 9.2012
 */
public class LabelLayoutRTree {
    /**
     * layout nodes for drawing
     *
     * @param graphView
     * @param gc
     */
    public void layout(GraphView graphView, Graphics gc) {
        Transform trans = graphView.trans;

        RTree<Node> rTree = new RTree<>();
        // add all nodes to avoid:
        Point center = new Point();
        int count = 0;
        for (Node v = graphView.getGraph().getFirstNode(); v != null; v = v.getNext()) {
            NodeView nv = graphView.getNV(v);
            String label = nv.getLabel();
            if (label != null && nv.getLabelVisible()) {
                nv.setLabelAngle(0);
                Rectangle bbox = nv.getBox(trans);
                //  if (nv.getHeight() > 1 || nv.getWidth() > 1) {
                rTree.add(bbox, v);
                //  }
                center.x += bbox.x + bbox.width / 2;
                center.y += bbox.y + bbox.height / 2;
                count++;
                if (count > 500)
                    return;
                if (nv.getLabelLayout() != NodeView.LAYOUT) {
                    Rectangle rect = nv.getLabelRect(trans);
                    if (rect != null)
                        rTree.add(rect, v);
                }
            }
        }
        if (count > 0) {
            center.x /= count;
            center.y /= count;
        }
        for (Node v = graphView.getGraph().getFirstNode(); v != null; v = v.getNext()) {
            NodeView nv = graphView.getNV(v);
            String label = nv.getLabel();
            if (label != null && nv.getLabelVisible() && nv.getLabelLayout() == NodeView.LAYOUT) {
                Dimension labelSize = Basic.getStringSize(gc, label, nv.getFont()).getSize();
                Point location = graphView.trans.w2d(nv.getLocation());
                boolean left = (location.x < center.x);
                // location.x += nv.getWidth() / 2;
                location.y -= labelSize.getHeight() / 2;
                location = rTree.addCloseTo(v.getId(), location, nv.getWidth() / 2, nv.getHeight() / 2, left, labelSize.getSize(), v);
                nv.setLabelPosition(location.x, location.y + labelSize.height, graphView.trans);
            }
        }
        //rTree.draw(gc);
        rTree.clear();
    }
}
