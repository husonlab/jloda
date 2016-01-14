/**
 * GraphViewDemo.java 
 * Copyright (C) 2016 Daniel H. Huson
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
package jloda.progs;

import jloda.graph.*;
import jloda.graphview.EdgeView;
import jloda.graphview.GraphView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Demo of using GraphView class
 * Daniel Huson, 6.2006
 */
public class GraphViewDemo {

    public static void main(String[] args) {
        // setup small graph:
        Graph graph = new Graph();
        final GraphView graphView = new GraphView(graph);

        graphView.getScrollPane().getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        graphView.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        graphView.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        graphView.getScrollPane().addKeyListener(graphView.getGraphViewListener());

        graphView.setSize(800, 800);
        graphView.setAllowMoveNodes(true);
        graphView.setAllowRubberbandNodes(true);
        graphView.setAutoLayoutLabels(true);
        graphView.setFixedNodeSize(true);
        graphView.setMaintainEdgeLengths(false);
        graphView.setAllowEdit(false);
        graphView.setCanvasColor(Color.WHITE);

        graphView.getScrollPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
                final Dimension ps = graphView.trans.getPreferredSize();
                int x = Math.max(ps.width, graphView.getScrollPane().getWidth() - 20);
                int y = Math.max(ps.height, graphView.getScrollPane().getHeight() - 20);
                ps.setSize(x, y);
                graphView.setPreferredSize(ps);
                graphView.getScrollPane().getViewport().setViewSize(new Dimension(x, y));
                graphView.repaint();
            }
        });

        Random rand = new Random(666);
        Node[] nodes = new Node[500];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = graph.newNode();
            graphView.setLocation(nodes[i], rand.nextInt(100), rand.nextInt(100));
            for (int j = 0; j < i; j++) {
                if (rand.nextDouble() < 0.003)
                    graph.newEdge(nodes[i], nodes[j]);
            }
        }

        // add labels to nodes:
        for (int i = 0; i < nodes.length; i++)
            graphView.setLabel(nodes[i], "Node " + i);

        // draw all edges directed edges
        for (Edge e = nodes[0].getFirstAdjacentEdge(); e != null; e = nodes[0].getNextAdjacentEdge(e)) {
            graphView.setDirection(e, EdgeView.DIRECTED);
        }

        // compute simple layout:
        FruchtermanReingoldLayout fruchtermanReingoldLayout = new FruchtermanReingoldLayout(graph, null);
        NodeArray<Point2D> coordinates = new NodeArray<>(graph);
        fruchtermanReingoldLayout.apply(1000, coordinates);
        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            graphView.setLocation(v, coordinates.get(v));
            graphView.setHeight(v, 10);
            graphView.setWidth(v, 10);
        }
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            graphView.setDirection(e, EdgeView.UNDIRECTED);
        }

        // setup jframe with graphView and quit button:
        final JFrame frame = new JFrame("GraphViewDemo");
        frame.setSize(graphView.getSize());
        frame.addKeyListener(graphView.getGraphViewListener());

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(graphView.getScrollPane(), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        // show the frame:
        frame.setVisible(true);

        graphView.setSize(400, 400);

        graphView.fitGraphToWindow();


    }

}
