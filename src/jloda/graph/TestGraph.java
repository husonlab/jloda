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

package jloda.graph;


import jloda.util.IterationUtils;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * test basic features of graph and associated data structures
 * Daniel Huson, 3.2021
 */
public class TestGraph extends TestCase {

    public void testAddNodesAndEdges() {
        var nodes = new ArrayList<Node>();
        var edges = new ArrayList<Edge>();
        final Graph graph = createGraph(nodes, edges);

        var nodeSet = graph.newNodeSet();
        nodeSet.addAll(IterationUtils.asList(graph.nodes()));

        for (var v : graph.nodes()) {
            assertTrue(nodeSet.contains(v));
        }

        var edgeSet = graph.newEdgeSet();
        edgeSet.addAll(IterationUtils.asList(graph.edges()));

        for (var e : graph.edges()) {
            assertTrue(edgeSet.contains(e));
        }

        assertEquals("nodes", 11, graph.getNumberOfNodes());
        assertEquals("edges", 20, graph.getNumberOfEdges());

    }

    public void testRemoveNodesAndEdges() {
        var nodes = new ArrayList<Node>();
        var edges = new ArrayList<Edge>();
        final Graph graph = createGraph(nodes, edges);


        Node vGone = graph.findNodeById(4);
        graph.deleteNode(vGone);
        assertNull(vGone.getOwner());

        Edge eGone = graph.findEdgeById(13);
        graph.deleteEdge(eGone);
        assertNull(eGone.getOwner());


        assertEquals("nodes", 10, graph.getNumberOfNodes());
        assertEquals("edges", 16, graph.getNumberOfEdges());
    }

    public void testNodeSetsEdgeSets() {
        var nodes = new ArrayList<Node>();
        var edges = new ArrayList<Edge>();
        final Graph graph = createGraph(nodes, edges);

        var nodeSet = graph.newNodeSet();
        nodeSet.addAll(IterationUtils.asList(graph.nodes()));

        assertEquals("nodes", graph.getNumberOfNodes(), nodeSet.size());

        var edgeSet = graph.newEdgeSet();
        edgeSet.addAll(IterationUtils.asList(graph.edges()));

        assertEquals("edges", graph.getNumberOfEdges(), edgeSet.size());

        Node vGone = graph.findNodeById(4);
        graph.deleteNode(vGone);
        assertNull(vGone.getOwner());

        Edge eGone = graph.findEdgeById(13);
        graph.deleteEdge(eGone);
        assertNull(eGone.getOwner());

        assertEquals("nodes", graph.getNumberOfNodes(), nodeSet.size());
        assertEquals("edges", graph.getNumberOfEdges(), edgeSet.size());


        for (var v : graph.nodes()) {
            assertTrue("node " + v.getId(), nodeSet.contains(v));
        }

        {
            NotOwnerException exception = null;
            try {
                nodeSet.contains(vGone); // should throw exception
            } catch (NotOwnerException ex) {
                exception = ex;
            }
            assertNotNull(exception);
        }

        for (var e : graph.edges()) {
            assertTrue("edge " + e.getId(), edgeSet.contains(e));
        }

        {
            NotOwnerException exception = null;

            try {
                edgeSet.contains(eGone);
            } catch (NotOwnerException ex) {
                exception = ex;
            }
            assertNotNull(exception);
        }
    }

    public void testNodeArraysEdgeArray() {
        var nodes = new ArrayList<Node>();
        var edges = new ArrayList<Edge>();
        final Graph graph = createGraph(nodes, edges);

        NodeArray<String> nodeArray = graph.newNodeArray();
        for (var v : graph.nodes()) {
            nodeArray.put(v, v.getInfo().toString());
        }
        assertEquals("nodes", graph.getNumberOfNodes(), IterationUtils.count(nodeArray.values()));

        EdgeArray<String> edgeArray = graph.newEdgeArray();
        for (var e : graph.edges()) {
            edgeArray.put(e, e.getInfo().toString());
        }
        assertEquals("edges", graph.getNumberOfEdges(), IterationUtils.count(edgeArray.values()));

        Node vGone = graph.findNodeById(4);
        graph.deleteNode(vGone);
        assertNull(vGone.getOwner());

        Edge eGone = graph.findEdgeById(13);
        graph.deleteEdge(eGone);
        assertNull(eGone.getOwner());

        assertEquals("nodes", graph.getNumberOfNodes(), IterationUtils.count(nodeArray.values()));
        assertEquals("edges", graph.getNumberOfEdges(), IterationUtils.count(edgeArray.values()));

        for (var v : graph.nodes()) {
            assertEquals(v.getInfo(), nodeArray.getValue(v));
        }

        for (var s : nodeArray.values()) {
            assertNotNull(s);
        }

        {
            NotOwnerException exception = null;
            try {
                nodeArray.getValue(vGone); // should throw exception
            } catch (NotOwnerException ex) {
                exception = ex;
            }
            assertNotNull(exception);
        }

        for (var e : graph.edges()) {
            assertEquals(e.getInfo(), edgeArray.getValue(e));
        }

        for (var s : edgeArray.values()) {
            assertNotNull(s);
        }
        {
            NotOwnerException exception = null;

            try {
                edgeArray.getValue(eGone);
            } catch (NotOwnerException ex) {
                exception = ex;
            }
            assertNotNull(exception);
        }
    }

    public static Graph createGraph(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        final Graph graph = new Graph();
        nodes.clear();
        for (int i = 0; i < 10; i++) {
            nodes.add(graph.newNode("V" + (i + 1)));
        }
        nodes.add(graph.newNode("V" + (11)));

        edges.clear();
        for (int i = 0; i < 10; i++) {
            edges.add(graph.newEdge(nodes.get(i), nodes.get((i + 1) % 10), "E(" + i + "," + ((i + 1) % 10) + ")"));
            edges.add(graph.newEdge(nodes.get(i), nodes.get(10), "E(" + i + ",11)"));

        }
        return graph;
    }
}
