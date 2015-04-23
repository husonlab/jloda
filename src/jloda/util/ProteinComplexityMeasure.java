/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @param sequence
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
     * @param counts
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
     * @param c
     * @return index
     */
    private static int getIndex(byte c) {
        switch (c) {
            default:
            case 'a':
            case 'A':
                return 0;
            case 'r':
            case 'R':
                return 1;
            case 'n':
            case 'N':
                return 2;
            case 'd':
            case 'D':
                return 3;
            case 'c':
            case 'C':
                return 4;
            case 'e':
            case 'E':
                return 5;
            case 'q':
            case 'Q':
                return 6;
            case 'g':
            case 'G':
                return 7;
            case 'h':
            case 'H':
                return 8;
            case 'i':
            case 'I':
                return 9;
            case 'l':
            case 'L':
                return 10;
            case 'k':
            case 'K':
                return 11;
            case 'm':
            case 'M':
                return 12;
            case 'f':
            case 'F':
                return 13;
            case 'p':
            case 'P':
                return 14;
            case 's':
            case 'S':
                return 15;
            case 't':
            case 'T':
                return 16;
            case 'w':
            case 'W':
                return 17;
            case 'y':
            case 'Y':
                return 18;
            case 'v':
            case 'V':
                return 19;
        }
    }
}
