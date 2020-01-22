/*
 * MinimumSpanningTree.java Copyright (C) 2020. Daniel H. Huson
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
 *
 */

package jloda.graph;

import jloda.util.Pair;
import jloda.util.ProgressListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;

/**
 * compute the edges of a minimum spanning tree
 * Daniel Huson, 3.2019
 */
public class MinimumSpanningTree {

    /**
     * compute a minimum spanning tree
     *
     * @param graph
     * @param weightFunction
     * @param progress
     * @return
     */
    public static EdgeSet apply(Graph graph, Function<Edge, Number> weightFunction, ProgressListener progress) {
        final ArrayList<Pair<Double, Edge>> edges = new ArrayList<>(graph.getNumberOfEdges());
        for (Object obj : graph.edges()) {
            Edge e = (Edge) obj;
            edges.add(new Pair<>(weightFunction.apply(e).doubleValue(), e));
        }
        edges.sort(Comparator.comparingDouble(Pair::getFirst));

        final NodeArray<Integer> component = new NodeArray<>(graph);
        int count = 0;
        for (Object obj : graph.getNodesAsSet()) {
            Node v = (Node) obj;
            component.put(v, ++count);
        }

        final EdgeSet result = new EdgeSet(graph);
        for (Pair<Double, Edge> pair : edges) {
            final Edge e = pair.getSecond();
            final int oldComponent = component.get(e.getSource());
            final int newComponent = component.get(e.getTarget());

            if (oldComponent != newComponent) {
                result.add(e);
                for (Object obj : graph.getNodesAsSet()) { // todo: should do this more efficiently
                    Node v = (Node) obj;
                    if (component.get(v) == oldComponent)
                        component.put(v, newComponent);
                }
            }
        }
        return result;
    }
}
