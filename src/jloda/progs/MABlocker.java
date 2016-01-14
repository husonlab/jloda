/**
 * MABlocker.java 
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

import jloda.graph.Graph;
import jloda.graph.MaxClique;
import jloda.graph.Node;
import jloda.graph.NodeIntegerArray;
import jloda.util.Basic;
import jloda.util.CommandLineOptions;
import jloda.util.FastA;
import jloda.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.*;

/**
 * Given a multi-alignment of individual reads, produces blocks of strongly aligned stuff
 *
 * @author huson
 *         Date: 10-Aug-2004
 */
public class MABlocker {
    static public void main(String[] args) throws Exception {
        CommandLineOptions options = new CommandLineOptions(args);
        String cname = options.getMandatoryOption("-i", "input file", "");
        int minLength = options.getOption("-l", "min length of block", 100);
        boolean generateFastA = options.getOption("+f", "create FastASequences files", false, true);
        boolean generateCGViz = options.getOption("-c", "create CGViz files", true, false);
        boolean usePercentOfPrevious = options.getOption("-p", "use precent of previous", true, false);
        boolean useGrowingAlgorithm = options.getOption("-g", "use growing algorithm", true, false);
        int minSequences = options.getOption("-s", "min number of sequences in a block", 4);
        int minOverlap = options.getOption("-m", "min length of pairwise overlap", 100);
        int minPercent = options.getOption("-d", "min percent of non-gap positions in a sequence", 80);
        options.done();

        System.err.println("# Options: " + options);

        FastA fasta = new FastA();
        fasta.read(new FileReader(new File(cname)));
        int numSequences = fasta.getSize();

        int length = 0;
        for (int i = 0; i < fasta.getSize(); i++) {
            if (i == 0)
                length = fasta.getSequence(i).length();
            else if (fasta.getSequence(i).length() != length)
                throw new Exception("Sequence 0 and " + i + ": lengths differ: " + length
                        + " and " + fasta.getSequence(i).length());
        }
        System.err.println("# Got " + numSequences + " sequences of length " + length);

        int[] starts = new int[numSequences];
        int[] stops = new int[numSequences];
        stopsStarts(fasta, length, starts, stops);

        int[] ordering = new int[numSequences];
        int[] inverse = new int[numSequences];
        computeEliminationScheme(numSequences, starts, ordering, inverse);
        int[][] matrix = computeCoverageMatrix(numSequences, starts, stops, minOverlap, ordering, usePercentOfPrevious);

        List blocks = new LinkedList();
        if (useGrowingAlgorithm) {
            BitSet positions = getGapPositions(fasta, length);
            for (int i = positions.nextSetBit(0); i != -1; i = positions.nextSetBit(i + 1)) {
                int j = positions.nextSetBit(i + 1);
                if (j != -1 && j - i + 1 >= 25) {
                    Block block = computeBlock(false, fasta, length, i, j, positions, starts, stops, minSequences, minPercent);
                    if (block != null && block.stopPos - block.startPos + 1 >= minLength) {
                        blocks.add(block);
                    }
                    block = computeBlock(true, fasta, length, i, j, positions, starts, stops, minSequences, minPercent);
                    if (block != null && block.stopPos - block.startPos + 1 >= minLength) {
                        blocks.add(block);
                    }
                }
            }
            makeBlocksUnique(blocks);
            System.err.println("# Number of blocks: " + blocks.size());
        } else {
            List maxCliques = MaxClique.computeAll(matrix, ordering);
            System.err.println("# MaxCliques: " + maxCliques.size());

            for (Object maxClique : maxCliques) {
                BitSet clique = (BitSet) maxClique;
                System.err.println("# MaxClique: " + clique);
                if (clique.cardinality() >= minSequences) {
                    Block block = buildBlock(clique, starts, stops);
                    if (block.stopPos - block.startPos + 1 >= minLength)
                        blocks.add(block);
                }
            }
        }

        if (generateFastA)
            writeFastAFiles(cname, fasta, blocks);
        if (generateCGViz)
            writeCGVizFiles(cname, fasta, blocks);

        {
            FileWriter w = new FileWriter(new File(cname + ".fa"));
            for (int t = 0; t < numSequences; t++) {
                w.write("> t" + t + "_" + fasta.getHeader(t) + "\n");
                w.write(Basic.wraparound(fasta.getSequence(t), 65));
            }
            w.close();
        }

        {
            FileWriter w = new FileWriter(new File("nexus.header"));
            {
                w.write("#nexus\n");
                w.write("begin taxa;\ndimensions ntax=" + numSequences + ";\ntaxlabels\n");
                for (int t = 0; t < numSequences; t++) {
                    w.write("t" + t + "_" + fasta.getHeader(t) + "\n");
                }
                w.write(";\nend;\n;");
                w.write("begin trees;\n");
            }
            w.close();
        }

        {
            FileWriter w = new FileWriter(new File("overview.cgv"));
            w.write("{DATA overview\n");
            w.write("[__GLOBAL__] dimension=2\n");
            for (int i = 0; i < numSequences; i++) {
                int v = ordering[i];
                w.write("seq=" + v + " start= " + starts[v] + " stop= " + stops[v] +
                        " len= " + (stops[v] - starts[v] + 1) + ": " + starts[v] + " " + i
                        + " " + stops[v] + " " + i + "\n");
            }
            w.write("}\n");
            w.write("{DATA blocks\n");
            Iterator it = blocks.iterator();
            int blockNumber = 0;
            while (it.hasNext()) {
                blockNumber++;
                Block block = (Block) it.next();
                int minOrder = numSequences + 1;
                int maxOrder = -1;
                for (int t = block.taxa.nextSetBit(0); t >= 0; t = block.taxa.nextSetBit(t + 1)) {
                    if (inverse[t] < minOrder)
                        minOrder = inverse[t];
                    if (inverse[t] > maxOrder)
                        maxOrder = inverse[t];
                }
                w.write("num=" + blockNumber + " start=" + block.startPos + " stop=" + block.stopPos
                        + " minOrder=" + minOrder + " maxOrder=" + maxOrder + " numSequences=" +
                        block.taxa.cardinality() + ": " + block.startPos + " " + (minOrder) +
                        " " + block.stopPos + " " + (maxOrder) + "\n");
            }
            w.write("}\n");
            w.close();
        }
    }

