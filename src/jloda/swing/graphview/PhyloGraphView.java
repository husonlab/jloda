/*
 * PhyloGraphView.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.graphview;

import jloda.graph.*;
import jloda.phylo.PhyloSplitsGraph;
import jloda.phylo.PhyloTree;
import jloda.swing.util.Geometry;
import jloda.util.Basic;
import jloda.util.Pair;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.*;

/**
 * PhyloGraph tree
 * Daniel Huson, 2002
 */

public class PhyloGraphView extends GraphView {
    private boolean useSplitSelectionModel = true;
    private boolean inEdgeClickSelection = false;

    /**
     * Constructs a tree of a phylogenetic graph, setting
     * window width and height to 400.
     *
     * @param phyloGraph the PhyloGraph
     */
    public PhyloGraphView(PhyloSplitsGraph phyloGraph) {
        this(phyloGraph, 400, 400);
    }

    /**
     * construcs a phylogenetic graphview initialized to an empty graph
     */
    public PhyloGraphView() {
        this(new PhyloSplitsGraph(), 400, 400);
    }

    /**
     * Constructs a tree of a phylogentic G.
     *
     * @param phyloGraph the PhyloGraph
     * @param w          the width
     * @param h          the height
     */
    public PhyloGraphView(PhyloSplitsGraph phyloGraph, int w, int h) {
        super(phyloGraph, w, h);

        setDefaultNodeLocation(0, 0);
        setDefaultNodeBackgroundColor(Color.BLACK);
        setDefaultNodeColor(Color.BLACK);
        setDefaultEdgeDirection(EdgeView.UNDIRECTED);
        setDefaultNodeLabelLayout(NodeView.LAYOUT);

        setMaintainEdgeLengths(true);
        setAllowEditEdgeLabelsOnDoubleClick(true);
        setAllowEditEdgeLabelsOnDoubleClick(true);
        setAllowEditNodeLabelsOnDoubleClick(true);
        setAllowEditNodeLabelsOnDoubleClick(true);
        // setAllowRubberbandEdges(false);

        resetViews();

        // this takes care of the split selection mode: whenever an edge is clicked on,
        // we select all edges of the same split and also all nodes on one side of the split
        addEdgeActionListener(new EdgeActionAdapter() {
            public void doClick(EdgeSet edges, int numClicks) {
                inEdgeClickSelection = true;
            }

            public void doRelease(EdgeSet edges) {
                inEdgeClickSelection = false;
            }

            public void doSelect(EdgeSet edges) {
                if (inEdgeClickSelection && getUseSplitSelectionModel() && edges.size() == 1) // exactly one edge selected, select split side
                {
                    final Edge e = edges.iterator().next();
                    try {
                        int splitId = getPhyloGraph().getSplit(e);
                        if (splitId == 0)
                            return;
                        selectAllNodes(false);
                        selectAllEdges(false);
                        // todo: for this to work for reticulate networks, reticulate edges
                        // must be oriented toward the reticulation node
                        selectGraphComponent(getGraph().getTarget(e), splitId);
                        if (2 * getSelectedNodes().size() > getGraph().getNumberOfNodes()) {
                            // invert selection of nodes:
                            for (Node v = getGraph().getFirstNode(); v != null; v = getGraph().getNextNode(v)) {
                                if (getSelected(v))
                                    selectedNodes.remove(v);
                                else
                                    selectedNodes.add(v);
                            }
                        }

                    } catch (NotOwnerException ex) {
                        jloda.util.Basic.caught(ex);
                    }
                }
            }
        });
    }

    /**
     * Constructs a tree of a phylogentic tree.
     *
     * @param tree PhyloTree
     */
    public PhyloGraphView(PhyloTree tree) {
        this(tree, false);
    }

