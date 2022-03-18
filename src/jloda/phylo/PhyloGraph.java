/*
 * PhyloGraph.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.phylo;


import jloda.graph.*;
import jloda.util.IteratorUtils;

import java.util.*;

/**
 * Phylogenetic graph
 *
 * @author Daniel Huson, 2005, 2018
 */
public class PhyloGraph extends Graph {
    public static final double DEFAULT_WEIGHT = 1.0;
    public static final double DEFAULT_CONFIDENCE = 1.0;
    private EdgeDoubleArray edgeWeights;
    private EdgeDoubleArray edgeConfidences;
    private Map<Integer, Node> taxon2node;
    private NodeArray<List<Integer>> node2taxa;

    // if you add anything here, make sure it gets added to copy, too!

    /**
     * Construct a new empty phylogenetic graph. Also registers a listener that will update the taxon2node
     * array if nodes are deleted.
     */
    public PhyloGraph() {
        super();

        addGraphUpdateListener(new GraphUpdateAdapter() {
            public void deleteNode(Node v) {
                if (node2taxa != null) {
                    List<Integer> list = node2taxa.get(v);
                    if (list != null) {
                        for (Integer t : list) {
                            taxon2node.put(t, null);
                        }
                    }
                }
            }
        });
    }

    /**
     * Clears the graph.  All auxiliary arrays are set to null.
     */
    public void clear() {
        super.clear();
        taxon2node = null;
        node2taxa = null;
        edgeWeights = null;
        edgeConfidences = null;
    }

    /**
     * copies one phylo graph to another
     *
     * @param src the source graph
     */
    public void copy(PhyloGraph src) {
        final NodeArray<Node> oldNode2NewNode = new NodeArray<>(src);
        copy(src, oldNode2NewNode, null);
    }

    /**
     * copies one phylo graph to another
     *
     * @param src             the source graph
     * @param oldNode2NewNode if non-null, will contain mapping of old nodes to new nodes
     * @param oldEdge2NewEdge if non-null, will contain mapping of old edges to new edges
     */
    public void copy(PhyloGraph src, NodeArray<Node> oldNode2NewNode, EdgeArray<Edge> oldEdge2NewEdge) {
        clear();
        if (oldNode2NewNode == null)
            oldNode2NewNode = new NodeArray<>(src);
        if (oldEdge2NewEdge == null)
            oldEdge2NewEdge = new EdgeArray<>(src);

        super.copy(src, oldNode2NewNode, oldEdge2NewEdge);

        setName(src.getName());

        if (src.taxon2node != null) {
            for (Node v : src.taxon2node.values()) {
                final var w = (oldNode2NewNode.get(v));
                for (var tax : src.getTaxa(v)) {
                    addTaxon(w, tax);
                }
            }
        }

        if (src.edgeWeights != null) {
            for (var e : src.edgeWeights.keys()) {
                setWeight(oldEdge2NewEdge.get(e), src.getWeight(e));
            }
        }

        if (src.edgeConfidences != null) {
            for (var e : src.edgeConfidences.keys()) {
                setConfidence(oldEdge2NewEdge.get(e), src.getConfidence(e));
            }
        }
    }


    /**
     * Gets the weight of an edge.
     *
     * @param e Edge
     * @return the edge weight, or 1, if not set
     */
    public double getWeight(Edge e) {
        if (edgeWeights == null)
            return DEFAULT_WEIGHT;
        else
            return edgeWeights.getOrDefault(e, DEFAULT_WEIGHT);
    }

    public void setWeight(Edge e, double value) {
        if (edgeWeights == null) {
            if (value == DEFAULT_WEIGHT)
                return;
            edgeWeights = new EdgeDoubleArray(this);
        }
        edgeWeights.put(e, value);
    }

    /**
     * Sets the confidence of an edge.
     *
     * @param e     Edge
     * @param value double
     */
    public void setConfidence(Edge e, double value) {
        if (edgeConfidences == null) {
            if (value == DEFAULT_CONFIDENCE)
                return;
            edgeConfidences = newEdgeDoubleArray();
        }
        edgeConfidences.put(e, value);
    }

    /**
     * Gets the confidence of an edge.
     *
     * @param e Edge
     * @return returns the edge confidence, or 1, if not set
     */
    public double getConfidence(Edge e) {
        if (edgeConfidences == null)
            return DEFAULT_CONFIDENCE;
        else
            return edgeConfidences.getOrDefault(e, DEFAULT_CONFIDENCE);
    }

    /**
     * find the corresponding node for a given taxon-id.
     *
     * @param taxId the taxon-id
     * @return the node associated with the given taxon
     */
    public Node getTaxon2Node(int taxId) {
        if (taxon2node == null)
            return null;
        else
            return taxon2node.get(taxId);
    }

    /**
     * returns the number of taxa
     *
     * @return number of taxa
     */
    public int getNumberOfTaxa() {
        if (taxon2node == null)
            return 0;
        else
            return taxon2node.size();
    }

    public Iterable<Integer> getTaxa() {
        if (taxon2node == null)
            return Collections.emptyList();
        else
            return taxon2node.keySet();
    }

    public boolean hasTaxa(Node v) {
        return getNumberOfTaxa(v) > 0;
    }

    public int getNumberOfTaxa(Node v) {
        if (node2taxa == null)
            return 0;
        else {
            var list = node2taxa.get(v);
            return list == null ? 0 : list.size();
        }
    }

    /**
     * add a taxon to be represented by the specified node
     *
     * @param v     the node.
     * @param taxId the id of the taxon to be added
     */
    public void addTaxon(Node v, int taxId) {
        if (taxon2node == null) {
            taxon2node = new HashMap<>();
            node2taxa = newNodeArray();
        }
        taxon2node.put(taxId, v);
        var list = node2taxa.get(v);
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
        if (taxon2node != null) {
            var list = node2taxa.get(v);
            if (list != null) {
                for (var t : list) {
                    if (taxon2node.get(t) == v)
                        taxon2node.remove(t);
                }
                node2taxa.put(v, null);
            }
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
            if (node2taxa == null || node2taxa.get(v) == null)
                return IteratorUtils.emptyIterator();
            else
                return node2taxa.get(v).iterator();
        };
    }

    public int getTaxon(Node v) {
        if (node2taxa == null || node2taxa.get(v) == null || node2taxa.get(v).size() == 0)
            return -1;
        else
            return node2taxa.get(v).get(0);
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
	 */
    public void removeTaxon(int taxonId) {
        if (taxon2node != null && taxonId > 0 && taxonId < taxon2node.size()) {
            taxon2node.put(taxonId, null);
            for (var v : nodes()) {
                var list = node2taxa.get(v);
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
        var result = new PhyloGraph();
        result.copy(this);
        return result;
    }


    /**
     * changes the node labels of the graph using the mapping old-to-new
     *
	 */
    public void changeLabels(Map<String, String> old2new) {
        for (var v : nodes()) {
            var label = getLabel(v);
            if (label != null && old2new.containsKey(label))
                setLabel(v, old2new.get(label));
        }
    }

    /**
     * add the nodes and edges of another graph to this graph. Doesn't make the graph connected, though!
     *
	 */
    public void add(PhyloGraph graph) {
        NodeArray<Node> old2new = new NodeArray<>(graph);
        for (var v : graph.nodes()) {
            var w = newNode();
            old2new.put(v, w);
            setLabel(w, graph.getLabel(v));

        }
        try {
            for (var e : edges()) {
                var f = newEdge(old2new.get(e.getSource()), old2new.get(e.getTarget()));
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
