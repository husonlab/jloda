/**
 * EditDistance.java 
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

package jloda.util;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * compute the edit distance between two sequences
 * Daniel Huson, 2003
 */
public class EditDistance {
    private String sequence1 = null;
    private String sequence2 = null;
    private String aligned1 = null;
    private String aligned2 = null;
    private int score = 0;


    /**
     * compute the edit distance between two sequences
     *
     * @param seq1
     * @param seq2
     * @return edit distance
     */
    static public int compute(String seq1, String seq2) {
        int rows = seq1.length();
        int cols = seq2.length();

        int[][] D = new int[rows + 1][cols + 1];

        // set base conditions:
        for (int r = 0; r <= rows; r++)
            D[r][0] = r;
        for (int c = 0; c <= cols; c++)
            D[0][c] = c;

        // recursion:
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                D[r][c] = min(D[r - 1][c] + 1,
                        D[r][c - 1] + 1,
                        D[r - 1][c - 1] + match(seq1.charAt(r - 1), seq2.charAt(c - 1)));

            }
        }

        return D[rows][cols];
    }

    /**
     * computes the edit distance and an alignment
     */
    public void compute() {
        int rows = getSequence1().length();
        int cols = getSequence2().length();

        int[][] D = new int[rows + 1][cols + 1];

        // set base conditions:
        for (int r = 0; r <= rows; r++)
            D[r][0] = r;
        for (int c = 0; c <= cols; c++)
            D[0][c] = c;

        // recursion:
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                D[r][c] = min(D[r - 1][c] + 1,
                        D[r][c - 1] + 1,
                        D[r - 1][c - 1]
                                + match(getSequence1().charAt(r - 1), getSequence2().charAt(c - 1)));

            }
        }

        // trace back alignment:
        int r = rows;
        int c = cols;
        Stack stack1 = new Stack();
        Stack stack2 = new Stack();

        while (r > 0 || c > 0) {
            if (r == 0 || D[r][c] == D[r - 1][c] + 1) // insertion in x
            {
                stack1.push(getSequence1().charAt(r-- - 1));
                stack2.push('-');
            } else if (c == 0 || D[r][c] == D[r][c - 1] + 1) // insertion in y
            {
                stack1.push('-');
                stack2.push(getSequence2().charAt(c-- - 1));
            } else // match-mismatch
            {
                stack1.push(getSequence1().charAt(r-- - 1));
                stack2.push(getSequence2().charAt(c-- - 1));
            }
        }

        // setup aligned sequences
        StringBuilder buffer1 = new StringBuilder();
        StringBuilder buffer2 = new StringBuilder();

        while (!stack1.empty())
            buffer1.append(((Character) stack1.pop()).charValue());
        setAligned1(buffer1.toString());

        while (!stack2.empty())
            buffer2.append(((Character) stack2.pop()).charValue());
        setAligned2(buffer2.toString());

        setScore(D[rows][cols]);
    }

    /**
     * get sequence 1
     *
     * @return sequence 1
     */
    public String getSequence1() {
        return sequence1;
    }

    /**
     * set sequence 1
     *
     * @param sequence1
     */
    public void setSequence1(String sequence1) {
        this.sequence1 = sequence1;
    }

    /**
     * get sequence 2
     *
     * @return sequence 2
     */
    public String getSequence2() {
        return sequence2;
    }

    /**
     * set sequence 2
     *
     * @param sequence2
     */
    public void setSequence2(String sequence2) {
        this.sequence2 = sequence2;
    }

    /**
     * get aligned version of sequence 1
     *
     * @return aligned sequence 1
     */
    public String getAligned1() {
        return aligned1;
    }

    /**
     * set aligned sequence 1
     *
     * @param aligned1
     */
    protected void setAligned1(String aligned1) {
        this.aligned1 = aligned1;
    }

    /**
     * get aligned version of sequence 2
     *
     * @return aligned sequence 2
     */
    public String getAligned2() {
        return aligned2;
    }

    /**
     * set aligned sequence 2
     *
     * @param aligned2
     */
    protected void setAligned2(String aligned2) {
        this.aligned2 = aligned2;
    }

    /**
     * get the computed score
     *
     * @return score
     */
    public int getScore() {
        return score;
    }

    /**
     * set the computed score
     *
     * @param score
     */
    protected void setScore(int score) {
        this.score = score;
    }

    /**
     * returns 0, if a=b, 1, else
     *
     * @param a
     * @param b
     * @return 0, if a=b, 1, else
     */
    static private int match(char a, char b) {
        if (a == b)
            return 0;
        else
            return 1;
    }

    /**
     * returns minimum of three numbers
     *
     * @param a
     * @param b
     * @param c
     * @return minimum
     */
    static private int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    /**
     * compute a distance matrix from chinese character codes
     *
     * @param args
     * @throws UsageException
     * @throws IOException
     */
    public static void main(String[] args) throws UsageException, IOException {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("Compute distance matrix from Chinese Character codes");

        String infile = options.getMandatoryOption("-i", "Input file", "");
        String outfile = options.getOption("-o", "Output file", "");


        options.done();

        PrintStream outs = System.out;

        if (outfile.length() > 0)
            outs = new PrintStream(new FileOutputStream(new File(outfile)));

        FastA fastA = new FastA();
        fastA.read(new FileReader(new File(infile)));

        List<Pair<String, String>> lines = new LinkedList<>();

        /*
        NexusStreamParser np=new NexusStreamParser(r);
        while(np.peekNextToken()!=NexusStreamParser.TT_EOF)
        {
            np.matchIgnoreCase("(");
            String ch=np.getWordRespectCase();
            String code=np.getWordRespectCase();
            np.matchIgnoreCase(")");
            lines.add(new Pair(ch,code));
        }
        */
        for (int i = 0; i < fastA.getSize(); i++) {
            String name = fastA.getHeader(i);
            String code = fastA.getSequence(i);
            lines.add(new Pair<>(name, code));
        }


        outs.println("#NEXUS");
        outs.println("begin taxa;");
        outs.println("dimensions ntax=" + lines.size() + ";");
        outs.println("end;");

        outs.println("begin distances;");
        outs.println("dimensions ntax=" + lines.size() + ";");
        outs.println("format triangle=both;");

        Pair[] data = lines.toArray(new Pair[lines.size()]);

        outs.println("matrix");
        for (Pair pi : data) {
            outs.print("'" + pi.getFirst() + "'");
            for (Pair pj : data) {
                int dist = compute((String) pi.getSecond(), (String) pj.getSecond());
                outs.print(" " + dist);
            }
            outs.println();
        }
        outs.println(";");
        outs.println("end;");

    }
}
