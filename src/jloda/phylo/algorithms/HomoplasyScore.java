/*
 * HomoplasyScore.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.phylo.algorithms;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeIntArray;
import jloda.phylo.PhyloTree;

import java.io.IOException;
import java.util.BitSet;

/**
 * Compute the homoplasy score on a tree
 * Daniel Huson, 2.2006
 */
public class HomoplasyScore {
    static public int computeBestHomoplasyScoreForSplit(PhyloTree tree, BitSet A, BitSet B) throws IOException {
        return computeBestHomoplasyScoreForSplit(tree, A, B, null);
    }

    /**
     * given a phylogentic tree, multifurcations, multiple and internal labels ok, computes
     * the best possible homplays score achievable for a given split, that is, the best
     * score over all possible refinements of the tree.
     * See Huson, Steel and Witfield, in preparation.
     *
     * @param A    one side of split
     * @param B    other side of split
     * @param root the root to use, this should not make any difference, but is here for testing
     *             purposes
     * @return homoplasy score for split
	 */
    static public int computeBestHomoplasyScoreForSplit(PhyloTree tree, BitSet A, BitSet B, Node root) throws IOException {
        if (tree.getNumberOfNodes() < 2 || A.cardinality() <= 1 || B.cardinality() <= 1)
            return 0;
        BitSet treeTaxa = new BitSet();
        // setup scoring map:
        NodeIntArray scoreA = new NodeIntArray(tree); // optimal score for subtree labeled A at root
        NodeIntArray scoreB = new NodeIntArray(tree); // optimal score for subtree labeled B at root
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {

            boolean hasA = false;
            boolean hasB = false;
            for (Integer t : tree.getTaxa(v)) {
                if (A.get(t))
                    hasA = true;
                else if (B.get(t))
                    hasB = true;
                else
                    throw new IOException("Taxon t=" + t + ": not present in split");
                treeTaxa.set(t);
            }
            int aValue = 0;
            int bValue = 0;
            if (hasA && !hasB)
                bValue = Integer.MAX_VALUE;
            else if (!hasA && hasB)
                aValue = Integer.MAX_VALUE;
            else if (hasA && hasB)
                aValue = bValue = 1; // TODO: is this really correct?
            scoreA.set(v, aValue);
            scoreB.set(v, bValue);
        }
        // if only 0 or 1 of either side of the split occurs in T, then score is 0:
        BitSet intersection = (BitSet) treeTaxa.clone();
        intersection.and(A);
        if (intersection.cardinality() <= 1)
            return 0;
        intersection = (BitSet) treeTaxa.clone();
        intersection.and(B);
        if (intersection.cardinality() <= 1)
            return 0;

        // recursively compute score:
        //Node root = null;
        if (root == null)
            root = tree.getFirstNode();

        /*
        for (root = tree.getFirstNode(); root != null; root = root.getNext())
           if (tree.getNode2Taxa(root) == null || tree.getNode2Taxa(root).size() == 0)
                break;
        System.err.println("root "+root);
        if (root == null)
            throw new JlodaException("No unlabeled node available as root");
        */
        //System.out.println("initially:");
        //printScores(tree,scoreA,scoreB);
        computeScoreRec(root, null, scoreA, scoreB);
        return Math.min(scoreA.get(root), scoreB.get(root)) - 1;
    }

    /**
     * recursively does the work
     *
	 */
    private static void computeScoreRec(Node v, Edge e, NodeIntArray scoreA,
                                        NodeIntArray scoreB) {
        //System.out.println("Entering with v="+v);
        //printScores(tree,scoreA,scoreB);
        boolean hasAMuchBetterThanB = false;
        boolean hasBMuchBetterThanA = false;
        int countA = 0;
        int countB = 0;
        // first visit all children to compute their scores:
        for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
            if (f != e) {
                Node w = f.getOpposite(v);
                if (w.getDegree() > 1)
                    computeScoreRec(w, f, scoreA, scoreB);
                if (scoreA.get(w) <= scoreB.get(w) - 1) {
                    hasAMuchBetterThanB = true;
                    countB += scoreA.get(w);
                } else {
                    countB += scoreB.get(w);
                }
                if (scoreB.get(w) <= scoreA.get(w) - 1) {
                    hasBMuchBetterThanA = true;
                    countA += scoreB.get(w);
                } else {
                    countA += scoreA.get(w);
                }
            }
        }
        // this might be a labeled internal node, treat it as an additional leaf node:
        if (scoreA.get(v) <= scoreB.get(v) - 1) {
            hasAMuchBetterThanB = true;
            countB += scoreA.get(v);
        } else {
            countB += scoreB.get(v);
        }
        if (scoreB.get(v) <= scoreA.get(v) - 1) {
            hasBMuchBetterThanA = true;
            countA += scoreB.get(v);
        } else {
            countA += scoreA.get(v);
        }
        // add 1 for change, if necessary:
        if (hasAMuchBetterThanB)
            countB += 1;
        if (hasBMuchBetterThanA)
            countA += 1;
        // set value for node
        scoreA.set(v, countA);
        scoreB.set(v, countB);

        //System.out.println("Exiting with v="+v);
        //printScores(tree,scoreA,scoreB);
    }

    static void printScores(PhyloTree tree, NodeIntArray scoreA, NodeIntArray scoreB) {
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            System.out.println("v=" + v + " scoreA=" + scoreA.get(v) + " scoreB=" + scoreB.get(v));
        }
    }
}
