/*
 * PhyloTreeView.java Copyright (C) 2019. Daniel H. Huson
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
import jloda.phylo.PhyloTree;
import jloda.swing.util.Geometry;
import jloda.util.Pair;

import java.util.*;

/**
 *  tree viewer
 *  Daniel Huson, 2000
 */

public class PhyloTreeView extends GraphView {

    /**
     * Constructs a tree of a phylogentic tree.
     *
     * @param tree PhyloTree
     */
    public PhyloTreeView(PhyloTree tree) {
        this(tree, 400, 400);
    }

    /**
     * Constructs a tree of a phylogentic tree.
     *
     * @param tree        PhyloTree
     * @param doEmbedding compute an embedding of the tree?
     */
    public PhyloTreeView(PhyloTree tree, boolean doEmbedding) {
        this(tree, 400, 400, doEmbedding);
    }

    /**
     * Constructs a tree of a phylogentic tree. Computes an embedding of the tree.
     *
     * @param tree PhyloTree
     * @param w    int
     * @param h    int
     */
    public PhyloTreeView(PhyloTree tree, int w, int h) {
        this(tree, w, h, true);

    }

    /**
     * Constructs a tree of a phylogentic tree. Optinally computes an embedding of the tree.
     *
     * @param tree        PhyloTree
     * @param w           int
     * @param h           int
     * @param doEmbedding
     */
    public PhyloTreeView(PhyloTree tree, int w, int h, boolean doEmbedding) {
        super(tree, w, h);
        setDefaultNodeLocation(0, 0);
        setMaintainEdgeLengths(true);

        resetViews();

        if (getGraph().getNumberOfNodes() != 0 && doEmbedding) {
            System.err.print("embedding:");
            embed();
            System.err.println("done");
        }
    }

    /**
     * Embeds the tree in linear time.
     */
    public void embed() {
        Graph G = getGraph();
        if (G.getNumberOfNodes() == 0)
            return;

        // synchronized(G)
        {
            Node root = G.getFirstNode();
            NodeSet leaves = new NodeSet(G);

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

            if (seen != leaves.size())
                System.err.println("Warning: Number of nodes seen: " + seen +
                        " != Number of leaves: " + leaves.size());

            // recursively compute node coordinates from edge angles:
            setCoordsRec(root, null, angle);
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

    private int setAnglesRec(int num, Node root, Edge entry, NodeSet leaves, EdgeDoubleArray angle, Random rand) {
        final Graph G = getGraph();

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
                setLocation(v,
                        Geometry.translateByAngle(getLocation(root), angle.get(e), ((PhyloTree) G).getWeight(e)));

                setCoordsRec(v, e, angle);
            }
        }
    }

    /**
     * show or hide labels of set of nodes
     *
     * @param nodes
     * @param show
     */
    public void showLabels(NodeSet nodes, boolean show) {
        for (Node v = nodes.getFirstElement(); v != null; v = nodes.getNextElement(v)) {
            setLabelVisible(v, show);
        }
    }

