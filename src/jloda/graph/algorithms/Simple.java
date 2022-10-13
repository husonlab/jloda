/*
 * Simple.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * methods for simple graphs
 * Daniel Huson, 5.2021
 */
public class Simple {
    /**
     * make a simple copy of the src graph
     */
    public static void makeSimple(Graph srcGraph, Graph tarGraph, NodeArray<Node> tar2srcNode, EdgeArray<Edge> tar2srcEdge) {
        var simpleEdges = simpleEdges(srcGraph);
        NodeArray<Node> src2tarNode = srcGraph.newNodeArray();
        EdgeArray<Edge> src2tarEdge = srcGraph.newEdgeArray();
        srcGraph.extract(srcGraph.getNodesAsList(), simpleEdges, tarGraph, src2tarNode, src2tarEdge);
        for (var v : src2tarNode.keys()) {
            var w = src2tarNode.get(v);
            if (w != null)
                tar2srcNode.put(w, v);
        }
        for (var e : src2tarEdge.keys()) {
            var f = src2tarEdge.get(e);
            if (f != null)
                tar2srcEdge.put(f, e);
        }
    }

    /**
     * get all simple edges
     */
    public static List<Edge> simpleEdges(Graph graph) {
        var pairs = new HashSet<Pair<Node, Node>>();
        var simpleEdges = new ArrayList<Edge>();
        for (var e : graph.edges()) {

            Pair<Node, Node> pair;
            if (e.getSource().getId() < e.getTarget().getId())
                pair = new Pair<>(e.getSource(), e.getTarget());
            else if (e.getSource().getId() > e.getTarget().getId())
                pair = new Pair<>(e.getTarget(), e.getSource());
            else // self loop...
                continue;
            if (!pairs.contains(pair)) {
                pairs.add(pair);
                simpleEdges.add(e);
            }
        }
        return simpleEdges;
    }
}
