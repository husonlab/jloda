/*
 * PhyloGraph.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.phylo;


import jloda.graph.*;
import jloda.util.EmptyIterator;

import java.util.*;

/**
 * Phylogenetic graph
 *
 * @author Daniel Huson, 2005, 2018
 */
public class PhyloGraph extends Graph {
    private EdgeDoubleArray edgeWeights;
    private EdgeDoubleArray edgeConfidences;
    final Map<Integer, Node> taxon2node;
    final NodeArray<List<Integer>> node2taxa;

    // if you add anything here, make sure it gets added to copy, too!

    /**
     * Construct a new empty phylogenetic graph. Also registers a listener that will update the taxon2node
     * array if nodes are deleted.
     */
    public PhyloGraph() {
        super();
        taxon2node = new HashMap<>();
        node2taxa = new NodeArray<>(this);

        addGraphUpdateListener(new GraphUpdateAdapter() {
            public void deleteNode(Node v) {
                List<Integer> list = node2taxa.get(v);
                if (list != null) {
                    for (Integer t : list) {
                        taxon2node.put(t, null);
                    }
                }
            }
        });
    }

    /**
     * Clears the graph.  All auxiliary arrays are cleared.
     */
    public void clear() {
        super.clear();
        taxon2node.clear();
        node2taxa.clear();
        edgeWeights = null;
        edgeConfidences = null;
    }

    /**
     * copies one phylo graph to another
     *
     * @param src the source graph
     * @return old node to new node mapping
     */
    public NodeArray<Node> copy(PhyloGraph src) {
        final NodeArray<Node> oldNode2NewNode = new NodeArray<>(src);
        copy(src, oldNode2NewNode, null);
        return oldNode2NewNode;
    }

    /**
     * copies one phylo graph to another
     *
     * @param src             the source graph
     * @param oldNode2NewNode
     * @param oldEdge2NewEdge
     */
    public NodeArray<Node> copy(PhyloGraph src, NodeArray<Node> oldNode2NewNode, EdgeArray<Edge> oldEdge2NewEdge) {
        clear();
        if (oldNode2NewNode == null)
            oldNode2NewNode = new NodeArray<>(src);
        if (oldEdge2NewEdge == null)
            oldEdge2NewEdge = new EdgeArray<>(src);

        super.copy(src, oldNode2NewNode, oldEdge2NewEdge);

        setName(src.getName());

        BitSet added = new BitSet();

        for (Node v : src.nodes()) {
            final Node w = (oldNode2NewNode.get(v));
            for (Integer tax : src.getTaxa(v)) {
                addTaxon(w, tax);
                added.set(tax);
            }
        }
        for (Edge e : src.edges()) {
            final Edge f = (oldEdge2NewEdge.get(e));
            if (src.edgeWeights != null)
                setWeight(f, src.getWeight(e));
            if (src.edgeConfidences != null)
                setConfidence(f, src.getConfidence(e));
        }
        return oldNode2NewNode;
    }


    /**
     * Gets the weight of an edge.
     *
     * @param e Edge
     * @return edgeWeights double
     */
    public double getWeight(Edge e) {
        if (edgeWeights == null)
            return 1.0;
        else
            return edgeWeights.getDouble(e);
    }

    public void setWeight(Edge e, double wgt) {
        if (edgeWeights == null) {
            edgeWeights = new EdgeDoubleArray(this);
        }
        edgeWeights.put(e, wgt);
    }

    /**
     * Sets the confidence of an edge.
     *
     * @param e Edge
     * @param d double
     */
    public void setConfidence(Edge e, Double d) {
        if (edgeConfidences == null) {
            if (d == null)
                return;
            edgeConfidences = newEdgeDoubleArray();
        }
        edgeConfidences.put(e, d);
    }

    /**
     * Gets the confidence of an edge.
     *
     * @param e Edge
     * @return confidence
     */
    public double getConfidence(Edge e) {
        if (edgeConfidences == null || edgeConfidences.get(e) == null)
            return 1;
        else
            return edgeConfidences.get(e);
    }


