/*
 * ConnectedComponents.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.graph.Graph;
import jloda.graph.Node;

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
    public static int count(Graph graph) {
        var result = 0;
        var used = graph.newNodeSet();

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
	 */
    public static void collect(Node v, Set<Node> used) {
        used.add(v);
        for (var e : v.adjacentEdges()) {
            var w = e.getOpposite(v);
            if (!used.contains(w))
                collect(w, used);
        }
    }
}
