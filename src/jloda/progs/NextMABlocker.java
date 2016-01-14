/**
 * NextMABlocker.java 
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
import jloda.graph.Node;
import jloda.graph.NodeIntegerArray;
import jloda.util.Basic;
import jloda.util.CommandLineOptions;
import jloda.util.FastA;
import jloda.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

/**
 * program for finding alignment blocks in a gappy alignment
 * Daniel Huson, 2.2009, Kaikoura
 */
public class NextMABlocker {
    static public void main(String[] args) throws Exception {
        CommandLineOptions options = new CommandLineOptions(args);
        String cname = options.getMandatoryOption("-i", "input file", "");
        int minLength = options.getOption("-l", "min length of block", 200);
        int gaps2Break = options.getOption("-g", "min number of gaps to break a segment", 20);
        boolean generateFastA = options.getOption("+f", "create FastASequences files", false, true);
        boolean generateCGViz = options.getOption("-c", "create CGViz files", true, false);
        int minSequences = options.getOption("-s", "min number of sequences in a block", 10);
        int minPercent = options.getOption("-d", "min percent of non-gap positions in a sequence", 90);
        options.done();

        System.err.println("Options: " + options);

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
        System.err.println("Sequences: " + numSequences + ", length: " + length);

        System.err.print("Computing segments: ");
        int numberOfSegments = 0;
        BitSet startPositions = new BitSet();
        BitSet stopPositions = new BitSet();
        List[] seq2segments = new LinkedList[fasta.getSize()];
        for (int s = 0; s < fasta.getSize(); s++) {
            seq2segments[s] = computeSegments(s, fasta.getSequence(s), gaps2Break);
            numberOfSegments += seq2segments[s].size();
            for (Object o : seq2segments[s]) {
                Segment seg = (Segment) o;
                startPositions.set(seg.start);
                stopPositions.set(seg.stop);
            }
        }
        System.err.println(numberOfSegments);
        System.err.println("Start positions: " + startPositions.cardinality());
        System.err.println("Stop positions:  " + stopPositions.cardinality());

        System.err.print("Computing blocks: ");
        List blocks = computeBlocks(fasta, startPositions, stopPositions, seq2segments, minLength, minSequences, minPercent);
        System.err.println(blocks.size());

        System.err.print("Reducing blocks:  ");
        makeBlocksUnique(blocks);
        System.err.println(blocks.size());

        System.err.print("Thining blocks:  ");
        makeThinCoverage(blocks);
        System.err.println(blocks.size());

        if (generateFastA)
            writeFastAFiles(cname, fasta, blocks);
        if (generateCGViz)
            writeCGVizFiles(cname, fasta, blocks);

        writeOverview(numSequences, seq2segments, blocks);
    }

    /**
     * compute all segments of a given sequence
     *
     * @param s
     * @param sequence
     * @param gaps2Break
     * @return list of segments
     */
    private static List computeSegments(int s, String sequence, int gaps2Break) {
        List segments = new LinkedList();

        int i = 0;

        List gapStartStops = new LinkedList();
        while (i < sequence.length()) {
            while (i < sequence.length() && sequence.charAt(i) != '-')
                i++;

            int start = i;
            int countgaps = 0;
            while (i < sequence.length() && sequence.charAt(i) == '-') {
                countgaps++;
                i++;
            }
            if (countgaps >= gaps2Break) {
                gapStartStops.add(new Pair(start, i));
            }
        }

        int start = 0;
        for (Object gapStartStop : gapStartStops) {
            Pair pair = (Pair) gapStartStop;
            if (pair.getFirstInt() > start) {
                Segment seg = new Segment();
                seg.start = start;
                seg.stop = pair.getFirstInt() - 1; // last position still in the alignment
                seg.taxon = s;
                segments.add(seg);
            }
            start = pair.getSecondInt();
        }
        if (start < sequence.length() - 1) {
            Segment seg = new Segment();
            seg.start = start;
            seg.stop = sequence.length() - 1; // last position still in the alignment
            seg.taxon = s;
            segments.add(seg);
        }
        return segments;
    }

