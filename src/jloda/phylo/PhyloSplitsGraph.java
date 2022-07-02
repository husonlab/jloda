/*
 * PhyloSplitsGraph.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.util.Basic;
import jloda.util.Pair;

import java.util.*;

/**
 * Splits graph
 * Daniel Huson, 1.2018
 */
public class PhyloSplitsGraph extends PhyloGraph {
	private EdgeIntArray edgeSplitMap;
	private EdgeDoubleArray edgeAngleMap;
	private Vector<Integer> taxonCycleMap;

	/**
	 * Construct a new empty phylogenetic tree.
	 */
	public PhyloSplitsGraph() {
		super();
	}

	/**
	 * Clears the tree.
     */
    public void clear() {
		super.clear();
		edgeSplitMap = null;
		edgeAngleMap = null;
		taxonCycleMap = null;
	}

    /**
     * copies a graph
     *
     * @param src original graph
     * @return mapping of old nodes to new nodes
     */
    public NodeArray<Node> copy(PhyloSplitsGraph src) {
        NodeArray<Node> oldNode2NewNode = src.newNodeArray();
        copy(src, oldNode2NewNode, null);
        return oldNode2NewNode;
    }

    /**
     * copies a graph
     *
	 */
    public NodeArray<Node> copy(PhyloSplitsGraph src, NodeArray<Node> oldNode2NewNode, EdgeArray<Edge> oldEdge2NewEdge) {
        clear();
        if (oldNode2NewNode == null)
            oldNode2NewNode = new NodeArray<>(src);
        if (oldEdge2NewEdge == null)
            oldEdge2NewEdge = new EdgeArray<>(src);

        super.copy(src, oldNode2NewNode, oldEdge2NewEdge);

        for (var e : src.edges()) {
            var f = (oldEdge2NewEdge.get(e));
            setAngle(f, src.getAngle(e));
            setSplit(f, src.getSplit(e));
        }

        setName(src.getName());

        return oldNode2NewNode;
    }

    /**
     * gets the set of split ids in the graph as a sorted array
     *
     * @return number of splits
     */
    public Integer[] getSplitIds() {
        final Set<Integer> ids = new TreeSet<>();
        for (Edge e : edges()) {
			var id = getSplit(e);
			if (id == 0)
				System.err.println("Split id is 0!!!");
			else
				ids.add(id);
		}
        return ids.toArray(new Integer[0]);
    }

    /**
     * sets the split-id of an edge
     *
     * @param e  the edge
     * @param id the id
     */
    public void setSplit(Edge e, int id) {
		getSplits().put(e, id);
    }

	/**
	 * gets the split-id of an edge
	 *
	 * @param e the edge
	 * @return the split-id of the given edge
	 */
	public int getSplit(Edge e) {
		return edgeSplitMap == null ? 0 : getSplits().getOrDefault(e, 0);
	}

	public EdgeIntArray getSplits() {
		if (edgeSplitMap == null) {
			synchronized (this) {
				if (edgeSplitMap == null)
					edgeSplitMap = newEdgeIntArray();
			}
		}
		return edgeSplitMap;
	}

	/**
	 * clones the current tree
	 *
	 * @return a clone of the current tree
	 */
	public Object clone() {
		final PhyloSplitsGraph phyloSplitsGraph = new PhyloSplitsGraph();
		phyloSplitsGraph.copy(this);
		return phyloSplitsGraph;
    }

    /**
     * removes a split from the graph by contracting all adjacentEdges associated with the split
     *
	 */
    public void removeSplit(int splitId) {
        Node one = getTaxon2Node(1);

        if (one != null) {
            // determine all nodes and adjacentEdges that separate S(1) from X-S(1)
            List<Pair<Node, Edge>> separators = new ArrayList<>(); // each is a pair consisting of a node and edge
            NodeSet seen = new NodeSet(this);

            getAllSeparators(splitId, one, null, seen, separators);

            // determine all nodes on opposite end of separating adjacentEdges
            NodeSet opposites = new NodeSet(this);
            for (var pair : separators) {
                opposites.add(getOpposite(pair.getFirst(), pair.getSecond()));
            }

            // reconnect adjacentEdges that are adjacent to opposite ends of separators:

            for (var pair : separators) {
                Node v = pair.getFirst();
                Edge e = pair.getSecond();
                Node w = getOpposite(v, e);

                for (Edge f : w.adjacentEdges()) {
                    if (f != e) {
                        Node u = getOpposite(w, f);
                        if (u != v && !opposites.contains(u)) {
                            Edge g = null;
                            try {
                                g = newEdge(u, v);
                            } catch (IllegalSelfEdgeException e1) {
                                Basic.caught(e1);
                            }
                            setSplit(g, getSplit(f));
                            setWeight(g, getWeight(f));
                            setAngle(g, getAngle(f));
                        }
                    }
                }

                if (getLabel(w) != null && getLabel(w).length() > 0) {
                    if (getLabel(v) == null)
                        setLabel(v, getLabel(w));
                    else
                        setLabel(v, getLabel(v) + ", " + getLabel(w));
                }

                if (getTaxa(w) != null) // node is labeled by taxa, move labels to v
                {
                    for (Integer t : getTaxa(w)) {
                        addTaxon(v, t);
                        addTaxon(v, t);
                    }
                    clearTaxa(w);
                }
                // delete old node w.
                deleteNode(w);
            }
        }
    }

    /**
     * recursively finds all adjacentEdges representing the named split.
     *
     * @param separators adds the resulting pair of (node,edge) into this list
	 */
    public void getAllSeparators(int splitId, Node v, Edge e, NodeSet seen, List<Pair<Node, Edge>> separators) {
        if (!seen.contains(v)) {
            seen.add(v);
            for (Edge f : v.adjacentEdges()) {
                if (f != e) {
                    if (getSplit(f) == splitId) {
                        separators.add(new Pair<>(v, f));
                    } else
                        getAllSeparators(splitId, getOpposite(v, f), f, seen, separators);
                }
            }
        }
    }

