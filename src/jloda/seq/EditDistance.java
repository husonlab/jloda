/*
 * EditDistance.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.seq;

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
        var stack1 = new Stack<Character>();
        var stack2 = new Stack<Character>();

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
            buffer1.append(stack1.pop());
        setAligned1(buffer1.toString());

        while (!stack2.empty())
            buffer2.append(stack2.pop());
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
	 */
    protected void setScore(int score) {
        this.score = score;
    }

    /**
     * returns 0, if a=b, 1, else
     *
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
     * @return minimum
     */
    static private int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

}
