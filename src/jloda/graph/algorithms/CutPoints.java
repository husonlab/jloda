/*
 * CutPoints.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.graph.NodeIntArray;
import jloda.graph.NodeSet;
import jloda.util.Counter;

import java.util.function.Function;

/**
 * computes all cut points in a graph
 * Daniel Huson, 6.2021
 * See https://cp-algorithms.com/graph/cutpoints.html
 */
public class CutPoints {
    public static NodeSet apply(Graph graph) {
        return apply(graph, v -> true);
    }

    public static NodeSet apply(Graph graph, Function<Node, Boolean> useNode) {
        var tin = graph.newNodeIntArray();
        var low = graph.newNodeIntArray();
        var timer = new Counter(1);

        var result = graph.newNodeSet();

        for (var v : graph.nodes()) {
            if (tin.get(v) == null && useNode.apply(v))
                dfs(v, null, timer, tin, low, useNode, result);
        }
        return result;
    }

    private static void dfs(Node v, Node p, Counter timer, NodeIntArray tin, NodeIntArray low, Function<Node, Boolean> useNode, NodeSet result) {
        var t = (int) timer.getAndIncrement();
        tin.put(v, t);
        low.put(v, t);
        var children = 0;
        for (var to : v.adjacentNodes()) {
            if (!useNode.apply(to) || to == p)
                continue;
            if (tin.get(to) != null) { // already visited
                low.put(v, Math.min(low.get(v), low.get(to)));
            } else {
                dfs(to, v, timer, tin, low, useNode, result);
                low.put(v, Math.min(low.get(v), low.get(to)));
                if (low.get(to) >= tin.get(v) && p != null) {
                    result.add(v);
                }
                children++;
            }
        }
        if (p == null && children > 1) {
            result.add(v);
        }
    }
}
