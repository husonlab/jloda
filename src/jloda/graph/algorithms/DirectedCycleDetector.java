/*
 * DirectedCycleDetector.java Copyright (C) 2024 Daniel H. Huson
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

import java.util.Collection;
import java.util.Stack;

/**
 * Detection of directed cycles
 * See Sedewick and Wayne, Algorithms, 4th ed., 2011
 * Created by huson on 8/21/16.
 */
public class DirectedCycleDetector {
    private final Graph graph;
    private final NodeSet marked;
    private final NodeArray<Edge> edgeTo;
    private final NodeSet onStack;
    private final Stack<Edge> cycle;

    public static boolean apply(Graph graph) {
        return (new DirectedCycleDetector(graph).hasCycle());
    }

    /**
     * constructor
     *
	 */
    public DirectedCycleDetector(Graph graph) {
        this.graph = graph;
        onStack = new NodeSet(graph);
        edgeTo = new NodeArray<>(graph);
        marked = new NodeSet(graph);
        cycle = new Stack<>();
    }

    /**
     * detects a cycle, if one exists
     *
     * @return true, if cycle detected
     */
    public boolean apply() {
        onStack.clear();
        edgeTo.clear();
        marked.clear();
        cycle.clear();

        for (var v : graph.nodes()) {
            if (!marked.contains(v))
                detectRec(v);
        }
        return hasCycle();
    }

    /**
     * recursively does the work
     *
	 */
    private void detectRec(Node v) {
        onStack.add(v);
        marked.add(v);

        for (var e : v.outEdges()) {
            var w = e.getTarget();
            if (this.hasCycle())
                return;
            else if (!marked.contains(w)) {
                edgeTo.put(w, e);
                detectRec(w);
            } else if (onStack.contains(w)) {
                cycle.push(e);
                for (Node x = v; x != w; x = edgeTo.get(x).getSource())
                    cycle.push(edgeTo.get(x));
            }
        }
        onStack.remove(v);
    }

    /**
     * does graph have a cycle?
     *
     * @return true, if has cycle
     */
    public boolean hasCycle() {
        return cycle.size() > 0;
    }

    /**
     * gets the cycle
     *
     * @return cycle
     */
    public Collection<Edge> cycle() {
        return cycle;
    }
}
