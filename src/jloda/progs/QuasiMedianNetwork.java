/**
 * QuasiMedianNetwork.java 
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

import jloda.export.PDFExportType;
import jloda.graph.*;
import jloda.graphview.GraphView;
import jloda.graphview.NodeView;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloGraphView;
import jloda.util.Basic;
import jloda.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;

/**
 * compute the quasi median closure of a set of sequences
 * Daniel Huson, 9.2009
 */
public class QuasiMedianNetwork {
    public static void main(String[] args) throws IOException {
        System.err.println("Please enter sequences, followed by a .");

        Set inputSequences = new TreeSet();
        int sequenceLength = 0;

        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));


        String aLine;
        while ((aLine = r.readLine()) != null) {
            aLine = aLine.trim();
            if (aLine.length() == 0 || aLine.startsWith("#"))
                continue;
            if (aLine.equals("."))
                break;
            if (sequenceLength == 0) {
                sequenceLength = aLine.length();
            } else if (sequenceLength != aLine.length())
                throw new IOException("Input line: '" + aLine + "': wrong length (" + aLine.length() + "), should be: " + sequenceLength);
            if (inputSequences.contains(aLine))
                System.err.println("Duplicate sequence in input (ignored): " + aLine);
            else {
                inputSequences.add(aLine);
            }
        }

        // check that all columns differ:
        for (int i = 0; i < sequenceLength; i++) {
            for (int j = i + 1; j < sequenceLength; j++) {
                boolean ok = false;
                char[] i2j = new char[256];
                char[] j2i = new char[256];

                for (Iterator it = inputSequences.iterator(); !ok && it.hasNext(); ) {
                    String sequence = (String) it.next();
                    char chari = sequence.charAt(i);
                    char charj = sequence.charAt(j);

                    if (i2j[chari] == (char) 0) {
                        i2j[chari] = charj;
                        if (j2i[charj] == (char) 0)
                            j2i[charj] = chari;
                        else if (j2i[charj] != chari)
                            ok = true; // differ
                    } else if (i2j[chari] != charj)
                        ok = true; // differ
                }
                if (!ok)
                    throw new IOException("Input has identical pattern in columns: " + i + " and " + j);
            }
        }
        System.err.println("Input: " + inputSequences.size());

        System.err.println("Enter optional character weights:");
        double[] weights = new double[sequenceLength];
        aLine = r.readLine();
        if (aLine.length() > 0) {
            StringTokenizer st = new StringTokenizer(aLine);
            for (int i = 0; i < weights.length; i++)
                weights[i] = Double.parseDouble(st.nextToken());
        } else
            for (int i = 0; i < weights.length; i++)
                weights[i] = 1;

        Set outputSequences;

        System.err.println("Enter q, g or j");
        aLine = r.readLine();

        PhyloGraph graph;
        switch (aLine) {
            case "q":
                outputSequences = computeQuasiMedianClosure(inputSequences, null, null);
                graph = computeOneStepGraph(outputSequences);
                break;
            case "g":
                outputSequences = computeGeodesicPrunedQuasiMedianClosure(inputSequences, sequenceLength);
                graph = computeOneStepGraph(outputSequences);
                break;
            default:
// if(aLine.equals("j"))

                System.err.println("Enter epsilon");
                aLine = r.readLine();
                int epsilon = 0;
                if (aLine.length() > 0)
                    epsilon = Integer.parseInt(aLine);
                outputSequences = new HashSet();
                graph = computeMedianJoiningNetwork(inputSequences, weights, epsilon);
                break;
        }

        System.err.println("Closure (" + outputSequences.size() + "):");
        for (Object outputSequence : outputSequences) {
            System.err.println(outputSequence);
        }

        showGraph(graph);
    }

    /**
     * compute the quasi median closure for the given set of sequences
     *
     * @param sequences
     * @param refA      if !=null, use this reference sequence in computation of quasi median
     * @param refB      if !=null, use this reference sequence in computation of quasi median
     * @return quasi median closure
     */
    public static Set<String> computeQuasiMedianClosure(Set<String> sequences, String refA, String refB) {
        Set<String> oldSequences = new TreeSet<>();
        Set<String> curSequences = new HashSet<>();
        Set<String> newSequences = new HashSet<>();

        System.err.println("Computing quasi-median closure:");
        oldSequences.addAll(sequences);
        curSequences.addAll(sequences);

        while (curSequences.size() > 0) {
            String[] oldArray = oldSequences.toArray(new String[oldSequences.size()]);
            newSequences.clear();
            for (String seqA : oldArray) {
                for (String seqB : oldArray) {
                    for (String seqC : curSequences) {
                        if (!seqC.equals(seqA) && !seqC.equals(seqB)) {
                            String[] medianSequences = refA != null ? computeQuasiMedian(seqA, seqB, seqC, refA, refB) :
                                    computeQuasiMedian(seqA, seqB, seqC);
                            for (String medianSequence : medianSequences) {
                                if (!oldSequences.contains(medianSequence) && !curSequences.contains(medianSequence)) {
                                    newSequences.add(medianSequence);
                                }
                            }
                        }
                    }
                }
            }
            oldSequences.addAll(curSequences);
            curSequences.clear();
            Set<String> tmp = curSequences;
            curSequences = newSequences;
            newSequences = tmp;
            System.err.println("Size: " + oldSequences.size());
        }
        return oldSequences;

    }

    /**
     * compute the quasi median closure for the given set of sequences
     *
     * @param sequences
     * @param sequenceLength
     * @return quasi median closure
     */
    public static Set computeGeodesicPrunedQuasiMedianClosure(Set sequences, int sequenceLength) {
        Set result = new TreeSet();

        System.err.println("Computing geodesically-pruned quasi-median closure:");

        String[] input = (String[]) sequences.toArray(new String[sequences.size()]);

        double[][] scores = computeScores(input, sequenceLength);

        for (int i = 0; i < input.length; i++) {
            for (int j = i + 1; j < input.length; j++) {
                System.err.println("Processing " + i + "," + j);
                BitSet use = new BitSet();
                for (int pos = 0; pos < sequenceLength; pos++) {
                    if (input[i].charAt(pos) != input[j].charAt(pos))
                        use.set(pos);
                }
                Set<String> compressed = new HashSet<>();
                for (String anInput : input) {
                    StringBuilder buf = new StringBuilder();
                    for (int p = 0; p < sequenceLength; p++) {
                        if (use.get(p))
                            buf.append(anInput.charAt(p));
                    }
                    compressed.add(buf.toString());
                }
                StringBuilder bufA = new StringBuilder();
                StringBuilder bufB = new StringBuilder();

                for (int p = 0; p < sequenceLength; p++) {
                    if (use.get(p)) {
                        bufA.append(input[i].charAt(p));
                        bufB.append(input[j].charAt(p));
                    }
                }
                String refA = bufA.toString();
                String refB = bufB.toString();


                Set closure = computeQuasiMedianClosure(compressed, refA, refB);

                Set expanded = new HashSet();
                for (Object aClosure : closure) {
                    String current = (String) aClosure;
                    StringBuilder buf = new StringBuilder();
                    int p = 0;
                    for (int k = 0; k < sequenceLength; k++) {
                        if (!use.get(k))
                            buf.append(input[i].charAt(k));
                        else // used in
                        {
                            buf.append(current.charAt(p++));
                        }
                    }
                    expanded.add(buf.toString());
                }
                Set geodesic = computeGeodesic(input[i], input[j], expanded, scores);
                result.addAll(geodesic);


                    System.err.println("------Sequences :");
                    System.err.println(input[i]);
                    System.err.println(input[j]);
                    for (int x = 0; x < sequenceLength; x++)
                        System.err.print(use.get(x) ? "x" : " ");
                    System.err.println();
                    System.err.println("Refs:");

                    System.err.println(refA);
                    System.err.println(refB);
                    System.err.println("Compressed (" + compressed.size() + "):");
                for (String aCompressed : compressed) System.err.println(aCompressed);
                    System.err.println("Closure (" + closure.size() + "):");
                for (Object aClosure : closure) System.err.println(aClosure);
                    System.err.println("Expanded (" + expanded.size() + "):");
                for (Object anExpanded : expanded) System.err.println(anExpanded);
                    System.err.println("Geodesic (" + geodesic.size() + "):");
                for (Object aGeodesic : geodesic) System.err.println(aGeodesic);

            }
        }
        return result;
    }

    /**
     * compute the best geodesic between two nodes
     *
     * @param seqA
     * @param seqB
     * @param expanded
     * @param scores
     * @return geodesic
     */
    private static Set computeGeodesic(String seqA, String seqB, Set expanded, double[][] scores) {
        Graph graph = new Graph();

        Node start = null;
        Node end = null;
        for (Object anExpanded : expanded) {
            String seq = (String) anExpanded;

            Node v = graph.newNode(seq);
            if (start == null && seq.equals(seqA))
                start = v;
            else if (end == null && seq.equals(seqB))
                end = v;
        }
        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            String aSeq = (String) graph.getInfo(v);
            for (Node w = v.getNext(); w != null; w = w.getNext()) {
                String bSeq = (String) graph.getInfo(w);
                if (computeOneStep(aSeq, bSeq) != -1)
                    graph.newEdge(v, w);
            }
        }

        Set bestPath = new HashSet();
        NodeSet inPath = new NodeSet(graph);
        NodeDoubleArray bestScore = new NodeDoubleArray(graph, Double.NEGATIVE_INFINITY);
        inPath.add(start);
        System.err.println("Finding best geodesic:");
        computeBestPathRec(graph, end, start, null, bestScore, inPath, 0, new HashSet(), bestPath, scores);
        return bestPath;
    }

    /**
     * get the best path from start to end
     *
     * @param end
     * @param v
     * @param e
     * @param currentScore
     * @param currentPath
     * @param bestPath
     */
    private static void computeBestPathRec(Graph graph, Node end, Node v, Edge e, NodeDoubleArray bestScore, NodeSet inPath, double currentScore,
                                           HashSet currentPath,
                                           Set bestPath, double[][] scores) {
        if (v == end) {
            if (currentScore > bestScore.getValue(end)) {
                System.err.println("Updating best score: " + bestScore.getValue(end) + " -> " + currentScore);
                bestPath.clear();
                bestPath.addAll(currentPath);
                bestScore.set(v, currentScore);
            } else if (currentScore == bestScore.getValue(end)) {
                bestPath.addAll(currentPath); // don't break ties
            }
        } else {
            if (currentScore >= bestScore.getValue(v)) {
                bestScore.set(v, currentScore);
                for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
                    if (f != e) {
                        Node w = f.getOpposite(v);
                        if (!inPath.contains(w)) {
                            inPath.add(w);
                            String seq = (String) graph.getInfo(w);
                            double add = getScore(seq, scores);
                            currentPath.add(seq);
                            computeBestPathRec(graph, end, w, f, bestScore, inPath, currentScore + add, currentPath, bestPath, scores);
                            currentPath.remove(seq);
                            inPath.remove(w);
                        }
                    }
                }
            }
        }
    }

    /**
     * computes a log score for each state at each   position of the alignment
     *
     * @param input
     * @param sequenceLength
     * @return scores
     */
    private static double[][] computeScores(String[] input, int sequenceLength) {
        double[][] scores = new double[sequenceLength][256];

        for (int pos = 0; pos < sequenceLength; pos++) {
            for (String anInput : input) {
                scores[pos][anInput.charAt(pos)]++;
            }
        }

        for (int pos = 0; pos < sequenceLength; pos++) {
            for (int i = 0; i < 256; i++) {
                if (scores[pos][i] != 0)
                    scores[pos][i] = Math.log(scores[pos][i] / input.length);
            }
        }
        return scores;
    }

    /**
     * get the log score of a sequence
     *
     * @param seq
     * @param scores
     * @return log score
     */
    private static double getScore(String seq, double[][] scores) {
        double score = 0;
        for (int i = 0; i < seq.length(); i++)
            score += scores[i][seq.charAt(i)];
        return score;
    }


    /**
     * if two sequences differ at exactly one position, gets position
     *
     * @param seqa
     * @param seqb
     * @return single difference position or -1
     */
    private static int computeOneStep(String seqa, String seqb) {
        int pos = -1;
        for (int i = 0; i < seqa.length(); i++) {
            if (seqa.charAt(i) != seqb.charAt(i)) {
                if (pos == -1)
                    pos = i;
                else
                    return -1;
            }
        }
        return pos;
    }

    /**
     * computes the quasi median for three sequences
     *
     * @param seqA
     * @param seqB
     * @param seqC
     * @return quasi median
     */
    private static String[] computeQuasiMedian(String seqA, String seqB, String seqC) {
        StringBuilder buf = new StringBuilder();
        boolean hasStar = false;
        for (int i = 0; i < seqA.length(); i++) {
            if (seqA.charAt(i) == seqB.charAt(i) || seqA.charAt(i) == seqC.charAt(i))
                buf.append(seqA.charAt(i));
            else if (seqB.charAt(i) == seqC.charAt(i))
                buf.append(seqB.charAt(i));
            else {
                buf.append("*");
                hasStar = true;
            }
        }
        if (!hasStar)
            return new String[]{buf.toString()};

        Set median = new HashSet();
        Stack stack = new Stack();
        stack.add(buf.toString());
        while (!stack.empty()) {
            String seq = (String) stack.pop();
            int pos = seq.indexOf('*');
            int pos2 = seq.indexOf('*', pos + 1);
            String first = seq.substring(0, pos) + seqA.charAt(pos) + seq.substring(pos + 1);
            String second = seq.substring(0, pos) + seqB.charAt(pos) + seq.substring(pos + 1);
            String third = seq.substring(0, pos) + seqC.charAt(pos) + seq.substring(pos + 1);
            if (pos2 == -1) {
                median.add(first);
                median.add(second);
                median.add(third);
            } else {
                stack.add(first);
                stack.add(second);
                stack.add(third);
            }
        }
        return (String[]) median.toArray(new String[median.size()]);
    }

    /**
     * computes the quasi median for three sequences. When resolving a three-way median, use only states in reference sequences
     *
     * @param seqA
     * @param seqB
     * @param seqC
     * @param refA
     * @param refB
     * @return quasi median
     */
    private static String[] computeQuasiMedian(String seqA, String seqB, String seqC, String refA, String refB) {
        StringBuilder buf = new StringBuilder();
        boolean hasStar = false;
        for (int i = 0; i < seqA.length(); i++) {
            if (seqA.charAt(i) == seqB.charAt(i) || seqA.charAt(i) == seqC.charAt(i))
                buf.append(seqA.charAt(i));
            else if (seqB.charAt(i) == seqC.charAt(i))
                buf.append(seqB.charAt(i));
            else {
                buf.append("*");
                hasStar = true;
            }
        }
        if (!hasStar)
            return new String[]{buf.toString()};

        Set median = new HashSet();
        Stack stack = new Stack();
        stack.add(buf.toString());
        while (!stack.empty()) {
            String seq = (String) stack.pop();
            int pos = seq.indexOf('*');
            int pos2 = seq.indexOf('*', pos + 1);
            String first = seq.substring(0, pos) + refA.charAt(pos) + seq.substring(pos + 1);
            if (pos2 == -1) {
                median.add(first);
            } else {
                stack.add(first);
            }
            if (refB.charAt(pos) != refA.charAt(pos)) {
                String second = seq.substring(0, pos) + refB.charAt(pos) + seq.substring(pos + 1);
                if (pos2 == -1) {
                    median.add(second);
                } else {
                    stack.add(second);
                }
            }
        }
        return (String[]) median.toArray(new String[median.size()]);
    }

    /**
     * computes the one-step graph
     *
     * @param sequences
     * @return one-step graph
     */
    public static PhyloGraph computeOneStepGraph(Set sequences) {
        PhyloGraph graph = new PhyloGraph();
        for (Object sequence : sequences) {
            String seq = (String) sequence;
            Node v = graph.newNode();
            graph.setLabel(v, seq);
            graph.setInfo(v, seq);
        }

        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            for (Node w = v.getNext(); w != null; w = w.getNext()) {
                int i = computeOneStep(graph.getLabel(v), graph.getLabel(w));
                if (i != -1)
                    graph.newEdge(v, w, "" + i);
            }
        }
        return graph;
    }

    /**
     * display the computed graph
     *
     * @param graph
     */
    private static void showGraph(PhyloGraph graph) {
        JFrame frame = new JFrame("quasi-median network");
        frame.setSize(400, 400);

        PhyloGraphView view = new PhyloGraphView(graph, 400, 400);
        view.setCanvasColor(Color.WHITE);
        view.setMaintainEdgeLengths(false);
        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            view.setShape(v, NodeView.NONE_NODE);
        }
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            view.setLabel(e, graph.getLabel(e));
            view.setLabelVisible(e, true);
        }
        frame.addKeyListener(view.getGraphViewListener());
        view.computeSpringEmbedding(5000, false);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(view.getScrollPane(), BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(new JButton(getSaveImage(view)));
        bottom.add(Box.createHorizontalGlue());
        bottom.add(new JButton(getClose()));
        frame.getContentPane().add(bottom, BorderLayout.SOUTH);

        frame.setVisible(true);
        view.fitGraphToWindow();
    }


    private static AbstractAction getSaveImage(final GraphView viewer) {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    PDFExportType.writeToFile(new File("/Users/huson/image.pdf"), viewer);
                } catch (IOException e) {
                    Basic.caught(e);
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Save image");
        return action;
    }

    private static AbstractAction getClose() {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        };
        action.putValue(AbstractAction.NAME, "Close");
        return action;
    }

    /**
     * runs the median joining algorithm
     *
     * @param inputSequences
     * @param weights
     * @param epsilon
     * @return median joining network
     */
    public static PhyloGraph computeMedianJoiningNetwork(Set inputSequences, double[] weights, int epsilon) {
        System.err.println("Computing the median joining network for epsilon=" + epsilon);
        PhyloGraph graph;
        Set<String> outputSequences = computeMedianJoiningMainLoop(inputSequences, weights, epsilon);
        boolean changed;
        do {
            graph = new PhyloGraph();
            EdgeSet feasibleLinks = new EdgeSet(graph);
            computeMinimumSpanningNetwork(outputSequences, weights, 0, graph, feasibleLinks);
            List toDelete = new LinkedList();
            for (Edge e = graph.getFirstEdge(); e != null; e = graph.getNextEdge(e)) {
                if (!feasibleLinks.contains(e))
                    toDelete.add(e);
            }
            for (Object aToDelete : toDelete) graph.deleteEdge((Edge) aToDelete);
            changed = removeObsoleteNodes(graph, inputSequences, outputSequences);
        }
        while (changed);
        return graph;
    }

    /**
     * Main loop of the median joining algorithm
     *
     * @param input
     * @param epsilon
     * @return sequences present in the median joining network
     */
    private static Set<String> computeMedianJoiningMainLoop(Set<String> input, double[] weights, int epsilon) {
        Set<String> sequences = new HashSet<>();
        sequences.addAll(input);

        boolean changed = true;
        while (changed) {
            changed = false;
            System.err.println("Median joining: Begin of main loop: " + sequences.size() + " sequences");
            PhyloGraph graph = new PhyloGraph();
            EdgeSet feasibleLinks = new EdgeSet(graph);
            computeMinimumSpanningNetwork(sequences, weights, epsilon, graph, feasibleLinks);
            if (removeObsoleteNodes(graph, input, sequences)) {
                changed = true;   // sequences have been changed, recompute graph
            } else {
                // determine min connection cost:
                double minConnectionCost = Double.MAX_VALUE;

                for (Node u = graph.getFirstNode(); u != null; u = u.getNext()) {
                    String seqU = (String) u.getInfo();
                    for (Edge e = u.getFirstAdjacentEdge(); e != null; e = u.getNextAdjacentEdge(e)) {
                        Node v = e.getOpposite(u);
                        String seqV = (String) v.getInfo();
                        for (Edge f = u.getNextAdjacentEdge(e); f != null; f = u.getNextAdjacentEdge(f)) {
                            Node w = f.getOpposite(u);
                            String seqW = (String) w.getInfo();
                            String[] qm = computeQuasiMedian(seqU, seqV, seqW);
                            for (String aQm : qm) {
                                if (!sequences.contains(aQm)) {
                                    double cost = computeConnectionCost(seqU, seqV, seqW, aQm, weights);
                                    if (cost < minConnectionCost)
                                        minConnectionCost = cost;
                                }
                            }
                        }
                    }
                }
                for (Edge e = feasibleLinks.getFirstElement(); e != null; e = feasibleLinks.getNextElement(e)) {
                    Node u = e.getSource();
                    Node v = e.getTarget();
                    String seqU = (String) u.getInfo();
                    String seqV = (String) v.getInfo();
                    for (Edge f = feasibleLinks.getNextElement(e); f != null; f = feasibleLinks.getNextElement(f)) {
                        Node w;
                        if (f.getSource() == u || f.getSource() == v)
                            w = f.getTarget();
                        else if (f.getTarget() == u || f.getTarget() == v)
                            w = f.getSource();
                        else
                            continue;
                        String seqW = (String) w.getInfo();
                        String[] qm = computeQuasiMedian(seqU, seqV, seqW);
                        for (String aQm : qm) {
                            if (!sequences.contains(aQm)) {
                                double cost = computeConnectionCost(seqU, seqV, seqW, aQm, weights);
                                if (cost <= minConnectionCost + epsilon) {
                                    sequences.add(aQm);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
            System.err.println("Median joining: End of main loop: " + sequences.size() + " sequences");
        }
        return sequences;
    }

    /**
     * computes the minimum spanning network upto a tolerance of epsilon
     *
     * @param sequences
     * @param weights
     * @param epsilon
     * @param graph
     * @param feasibleLinks
     */
    private static void computeMinimumSpanningNetwork(Set sequences, double[] weights, int epsilon, PhyloGraph graph, EdgeSet feasibleLinks) {
        String[] array = (String[]) sequences.toArray(new String[sequences.size()]);
        // compute a distance matrix between all sequences:
        double[][] matrix = new double[array.length][array.length];

        // sort pairs of taxa into groups bey ascending edge length
        SortedMap<Double, List<Pair<Integer, Integer>>> value2pairs = new TreeMap<>();
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                matrix[i][j] = computeDistance(array[i], array[j], weights);
                Double value = matrix[i][j];
                List<Pair<Integer, Integer>> pairs = value2pairs.get(value);
                if (pairs == null) {
                    pairs = new LinkedList<>();
                    value2pairs.put(value, pairs);
                }
                pairs.add(new Pair<>(i, j));
            }
        }

        // set up array of nodes and arrays to track components in the minimum spanning network and in the threshold graph
        Node[] nodes = new Node[array.length];
        int[] componentsOfMSN = new int[array.length];
        int[] componentsOfThresholdGraph = new int[array.length];

        for (int i = 0; i < array.length; i++) {
            nodes[i] = graph.newNode(array[i]);
            graph.setLabel(nodes[i], array[i]);
            componentsOfMSN[i] = i;
            componentsOfThresholdGraph[i] = i;
        }
        int numComponentsMSN = array.length;

        double maxValue = Double.MAX_VALUE;

        // consider each set of pairs of taxa for a given edge length, in ascending order of edge lengths
        for (Double value : value2pairs.keySet()) {
            List<Pair<Integer, Integer>> ijPairs = value2pairs.get(value);
            double threshold = value;
            if (threshold > maxValue)
                break;

            // update threshold graph components:
            for (int i = 0; i < array.length; i++) {
                for (int j = i + 1; j < array.length; j++) {
                    if (componentsOfThresholdGraph[i] != componentsOfThresholdGraph[j] && matrix[i][j] < threshold - epsilon) {
                        int oldComponent = componentsOfMSN[j];
                        for (int k = 0; k < array.length; k++) {
                            if (componentsOfThresholdGraph[k] == oldComponent)
                                componentsOfThresholdGraph[k] = componentsOfThresholdGraph[i];
                        }
                    }
                }
            }

            // determine new edges for minimum spanning network and determine feasible links
            List<Pair<Integer, Integer>> newPairs = new LinkedList<>();
            for (Pair<Integer, Integer> ijPair : ijPairs) {
                int i = ijPair.getFirst();
                int j = ijPair.getSecond();

                Edge e = graph.newEdge(nodes[i], nodes[j]);
                if (feasibleLinks != null && componentsOfThresholdGraph[i] != componentsOfThresholdGraph[j]) {
                    feasibleLinks.add(e);
                }
                newPairs.add(new Pair<>(i, j));
            }

            // update MSN components
            for (Pair<Integer, Integer> pair : newPairs) {
                int i = pair.getFirstInt();
                int j = pair.getSecondInt();
                if (componentsOfMSN[i] != componentsOfMSN[j]) {
                    numComponentsMSN--;
                    int oldComponent = componentsOfMSN[j];
                    for (int k = 0; k < array.length; k++)
                        if (componentsOfMSN[k] == oldComponent)
                            componentsOfMSN[k] = componentsOfMSN[i];
                }
            }
            if (numComponentsMSN == 1 && maxValue == Double.MAX_VALUE)
                maxValue = threshold + epsilon; // once network is connected, add all edges upto threshold+epsilon
        }
    }

    /**
     * iteratively removes all nodes that are connected to only two other and are not part of the original input
     *
     * @param graph
     * @param input
     * @param sequences
     * @return true, if anything was removed
     */
    private static boolean removeObsoleteNodes(PhyloGraph graph, Set<String> input, Set<String> sequences) {
        int removed = 0;
        boolean changed = true;
        while (changed) {
            changed = false;
            List<Node> toDelete = new LinkedList<>();

            for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
                String seqV = (String) v.getInfo();
                if (v.getDegree() <= 2 && !input.contains(seqV))
                    toDelete.add(v);
            }
            if (toDelete.size() > 0) {
                changed = true;
                removed += toDelete.size();
                for (Node v : toDelete) {
                    sequences.remove(v.getInfo());
                    graph.deleteNode(v);
                }
            }
        }
        return removed > 0;
    }


    /**
     * compute the cost of connecting seqM to the other three sequences
     *
     * @param seqU
     * @param seqV
     * @param seqW
     * @param seqM
     * @return cost
     */
    private static double computeConnectionCost(String seqU, String seqV, String seqW, String seqM, double[] weights) {
        return computeDistance(seqU, seqM, weights) + computeDistance(seqV, seqM, weights) + computeDistance(seqW, seqM, weights);
    }

    /**
     * compute weighted distance between two sequences
     *
     * @param seqA
     * @param seqB
     * @return distance
     */
    private static double computeDistance(String seqA, String seqB, double[] weights) {
        double cost = 0;
        for (int i = 0; i < seqA.length(); i++) {
            if (seqA.charAt(i) != seqB.charAt(i))
                if (weights != null)
                    cost += weights[i];
                else
                    cost++;
        }
        return cost;
    }
}
