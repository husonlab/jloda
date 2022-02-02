/*
 * BipartiteMatching.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph.algorithms;

import jloda.graph.*;

import java.util.ArrayList;

/**
 * computes a maximal matching in a bipartite graph
 * Daniel Huson, 1.2020
 */
public class BipartiteMatching {
    /**
     * computes a maximum matching, or null, if graph is not bipartite
     *
     * @param graph bipartite graph
     * @return maximum matching
     */
    public static EdgeSet computeBipartiteMatching(Graph graph) {
        final NodeSet oneSide = computeBipartite(graph);
        if (oneSide == null)
            return null;
        else
            return computeBipartiteMatching(graph, oneSide);
    }

    /**
     * computes a maximum matching, or null, if graph is note bipartite
     *
     * @param graph   bipartite graph
     * @param oneSide one set of nodes in bipartition of graph
     * @return maximum matching
     */
    public static EdgeSet computeBipartiteMatching(Graph graph, NodeSet oneSide) {
        final EdgeSet matching = new EdgeSet(graph);

        final NodeSet uncovered = new NodeSet(graph); // all nodes on one side that have degree >0 and are not covered by the matching
        for (Node v : oneSide) {
            if (v.getDegree() > 0)
                uncovered.add(v);
        }

        while (uncovered.size() > 0) {
            final ArrayList<Edge> path = findAlternatingPath(matching, uncovered);
            // augment:
            for (Edge e : path) {
                if (matching.contains(e))
                    matching.remove(e);
                else
                    matching.add(e);
            }
        }
        return matching;
    }

    /**
     * find an alternating path
     *
     * @param uncovered - methods removes nodes that will become covered through path augmentation
     * @return augmenting path
     */
    private static ArrayList<Edge> findAlternatingPath(EdgeSet matching, NodeSet uncovered) {
        final ArrayList<Edge> path = new ArrayList<>();

        Node v = uncovered.getFirstElement();
        uncovered.remove(v);
        Edge e = null;

        final NodeSet visited = new NodeSet(matching.getOwner());
        boolean extended;
        do {
            extended = false;
            for (Edge f : v.adjacentEdges()) {
                Node w = f.getOpposite(v);
                if (!visited.contains(w)) {
                    visited.add(w);
                    if (matching.contains(f) != (e == null || matching.contains(e))) {
                        path.add(f);
                        e = f;
                        v = w;
                        extended = true;
                        break;
                    }
                }
            }
        }
        while (extended);

        uncovered.remove(v); // last node in path will become covered (if not already)

        return path;
    }


    /**
     * computes one set of nodes, if graph is bipartite, or null, else
     *
     * @return one side or null
     */
    public static NodeSet computeBipartite(Graph graph) {
        final NodeSet oneSide = new NodeSet(graph);
        final NodeSet visited = new NodeSet(graph);


        for (Node v : graph.nodes()) {
            if (!visited.contains(v)) {
                oneSide.add(v);
                if (!computeBipartiteRec(v, oneSide, visited))
                    return null;
            }
        }
        return oneSide;
    }

    /**
     * recursively dones the work
     *
     * @return true, if graph is bipartite
     */
    private static boolean computeBipartiteRec(Node v, NodeSet oneSide, NodeSet visited) {
        for (Node w : v.adjacentNodes()) {
            if (!visited.contains(w)) {
                visited.add(w);
                if (!oneSide.contains(v))
                    oneSide.add(w);
                if (!computeBipartiteRec(w, oneSide, visited))
                    return false;
            } else if (oneSide.contains(v) == oneSide.contains(w))
                return false;
        }
        return true;
    }
}