    /**
     * Constructs a tree of a phylogentic tree.
     *
     * @param tree PhyloTree
     */
    public PhyloGraphView(PhyloTree tree, boolean computeEmbedding) {
        this(tree, 400, 400);
        setDefaultNodeLocation(0, 0);
        setMaintainEdgeLengths(true);

        try {
            for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v))
                setLabel(v, tree.getLabel(v));
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
        //System.err.print("embedding:");
        if (computeEmbedding)
            embed();
        //System.err.println("done");
    }


    /**
     * selects all nodes and edges on the smaller part of the split
     *
     * @param v  the start node
     * @param id the split id
     */
    private void selectGraphComponent(Node v, int id) {
        try {
            if (!getSelected(v)) {
                selectedNodes.add(v);  // don't use setSelected, infinite loop!

                for (Edge e = getGraph().getFirstAdjacentEdge(v); e != null; e = getGraph().getNextAdjacentEdge(e, v)) {
                    if (!getSelected(e)) {
                        if (getPhyloGraph().getSplit(e) == id)
                            selectedEdges.add(e);
                        else {
                            Node w = getGraph().getOpposite(v, e);
                            selectGraphComponent(w, id);
                        }
                    }
                }
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * update tree of nodes and edges
     */
    public void resetViews() {
        PhyloSplitsGraph G = (PhyloSplitsGraph) getGraph();

        for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
            setLabel(v, G.getLabel(v));
            //setShape(v, NodeView.NONE_NODE);
            /*
        if (G.getLabel(v) != null && G.getLabel(v).equals("") == false)
            setShape(v, NodeView.OVAL_NODE);
        else
            setShape(v, NodeView.NONE_NODE);
            */

        }
        for (Edge e = G.getFirstEdge(); e != null; e = G.getNextEdge(e)) {
            setLabel(e, G.getLabel(e));
            //setDirection(e, EdgeView.UNDIRECTED);
        }
    }

    /**
     * returns the phylograph  associated with this phylographview
     *
     * @return the phylograph
     */
    public PhyloSplitsGraph getPhyloGraph() {
        return (PhyloSplitsGraph) super.getGraph();
    }

    /**
     * Select all nodes labeled
     */
    public void selectAllLabeledNodes() {
        selectedNodes.clear();
        for (Node v = getGraph().getFirstNode(); v != null; v = v.getNext()) {
            if (getPhyloGraph().getTaxa(v) != null && getLabel(v) != null && getLabel(v).length() > 0)
                selectedNodes.add(v);
        }
    }

    /**
     * select all nodes labeled by any of the given taxa set
     *
     * @param taxaSet collection of taxa
     */
    public void selectNodesLabeledByTaxa(BitSet taxaSet) {
        selectedNodes.clear();
        if (taxaSet.cardinality() == 0)
            return;
        int count = 0;

        doubleLoop:
        for (Node v : getPhyloGraph().nodes()) {
            for (Integer t : getPhyloGraph().getTaxa(v)) {
                if (taxaSet.get(t)) {
                    selectedNodes.add(v);
                    if (++count == taxaSet.cardinality())
                        break doubleLoop;
                    break;
                }
            }
        }
        fireDoSelect(selectedNodes);
    }

    /**
     * are we using the split selection model?
     *
     * @return boolean
     */
    public boolean getUseSplitSelectionModel() {
        return useSplitSelectionModel;
    }

    /**
     * set or unset use of split selection model
     *
     * @param useSplitSelectionModel
     */
    public void setUseSplitSelectionModel(boolean useSplitSelectionModel) {
        this.useSplitSelectionModel = useSplitSelectionModel;
    }

    /**
     * removes the given split from the graph by contracting all edges representing
     * the split
     *
     * @param splitId
     */
    public void removeSplit(int splitId, boolean updateNodePositions) {
        PhyloSplitsGraph graph = getPhyloGraph();
        try {
            Node one = graph.getTaxon2Node(1);
            if (one != null) {
                List<Pair<Node, Edge>> separators = new LinkedList<>();
                graph.getAllSeparators(splitId, one, null, new NodeSet(graph), separators);
                if (updateNodePositions) {
                        // move all the nodes on one side of the split:
                    Pair separator = separators.get(0);
                        Node v = (Node) separator.getFirst();
                        Edge e = (Edge) separator.getSecond();
                        Node w = graph.getOpposite(v, e);
                        Point2D offset = Geometry.diff(getLocation(w), getLocation(v));
                        Point2D oneSideOffset = new Point2D.Double(0.5 * offset.getX(), 0.5 * offset.getY());
                        moveNodes(splitId, oneSideOffset, v, null, new NodeSet(graph));
                        Point2D otherSideOffset = new Point2D.Double(-0.5 * offset.getX(), -0.5 * offset.getY());
                        moveNodes(splitId, otherSideOffset, w, null, new NodeSet(graph));
                    }

                    // remove the split in the graph
                    graph.removeSplit(splitId);

                    // update labels:

                for (Pair<Node, Edge> separator : separators) {
                    Node u = separator.getFirst();
                        setLabel(u, graph.getLabel(u));
                    }
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * moves all nodes that are reachable without using an edge of the given splitId.
     *
     * @param splitId forbidden splitId
     * @param offset  move nodes by this amount
     * @param v       current node
     * @param e       current edge
     * @param seen    set of nodes already visited
     * @throws NotOwnerException
     */
    private void moveNodes(int splitId, Point2D offset, Node v, Edge e, NodeSet seen) throws NotOwnerException {
        if (!seen.contains(v)) {
            seen.add(v);
            Point2D origLocation = getLocation(v);
            Point2D newLocation = new Point2D.Double(origLocation.getX() + offset.getX(), origLocation.getY() + offset.getY());
            setLocation(v, newLocation);
            PhyloSplitsGraph graph = getPhyloGraph();
            for (Edge f = graph.getFirstAdjacentEdge(v); f != null; f = graph.getNextAdjacentEdge(f, v)) {
                if (f != e) {
                    if (graph.getSplit(f) != splitId)
                        moveNodes(splitId, offset, graph.getOpposite(v, f), f, seen);
                }
            }
        }
    }

    /**
     * Writes a tree in bracket notation
     *
     * @param wgts write edge weights or not
     */
    public String getNewick(boolean wgts) {
        if (getPhyloGraph().getNumberOfEdges() != getPhyloGraph().getNumberOfNodes() - 1
                || getPhyloGraph().getNumberConnectedComponents() != 1)
            return null; // graph is not a tree

        StringWriter out = new StringWriter();
        if (getPhyloGraph().getNumberOfEdges() > 0) {
            try {
                boolean ok;

                NodeSet seen = new NodeSet(getGraph());

                Edge e = getPhyloGraph().getFirstEdge();
                Node v = getPhyloGraph().getSource(e);
                Node u = getPhyloGraph().getTarget(e);
                out.write("(");
                ok = writeRec(seen, out, v, e, wgts);
                if (wgts) {
                    double weight = getPhyloGraph().getWeight(e);
                    try {
                        weight = Double.parseDouble(this.getLabel(e));
                    } catch (Exception ex) {
                    }
                    out.write(":" + (float) (weight / 2.0));
                }
                out.write(",");
                if (ok)
                    ok = writeRec(seen, out, u, e, wgts);
                if (wgts) {
                    double weight = getPhyloGraph().getWeight(e);
                    try {
                        weight = Double.parseDouble(this.getLabel(e));
                    } catch (Exception ex) {
                    }

                    out.write(":" + (float) (weight / 2.0) + "):0;");
                } else
                    out.write(");");
                if (!ok || seen.size() != getPhyloGraph().getNumberOfNodes())
                    return null;
            } catch (IOException ex) {
                Basic.caught(ex);
                return null;
            }
        } else if (getPhyloGraph().getNumberOfNodes() == 1) {
            out.write("(" + this.getLabel(getPhyloGraph().getFirstNode()) + ");");
        } else
            out.write("();");
        return out.toString();
    }

    /**
     * Recursively writes a tree in bracket notation
     *
     * @param out  Writer
     * @param r    Node
     * @param e    Edge
     * @param wgts boolean
     */
    private boolean writeRec(NodeSet seen, Writer out, Node r, Edge e, boolean wgts)
            throws IOException {
        if (seen.contains(r))
            return false;
        seen.add(r);

        if (getPhyloGraph().getDegree(r) == 1) {
            out.write(this.getLabel(r));
        } else // degree >=2
        {
            if (this.getLabel(r) != null && this.getLabel(r).length() > 0)
                out.write(this.getLabel(r) + ",");
            boolean first = true;
            out.write("(");
            for (Edge f : r.adjacentEdges()) {
                if (f != e) {
                    if (first)
                        first = false;
                    else
                        out.write(",");

                    Node v = getPhyloGraph().getOpposite(r, f);
                    if (!writeRec(seen, out, v, f, wgts))
                        return false;
                    if (wgts) {
                        double weight = getPhyloGraph().getWeight(f);
                        try {
                            weight = Double.parseDouble(this.getLabel(f));
                        } catch (Exception ex) {
                        }
                        out.write(":" + (float) (weight));
                    }
                }
            }
            out.write(")");
        }
        return true;
    }


    /**
     * Embeds the tree in linear time.
     */
    public void embed() {
        Graph G = getGraph();
        if (G.getNumberOfNodes() == 0)
            return;

        {
            Node root = G.getFirstNode();
            NodeSet leaves = new NodeSet(G);

            try {
                for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                    if (G.getDegree(v) == 1)
                        leaves.add(v);
                    if (G.getDegree(v) > G.getDegree(root))
                        root = v;
                }

                // recursively visit all nodes in the tree and determine the
                // angle 0-2PI of each edge. nodes are placed around the unit
                // circle at position
                // n=1,2,3,... and then an edge along which we visited nodes
                // k,k+1,...j-1,j is directed towards positions k,k+1,...,j

                EdgeDoubleArray angle = new EdgeDoubleArray(G); // angle of edge
                Random rand = new Random();
                rand.setSeed(1);
                int seen = setAnglesRec(0, root, null, leaves, angle, rand);

                // rotate all edges so that taxon number 1 appears on the right:
                Node v = getPhyloGraph().getTaxon2Node(1);
                if (v != null) {
                    Edge e = v.getFirstAdjacentEdge();
                    if (e != null) {
                        double alpha = angle.get(e);
                        for (Edge f = getGraph().getFirstEdge(); f != null; f = f.getNext()) {
                            angle.set(f, angle.get(f) - alpha);
                        }
                    }
                }

                if (seen != leaves.size())
                    System.err.println("Warning: Number of nodes seen: " + seen +
                            " != Number of leaves: " + leaves.size());

                // recursively compute node coordinates from edge angles:
                setCoordsRec(root, null, angle);
            } catch (NotOwnerException ex) {
                Basic.caught(ex);
            }
        }
    }

    /**
     * Recursively determines the angle of every tree edge.
     *
     * @param num    int
     * @param root   Node
     * @param entry  Edge
     * @param leaves NodeSet
     * @param angle  EdgeDoubleArray
     * @param rand   Random
     * @return b int
     */

    private int setAnglesRec(int num, Node root, Edge entry, NodeSet leaves, EdgeDoubleArray angle, Random rand) throws NotOwnerException {
        Graph G = getGraph();

        if (leaves.contains(root))
            return num + 1;
        else {
            int a = num; // is number of nodes seen so far
            int b = 0;     // number of nodes after visiting subtree

            for (Edge e : root.adjacentEdges()) {
                if (e != entry) {
                    b = setAnglesRec(a, G.getOpposite(root, e), e, leaves, angle, rand);

                    // point towards the segment of the unit circle a...b:
                    angle.set(e, Math.PI * (a + b) / leaves.size());

                    a = b;
                }
            }
            if (b == 0)
                System.err.println("Warning: setAnglesRec: recursion failed");
            return b;
        }
    }

    /**
     * recursively compute node coordinates from edge angles:
     *
     * @param root  Node
     * @param entry Edge
     * @param angle EdgeDouble
     */

    private void setCoordsRec(Node root, Edge entry, EdgeDoubleArray angle) {
        final Graph G = getGraph();

        for (Edge e : root.adjacentEdges()) {
            if (e != entry) {
                Node v = G.getOpposite(root, e);

                // translate in the computed direction by the given amount
                setLocation(v, Geometry.translateByAngle(getLocation(root), angle.get(e), ((PhyloTree) G).getWeight(e)));

                setCoordsRec(v, e, angle);
            }
        }
    }

    /**
     * gets the set of selected node labels
     *
     * @return selected node labels
     */
    public Set<String> getSelectedNodeLabels() {
        Set<String> selectedLabels = new HashSet<>();
        for (Node v = getSelectedNodes().getFirstElement(); v != null; v = getSelectedNodes().getNextElement(v))
            if (getPhyloGraph().getLabel(v) != null)
                selectedLabels.add(getPhyloGraph().getLabel(v));
        return selectedLabels;

    }

    /**
     * contract all given edges
     *
     * @param edges
     * @return number of edges successfully removed
     */
    public boolean contractAll(Set<Edge> edges) {
        boolean result = false;
        final PhyloSplitsGraph graph = getPhyloGraph();
        final Set<Node> diVertices = new HashSet<>();
        while (edges.size() > 0) {
            final Edge e = edges.iterator().next();
            edges.remove(e);
            if (!graph.isSpecial(e) && e.getTarget().getOutDegree() > 0) {
                final Node v = e.getSource();
                final Node w = e.getTarget();
                if (w.getOutDegree() == 0) {
                    if (graph.getLabel(w) != null && graph.getLabel(w).length() > 0) {
                        if (graph.getLabel(v) == null || graph.getLabel(v).length() == 0)
                            graph.setLabel(v, graph.getLabel(w));
                        else {
                            graph.setLabel(v, graph.getLabel(v) + "+" + graph.getLabel(w));
                        }
                    }
                    graph.deleteEdge(e);
                } else {
                    for (Edge f = w.getFirstOutEdge(); f != null; f = w.getNextOutEdge(f)) {
                        final Edge h = graph.newEdge(v, f.getTarget());
                        graph.setWeight(h, graph.getWeight(f));
                        graph.setConfidence(h, graph.getConfidence(f));
                        if (edges.remove(f))
                            edges.add(h);
                        result = true;
                    }
                    diVertices.remove(w);
                    graph.deleteNode(w);
                    if (v.getInDegree() == 1 && v.getOutDegree() == 1)
                        diVertices.add(v);
                }
            }
        }

        for (Node v : diVertices)
        {
            Edge f = graph.newEdge(v.getFirstInEdge().getSource(), v.getFirstOutEdge().getTarget());
            graph.setWeight(f, graph.getWeight(v.getFirstInEdge()) + graph.getWeight(v.getFirstOutEdge()));
            graph.setConfidence(f, 0.5 * (graph.getConfidence(v.getFirstInEdge()) + graph.getConfidence(v.getFirstOutEdge())));
        }
        return result;
    }
}

// EOF
