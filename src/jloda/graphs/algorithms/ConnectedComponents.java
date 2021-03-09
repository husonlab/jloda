/*
 * ConnectedComponents.java Copyright (C) 2021. Daniel H. Huson
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

import java.util.Set;

/**
 * connected components
 * Daniel Huson, 3.2021
 */
public class ConnectedComponents {
    /**
     * gets the number of connected components of the graph
     *
     * @return connected components
     */
    public static <N extends INode, E extends IEdge> int count(IGraph<N, E> graph) {
        int result = 0;
        INodeSet<N> used = graph.newNodeSet();

        for (var v : graph.nodes()) {
            if (!used.contains(v)) {
                collect(v, used);
                result++;
            }
        }
        return result;
    }

    /**
     * visit all nodes in a connected component
     *
     * @param v
     * @param used
     */
    public static <N extends INode> void collect(N v, Set<N> used) {
        used.add(v);
        for (var e : v.adjacentEdges()) {
            N w = (N) e.getOpposite(v);
            if (!used.contains(w))
                collect(w, used);
        }
    }
}
