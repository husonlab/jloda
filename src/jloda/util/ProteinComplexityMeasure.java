/*
 * ProteinComplexityMeasure.java Copyright (C) 2022 Daniel H. Huson
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

/**
 * computes the minimum complexity encountered in a protein string
 * Daniel Huson, 10.2012
 */
public class ProteinComplexityMeasure {
    private static final int N = 20; // alphabet size
    private static final int L = 16;  // window size
    private static final double LFactorial = 20922789888000.0;
    private static double[] factorial = null;

    /**
     * uses Wootten and Federhen to compute the complexity of a sequence
     *
     * @return average complexity
     */
    public static float getMinimumProteinComplexityWoottenFederhen(byte[] sequence, int length) {
        if (sequence == null || length < L)
            return 0;

        int[] counts = new int[N];

        for (int pos = 0; pos < L; pos++) // first 12 values
        {
            counts[getIndex(sequence[pos])]++;
        }
        double minComplexity = 1;

        // System.err.print("Values: ");
        for (int pos = L; pos < length - L; pos += L) {
            double product = computeProductOfFactorials(counts);
            double K = 1.0 / L * Math.log(LFactorial / product) / Math.log(N);
            counts[getIndex(sequence[pos - L])]--;
            counts[getIndex(sequence[pos])]++;
            // System.err.print(" "+K);
            if (K < minComplexity)
                minComplexity = K;
        }
        // System.err.println("minComplexity="+minComplexity+", sequence: "+s);
        return (float) Math.max(0.0001, minComplexity);   // MEGAN interprets 0 as being turned off...
    }

    /**
     * computes the produce of factorials (of values up to L)
     *
     * @return produce of factorials
     */
    private static double computeProductOfFactorials(int[] counts) {
        if (factorial == null) {
            factorial = new double[L + 1];
            double value = 1.0;
            for (int i = 0; i <= L; i++) {
                if (i > 0)
                    value *= i;
                factorial[i] = value;
            }
        }
        double result = 1.0;
        for (int count : counts) {
            result *= factorial[count];
        }
        return result;
    }

    /**
     * gets the index
     *
     * @return index
     */
    private static int getIndex(byte c) {
        return switch (c) {
            default -> 0;

            case 'a', 'A' -> 0;
            case 'r', 'R' -> 1;
            case 'n', 'N' -> 2;
            case 'd', 'D' -> 3;
            case 'c', 'C' -> 4;
            case 'e', 'E' -> 5;
            case 'q', 'Q' -> 6;
            case 'g', 'G' -> 7;
            case 'h', 'H' -> 8;
            case 'i', 'I' -> 9;
            case 'l', 'L' -> 10;
            case 'k', 'K' -> 11;
            case 'm', 'M' -> 12;
            case 'f', 'F' -> 13;
            case 'p', 'P' -> 14;
            case 's', 'S' -> 15;
            case 't', 'T' -> 16;
            case 'w', 'W' -> 17;
            case 'y', 'Y' -> 18;
            case 'v', 'V' -> 19;
        };
    }
}