    /**
     * make blocks sufficiently unique
     *
     * @param blocks
     */
    private static void makeBlocksUnique(List blocks) {
        Block[] blocksArray = (Block[]) blocks.toArray(new Block[blocks.size()]);
        blocks.clear();

        Graph containmentGraph = new Graph();
        Node[] id2node = new Node[blocksArray.length];
        NodeIntegerArray node2id = new NodeIntegerArray(containmentGraph);

        for (int i = 0; i < blocksArray.length; i++) {
            id2node[i] = containmentGraph.newNode();
            node2id.set(id2node[i], i);
        }
        for (int i = 0; i < blocksArray.length; i++) {
            for (int j = 0; j < blocksArray.length; j++) {
                if (i != j) {
                    // System.err.println("Block "+blocksArray[i]+" contains block "+blocksArray[j]+": "+contains(blocksArray[i], blocksArray[j]));

                    if (contains(blocksArray[i], blocksArray[j])
                            && (i < j || !contains(blocksArray[j], blocksArray[i])))
                        containmentGraph.newEdge(id2node[i], id2node[j]);
                }
            }
        }
        // System.err.println("Graph: "+containmentGraph.toString());

        for (Node v = containmentGraph.getFirstNode(); v != null; v = v.getNext()) {
            if (v.getInDegree() == 0) {
                Block block = blocksArray[node2id.getValue(v)];
                blocks.add(block);
                /*
                 {
                System.err.print("Block "+block+" contains:");
                Stack stack=new Stack();
                stack.add(v);
                while(stack.size()>0) {
                    Node w=(Node)stack.pop();
                    for(Edge e=w.getFirstOutEdge();e!=null;e=w.getNextOutEdge(e))
                    {
                        Node u=e.getTarget();
                        System.err.print(" "+blocksArray[node2id.getValue(u)]);
                        stack.add(u);
                    }
                }
                System.err.println();
                }
                */
            }
        }
    }