    /**
     * find the corresponding node for a given taxon-id.
     *
     * @param taxId the taxon-id
     * @return the Node representing the taxon with id <code>taxId</code>.
     */
    public Node getTaxon2Node(int taxId) {
        return taxon2node.get(taxId);
    }

    /**
     * returns the number of taxa
     *
     * @return number of taxa
     */
    public int getNumberOfTaxa() {
        return taxon2node.size();
    }

    public boolean hasTaxa(Node v) {
        return getNumberOfTaxa(v) > 0;
    }

    public int getNumberOfTaxa(Node v) {
        final List<Integer> list = node2taxa.get(v);
        return list == null ? 0 : list.size();
    }

    /**
     * add a taxon to be represented by the specified node
     *
     * @param v     the node.
     * @param taxId the id of the taxon to be added
     */
    public void addTaxon(Node v, int taxId) {
        taxon2node.put(taxId, v);
        List<Integer> list = node2taxa.get(v);
        if (list == null) {
            list = new ArrayList<>();
            list.add(taxId);
            node2taxa.put(v, list);
        } else if (!list.contains(taxId))
            list.add(taxId);
        //else
        //    System.err.println("Already contained");
    }

    /**
     * Clears the taxa entries for the specified node
     *
     * @param v the node
     */
    public void clearTaxa(Node v) {
        final List<Integer> list = node2taxa.get(v);
        if (list != null) {
            for (int t : list) {
                if (taxon2node.get(t) == v)
                    taxon2node.remove(t);
            }
            node2taxa.put(v, null);
        }
    }

    /**
     * Iterates over all taxon ids of a node
     *
     * @param v the node
     * @return taxa
     */
    public Iterable<Integer> getTaxa(Node v) {
        return () -> {
            if (node2taxa.get(v) == null)
                return new EmptyIterator<>();
            else
                return node2taxa.get(v).iterator();
        };
    }

    /**
     * Clears all taxa
     */
    public void clearTaxa() {
        node2taxa.clear();
        taxon2node.clear();
    }

    /**
     * removes a taxon from the graph, but leaves the corresponding node label, if any
     *
     * @param taxonId
     */
    public void removeTaxon(int taxonId) {
        if (taxonId > 0 && taxonId < taxon2node.size()) {
            taxon2node.put(taxonId, null);
            for (Node v : nodes()) {
                List<Integer> list = node2taxa.get(v);
                if (list != null && list.contains(taxonId)) {
                    list.remove((Integer) taxonId);
                    if (list.size() == 0)
                        node2taxa.put(v, null);
                    return;
                }
            }
        }
    }

    /**
     * produces a clone of this graph
     *
     * @return a clone of this graph
     */
    public Object clone() {
        super.clone();
        PhyloGraph result = new PhyloGraph();
        result.copy(this);
        return result;
    }


    /**
     * changes the node labels of the graph using the mapping old-to-new
     *
     * @param old2new
     */
    public void changeLabels(Map<String, String> old2new) {
        for (Node v : nodes()) {
            String label = getLabel(v);
            if (label != null && old2new.containsKey(label))
                setLabel(v, old2new.get(label));
        }
    }

    /**
     * add the nodes and adjacentEdges of another graph to this graph. Doesn't make the graph connected, though!
     *
     * @param graph
     */
    public void add(PhyloGraph graph) {
        NodeArray<Node> old2new = new NodeArray<>(graph);
        for (Node v : graph.nodes()) {
            Node w = newNode();
            old2new.put(v, w);
            setLabel(w, graph.getLabel(v));

        }
        try {
            for (Edge e : edges()) {
                Edge f = newEdge(old2new.get(e.getSource()), old2new.get(e.getTarget()));
                setLabel(f, graph.getLabel(e));
                setWeight(f, graph.getWeight(e));
                if (graph.edgeConfidences.get(e) != null)
                    setConfidence(f, graph.getConfidence(e));
            }
        } catch (IllegalSelfEdgeException e1) {
            throw new RuntimeException(e1);
        }
    }
}
