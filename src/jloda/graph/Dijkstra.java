/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package jloda.graph;

import jloda.util.Basic;
import jloda.util.NotOwnerException;

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
                predecessor.set(v, null);
            }
            dist.set(source, 0);

            // main loop:
            while (priorityQueue.isEmpty() == false) {
                int size = priorityQueue.size();
                Node u = priorityQueue.first();
                priorityQueue.remove(u);
                if (priorityQueue.size() != size - 1)
                    throw new RuntimeException("remove u=" + u + " failed: size=" + size);

                Iterator out = graph.getOutEdges(u);
                while (out.hasNext()) {
                    Edge e = (Edge) out.next();
                    int weight = (Integer) graph.getInfo(e);
                    Node v = graph.getOpposite(u, e);
                    if (dist.getValue(v) > dist.getValue(u) + weight) {
                        // priorty of v changes, so must re-and to queue:
                        priorityQueue.remove(v);
                        dist.set(v, dist.getValue(u) + weight);
                        priorityQueue.add(v);
                        predecessor.set(v, u);
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
        SortedSet<Node> queue = new TreeSet<>(new Comparator<Node>() {
            public int compare(Node v1, Node v2) {
                int weight1 = dist.getValue(v1);
                int weight2 = dist.getValue(v2);
                //System.out.println("weight1 " + weight1 + " weight2 " + weight2);
                //System.out.println("graph.getId(v1) " + graph.getId(v1) + " graph.getId(v2) " + graph.getId(v2));
                if (weight1 < weight2)
                    return -1;
                else if (weight1 > weight2)
                    return 1;
                else if (graph.getId(v1) < graph.getId(v2))
                    return -1;
                else if (graph.getId(v1) > graph.getId(v2))
                    return 1;
                else
                    return 0;
            }
        });
        for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v))
            queue.add(v);
        return queue;
    }


}