    /**
     * does block a contain block b?
     *
     * @param a
     * @param b
     * @return true, if a contains b
     */
    private static boolean contains(Block a, Block b) {
        BitSet intersection = (BitSet) a.taxa.clone();
        intersection.and(b.taxa);
        if (intersection.cardinality() != b.taxa.cardinality())
            return false;

        for (int s = intersection.nextSetBit(0); s != -1; s = intersection.nextSetBit(s + 1)) {
            if (a.startPos > b.startPos + 5 || a.stopPos < b.stopPos - 5)
                return false;
        }
        return true;
    }

    /**
     * for a given seed segment (a,b), attempts to compute the best block
     *
     * @param fasta
     * @param length
     * @param a
     * @param b
     * @param positions
     * @param starts
     * @param stops
     * @param minSequences
     * @param minPercentSites
     * @return
     */
    private static Block computeBlock(boolean lengthOverNumber, FastA fasta, int length, int a, int b, BitSet positions, int[] starts, int[] stops, int minSequences, int minPercentSites) {
        // setup active sequences:
        BitSet active = new BitSet();
        int[] nonGapCount = new int[fasta.getSize()];

        for (int s = 0; s < fasta.getSize(); s++) {
            String sequence = fasta.getSequence(s);
            int count = 0;
            for (int pos = a; pos <= b; pos++) {
                if (sequence.charAt(pos) != '-')
                    count++;
            }
            nonGapCount[s] = count;
            if (100 * count / (b - a + 1) >= minPercentSites)
                active.set(s);
        }

        // extend to the left
        int left;
        if (active.cardinality() >= minSequences) {
            for (left = a; left >= 0; left--) {
                for (int s = active.nextSetBit(0); s != -1; s = active.nextSetBit(s + 1))
                    if (fasta.getSequence(s).charAt(left) != '-')
                        nonGapCount[s]++;
                if (positions.get(left)) {
                    BitSet dead = new BitSet();
                    for (int s = active.nextSetBit(0); s != -1; s = active.nextSetBit(s + 1)) {
                        if (left <= starts[s])
                            dead.set(s);
                        else {
                            int z = 0;
                            boolean ok = false;
                            for (int j = left - 1; !ok && z <= 10 && j >= 0; j--, z++) {
                                if (fasta.getSequence(s).charAt(j) != '-')
                                    ok = true;
                            }
                            if (!ok)
                                dead.set(s);
                        }
                    }
                    if (lengthOverNumber && active.cardinality() - dead.cardinality() > minSequences)
                        active.andNot(dead);
                    else if (dead.cardinality() >= 0.2 * active.cardinality())
                        break;

                    for (int s = active.nextSetBit(0); s != -1; s = active.nextSetBit(s + 1)) {
                        if (100 * nonGapCount[s] / (b - left + 1) < minPercentSites) {
                            active.set(s, false);
                            if (active.cardinality() <= minSequences)
                                break;
                        } else {
                            int z = 0;
                            boolean ok = false;
                            for (int j = left - 1; !ok && z <= 10 && j >= 0; j--, z++) {
                                if (fasta.getSequence(s).charAt(j) != '-')
                                    ok = true;
                            }
                            if (!ok) {
                                active.set(s, false);
                                if (active.cardinality() <= minSequences)
                                    break;
                            }
                        }
                    }
                }
            }

            // extend to the right
            if (active.cardinality() >= minSequences) {
                int right;
                for (right = b; right < length - 2; right++) {
                    for (int s = active.nextSetBit(0); s != -1; s = active.nextSetBit(s + 1))
                        if (fasta.getSequence(s).charAt(right) != '-')
                            nonGapCount[s]++;
                    if (positions.get(right)) {
                        BitSet dead = new BitSet();
                        for (int s = active.nextSetBit(0); s != -1; s = active.nextSetBit(s + 1)) {
                            if (right >= stops[s])
                                dead.set(s);
                            else {
                                int z = 0;
                                boolean ok = false;
                                for (int j = right + 1; !ok && z <= 10 && j < length; j++, z++) {
                                    if (fasta.getSequence(s).charAt(j) != '-')
                                        ok = true;
                                }
                                if (!ok)
                                    dead.set(s);
                            }
                        }
                        if (lengthOverNumber && active.cardinality() - dead.cardinality() > minSequences)
                            active.andNot(dead);
                        else if (dead.cardinality() >= 0.2 * active.cardinality())
                            break;

                        for (int s = active.nextSetBit(0); s != -1; s = active.nextSetBit(s + 1)) {
                            if (100 * nonGapCount[s] / (right - left + 1) < minPercentSites) {
                                active.set(s, false);
                                if (active.cardinality() <= minSequences)
                                    break;
                            } else {
                                int z = 0;
                                boolean ok = false;
                                for (int j = right + 1; !ok && z <= 10 && j < length; j++, z++) {
                                    if (fasta.getSequence(s).charAt(j) != '-')
                                        ok = true;
                                }
                                if (!ok) {
                                    active.set(s, false);
                                    if (active.cardinality() <= minSequences)
                                        break;
                                }
                            }
                        }
                    }
                }
                if (active.cardinality() >= minSequences) {
                    Block block = new Block();
                    block.startPos = left;
                    block.stopPos = right;
                    block.taxa = active;
                    return block;
                }
            }
        }

        return null;
    }

