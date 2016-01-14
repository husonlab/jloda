/**
 * GeneEvolutionSimulator.java 
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
/**
 * simulates gene evolution along a tree
 * @version $Id: GeneEvolutionSimulator.java,v 1.12 2007-01-14 02:55:44 huson Exp $
 * @author Daniel Huson
 * 8.2003
 */
package jloda.progs;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graphview.EdgeView;
import jloda.graphview.GraphViewListener;
import jloda.phylo.PhyloTree;
import jloda.phylo.PhyloTreeView;
import jloda.util.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.BitSet;
import java.util.Random;

/**
 * Simulates gene evolution along a tree
 */
public class GeneEvolutionSimulator {
    private static final Random rand = new Random();
    private static boolean verbose = false;

    /**
     * run the program
     */
    public static void main(String args[]) throws Exception {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("GeneEvolutionSimulator" +
                "- simulate birth and death of genes along a tree");

        int initNumber = options.getOption("-n", "Initial number of genes", 100);
        boolean gaussianInitialNumber = options.getOption("-a", "Gaussian distribution of initial number", true, false);
        float probGain = (float) options.getOption("-g", "Prob of gain in a time step", 0.1);
        float probLoss = (float) options.getOption("-l", "Prob of loss in a time step", 0.1);
        float factor = (float) options.getOption("-f", "Multipler for all edge lengths", 1.0);
        long seed = options.getOption("-s", "Set seed from random number generator", -1);
        boolean phylipFormat = options.getOption("+p", "PhylipSequences format output", false, true);
        boolean display = options.getOption("-d", "Display tree", true, false);
        verbose = options.getOption("-v", "Verbose mode", true, false);
        String fileName = options.getMandatoryOption("-i", "Input tree file", "");
        options.done();

        if (seed >= 0) {
            rand.setSeed(seed);
        }

        /** Gaussian initial number */
        if (gaussianInitialNumber) {
            RandomGaussian randGaussian = new RandomGaussian(initNumber, Math.sqrt(initNumber));
            if (seed >= 0)
                randGaussian.setSeed(seed);
            initNumber = randGaussian.nextInt();
            System.err.println("# Initial length changed to: " + initNumber);
        }

        FileReader r = new FileReader(new File(fileName));
        PhyloTree tree = new PhyloTree();
        tree.read(r, true);


        if (verbose) {
            System.err.println("# tree: " + tree);
            System.err.println("# Factor: " + factor + " N: " + initNumber + " pG; " + probGain + " pL: " + probLoss);
        }
        if (factor != 1.0)
            tree.scaleEdgeWeights(factor);

        simulate(initNumber, probGain, probLoss, tree);

        if (phylipFormat)
            printGenesPhylip(tree, System.out);
        if (verbose)
            printGenes(tree, System.err);

        if (display)
            show(tree);
    }


    /**
     * simulate gene birth and death along the tree
     *
     * @param initNumber initial number of genes
     * @param probGain   probability of gene being born in time step
     * @param probLoss   probability of gene being lost in time step
     * @param tree       the rooted tree
     */
    static public void simulate(int initNumber, float probGain, float probLoss,
                                PhyloTree tree) throws Exception {
        BitSet initialGenes = new BitSet();
        for (int g = 1; g <= initNumber; g++)
            initialGenes.set(g);
        int firstNewGene = initNumber + 1;

        // setup genes at root:
        tree.setInfo(tree.getRoot(), initialGenes);
        tree.setLabel(tree.getRoot(), "root");

        simulateRec(tree.getRoot(), null, probGain / 10.0, probLoss / 10.0, firstNewGene, tree);
    }