    /**
     * compute the blocks
     *
     * @param startPositions positions where segments start
     * @param stopPositions  positions where segments stop
     * @param seq2segments
     * @return list of blocks
     */
    private static List computeBlocks(FastA fasta, BitSet startPositions, BitSet stopPositions, List[] seq2segments, int minLength, int minSequences,
                                      int minPercentNonGaps) {
        List blocks = new LinkedList();

        for (int start = startPositions.nextSetBit(0); start != -1; start = startPositions.nextSetBit(start + 1)) {
            for (int stop = stopPositions.nextSetBit(start + minLength - 1); stop != -1; stop = stopPositions.nextSetBit(stop + 1)) {
                BitSet active = new BitSet();

                for (int s = 0; s < seq2segments.length; s++) {
                    // if (covers(fasta.getSequence(s),minPercentNonGaps,start, stop)) {
                    if (covers(seq2segments[s], minPercentNonGaps, start, stop)) {
                        active.set(s);
                    }
                }
                if (active.cardinality() >= minSequences) {
                    NextBlock block = new NextBlock();
                    block.start = start;
                    block.stop = stop;
                    block.taxa = active;
                    block.weight = active.cardinality() * (stop - start + 1);
                    block.segs = new Segments(block.start, block.stop, block.taxa);
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    /**
     * does the given sequence cover the given interval?
     *
     * @param sequence
     * @param start
     * @param stop
     * @return true, if interval is covered
     */
    private static boolean covers(String sequence, int minPercentNonGaps, int start, int stop) {
        int nongaps = 0;
        for (int i = start; i <= stop; i++)
            if (sequence.charAt(i) != '-')
                nongaps++;
        return ((100 * nongaps) / (stop - start + 1) >= minPercentNonGaps);
    }

    /**
     * do the given segments cover the given interval?
     *
     * @param segments
     * @param start
     * @param stop
     * @return true, if interval is covered
     */
    private static boolean covers(List segments, int minPercentNonGaps, int start, int stop) {
        for (Object segment : segments) {
            Segment seg = (Segment) segment;
            int overlap = Math.max(0, Math.min(seg.stop, stop) - Math.max(seg.start, start) + 1);
            if ((100 * overlap) / (stop - start + 1) >= minPercentNonGaps)
                return true;

        }
        return false;
    }

    /**
     * make blocks sufficiently unique
     *
     * @param blocks
     */
    private static void makeBlocksUnique(List blocks) {
        NextBlock[] blocksArray = (NextBlock[]) blocks.toArray(new NextBlock[blocks.size()]);
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
                    // System.err.println("NextBlock "+blocksArray[i]+" contains block "+blocksArray[j]+": "+contains(blocksArray[i], blocksArray[j]));

                    if (contains(blocksArray[i], blocksArray[j]))
                        containmentGraph.newEdge(id2node[i], id2node[j]);
                }
            }
        }
        // System.err.println("Graph: "+containmentGraph.toString());

        for (Node v = containmentGraph.getFirstNode(); v != null; v = v.getNext()) {
            if (v.getInDegree() == 0) {
                NextBlock block = blocksArray[node2id.getValue(v)];
                blocks.add(block);
                /*
                 {
                System.err.print("NextBlock "+block+" contains:");
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
    private static boolean contains(NextBlock a, NextBlock b) {
        for (int i = b.taxa.nextSetBit(0); i != -1; i = b.taxa.nextSetBit(i + 1)) {
            if (!a.taxa.get(i))
                return false;
        }
        return (a.start <= b.start && a.stop >= b.stop);
    }

    /**
     * make blocks sufficiently unique
     *
     * @param blocks
     */
    private static void makeThinCoverage(List blocks) {
        SortedSet set = new TreeSet(new NextBlock());
        set.addAll(blocks);
        blocks.clear();

        while (set.size() > 0) {
            NextBlock currentBlock = (NextBlock) set.first();
            set.remove(currentBlock);
            blocks.add(currentBlock);

            List overlaps = new LinkedList();
            for (Object aSet : set) {
                NextBlock aBlock = (NextBlock) aSet;
                if (aBlock.getId() != currentBlock.getId() && currentBlock.segs.overlaps(aBlock.segs)) {
                    overlaps.add(aBlock);
                }
            }
            set.removeAll(overlaps);

            for (Object overlap : overlaps) {
                NextBlock aBlock = (NextBlock) overlap;
                Segments remains = aBlock.segs.substract(currentBlock.segs);
                if (remains.size() > 0) {
                    aBlock.segs = remains;
                    aBlock.weight = remains.size();
                    set.add(aBlock);
                }
            }
        }
    }

    /**
     * write an overview in CGViz format
     *
     * @param seq2segments
     * @throws IOException
     */
    private static void writeOverview(int numSequences, List[] seq2segments, List blocks) throws IOException {
        FileWriter w = new FileWriter(new File("overview.cgv"));
        w.write("{DATA overview\n");
        w.write("[__GLOBAL__] dimension=2\n");
        for (int i = 0; i < numSequences; i++) {
            for (Object o : seq2segments[i]) {
                Segment seg = (Segment) o;

                w.write("seq=" + i + " start= " + seg.start + " stop= " + seg.stop +
                        " len= " + (seg.stop - seg.start + 1) + ": " + seg.start + " " + i
                        + " " + seg.stop + " " + i + "\n");
            }
        }
        w.write("}\n");

        w.write("{DATA blocks\n");
        Iterator it = blocks.iterator();
        int blockNumber = 0;
        while (it.hasNext()) {
            blockNumber++;
            NextBlock block = (NextBlock) it.next();
            int minOrder = numSequences + 1;
            int maxOrder = -1;
            for (int t = block.taxa.nextSetBit(0); t >= 0; t = block.taxa.nextSetBit(t + 1)) {
                if (t < minOrder)
                    minOrder = t;
                if (t > maxOrder)
                    maxOrder = t;
            }
            w.write("num=" + blockNumber + " start=" + block.start + " stop=" + block.stop
                    + " minOrder=" + minOrder + " maxOrder=" + maxOrder + " numSequences=" +
                    block.taxa.cardinality() + ": " + block.start + " " + (minOrder) +
                    " " + block.stop + " " + (maxOrder) + "\n");
        }
        w.write("}\n");
        w.close();
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
            NextBlock block = (NextBlock) block1;
            BitSet taxa = block.taxa;
            int startPos = block.start;
            int stopPos = block.stop;
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
            NextBlock block = (NextBlock) block1;
            BitSet taxa = block.taxa;
            int startPos = block.start;
            int stopPos = block.stop;
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

class Segment {
    int start;
    int stop;
    int taxon;

    public Segment() {
    }

    public Segment(int start, int stop, int taxon) {
        this.start = start;
        this.stop = stop;
        this.taxon = taxon;
    }

    /**
     * substract a set of segments from a segment
     *
     * @param subs
     * @return resulting segments
     */
    public Segments subtract(Segments subs) {
        Segments segs = new Segments();
        segs.add(this);
        for (Iterator it = subs.iterator(); it.hasNext(); ) {
            Segment seg = (Segment) it.next();
            segs = segs.substract(seg);

        }
        return segs;
    }
}

class Segments {
    private final Collection segments;
    private int size;

    public Segments() {
        segments = new LinkedList();
        size = 0;
    }

    /**
     * initialize segment for each taxon from start to stop
     *
     * @param start
     * @param stop
     * @param taxa
     */
    public Segments(int start, int stop, BitSet taxa) {
        this();
        for (int t = taxa.nextSetBit(0); t != -1; t = taxa.nextSetBit(t + 1))
            add(new Segment(start, stop, t));
        recomputeSize();
    }

    public Collection getSegments() {
        return segments;
    }

    /**
     * substract all given segments
     *
     * @param subs
     * @return resulting segments
     */
    public Segments substract(Segments subs) {
        Segments result = new Segments();

        for (Iterator it = iterator(); it.hasNext(); ) {
            Segment seg = (Segment) it.next();
            Segments segs = seg.subtract(subs);
            if (segs.size() > 0)
                result.add(seg);
        }
        result.recomputeSize();
        return result;
    }

    /**
     * subtract a segment from a list of segments
     *
     * @param sub
     * @return new list
     */
    public Segments substract(Segment sub) {
        Segments result = new Segments();
        for (Iterator it = iterator(); it.hasNext(); ) {
            Segment seg = (Segment) it.next();
            if (seg.taxon != sub.taxon || sub.stop < seg.start || sub.start > seg.stop)     // misses
                result.add(seg);
            else if (sub.start <= seg.start && sub.stop >= seg.start && sub.stop < seg.stop) // overlaps start
                result.add(new Segment(sub.stop + 1, seg.stop, seg.taxon));
            else if (sub.start > seg.start && sub.start <= seg.stop && sub.stop >= seg.stop) // overlaps stop
                result.add(new Segment(seg.start, sub.start - 1, seg.taxon));
            else if (sub.start > seg.start && sub.stop < seg.stop)  // in the middle
            {
                result.add(new Segment(seg.start, sub.start - 1, seg.taxon));
                result.add(new Segment(sub.stop + 1, seg.stop, seg.taxon));
            }
        }
        result.recomputeSize();
        return result;
    }

    public void add(Segment seg) {
        segments.add(seg);
        size += seg.stop - seg.start + 1;

    }

    public void addAll(Collection segs) {
        segments.addAll(segs);
        recomputeSize();
    }

    public void recomputeSize() {
        size = 0;
        for (Object segment : segments) {
            Segment seg = (Segment) segment;
            size += (seg.stop - seg.start + 1);
        }
    }

    public int size() {
        return size;
    }

    public Iterator iterator() {
        return segments.iterator();
    }

    public void clear() {
        segments.clear();
        size = 0;
    }

    /**
     * do two segments intersect?
     *
     * @param segs
     * @return true, if pair of intersecting segments encountered
     */
    public boolean overlaps(Segments segs) {
        for (Iterator it1 = iterator(); it1.hasNext(); ) {
            Segment seg1 = (Segment) it1.next();
            for (Iterator it2 = segs.iterator(); it2.hasNext(); ) {
                Segment seg2 = (Segment) it2.next();
                if (!((seg1.start < seg2.start && seg1.stop < seg2.stop) || (seg1.start > seg2.start && seg1.stop > seg2.stop)))
                    return true;
            }
        }
        return false;
    }
}

class NextBlock implements Comparator {
    int start;
    int stop;
    BitSet taxa;
    int weight;
    Segments segs;
    private final int id;
    static private int maxId = 0;

    public NextBlock() {
        id = (++maxId);
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return "start=" + start + " stop=" + stop + " wgt=" + weight + " " + Basic.toString(taxa);
    }

    public int compare(Object o1, Object o2) {
        NextBlock wb1 = (NextBlock) o1;
        NextBlock wb2 = (NextBlock) o2;

        if (wb1.weight > wb2.weight)
            return -1;
        else if (wb1.weight < wb2.weight)
            return 1;
        else if (wb1.taxa.cardinality() > wb2.taxa.cardinality())
            return -1;
        else if (wb1.taxa.cardinality() < wb2.taxa.cardinality())
            return 1;
        else if (wb1.stop - wb1.start > wb2.stop - wb2.start)
            return -1;
        else if (wb1.stop - wb1.start < wb2.stop - wb2.start)
            return 1;
        else if (wb1.start < wb2.start)
            return -1;
        else if (wb1.start < wb2.start)
            return 1;
        else if (wb1.id < wb2.id)
            return -1;
        else if (wb1.id > wb2.id)
            return 1;
        else
            return 0;
    }
}