    /**
     * builds the alignment block for the given clique
     *
     * @param clique
     * @param starts
     * @param stops
     * @return alignment block
     */
    private static Block buildBlock(BitSet clique, int[] starts, int[] stops) {
        int startPos = 10000000;
        int stopPos = -1;
        for (int v = clique.nextSetBit(0); v >= 0; v = clique.nextSetBit(v + 1)) {
            if (starts[v] < startPos)
                startPos = starts[v];
            if (stops[v] > stopPos)
                stopPos = stops[v];
        }
        Block block = new Block();
        block.startPos = startPos;
        block.stopPos = stopPos;
        block.taxa = clique;
        return block;
    }

    /**
     * given the start positions for all sequences, returns an array of sequence ids
     * ordered by the starts
     *
     * @param numSequences
     * @param starts
     * @param ordering
     * @param inverse
     */
    private static void computeEliminationScheme(int numSequences, int[] starts, int[] ordering, int[] inverse) {
        List events = new LinkedList();
        for (int i = 0; i < numSequences; i++) {
            Pair pair = new Pair(starts[i], i);
            events.add(pair);
        }
        Collections.sort(events);
        Iterator it = events.iterator();
        int i = 0;
        while (it.hasNext()) {
            Pair pair = (Pair) it.next();
            ordering[i++] = pair.getSecondInt();
        }
        for (i = 0; i < numSequences; i++)
            inverse[ordering[i]] = i;
    }


    /**
     * computes the pairwise coverage matrix
     *
     * @param numSequences
     * @param starts
     * @param stops
     * @param minOverlap
     * @return pairwise coverage matrix
     */
    private static int[][] computeCoverageMatrix(int numSequences, int[] starts, int[] stops, int minOverlap,
                                                 int[] ordering, boolean usePercentOfPrevious) {
        int[][] matrix = new int[numSequences][numSequences];

        for (int i = 0; i < numSequences; i++) {
            int p = ordering[i];
            for (int j = i + 1; j < numSequences; j++) {
                int q = ordering[j];
                int startPQ = Math.max(starts[p], starts[q]);
                int stopPQ = Math.min(stops[p], stops[q]);
                int overlap = stopPQ - startPQ + 1;
                if (usePercentOfPrevious) {
                    if (overlap >= minOverlap / 100.0 * (stops[p] - starts[p] + 1))
                        matrix[p][q] = matrix[q][p] = overlap;
                } else {
                    if (overlap >= minOverlap)
                        matrix[p][q] = matrix[q][p] = overlap;
                }
            }
        }

/*
        System.err.println("Matrix for startPos=" + startPos + ", stopPos=" + stopPos + ":");
        for (int p = 0; p < numSequences; p++) {
            for (int q = 0; q < numSequences; q++) {
                System.err.print(" " + matrix[p][q]);
            }
            System.err.println();
        }
        */

        return matrix;
    }

