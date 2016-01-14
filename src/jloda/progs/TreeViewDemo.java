/**
 * TreeViewDemo.java 
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

import jloda.graphview.IGraphDrawer;
import jloda.graphview.ITransformChangeListener;
import jloda.graphview.Transform;
import jloda.phylo.PhyloGraphView;
import jloda.phylo.PhyloTree;
import jloda.phylo.TreeDrawerCircular;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

/**
 * Demo of using tree view
 * Daniel Huson, 9.2011
 */
public class TreeViewDemo {

    public static void main(String[] args) throws IOException {


        // setup small tree:
        PhyloTree tree = new PhyloTree();

        tree.parseBracketNotation("((a,b),(c,d),e);", true);

        // setup graph view:

        final PhyloGraphView treeView = new PhyloGraphView(tree);

        // add labels to nodes:

        // compute simple layout:
        IGraphDrawer treeDrawer = new TreeDrawerCircular(treeView, tree);
        treeDrawer.computeEmbedding(true);
        treeView.setGraphDrawer(treeDrawer);

        // setup jframe with graphView and quit button:
        JFrame frame = new JFrame("Tree View Demo");
        treeView.setSize(800, 800);
        frame.setSize(treeView.getSize());
        frame.addKeyListener(treeView.getGraphViewListener());

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(treeView.getScrollPane(), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        treeView.trans.addChangeListener(new ITransformChangeListener() {
            public void hasChanged(Transform trans) {
                final Dimension ps = trans.getPreferredSize();
                int x = Math.max(ps.width, treeView.getScrollPane().getWidth() - 20);
                int y = Math.max(ps.height, treeView.getScrollPane().getHeight() - 20);
                ps.setSize(x, y);
                treeView.setPreferredSize(ps);
                treeView.getScrollPane().getViewport().setViewSize(new Dimension(x, y));
                treeView.repaint();
            }
        });

        treeView.getScrollPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
                {
                    if (treeView.getScrollPane().getSize().getHeight() > 400 && treeView.getScrollPane().getSize().getWidth() > 400)
                        treeView.fitGraphToWindow();
                    else
                        treeView.trans.fireHasChanged();
                }
            }
        });

        // show the frame:
        frame.setVisible(true);

        treeView.trans.setCoordinateRect(treeView.getBBox());
        treeView.getScrollPane().revalidate();
        treeView.fitGraphToWindow();
    }

}
