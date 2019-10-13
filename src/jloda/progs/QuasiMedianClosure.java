/*
 * QuasiMedianClosure.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.progs;

import jloda.graph.Node;
import jloda.phylo.PhyloSplitsGraph;
import jloda.swing.graphview.PhyloGraphView;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * compute the quasi median closure of a set of sequences
 * Daniel Huson, 9.2009
 */
public class QuasiMedianClosure {
    public static void main(String[] args) throws IOException {
        System.err.println("Please enter sequences, followed by a .");

        Set oldSequences = new TreeSet();
        Set newSequences = new HashSet();
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
            if (oldSequences.contains(aLine))
                System.err.println("Duplicate sequence in input (ignored): " + aLine);
            else {
                oldSequences.add(aLine);
            }
        }

        // check that all columns differ:
        for (int i = 0; i < sequenceLength; i++) {
            for (int j = i + 1; j < sequenceLength; j++) {
                boolean ok = false;
                char[] i2j = new char[256];
                char[] j2i = new char[256];

                for (Iterator it = oldSequences.iterator(); !ok && it.hasNext(); ) {
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


        Set curSequences = new HashSet(oldSequences);

        while (curSequences.size() > 0) {
            String[] oldArray = (String[]) oldSequences.toArray(new String[0]);
            newSequences.clear();
            for (String seqA : oldArray) {
                for (String seqB : oldArray) {
                    for (Object curSequence : curSequences) {
                        String seqC = (String) curSequence;
                        if (!seqC.equals(seqA) && !seqC.equals(seqB)) {
                            String[] medianSequences = computeQuasiMedian(seqA, seqB, seqC);
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
            Set tmp = curSequences;
            curSequences = newSequences;
            newSequences = tmp;
        }

        System.out.println("Closure (" + oldSequences.size() + "):");
        for (Object oldSequence : oldSequences) {
            System.out.println(oldSequence);
        }

        showGraph(oldSequences);
    }

    private static void showGraph(Set oldSequences) {
        PhyloSplitsGraph graph = new PhyloSplitsGraph();
        for (Object oldSequence : oldSequences) {
            String seq = (String) oldSequence;
            Node v = graph.newNode();
            graph.setLabel(v, seq);
        }

        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            for (Node w = v.getNext(); w != null; w = w.getNext()) {
                int i = computeOneStep(graph.getLabel(v), graph.getLabel(w));
                if (i != -1)
                    graph.newEdge(v, w, "" + i);
            }
        }

        JFrame frame = new JFrame("quasi-median network");
        frame.setSize(400, 400);

        PhyloGraphView view = new PhyloGraphView(graph, 400, 400);
        view.computeSpringEmbedding(1000, false);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(view.getScrollPane(), BorderLayout.CENTER);
        frame.setVisible(true);

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


        return (String[]) median.toArray(new String[0]);
    }
}
