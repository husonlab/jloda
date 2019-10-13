/*
 * CoverDigraph.java Copyright (C) 2019. Daniel H. Huson
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

/**
 * Cover digraph construction
 * @version $Id: CoverDigraph.java,v 1.6 2009-09-25 13:47:12 huson Exp $
 * @author daniel Huson
 * 7.03
 */
package jloda.progs;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeIntegerArray;
import jloda.graph.NotOwnerException;
import jloda.phylo.PhyloSplitsGraph;
import jloda.swing.graphview.GraphViewListener;
import jloda.swing.graphview.PhyloGraphView;
import jloda.swing.util.CommandLineOptions;
import jloda.util.Basic;
import jloda.util.PhylipUtils;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

/**
 * Cover digraph construction
 */
public class CoverDigraph {
    private int ntax;
    private GeneOccurrences[] genes;
    private PhyloSplitsGraph graph;

    /**
     * read the gene sets from a stream.
     * Format:
     * ntax ngenes
     * label-gene1 taxon taxon ....
     * label-gene2 taxon taxon ...
     * ....
     *
     * @param r the reader
     */
    public void readGenes(Reader r) throws IOException {
        String[][] data = new String[2][];

        PhylipUtils.read(data, r);
        ntax = Array.getLength(data[0]) - 1;
        int ngenes = data[1][1].length();

        String[] tax2name = new String[ntax + 1];
        genes = new GeneOccurrences[ngenes];
        for (int c = 0; c < genes.length; c++) {

            genes[c] = new GeneOccurrences();
            genes[c].label = "g" + c;
            genes[c].taxa = new BitSet();
        }

        for (int i = 1; i <= ntax; i++) {
            tax2name[i] = data[0][i];
            String seq = data[1][i];
            System.err.println("taxa " + tax2name[i] + ": " + seq);

            for (int c = 0; c < seq.length(); c++) {
                if (seq.charAt(c) == '1')
                    genes[c].taxa.set(i);
            }
        }
    }

    /**
     * write the input data
     *
     * @param w writer
     */
    public void writeGenes(Writer w) throws IOException {
        for (GeneOccurrences gene : genes) {
            w.write(gene.label + " ");
            for (int t = 1; t <= ntax; t++)
                if (gene.taxa.get(t))
                    w.write(" " + t);
            w.write("\n");
        }
    }

    /**
     * write the whole thing to a string
     *
     * @return a string
     */
    public String toString() {
        Writer sw = new StringWriter();
        try {
            writeGenes(sw);
        } catch (IOException ex) {
            Basic.caught(ex);
        }
        return sw.toString();
    }

    /**
     * computes the cover digraph
     */
    public void computeGraph() throws NotOwnerException {
        /* order sets of genes by their size: */
        Arrays.sort(genes, new BitSetComparator());
        graph = new PhyloSplitsGraph();
        Node[] gene2node = new Node[genes.length];
        NodeIntegerArray node2covered = new NodeIntegerArray(graph);


        for (int i = 0; i < genes.length; i++) {
            // prepare label
            StringBuilder label = new StringBuilder("" + genes[i].label + ":");
            for (int t = 1; t <= ntax; t++)
                if (genes[i].taxa.get(t))
                    label.append(" ").append(t);

            // check whether gene has same profile as a previous one:
            boolean found = false;
            for (int j = i - 1; !found && j >= 0; j--) {
                if (genes[i].taxa.cardinality() < genes[j].taxa.cardinality())
                    break; // because genes are ordered by increasing size
                if (genes[i].taxa.equals(genes[j].taxa)) {   // genes have same profile
                    Node v = gene2node[j];
                    gene2node[i] = v;
                    graph.setLabel(v, graph.getLabel(v) + ", " + label);
                    // add label of this gene
                    found = true;
                }
            }
            if (!found) {

                Node v = graph.newNode(genes[i].taxa);

                graph.setLabel(v, label.toString());
                gene2node[i] = v;
                for (int j = i - 1; j >= 0; j--) {
                    Node w = gene2node[j];

                    if (node2covered.getValue(w) < i + 1) // doesn't cover a node between v and w
                    {
                        if (BitSetComparator.isSubset(genes[i].taxa, genes[j].taxa)) {
                            graph.newEdge(w, v);
                            markAllCoveringNodesRec(i + 1, w, node2covered);
                        }
                    }
                }
            }
        }
    }

