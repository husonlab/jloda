/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package jloda.phylo;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.graphview.*;

import java.awt.*;

/**
 * draws a tree using parallel edges
 * Daniel Huson, 1.2007
 */
public class TreeDrawerAngled extends DefaultGraphDrawer implements IGraphDrawer {
    private final PhyloGraphView treeView;
    private final PhyloTree tree;

    final static public String DESCRIPTION = "Draw (rooted) trees using angled lines";

    /**
     * constructor
     *
     * @param graphView
     * @param graph
     */
    public TreeDrawerAngled(PhyloGraphView graphView, PhyloTree graph) {
        super(graphView);
        this.treeView = graphView;
        this.tree = graph;
        setupGraphView(graphView);
    }

    /**
     * setdup the graphview
     *
     * @param graphView
     */
    public void setupGraphView(GraphView graphView) {
        graphView.setAllowInternalEdgePoints(false);
        graphView.setMaintainEdgeLengths(true);
        graphView.setAllowMoveNodes(true);
        graphView.setAllowMoveInternalEdgePoints(false);
        graphView.setKeepAspectRatio(false);
        graphView.setAllowRotationArbitraryAngle(false);
        graphView.trans.setAngle(0);
    }

    /**
     * paint the graph. If rect is non-null, only need to cover rect
     *
     * @param graphics
     * @param rect
     */
    public void paint(Graphics graphics, Rectangle rect) {
        super.paint(graphics, rect);
    }

    /**
     * compute an embedding of the graph
     *
     * @param toScale if true, build to-scale embedding
     * @return true, if embedding was computed
     */
    public boolean computeEmbedding(boolean toScale) {
        if (tree.getNumberOfNodes() == 0)
            return true;

        treeView.removeAllInternalPoints();

        Node root = tree.getRoot();
        if (root == null)
            root = tree.getFirstNode();

        computeEmbeddingRec(root, null, 0, 0, toScale);

        return true;
    }

    /**
     * recursively compute the embedding
     *
     * @param v
     * @param e
     * @param hDistToRoot horizontal distance from node to root
     * @param leafNumber  rank of leaf in vertical ordering
     * @param toScale
     * @return index of last leaf
     */
    private int computeEmbeddingRec(Node v, Edge e, double hDistToRoot, int leafNumber, boolean toScale) {
        if (v.getDegree() == 1 && e != null)  // hit a leaf
        {
            treeView.setLocation(v, toScale ? hDistToRoot : 0, ++leafNumber);
        } else {
            int old = leafNumber + 1;
            for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
                if (f != e) {
                    Node w = f.getOpposite(v);
                    leafNumber = computeEmbeddingRec(w, f, hDistToRoot + tree.getWeight(f), leafNumber, toScale);
                }
            }
            double x;
            if (toScale)
                x = hDistToRoot;
            else
                x = -0.5 * (leafNumber - old);
            double y = 0.5 * (leafNumber + old);
            treeView.setLocation(v, x, y);
        }
        return leafNumber;
    }

    /**
     * get all nodes hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return nodes hit
     */
    public NodeSet getHitNodes(int x, int y) {
        return super.getHitNodes(x, y);
    }

    /**
     * get all nodes hit by mouse at (x,y) with tolerance of d pixels
     *
     * @param x
     * @param y
     * @param d tolerance
     * @return nodes hit
     */
    public NodeSet getHitNodes(int x, int y, int d) {
        return super.getHitNodes(x, y, d);
    }

    public NodeSet getHitNodeLabels(int x, int y) {
        return super.getHitNodeLabels(x, y);
    }

    /**
     * get all nodes contained in rect
     *
     * @param rect
     * @return nodes contained in rect
     */
    public NodeSet getHitNodes(Rectangle rect) {
        return super.getHitNodes(rect);
    }

    /**
     * get all node labels contained in rect
     *
     * @param rect
     * @return node labels contained in rect
     */
    public NodeSet getHitNodeLabels(Rectangle rect) {
        return null;
    }

    /**
     * get all edges hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return edges hits
     */
    public EdgeSet getHitEdges(int x, int y) {
        return super.getHitEdges(x, y);

    }

    /**
     * get all edge labels hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return edge labels
     */
    public EdgeSet getHitEdgeLabels(int x, int y) {
        return super.getHitEdgeLabels(x, y);
    }

    /**
     * get all edges contained in rect
     *
     * @param rect
     * @return edges contained in rect
     */
    public EdgeSet getHitEdges(Rectangle rect) {
        return super.getHitEdges(rect);
    }

    /**
     * get all edge labels contained in rect
     *
     * @param rect
     * @return edges contained in rect
     */
    public EdgeSet getHitEdgeLabels(Rectangle rect) {
        return super.getHitEdgeLabels(rect);
    }

    /**
     * set the default label positions for nodes and edges
     *
     * @param resetAll
     */
    public void resetLabelPositions(boolean resetAll) {
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext())
            treeView.setLabelLayout(v, NodeView.EAST);
        if (tree.getRoot() != null)
            treeView.setLabelLayout(tree.getRoot(), NodeView.WEST);
        for (Edge e = tree.getFirstEdge(); e != null; e = e.getNext())
            treeView.setLabelLayout(e, EdgeView.CENTRAL);

    }
}