    /**
     * update tree of nodes and edges
     */
    public void resetViews() {
        PhyloTree G = (PhyloTree) getGraph();

        for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
            setLabel(v, G.getLabel(v));
            //setShape(v, NodeView.NONE_NODE);

            if (G.getLabel(v) != null && !G.getLabel(v).equals("")) {
                setNodeShape(v, NodeShape.Oval);
                setLabelLayout(v, NodeView.LAYOUT);
                setWidth(v, 1);
                setHeight(v, 1);
            } else
                setNodeShape(v, NodeShape.None);

        }
        for (Edge e = G.getFirstEdge(); e != null; e = G.getNextEdge(e)) {
            setLabel(e, G.getLabel(e));
            setDirection(e, EdgeView.UNDIRECTED);
        }
    }

    /**
     * get the tree induced by the given selection of nodes
     *
     * @return induced tree or null
     * @selected
     */
    public PhyloTree getInducedTree(Map<Integer, String> id2name, NodeSet selected) {
        if (getNumberSelectedNodes() > 0) {
            PhyloTree tarTree = new PhyloTree();
            Node root = getInducedTreeRec(id2name, selected, getPhyloTree().getRoot(), tarTree);
            if (root != null) {
                tarTree.setRoot(root);

                while (false && root != null && root.getOutDegree() == 1)  // delete path from original root down to first branching node
                {
                    root = root.getFirstOutEdge().getTarget();
                    tarTree.deleteNode(tarTree.getRoot());
                    tarTree.setRoot(root);
                }
                return tarTree;
            }
        }
        return null;
    }

    /**
     * recursively does the work
     *
     * @param srcV
     * @param tarTree
     * @return node if any selected nodes here
     */
    private Node getInducedTreeRec(Map<Integer, String> id2name, NodeSet selected, Node srcV, PhyloTree tarTree) {
        LinkedList<Node> below = new LinkedList<>();
        for (Edge e = srcV.getFirstOutEdge(); e != null; e = srcV.getNextOutEdge(e)) {
            Node srcW = e.getTarget();
            Node tarW = getInducedTreeRec(id2name, selected, srcW, tarTree);
            if (tarW != null)
                below.add(tarW);
        }
        boolean hasNodeData = (srcV.getData() != null && srcV.getData() instanceof NodeData);  // if this has node data, don't use counts of things not present

        if (below.size() == 0) {
            if (selected.contains(srcV)) {
                Node tarV = tarTree.newNode();
                tarV.setInfo(srcV.getInfo());
                if (hasNodeData) {
                    NodeData srcND = (NodeData) srcV.getData();
                    NodeData tarND = new NodeData(srcND.getSummarized(), srcND.getSummarized());
                    tarV.setData(tarND);
                } else
                    tarV.setData(srcV.getData());
                tarTree.setLabel(tarV, id2name.get(srcV.getInfo()));
                return tarV;
            } else
                return null;
        } else if (below.size() == 1 && !selected.contains(srcV)) {
            return below.getFirst();
        } else {  // has at least two children
            Node tarV = tarTree.newNode();
            if (selected.contains(srcV))
                tarV.setInfo(srcV.getInfo());
            Set<Node> toDelete = new HashSet<>();
            Set<Node> toAdd = new HashSet<>();
            for (Node u : below) {
                if (u.getInfo() != null)
                    tarTree.newEdge(tarV, u);
                else {  // child is not selected, connect all its children directly
                    for (Edge f = u.getFirstOutEdge(); f != null; f = u.getNextOutEdge(f)) {
                        Node z = f.getTarget();
                        tarTree.newEdge(tarV, z);
                        toAdd.add(z);
                    }
                    tarTree.deleteNode(u);
                    toDelete.add(u);
                }
            }
            below.removeAll(toDelete);
            below.addAll(toAdd);

            if (hasNodeData) {   // recompute summarized
                NodeData srcND = (NodeData) srcV.getData();
                float[] summarized = Arrays.copyOf(srcND.getAssigned(), srcND.getAssigned().length);
                for (Node u : below) {
                    for (int i = 0; i < summarized.length; i++) {
                        final float[] uSummarized = ((NodeData) u.getData()).getSummarized();
                        final float value = (i < uSummarized.length ? uSummarized[i] : 0);
                        summarized[i] += value;
                    }
                }
                tarV.setData(new NodeData(srcND.getAssigned(), summarized));
            } else
                tarV.setData(srcV.getData());
            tarTree.setLabel(tarV, id2name.get(srcV.getInfo()));
            return tarV;
        }
    }

    /**
     * get the associated phyloTree
     *
     * @return phyloTree
     */
    public PhyloTree getPhyloTree() {
        return (PhyloTree) getGraph();
    }

    /**
     * rotate the tree so that node labels are alphabetically sorted
     * Note that this is a topological operation that does not modify coordinates
     */
    public void topologicallySortTreeLexicographically() {
        if (getPhyloTree().getRoot() != null)
            sortTreeAlphabeticallyRec(getPhyloTree().getRoot());
    }

    /**
     * rotates tree so as to sort leaves alphabetically
     *
     * @param v
     * @return lexicographic smallest leaf label below
     */
    private String sortTreeAlphabeticallyRec(Node v) {
        if (v.getOutDegree() == 0)
            return getLabel(v);
        else { // out degree must be >0
            final ArrayList<Pair<String, Edge>> list = new ArrayList<>(v.getOutDegree());
            for (Edge e = v.getFirstOutEdge(); e != null; e = v.getNextOutEdge(e)) {
                String first = sortTreeAlphabeticallyRec(e.getTarget());
                list.add(new Pair<>(first, e));
            }
            list.sort((a, b) -> {
                int compare = a.getFirst().compareTo(b.getFirst());
                if (compare != 0)
                    return compare;
                else return Integer.compare(a.getSecond().getId(), b.getSecond().getId());
            });
            final ArrayList<Edge> edges = new ArrayList<>(v.getDegree());
            for (Pair<String, Edge> pair : list) {
                edges.add(pair.getSecond());
            }
            if (v.getInDegree() > 0)
                edges.add(v.getFirstInEdge());
            v.rearrangeAdjacentEdges(edges);

            if (getLabel(v) != null && getLabel(v).compareTo(list.get(0).getFirst()) < 0)
                return getLabel(v);
            else
                return list.get(0).getFirst();
        }
    }
}

// EOF
