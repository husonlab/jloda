/**
 * Tree2MeganCSV.java 
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

import jloda.graph.Node;
import jloda.graphview.IGraphDrawer;
import jloda.phylo.PhyloGraphView;
import jloda.phylo.PhyloTree;
import jloda.phylo.TreeDrawerRadial;
import jloda.util.CommandLineOptions;
import jloda.util.UsageException;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * processes a tree containing placements of reads and returns a CVS file that can be processed by MEGAN
 * Daniel Huson, 10.2008
 */
public class Tree2MeganCSV {

    public static void main(String[] args) throws UsageException, IOException {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("Tree2MerganCSV - analyzes placement of reads in a tree and output a MEGAN CSV file");

        String inFile = options.getOption("-i", "Input file containing a tree in Newick format", "");
        String outFile = options.getOption("-o", "Out file (in CSV) format", "");
        List readIds = options.getOption("-r", "List of read ids", new LinkedList());
        boolean showTree = options.getOption("-s", "Show first tree", true, false);
        options.done();

        System.err.println("Read Ids:");
        for (Object readId2 : readIds) {
            System.err.println(" " + readId2);
        }


        BufferedReader r;
        if (inFile.length() == 0) // no input file given, read from standard in
            r = new BufferedReader(new InputStreamReader(System.in));
        else
            r = new BufferedReader(new FileReader(new File(inFile)));

        String aLine;
        int treeNumber = 0;
        while ((aLine = r.readLine()) != null) {
            if (aLine.length() > 0 && !aLine.startsWith("#")) {
                PhyloTree tree = new PhyloTree();

                tree.parseBracketNotation(aLine, false);
                treeNumber++;

                // for debugging purposes, show the  first tree:
                if (treeNumber == 1 && showTree)
                    showTree(tree);

                // see if we can find any of the reads in the tree:
                for (Object readId1 : readIds) {
                    String readId = (String) readId1;

                    boolean found = false;
                    for (Node v = tree.getFirstNode(); !found && v != null; v = tree.getNextNode(v)) {
                        if (tree.getLabel(v) != null && tree.getLabel(v).equals(readId)) {
                            System.err.println("Found readId " + readId + " in tree " + treeNumber + " on node v=" + v);
                            found = true;
                        }
                    }
                    if (!found)
                        System.err.println("Warning: readID " + readId + " in tree " + treeNumber + ": not found");
                }

            }
        }
    }

    /**
     * draws the tree
     *
     * @param tree
     */
    public static void showTree(PhyloTree tree) {
        PhyloGraphView treeView = new PhyloGraphView(tree);

        IGraphDrawer drawer = new TreeDrawerRadial(treeView, tree);
        drawer.computeEmbedding(true);

        JFrame frame = new JFrame("Tree");
        frame.setSize(treeView.getSize());
        frame.addKeyListener(treeView.getGraphViewListener());

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(treeView.getScrollPane(), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // show the frame:
        frame.setVisible(true);

        treeView.setSize(600, 600);
        treeView.fitGraphToWindow();

    }
}
