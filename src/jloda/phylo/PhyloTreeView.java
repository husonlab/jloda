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

import jloda.graph.*;
import jloda.graphview.EdgeView;
import jloda.graphview.GraphView;
import jloda.graphview.NodeView;
import jloda.util.Geometry;
import jloda.util.NotOwnerException;

import java.util.*;

/**
 *  tree viewer
 *  Daniel Huson, 2000
 */

public class PhyloTreeView extends GraphView {

    /**
     * Constructs a view of a phylogentic tree.
     *
     * @param tree PhyloTree
     */
    public PhyloTreeView(PhyloTree tree) {
        this(tree, 400, 400);
    }

    /**
     * Constructs a view of a phylogentic tree.
     *
     * @param tree        PhyloTree
     * @param doEmbedding compute an embedding of the tree?
     */
    public PhyloTreeView(PhyloTree tree, boolean doEmbedding) {
        this(tree, 400, 400, doEmbedding);
    }

    /**
     * Constructs a view of a phylogentic tree. Computes an embedding of the tree.
     *
     * @param tree PhyloTree
     * @param w    int
     * @param h    int
     */
    public PhyloTreeView(PhyloTree tree, int w, int h) {
        this(tree, w, h, true);

    }

    /**
     * Constructs a view of a phylogentic tree. Optinally computes an embedding of the tree.
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

    private int setAnglesRec(int num, Node root, Edge entry, NodeSet leaves, EdgeDoubleArray angle, Random rand) throws NotOwnerException {
        Graph G = getGraph();

        if (leaves.contains(root))
            return num + 1;
        else {
            Iterator edges = G.getAdjacentEdges(root);

            // edges.permute(); // look at children in random order

            int a = num; // is number of nodes seen so far
            int b = 0;     // number of nodes after visiting subtree

            while (edges.hasNext()) {
                Edge e = (Edge) edges.next();
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

    private void setCoordsRec(Node root, Edge entry, EdgeDoubleArray angle)
            throws NotOwnerException {
        Graph G = getGraph();

        Iterator<Edge> edges = G.getAdjacentEdges(root);

        while (edges.hasNext()) {
            Edge e = edges.next();

            if (e != entry) {
                Node v = G.getOpposite(root, e);

                // translate in the computed direction by the given amount
                setLocation(v,
                        Geometry.translateByAngle(getLocation(root), angle.getValue(e),
                                ((PhyloTree) G).getWeight(e)));

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
     * update view of nodes and edges
     */
    public void resetViews() {
        PhyloTree G = (PhyloTree) getGraph();

        for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
            setLabel(v, G.getLabel(v));
            //setShape(v, NodeView.NONE_NODE);

            if (G.getLabel(v) != null && !G.getLabel(v).equals("")) {
                setShape(v, NodeView.OVAL_NODE);
                setLabelLayout(v, NodeView.LAYOUT);
                setWidth(v, 1);
                setHeight(v, 1);
            } else
                setShape(v, NodeView.NONE_NODE);

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
                int[] summarized = Arrays.copyOf(srcND.getAssigned(), srcND.getAssigned().length);
                for (Node u : below) {
                    for (int i = 0; i < summarized.length; i++) {
                        final int[] uSummarized = ((NodeData) u.getData()).getSummarized();
                        final int value = (i < uSummarized.length ? uSummarized[i] : 0);
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
}

// EOF