    /**
     * finds an edge with the given split id that separates 1 from rest of graph
     *
     * @return Pair consisting of node and edge
	 */
    public Pair<Node, Edge> getSeparator(int splitId, Node v, Edge e, NodeSet seen) {
        if (!seen.contains(v)) {
            seen.add(v);
            for (Edge f : v.adjacentEdges()) {
                if (f != e) {
                    if (getSplit(f) == splitId)
                        return new Pair<>(v, f);
                    else {
                        Pair<Node, Edge> pair = getSeparator(splitId, getOpposite(v, f), f, seen);
                        if (pair != null)
                            return pair;
                    }
                }
            }
        }
        return null;
    }

    /**
     * returns a labeling of all nodes by the sets of characters in state 1
     *
     * @return labeling of all nodes by 01 strings
     */
    public NodeArray labelNodesBySequences(Map split2chars, char[] firstChars) {
        final NodeArray<String> labels = new NodeArray<>(this);
        System.err.println("base-line= " + (new String(firstChars)));
        Node v = getTaxon2Node(1);
        BitSet used = new BitSet(); // set of splits used in current path

        labelNodesBySequencesRec(v, used, split2chars, firstChars, labels);
        return labels;
    }

    /**
     * recursively do the work
     *
	 */
    private void labelNodesBySequencesRec(Node v, BitSet used, Map split2chars, char[] firstChars, NodeArray<String> labels) {
        if (labels.get(v) == null) {
            BitSet flips = new BitSet();
            for (int s = used.nextSetBit(1); s >= 0; s = used.nextSetBit(s + 1)) {
                if (s > 0)
                    flips.or((BitSet) split2chars.get(s));
                // s=0 happens in rooted graph
            }
            StringBuilder label = new StringBuilder();
            for (int c = 1; c < firstChars.length; c++) {
                if (flips.get(c) == (firstChars[c] == '1'))
                    label.append("0");
                else
                    label.append("1");
            }
            labels.put(v, label.toString());
            for (Edge e : v.adjacentEdges()) {
                int s = getSplit(e);
                if (!used.get(s)) {
                    used.set(s);
                    labelNodesBySequencesRec(v.getOpposite(e), used, split2chars, firstChars, labels);
                    used.set(s, false);
                }
            }
        }
    }

    /**
     * Sets the angle of an edge.
     *
     * @param e Edge
     * @param d angle
     */
    public void setAngle(Edge e, double d) {
		getAngles().put(e, d);
	}

	/**
	 * Gets the angle of an edge.
	 *
	 * @param e Edge
	 * @return angle
	 */
	public double getAngle(Edge e) {
		return edgeAngleMap == null ? 0 : edgeAngleMap.getOrDefault(e, 0.0);
	}

	public EdgeDoubleArray getAngles() {
		if (edgeAngleMap == null) {
			synchronized (this) {
				if (edgeAngleMap == null)
					edgeAngleMap = newEdgeDoubleArray();
			}
		}
		return edgeAngleMap;
	}

	/**
	 * find the position of a taxon in the cyclic ordering.
	 *
	 * @param taxId the taxon-id.
	 * @return the index of taxon with id <code>taxId</code> in the cyclic ordering.
	 */
	public int getTaxon2Cycle(int taxId) {
		var taxon2cycle = getTaxon2Cycle();
		if (taxId > 0 && taxId <= taxon2cycle.size())
			return taxon2cycle.get(taxId - 1);
		else {
			System.err.println("getTaxon2Cycle: no cycle-index set for taxId " + taxId + " (taxon2cycle.size(): " + taxon2cycle.size() + ")");
			return -1;
		}
	}

    /**
     * gets the cycle of taxa
     *
     * @return cyclic ordering of taxa
     */
    public int[] getCycle() {
        int[] cycle = new int[getNumberOfTaxa() + 1];
        for (int t = 1; t <= getNumberOfTaxa(); t++)
            cycle[getTaxon2Cycle(t)] = t;
        return cycle;
    }


    /**
     * set the position of a taxon in the cyclic ordering.
     *
     * @param taxId      the taxon-id.
     * @param cycleIndex the index of taxon with id <code>taxId</code> in the cyclic ordering.
     */
	public void setTaxon2Cycle(int taxId, int cycleIndex) {
		var taxon2cycle = getTaxon2Cycle();
		if (taxId <= taxon2cycle.size()) {
			taxon2cycle.setElementAt(cycleIndex, taxId - 1);
		} else {
			taxon2cycle.setSize(taxId);
			taxon2cycle.setElementAt(cycleIndex, taxId - 1);
		}
	}

	public Vector<Integer> getTaxon2Cycle() {
		if (taxonCycleMap == null) {
			synchronized (this) {
				taxonCycleMap = new Vector<>();
			}
		}
		return taxonCycleMap;
	}

	/**
	 * returns the number of splits mentioned in this graph
	 *
	 * @return number of splits
	 */
	public int countSplits() {
		BitSet seen = new BitSet();
		for (Edge e = getFirstEdge(); e != null; e = getNextEdge(e))
			seen.set(getSplit(e));
        return seen.cardinality();
    }

    /**
     * returns the highest split id mentioned in this graph
     *
     * @return highest split id mentioned
     */
    public int getMaxSplitId() {
        int max = 0;
        for (Edge e = getFirstEdge(); e != null; e = getNextEdge(e)) {
            if (getSplit(e) > max)
                max = getSplit(e);
        }
        return max;
    }
}

// EOF
