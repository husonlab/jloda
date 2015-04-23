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

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graphview.EdgeView;
import jloda.graphview.GraphViewListener;
import jloda.phylo.*;
import jloda.util.Basic;
import jloda.util.CommandLineOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;

/**
 * tree view
 * daniel Huson, 2008
 */
public class TreeView {
    String infile;
    float factor;
    boolean rename;
    boolean rooted;
    boolean showEdgeWeights;
    boolean showNodeLabels = true;
    boolean magnifier = false;

    boolean toScale;
    String layout;

    public static void main(String[] args) throws Exception {
        new TreeView().run(args);
    }

    public void run(String[] args) throws Exception {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("TreeView - visualize a tree");
        infile = options.getMandatoryOption("-i", "input file", "");
        factor = (float) options.getOption("-f", "Scale edges by this factor", 1.0);
        rename = options.getOption("-n", "rename taxa to t1, t2...", true, false);
        rooted = options.getOption("-r", "Consider tree rooted", true, false);
        showEdgeWeights = options.getOption("+w", "Show edge weights", false, true);
        toScale = options.getOption("+s", "draw edges to scale", false, true);
        layout = options.getOption("-l", "layout: parallel, angled, circular", "parallel");
        options.done();

        final PhyloTree tree = new PhyloTree();
        tree.read(new FileReader(new File(infile)), rooted);
        //System.err.println(tree);

        if (factor != 1.0)
            tree.scaleEdgeWeights(factor);

        if (rename) {
            int count = 0;
            for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
                if (tree.getLabel(v) != null)
                    tree.setLabel(v, "t" + (++count));
            }
            tree.print(System.out, true);
        }

        final PhyloGraphView treeView = new PhyloGraphView(tree, 400, 400);
        if ("parallel".startsWith(layout)) {
            treeView.setGraphDrawer(new TreeDrawerParallel(treeView, tree));
        } else if ("angled".startsWith(layout)) {
            treeView.setGraphDrawer(new TreeDrawerAngled(treeView, tree));
        } else // circular
        {
            treeView.setGraphDrawer(new TreeDrawerParallel(treeView, tree));
        }

        treeView.getGraphDrawer().computeEmbedding(toScale);
        treeView.setDefaultEdgeDirection(EdgeView.UNDIRECTED); // for when we delete divertices

        for (Edge e = tree.getFirstEdge(); e != null; e = tree.getNextEdge(e)) {
            treeView.setDirection(e, EdgeView.UNDIRECTED);
            if (showEdgeWeights) {
                treeView.setLabel(e, "" + tree.getWeight(e));
                treeView.setLabelVisible(e, true);
            }
        }
        JFrame frame = new JFrame("TreeView");
        frame.setSize(new Dimension(400, 400));
        treeView.setPreferredSize(frame.getSize());

        treeView.trans.setLeftMargin(500);
        treeView.trans.setRightMargin(500);
        treeView.trans.setTopMargin(500);
        treeView.trans.setBottomMargin(500);