    /**
     * computes the sorted list of fragment start and fragment stop events
     *
     * @param fasta
     * @param length
     * @param starts
     * @param stops
     */
    private static void stopsStarts(FastA fasta, int length, int[] starts, int[] stops) {
        for (int s = 0; s < fasta.getSize(); s++) {
            starts[s] = -1;
        }

        for (int i = 0; i < length; i++) {
            for (int s = 0; s < fasta.getSize(); s++) {
                if (fasta.getSequence(s).charAt(i) != '-') {
                    if (starts[s] == -1) {
                        starts[s] = i;
                    }
                    stops[s] = i;
                }
            }
        }
    }

    /**
     * computes the sorted list of fragment start and fragment stop events
     *
     * @param fasta
     * @param length
     * @return set of starts and stops
     */
    private static BitSet getGapPositions(FastA fasta, int length) {

        BitSet result = new BitSet();
        result.set(0);
        result.set(fasta.getFirstSequence().length() - 1);
        for (int i = 0; i < length; i++) {
            for (int s = 0; s < fasta.getSize(); s++) {
                if (i < length - 2 && fasta.getSequence(s).charAt(i) != '-' && fasta.getSequence(s).charAt(i + 1) == '-'
                        && fasta.getSequence(s).charAt(i + 2) == '-') {
                    result.set(i);
                } else if (i >= 2 && fasta.getSequence(s).charAt(i) != '-' && fasta.getSequence(s).charAt(i - 1) == '-'
                        && fasta.getSequence(s).charAt(i - 2) == '-') {
                    result.set(i);
                }
            }
        }
        return result;
    }

    /**
     * writes all subproblems to files
     *
     * @param inFile
     * @param fasta
     * @param blocks
     * @throws java.io.IOException
     */
    static public void writeFastAFiles(String inFile, FastA fasta, List blocks) throws java.io.IOException {
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(4);

        for (Object block1 : blocks) {
            Block block = (Block) block1;
            BitSet taxa = block.taxa;
            int startPos = block.startPos;
            int stopPos = block.stopPos;
            String cname = inFile + ":" + nf.format(startPos) + "_" + nf.format(stopPos);
            System.err.println("# Writing " + cname);
            FileWriter w = new FileWriter(new File(cname));


            for (int s = taxa.nextSetBit(0); s >= 0; s = taxa.nextSetBit(s + 1)) {
                String name = "t" + s + "_" + fasta.getHeader(s);
                name = name.replaceAll(" ", "_");
                w.write("> " + name + "\n");
                w.write(Basic.wraparound(fasta.getSequence(s).substring(startPos, stopPos + 1), 65));
            }
            w.close();
        }
    }

    /**
     * writes all subproblems to files
     *
     * @param inFile
     * @param fasta
     * @param blocks
     * @throws java.io.IOException
     */
    static public void writeCGVizFiles(String inFile, FastA fasta, List blocks) throws java.io.IOException {
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(4);

        for (Object block1 : blocks) {
            Block block = (Block) block1;
            BitSet taxa = block.taxa;
            int startPos = block.startPos;
            int stopPos = block.stopPos;
            String cname = inFile + ":" + nf.format(startPos) + "_" + nf.format(stopPos) + ".cgv";

            System.err.println("# Writing " + cname);
            FileWriter w = new FileWriter(new File(cname));

            w.write("{DATA " + cname + "\n");
            w.write("[__GLOBAL__] tracks=" + fasta.getSize() + " dimension=1:\n");

            for (int s = taxa.nextSetBit(0); s >= 0; s = taxa.nextSetBit(s + 1)) {
                w.write("track=" + (s + 1) + " type=DNA sequence=\"" + fasta.getSequence(s).substring(startPos, stopPos + 1)
                        + "\": " + startPos + " " + stopPos + "\n");
            }
            w.write("}\n");
            w.close();
        }
    }

}

class Block {
    int startPos;
    int stopPos;
    BitSet taxa;

    public String toString() {
        return "start=" + startPos + " stop=" + stopPos + " " + Basic.toString(taxa);
    }
}
