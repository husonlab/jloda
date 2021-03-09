/*
 *  TestAGraph.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.graphs.agraph;

import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graphs.bgraph.BGraph;
import jloda.graphs.interfaces.*;
import junit.framework.TestCase;

import java.util.ArrayList;

public class TestAGraph extends TestCase {

    public void testCreateAGraph() {

        final Graph graph = new Graph();

        var nodes = new ArrayList<Node>();
        for (int i = 0; i < 10; i++) {
            nodes.add(graph.newNode("V" + i));
        }

        for (int i = 0; i < 10; i++) {
            graph.newEdge(nodes.get(i), nodes.get((i + 1) % 10), "E(" + i + "," + ((i + 1) % 10) + ")");
        }

        final AGraph aGraph = AGraph.create(graph);

        assertEquals(graph.getNumberOfNodes(), aGraph.getNumberOfNodes());
        assertEquals(graph.getNumberOfEdges(), aGraph.getNumberOfEdges());

    }

    public void testCreateASubGraphFromGraph() {
        runTestCreateASubGraph(new Graph());
    }

    public void testCreateASubGraphFromBGraph() {
        runTestCreateASubGraph(new BGraph());
    }

    public void testCreateAGraphFromAGraph() {
        var graph1 = runTestCreateASubGraph(new BGraph());

        var graph2 = AGraph.create(graph1);

        assertEquals(graph1.getNumberOfNodes(), graph2.getNumberOfNodes());
        assertEquals(graph1.getNumberOfEdges(), graph2.getNumberOfEdges());

    }

    private <N extends INode, E extends IEdge> AGraph runTestCreateASubGraph(IEditableGraph<N, E> graph) {
        var nodes = new ArrayList<N>();
        for (int i = 0; i < 10; i++) {
            nodes.add(graph.newNode("V" + i));
        }

        for (int i = 0; i < 10; i++) {
            graph.newEdge(nodes.get(i), nodes.get((i + 1) % 10), "E(" + i + "," + ((i + 1) % 10) + ")");
        }

        INodeSet<N> nodeSet = graph.newNodeSet();
        nodeSet.add(nodes.get(1));
        nodeSet.add(nodes.get(2));
        nodeSet.add(nodes.get(3));

        IEdgeSet<E> edgeSet = graph.newEdgeSet();

        for (var v : nodeSet) {
            for (var e : v.outEdges()) {
                if (nodeSet.contains(e.getTarget()))
                    edgeSet.add((E) e);
            }
        }


        final AGraph aGraph = AGraph.create(graph, nodeSet, edgeSet);

        assertEquals(aGraph.getNumberOfNodes(), nodeSet.size());
        assertEquals(aGraph.getNumberOfEdges(), edgeSet.size());

        return aGraph;

    }
}