        // frame.setResizable(false);
        frame.getContentPane().add(treeView.getScrollPane());
        frame.addKeyListener(new GraphViewListener(treeView));
        frame.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                switch (ke.getKeyChar()) {
                    case '+':
                    case '=':
                        if (treeView.trans.getMagnifier().getDisplacement() + 0.05 < 1) {
                            treeView.trans.getMagnifier().setDisplacement(treeView.trans.getMagnifier().getDisplacement() + 0.05);
                            treeView.repaint();
                        }
                        break;
                    case '-':
                        if (treeView.trans.getMagnifier().getDisplacement() - 0.05 > 0.5) {
                            treeView.trans.getMagnifier().setDisplacement(treeView.trans.getMagnifier().getDisplacement() - 0.05);
                            treeView.repaint();
                        }
                        break;
                    case 'f':
                        treeView.fitGraphToWindow();
                        break;
                    case 'm': {
                        magnifier = !magnifier;
                        if (magnifier)
                            treeView.trans.getMagnifier().setMagnifier(50, 50, 100, 0.75);
                        treeView.trans.getMagnifier().setActive(magnifier);
                        break;
                    }
                    case 'p':
                        treeView.setGraphDrawer(new TreeDrawerParallel(treeView, tree));
                        treeView.getGraphDrawer().computeEmbedding(toScale);
                        treeView.trans.setCoordinateRect(treeView.getBBox());
                        treeView.fitGraphToWindow();
                        break;
                    case 'a':
                        treeView.setGraphDrawer(new TreeDrawerAngled(treeView, tree));
                        treeView.getGraphDrawer().computeEmbedding(toScale);
                        treeView.trans.setCoordinateRect(treeView.getBBox());
                        treeView.fitGraphToWindow();
                        break;
                    case 'r':
                        treeView.setGraphDrawer(new TreeDrawerRadial(treeView, tree));
                        treeView.getGraphDrawer().computeEmbedding(toScale);
                        treeView.trans.setCoordinateRect(treeView.getBBox());
                        treeView.fitGraphToWindow();
                        break;

                    case 's':
                        toScale = !toScale;
                        treeView.getGraphDrawer().computeEmbedding(toScale);
                        treeView.trans.setCoordinateRect(treeView.getBBox());
                        treeView.fitGraphToWindow();
                        break;
                    case 'w':
                        showEdgeWeights = !showEdgeWeights;
                        for (Edge e = tree.getFirstEdge(); e != null; e = tree.getNextEdge(e)) {
                            treeView.setDirection(e, EdgeView.UNDIRECTED);
                            if (showEdgeWeights) {
                                treeView.setLabel(e, "" + tree.getWeight(e));
                                treeView.setLabelVisible(e, true);
                            } else
                                treeView.setLabelVisible(e, false);
                        }
                        treeView.repaint();
                        break;

                    case 'l':
                        showNodeLabels = !showNodeLabels;
                        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
                            if (showNodeLabels) {
                                treeView.setLabelVisible(v, true);
                            } else
                                treeView.setLabelVisible(v, false);
                        }
                        treeView.repaint();
                        break;
                    case 'q':
                        System.exit(0);
                    case 'P': {
                        try {
                            tree.print(System.out, true);
                        } catch (Exception ex) {
                            Basic.caught(ex);
                        }
                    }
                    case 't': {
                        if (treeView.getNumberSelectedNodes() == 1) {
                            Node v = tree.getRoot();
                            if (v != null) {
                                if (tree.getDegree(v) == 2 && tree.getLabel(v) == null) {
                                    Edge g = tree.delDivertex(v);
                                    treeView.setDirection(g, EdgeView.UNDIRECTED);
                                    if (showEdgeWeights == true) {
                                        treeView.setLabel(g, "" + tree.getWeight(g));
                                        treeView.setLabelVisible(g, true);
                                    }
                                }
                            }
                            v = treeView.getSelectedNodes().getFirstElement();
                            tree.setRoot(v);
                            treeView.getGraphDrawer().computeEmbedding(toScale);
                            //treeView.repaint();
                        } else if (treeView.getNumberSelectedEdges() == 1) {
                            Edge e = treeView.getSelectedEdges().getFirstElement();
                            tree.setRoot(e);
                            if (showEdgeWeights && tree.getRoot() != null) {
                                Edge f = tree.getRoot().getFirstAdjacentEdge();
                                treeView.setLabel(f, "" + tree.getWeight(f));
                                treeView.setLabelVisible(f, true);
                                Edge g = tree.getRoot().getLastAdjacentEdge();
                                treeView.setLabel(g, "" + tree.getWeight(g));
                                treeView.setLabelVisible(g, true);
                            }
                            treeView.getGraphDrawer().computeEmbedding(toScale);
                        }
                        treeView.repaint();
                        break;

                    }
                    case 'x': {
                        treeView.trans.setCoordinateRect(treeView.getBBox());
                        treeView.fitGraphToWindow();
                        break;
                    }
                }
            }
        });
        frame.setVisible(true);

        treeView.trans.setCoordinateRect(treeView.getBBox());
        treeView.fitGraphToWindow();
        treeView.repaint();
    }
}

// EOF
