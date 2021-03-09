/*
 * MinimumSpanningTree.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.graphs.interfaces.*;
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
    public static <N extends INode, E extends IEdge> IEdgeSet<E> apply(IGraph<N, E> graph, Function<E, Number> weightFunction, ProgressListener progress) {
        final ArrayList<Pair<Double, E>> edges = new ArrayList<>(graph.getNumberOfEdges());
        for (var e : graph.edges()) {
            edges.add(new Pair<>(weightFunction.apply(e).doubleValue(), e));
        }
        edges.sort(Comparator.comparingDouble(Pair::getFirst));

        final INodeIntegerArray<N> component = graph.newNodeIntArray();
        int count = 0;
        for (var v : graph.nodes()) {
            component.put(v, ++count);
        }

        final IEdgeSet<E> result = graph.newEdgeSet();
        for (Pair<Double, E> pair : edges) {
            final E e = pair.getSecond();
            final int oldComponent = component.getValue((N) e.getSource());
            final int newComponent = component.getValue((N) e.getTarget());

            if (oldComponent != newComponent) {
                result.add(e);
                for (var v : graph.nodes()) {
                    if (component.getValue(v) == oldComponent)
                        component.put(v, newComponent);
                }
            }
        }
        return result;
    }
}
