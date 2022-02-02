/*
 * MinimumSpanningTree.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.graph.EdgeSet;
import jloda.graph.Graph;
import jloda.util.Pair;
import jloda.util.progress.ProgressListener;

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
	 */
    public static EdgeSet apply(Graph graph, Function<Edge, Number> weightFunction, ProgressListener progress) {
        final var edges = new ArrayList<Pair<Double, Edge>>(graph.getNumberOfEdges());
        for (var e : graph.edges()) {
            edges.add(new Pair<>(weightFunction.apply(e).doubleValue(), e));
        }
        edges.sort(Comparator.comparingDouble(Pair::getFirst));

        var component = graph.newNodeIntArray();
        int count = 0;
        for (var v : graph.nodes()) {
            component.put(v, ++count);
        }

        var result = graph.newEdgeSet();
        for (Pair<Double, Edge> pair : edges) {
            var e = pair.getSecond();
            final int oldComponent = component.get(e.getSource());
            final int newComponent = component.get(e.getTarget());

            if (oldComponent != newComponent) {
                result.add(e);
                for (var v : graph.nodes()) {
                    if (component.get(v) == oldComponent)
                        component.put(v, newComponent);
                }
            }
        }
        return result;
    }
}
