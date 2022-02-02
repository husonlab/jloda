/*
 * Dijkstra.java Copyright (C) 2022 Daniel H. Huson
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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Dijkstras algorithm for single source shortest path, non-negative edge lengths
 *
 * @author huson
 * Date: 11-Dec-2004
 */
public class Dijkstra {
    /**
     * compute single source shortest path from source to sink, non-negative edge weights
     *
     * @param graph  with adjacentEdges labeled by Integers
     * @return shortest path from source to sink
     */
    public static List<Node> compute(final Graph graph, Node source, Node sink, Function<Edge, Number> weights) {
        NodeArray<Node> predecessor = graph.newNodeArray();

        var dist = graph.newNodeDoubleArray();
        var priorityQueue = newFullQueue(graph, dist);

        // init:
        for (var v : graph.nodes()) {
            dist.put(v, 1000000.0);
            predecessor.put(v, null);
        }
        dist.put(source, 0.0);

        // main loop:
        while (!priorityQueue.isEmpty()) {
            var size = priorityQueue.size();
            var u = priorityQueue.first();
            priorityQueue.remove(u);
            if (priorityQueue.size() != size - 1)
                throw new RuntimeException("remove u=" + u + " failed: size=" + size);

            for (var e : u.outEdges()) {
                var weight = weights.apply(e).doubleValue();
                var v = e.getOpposite(u);
                if (dist.get(v) > dist.get(u) + weight) {
                    // priorty of v changes, so must re-and to queue:
                    priorityQueue.remove(v);
                    dist.put(v, dist.get(u) + weight);
                    priorityQueue.add(v);
                    predecessor.put(v, u);
                }
            }
        }
        System.err.println("done main loop");
        var result = new ArrayList<Node>();
        var v = sink;
        while (v != source) {
            if (v == null)
                throw new RuntimeException("No path from sink back to source");
            System.err.println("v: " + v);
            if (v != sink)
                result.add(0, v);
            v = predecessor.get(v);
        }
        System.err.println("# Dijkstra: " + result.size());
        return result;
    }

    /**
     * setups the priority queue
     *
     * @return full priority queue
     * @
     */
    static public SortedSet<Node> newFullQueue(final Graph graph, final NodeDoubleArray dist) {
        var queue = new TreeSet<Node>((v1, v2) -> {
            var weight1 = dist.get(v1);
            var weight2 = dist.get(v2);
            //System.out.println("weight1 " + weight1 + " weight2 " + weight2);
            //System.out.println("graph.getId(v1) " + graph.getId(v1) + " graph.getId(v2) " + graph.getId(v2));
            if (weight1 < weight2)
                return -1;
            else if (weight1 > weight2)
                return 1;
            else return Integer.compare(v1.getId(), v2.getId());
        });
        for (var v : graph.nodes())
            queue.add(v);
        return queue;
    }
}
