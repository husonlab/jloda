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

package jloda.progs;

import jloda.graph.*;
import jloda.graphview.EdgeView;
import jloda.graphview.GraphView;
import jloda.graphview.ITransformChangeListener;
import jloda.graphview.Transform;

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

        graphView.trans.addChangeListener(new ITransformChangeListener() {
            public void hasChanged(Transform trans) {
                if (false) {
                    final Dimension ps = trans.getPreferredSize();
                    //int x = Math.max(ps.width, graphView.getScrollPane().getWidth() - 20);//
                    //int y = Math.max(ps.height, graphView.getScrollPane().getHeight() - 20);//
                    int x = frame.getWidth();
                    int y = frame.getHeight();
                    ps.setSize(x, y);
                    graphView.setPreferredSize(trans.getPreferredSize());
                    graphView.getScrollPane().getViewport().setViewSize(trans.getPreferredSize());
                    graphView.repaint();
                }
            }
        });
        graphView.getScrollPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
                {
                    if (graphView.getScrollPane().getSize().getHeight() > 400 && graphView.getScrollPane().getSize().getWidth() > 400)
                        graphView.fitGraphToWindow();
                    else
                        graphView.trans.fireHasChanged();
                }
            }
        });


        // show the frame:
        frame.setVisible(true);

        graphView.setSize(400, 400);

        graphView.fitGraphToWindow();


    }

}
