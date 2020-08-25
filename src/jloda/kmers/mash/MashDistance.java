/*
 *  MashDistance.java Copyright (C) 2020 Daniel H. Huson
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

package jloda.kmers.mash;

import jloda.util.Basic;
import jloda.kmers.GenomeDistanceType;

/**
 * compute the distance between two mash sketches
 * Daniel Huson, 1.2020
 */
public class MashDistance {
    /**
     * computes the mash distance
     *
     * @param a
     * @param b
     * @return mash distance
     */
    public static double compute(MashSketch a, MashSketch b, GenomeDistanceType genomeDistanceType) {
        final double jaccardIndex = computeJaccardIndex(a, b);
        if (genomeDistanceType == GenomeDistanceType.Mash) {
            if (jaccardIndex == 0)
                return 1;
            else
                return Math.max(0f, -1.0 / a.getkSize() * Math.log(2.0 * jaccardIndex / (1 + jaccardIndex)));
        } else
            return 1 - jaccardIndex;
    }

    /**
     * computes the mash distance
     *
     * @param a
     * @param b
     * @return mash distance
     */
    public static double compute(MashSketch a, MashSketch b) {
        final double j = computeJaccardIndex(a, b);
        if (j == 0)
            return 1;
        else
            return Math.max(0f, -1.0 / a.getkSize() * Math.log(2.0 * j / (1 + j)));
    }

    /**
     * computes the Jaccard index for two sketches
     *
     * @param sketch1
     * @param sketch2
     * @return Jaccard index
     */
    public static double computeJaccardIndex(MashSketch sketch1, MashSketch sketch2) {
        final int sketchSize = Basic.min(sketch1.getSketchSize(), sketch1.getValues().length, sketch2.getSketchSize(), sketch2.getValues().length);

        final long[] union = new long[sketchSize];

        // compute the union:
        {
            int i = 0;
            int j = 0;
            for (int k = 0; k < sketchSize; k++) { // union upto MashSketch size
                final long value1 = sketch1.getValue(i);
                final long value2 = sketch2.getValue(j);
                if (value1 < value2) {
                    union[k] = value1;
                    i++;
                } else if (value1 > value2) {
                    union[k] = value2;
                    j++;
                } else // if (values1[i] == values2[j])
                {
                    union[k] = value1;
                    i++;
                    j++;
                }
            }
        }
        // compute intersection size:
        int intersectionSize = 0;
        {
            int i = 0;
            int j = 0;
            int k = 0;
            while (k < sketchSize) {
                final long value1 = sketch1.getValue(i);
                final long value2 = sketch2.getValue(j);

                if (value1 < union[k]) {
                    i++;
                    if (i == sketchSize)
                        break;
                } else if (value2 < union[k]) {
                    j++;
                    if (j == sketchSize)
                        break;
                } else if (value1 == union[k] && value2 == union[k]) {
                    intersectionSize++;
                    i++;
                    j++;
                    k++;
                } else // one of values1[i] and values2[j] is larger than union[k], let k catch up
                    k++;
            }
        }

        return (double) intersectionSize / (double) union.length;
    }
}
