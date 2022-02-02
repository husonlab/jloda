/*
 * OffspringGraphMatching.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.graph.EdgeSet;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.algorithms.BipartiteMatching;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;

/**
 * computes the offspring graph matching
 * Daniel Huson, 1.2020
 */
public class OffspringGraphMatching {
    /**
     * computes the matching
     *
     */
    public static EdgeSet compute(PhyloTree tree, ProgressListener progress) throws CanceledException {
        var graph = new Graph();

        progress.setSubtask("Offspring graph matching");

        NodeArray<Node> tree2a = tree.newNodeArray();
        NodeArray<Node> tree2b = tree.newNodeArray();

        progress.setMaximum(tree.getNumberOfNodes() + tree.getNumberOfEdges() + 1);
        progress.setProgress(0);
        for (var v : tree.nodes()) {
            tree2a.put(v, graph.newNode());
            tree2b.put(v, graph.newNode());
            progress.incrementProgress();
        }

        progress.setProgress(0);
        for (var e : tree.edges()) {
            graph.newEdge(tree2a.get(e.getSource()), tree2b.get(e.getTarget()));
            progress.incrementProgress();
        }

        var oneSide = graph.newNodeSet();
        oneSide.addAll(tree2a.values());

        try {
            return BipartiteMatching.computeBipartiteMatching(graph, oneSide);
        } finally {
            progress.reportTaskCompleted();
        }
    }

    public static boolean isTreeBased(PhyloTree tree, EdgeSet matching) {
        return discrepancy(tree, matching) == 0;
    }

    public static int discrepancy(PhyloTree tree, EdgeSet matching) {
        return (tree.getNumberOfNodes() - tree.countLeaves()) - matching.size();
    }

}
