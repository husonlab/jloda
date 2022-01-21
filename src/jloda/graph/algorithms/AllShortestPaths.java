/*
 * AllShortestPaths.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.graph.NodeIntArray;

import java.util.function.Function;

/**
 * provides all shortest paths
 * Adapted from: http://underpop.online.fr/j/java/help/all-pairs-shortest-paths.html
 * Daniel Huson, 3.2021
 */
public class AllShortestPaths {
    private final NodeIntArray node2index;
    private final Edge[][] path;
    private final double[][] distances;

    public AllShortestPaths(Graph graph) {
        this(graph, e -> 1.0);
    }

    public AllShortestPaths(Graph graph, Function<Edge, ? extends Number> weights) {
        final var n = graph.getNumberOfNodes();
        node2index = graph.newNodeIntArray();
        var nodes = new Node[n];
        {
            var index = 0;
            for (var v : graph.nodes()) {
                node2index.put(v, index);
                nodes[index++] = v;
            }
        }
        path = new Edge[n][n];
        distances = new double[n][n];

        for (var s = 0; s < n; s++) {
            for (var t = 0; t < n; t++) {
                if (s != t)
                    distances[s][t] = Double.MAX_VALUE;
            }
        }

        for (var s = 0; s < n; s++) {
            for (var t = 0; t < n; t++) {
                if (s != t) {
                    var e = nodes[s].getCommonEdge(nodes[t]);
                    if (e != null) {
                        path[s][t] = e;
                        distances[s][t] = weights.apply(e).doubleValue();
                    }
                }
            }
        }
        for (var i = 0; i < n; i++)
            for (var s = 0; s < n; s++)
                if (path[s][i] != null)
                    for (var t = 0; t < n; t++)
                        if (s != t)
                            if (distances[s][t] > distances[s][i] + distances[i][t]) {
                                path[s][t] = path[s][i];
                                distances[s][t] = distances[s][i] + distances[i][t];
                            }
    }

    public Edge path(Node s, Node t) {
        return path[node2index.get(s)][node2index.get(t)];
    }

    public double getDistance(Node s, Node t) {
        return distances[node2index.get(s)][node2index.get(t)];
    }

    public double[][] getDistances() {
        return distances;
    }

    public static double[][] apply(Graph graph) {
        var algorithm = new AllShortestPaths(graph);
        return algorithm.getDistances();
    }

    public static double[][] apply(Graph graph, Function<Edge, ? extends Number> weights) {
        var algorithm = new AllShortestPaths(graph, weights);
        return algorithm.getDistances();
    }
}