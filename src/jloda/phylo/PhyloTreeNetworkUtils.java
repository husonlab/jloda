/*
 * PhyloTreeNetworkUtils.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.util.Pair;

import java.util.HashSet;

/**
 * some utilities used in phylotree to write and parse rooted networks
 * Daniel Huson, 8.2007
 */
public class PhyloTreeNetworkUtils {
    /**
     * looks for a suffix of the label that starts with '#'
     *
     * @param label
     * @return label or null
     */
    public static String findReticulateLabel(String label) {
        int pos = label.lastIndexOf("#"); // look for last instance of '#',
        // followed by H or L
        if (pos >= 0 && pos < label.length() - 1 && "HLhl".indexOf(label.charAt(pos + 1)) != -1)
            return label.substring(pos + 1);
        else
            return null;
    }

    /**
     * determines whether this a reticulate node
     *
     * @param label the node label
     * @return true, if label contains # followed by H L h or l
     */
    public static boolean isReticulateNode(String label) {
        label = label.toUpperCase();
        return label.contains("#H") || label.contains("#L");
    }

    /**
     * determines whether the edge leading to this instance of a reticulate node
     * should be treated as an acceptor edge, i.e. as a tree edge that is the
     * target of of HGT edge. At most one such edge per reticulate node is
     * allowed
     *
     * @param label the node label
     * @return true, if label contains ## followed by H L h or l
     */
    public static boolean isReticulateAcceptorEdge(String label) {
        label = label.toUpperCase();
        return label.contains("##H") || label.contains("##L");
    }

    /**
     * removes the reticulate node string from the node label
     *
     * @param label the node label
     * @return string without label or null, if string only consisted of
     * substring
     */
    public static String removeReticulateNodeSuffix(String label) {
        var pos = label.indexOf("#");
        if (pos == -1)
            return label;
        else if (pos == 0)
            return null;
        else
            return label.substring(0, pos);
    }

    /**
     * makes the node label for a reticulate node
     *
     * @return label
     */
    static String makeReticulateNodeLabel(boolean asAcceptorEdgeTarget, int number) {
        if (asAcceptorEdgeTarget)
            return "##H" + number;
        else
            return "#H" + number;
    }

    /**
     * determines whether it is ok to descend an edge in a recursive
     * traverse of a tree. It is ok if the edge is not a reticulate edge or is
     * the first reticulate edge that enters its target
     *
     * @return true, if we should descend this edge, false else
     */
    static public boolean okToDescendDownThisEdge(PhyloTree tree, Edge e, Node v) {
        if (tree.isSpecial(e)) {
            if (v != e.getSource())
                return false; // only go DOWN special edges.
            var w = e.getTarget();
            for (var f : w.inEdges()) {
                if (tree.isSpecial(f)) {
                    return f == e; // e must be first in-coming special edge
                }
            }
        }
        return true; // can't happen
    }

    /**
     * determines whether it is ok to descend an edge in a recursive
     * traverse of a tree. It is ok if the edge is not a reticulate edge or if it is
     * the first reticulate edge that enters its target
     */
    static public boolean okToDescendDownThisEdge(PhyloTree tree, Edge e) {
        if (!tree.isSpecial(e))
            return true;
        else {
            var w = e.getTarget();
            for (var f : w.inEdges()) {
                if (tree.isSpecial(f)) {
                    return f == e; // e must be first in-coming special edge
                }
            }
        }
        return false; // can't happen
    }

    /**
     * determines whether the two trees are single-labeled trees on the same
     * taxon sets
     *
     * @return true, if ok
     */
    public static boolean areSingleLabeledTreesWithSameTaxa(PhyloTree tree1, PhyloTree tree2) {
        var labels1 = new HashSet<String>();

        if (tree1.getNumberSpecialEdges() > 0 || tree2.getNumberSpecialEdges() > 0)
            return false;

        for (var v : tree1.nodes()) {
            if (v.getOutDegree() == 0) {
                if (labels1.contains(tree1.getLabel(v)))
                    return false; // not single labeled
                else
                    labels1.add(tree1.getLabel(v));
            }
        }

        var labels2 = new HashSet<String>();
        for (var v : tree2.nodes()) {
            if (v.getOutDegree() == 0) {
                if (!labels1.contains(tree2.getLabel(v)))
                    return false; // not present in first tree
                if (labels2.contains(tree2.getLabel(v)))
                    return false; // not single labeled
                else
                    labels2.add(tree2.getLabel(v));
            }
        }
        return labels1.size() == labels2.size();
    }

    /**
     * gets the average distance from this node to a leaf.
     *
     * @param v node
     * @return average distance to a leaf
     */
    public static double computeAverageDistanceToALeaf(PhyloTree tree, Node v) {
        // assumes that all edges are oriented away from the root
        var seen = tree.newNodeSet();
        var pair = new Pair<>(0.0, 0);
        computeAverageDistanceToLeafRec(tree, v, null, 0, seen, pair);
        var sum = pair.getFirst();
        var leaves = pair.getSecond();
        if (leaves > 0)
            return sum / leaves;
        else
            return 0;
    }

    /**
     * recursively does the work
     */
    private static void computeAverageDistanceToLeafRec(PhyloTree tree, Node v, Edge e, double distance, NodeSet seen, Pair<Double, Integer> pair) {
        if (!seen.contains(v)) {
            seen.add(v);

            if (v.getOutDegree() > 0) {
                for (var f : v.adjacentEdges()) {
                    if (f != e) {
                        computeAverageDistanceToLeafRec(tree, f.getOpposite(v), f, distance + tree.getWeight(f), seen, pair);
                    }
                }
            } else {
                pair.setFirst(pair.getFirst() + distance);
                pair.setSecond(pair.getSecond() + 1);
            }
        }
    }
}