    /**
     * recursively does the work
     *
     * @param v            current node
     * @param e            entry edge
     * @param probGain10   prob gain/10
     * @param probLoss10   prob loss/10
     * @param firstNewGene first new gene name available
     * @param tree
     */
    static private int simulateRec(Node v, Edge e, double probGain10, double probLoss10,
                                   int firstNewGene, PhyloTree tree) throws Exception {
        int nGained = 0;
        int nLost = 0;
        for (Edge f = tree.getFirstAdjacentEdge(v); f != null; f = tree.getNextAdjacentEdge(f, v)) {
            if (f != e) {
                Node w = tree.getOpposite(v, f); // child node
                if (tree.getInfo(w) != null)
                    throw new Exception("Reccurent node: " + w);
                BitSet genesW = new BitSet();

                int ticks = (int) (10.0 * tree.getWeight(f));

                // determine which genes survive from v to w:
                BitSet genesV = (BitSet) tree.getInfo(v);
                for (int i = genesV.nextSetBit(0); i >= 0; i = genesV.nextSetBit(i + 1)) {
                    boolean ok = true;

                    for (int t = 1; ok && t <= ticks; t++) {
                        if (flipCoin(probLoss10))
                            ok = false;
                    }
                    if (ok)
                        genesW.set(i);
                    else
                        nLost++;
                }
                // add new genes
                for (int t = 1; t <= 10.0 * tree.getWeight(f); t++) {
                    if (flipCoin(probGain10)) {
                        genesW.set(firstNewGene++);
                        nGained++;
                    }
                }

                System.err.println("Weight: " + tree.getWeight(f) + " Gained: " + nGained + " Lost: " + nLost);
                tree.setInfo(w, genesW);

                firstNewGene = simulateRec(w, f, probGain10, probLoss10, firstNewGene, tree);
            }
        }
        return firstNewGene;
    }

    /**
     * show the tree
     *
     * @param tree
     */
    static public void show(PhyloTree tree) throws NotOwnerException {
        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            BitSet genesV = (BitSet) tree.getInfo(v);
            tree.setLabel(v, tree.getLabel(v) + ":" + Basic.toString(genesV));
        }

        PhyloTreeView TV = new PhyloTreeView(tree, 600, 600);

        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            Point2D apt = TV.getLocation(v);
            TV.setLocation(v, apt.getX() + 200, apt.getY() + 200);
        }
        for (Edge e = tree.getFirstEdge(); e != null; e = tree.getNextEdge(e)) {
            TV.setDirection(e, EdgeView.UNDIRECTED);
            TV.setLabelVisible(e, true);
        }
        Frame F = new Frame("TreeView");
        F.setSize(TV.getSize());
        // F.setResizable(false);
        F.add(TV);
        F.addKeyListener(new GraphViewListener(TV));
        F.setVisible(true);
        TV.fitGraphToWindow();

    }

    /**
     * flip a coin
     *
     * @param prob of heads
     * @return true, if heads
     */
    static public boolean flipCoin(double prob) {
        return rand.nextDouble() <= prob;
    }

    /**
     * prints the evolved genes in phylip format
     *
     * @param tree
     * @param out
     * @throws NotOwnerException
     */
    private static void printGenesPhylip(PhyloTree tree, PrintStream out)
            throws NotOwnerException {
        // determine the set of all mentioned genes:
        int ntax = 0;
        BitSet genes = new BitSet();
        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            if (tree.getLabel(v) != null && !tree.getLabel(v).equals("root")) {
                ntax++;
                genes.or((BitSet) tree.getInfo(v));
            }
        }
        out.println(ntax + " " + genes.cardinality());

        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            if (tree.getLabel(v) != null && !tree.getLabel(v).equals("root")) {
                // int count=0;
                out.print(PhylipUtils.padLabel(tree.getLabel(v), 10) + " ");
                BitSet genesV = (BitSet) tree.getInfo(v);
                for (int i = genes.nextSetBit(0); i >= 0; i = genes.nextSetBit(i + 1)) {
                    /*
                    if(count==50)
                    {
                        out.print("\n");
                        count=0;
                    }
                    else
                        count++;
                    */
                    if (genesV.get(i))
                        out.print("1");
                    else
                        out.print("0");
                }
                out.print("\n");

            }
        }
    }


    /**
     * prints the generated data
     *
     * @param tree
     * @param ps   print stream
     */
    private static void printGenes(PhyloTree tree, PrintStream ps)
            throws NotOwnerException {
        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            if (tree.getLabel(v) != null) {
                if (tree.getLabel(v).equals("root"))
                    ps.print("# ");
                ps.println(tree.getLabel(v) + ": " + tree.getInfo(v));
            }
        }
    }

}
