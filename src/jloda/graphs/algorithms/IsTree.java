/*
 * IsTree.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.graphs.algorithms;


import jloda.graphs.interfaces.IEdge;
import jloda.graphs.interfaces.IGraph;
import jloda.graphs.interfaces.INode;
import jloda.graphs.interfaces.INodeSet;

/**
 * is the graph a tree?
 */
public class IsTree {

    public static <N extends INode, E extends IEdge> boolean apply(IGraph<N, E> graph) {
        try {
            if (graph.getNumberOfNodes() > 0) {
                final INodeSet<N> visited = graph.newNodeSet();
                return !hasCycleRec(graph.nodes().iterator().next(), null, visited) && visited.size() == graph.getNumberOfNodes();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static <N extends INode, E extends IEdge> boolean hasCycleRec(N v, E e, INodeSet<N> visited) {
        visited.add(v);
        for (var f : v.adjacentEdges()) {
            if (f != e) {
                N w = (N) f.getOpposite(v);
                if (visited.contains(w) || hasCycleRec(w, f, visited))
                    return true;
            }
        }
        return false;
    }

}