    /**
     * mark all nodes above this one as covered
     *
     * @param id
     * @param v
     * @param node2covered
     */
    private void markAllCoveringNodesRec(int id, Node v, NodeIntegerArray node2covered)
            throws NotOwnerException {
        if (node2covered.getValue(v) >= id)
            return;

        node2covered.set(v, id);

        for (Edge e = graph.getFirstAdjacentEdge(v); e != null; e = graph.getNextAdjacentEdge(e, v))
            if (graph.getTarget(e) == v) {
                Node w = graph.getOpposite(v, e);
                if (node2covered.getValue(w) < id) {
                    markAllCoveringNodesRec(id, w, node2covered);
                }
            }
    }


    /**
     * displays the graph
     */
    public void showGraph() {

        JFrame F = new JFrame("CoveredDigraph");
        PhyloGraphView view = new PhyloGraphView(graph);
        view.setAutoLayoutLabels(true);
        F.getContentPane().add(view);
        F.setSize(view.getSize());
        // F.setResizable(false);
        F.addKeyListener(new GraphViewListener(view));


        // set node locations:
        try {
            int[] v2h = new int[ntax + 1];

            for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
                BitSet taxa = (BitSet) graph.getInfo(v);
                int vert = taxa.cardinality();
                int hor = (v2h[vert]++);
                view.setLocation(v, hor, -vert);
            }
        } catch (NotOwnerException e) {
            Basic.caught(e);
        }
        F.setVisible(true);
        view.fitGraphToWindow();
        view.setMaintainEdgeLengths(false);
    }

    /**
     * run the program
     */
    public static void main(String[] args) throws Exception {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("CoverDigraph" +
                "- compute cover digraph from gene content");
        String infile = options.getOption("-i", "Input file", "");
        options.done();

        FileReader r = new FileReader(infile);

        CoverDigraph cd = new CoverDigraph();

        cd.readGenes(r);
        System.err.println("got:\n" + cd);

        cd.computeGraph();


        cd.showGraph();

        System.err.println("ordered:\n" + cd);
    }

}

class BitSetComparator implements Comparator {
    /**
     * Compares its two sets for order.   First by size, then lexicographically
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     * @throws ClassCastException if the arguments' types prevent them from
     *                            being compared by this Comparator.
     */
    public int compare(Object o1, Object o2) {
        BitSet bs1 = ((GeneOccurrences) o1).taxa;
        BitSet bs2 = ((GeneOccurrences) o2).taxa;

        if (bs1.cardinality() < bs2.cardinality())
            return 1;
        else if (bs1.cardinality() > bs2.cardinality())
            return -1;


        int top = Math.max(bs1.length(), bs2.length()) + 1;

        for (int t = 1; t <= top; t++) {
            if (bs1.get(t) && !bs2.get(t))
                return 1;
            else if (!bs1.get(t) && bs2.get(t))
                return -1;
        }
        return 0;
    }

    /**
     * is bs1 subset of bs2?
     *
     * @param bs1 first bit set
     * @param bs2 second bit set
     * @return true, if bs1 subset of bs2
     */
    public static boolean isSubset(BitSet bs1, BitSet bs2) {
        int top = bs1.length() + 1;

        for (int t = 1; t <= top; t++) {
            if (bs1.get(t) && !bs2.get(t))
                return false;
        }
        return true;
    }
}

/**
 * a gene with occurrences
 */
class GeneOccurrences {
    BitSet taxa;
    String label;
}
