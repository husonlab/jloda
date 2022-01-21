/*
 * IsTree.java Copyright (C) 2022 Daniel H. Huson
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


import jloda.graph.Edge;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeSet;

/**
 * is the graph a tree?
 */
public class IsTree {

    public static boolean apply(Graph graph) {
        try {
            if (graph.getNumberOfNodes() > 0) {
                var visited = graph.newNodeSet();
                return !hasCycleRec(graph.getFirstNode(), null, visited) && visited.size() == graph.getNumberOfNodes();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean hasCycleRec(Node v, Edge e, NodeSet visited) {
        visited.add(v);
        for (var f : v.adjacentEdges()) {
            if (f != e) {
                Node w = f.getOpposite(v);
                if (visited.contains(w) || hasCycleRec(w, f, visited))
                    return true;
            }
        }
        return false;
    }

}
