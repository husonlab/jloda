/*
 * Dijkstra.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.graph;

import jloda.util.Basic;

import java.util.*;

/**
 * Dijkstras algorithm for single source shortest path, non-negative edge lengths
 *
 * @author huson
 *         Date: 11-Dec-2004
 */
public class Dijkstra {
    /**
     * compute single source shortest path from source to sink, non-negative edge weights
     *
     * @param graph  with edges labeled by Integers
     * @param source
     * @param sink
     * @return shortest path from source to sink
     */
    public static List compute(final Graph graph, Node source, Node sink) throws Exception {
        try {
            NodeArray<Node> predecessor = new NodeArray<>(graph);
            NodeIntegerArray dist = new NodeIntegerArray(graph);
            SortedSet<Node> priorityQueue = newFullQueue(graph, dist);

            // init:
            for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
                dist.set(v, 1000000);
                predecessor.put(v, null);
            }
            dist.set(source, 0);

            // main loop:
            while (!priorityQueue.isEmpty()) {
                int size = priorityQueue.size();
                Node u = priorityQueue.first();
                priorityQueue.remove(u);
                if (priorityQueue.size() != size - 1)
                    throw new RuntimeException("remove u=" + u + " failed: size=" + size);

                for (Edge e : u.outEdges()) {
                    int weight = (Integer) graph.getInfo(e);
                    Node v = graph.getOpposite(u, e);
                    if (dist.getValue(v) > dist.getValue(u) + weight) {
                        // priorty of v changes, so must re-and to queue:
                        priorityQueue.remove(v);
                        dist.set(v, dist.getValue(u) + weight);
                        priorityQueue.add(v);
                        predecessor.put(v, u);
                    }
                }
            }
            System.err.println("done main loop");
            List<Node> result = new LinkedList<>();
            Node v = sink;
            while (v != source) {
                if (v == null)
                    throw new Exception("No path from sink back to source");
                System.err.println("v: " + v);
                if (v != sink)
                    result.add(0, v);
                v = predecessor.get(v);
            }
            System.err.println("# Dijkstra: " + result.size());
            return result;

        } catch (NotOwnerException ex) {
            Basic.caught(ex);
            return null;
        }
    }

    /**
     * setups the priority queue
     *
     * @param graph
     * @param dist
     * @return full priority queue
     * @
     */
    static public SortedSet<Node> newFullQueue(final Graph graph, final NodeIntegerArray dist) {
        SortedSet<Node> queue = new TreeSet<>((v1, v2) -> {
            int weight1 = dist.getValue(v1);
            int weight2 = dist.getValue(v2);
            //System.out.println("weight1 " + weight1 + " weight2 " + weight2);
            //System.out.println("graph.getId(v1) " + graph.getId(v1) + " graph.getId(v2) " + graph.getId(v2));
            if (weight1 < weight2)
                return -1;
            else if (weight1 > weight2)
                return 1;
            else return Integer.compare(graph.getId(v1), graph.getId(v2));
        });
        for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v))
            queue.add(v);
        return queue;
    }


}
