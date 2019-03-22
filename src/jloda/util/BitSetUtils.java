/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package jloda.util;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;

/**
 * some bit set convenience methods
 * Daniel Huson, 3.2018
 */
public class BitSetUtils {
    /**
     * get the union of some bit sets
     *
     * @param sets
     * @return union
     */
    public static BitSet union(BitSet... sets) {
        final BitSet result = new BitSet();
        for (BitSet a : sets)
            result.or(a);
        return result;
    }

    /**
     * get the intersection of some bit sets
     *
     * @param sets
     * @return union
     */
    public static BitSet intersection(BitSet... sets) {
        final BitSet result = new BitSet();
        boolean first = true;
        for (BitSet b : sets) {
            if (first) {
                result.or(b);
                first = false;
            } else
                result.and(b);
        }
        return result;
    }

    /**
     * does set a contain set b?
     *
     * @param a
     * @param b
     * @return true if b is contained in a
     */
    public static boolean contains(BitSet a, BitSet b) {
        return intersection(a, b).cardinality() == b.cardinality();
    }

    /**
     * iterable over all members
     *
     * @param set
     * @return members
     */
    public static Iterable<Integer> members(BitSet set) {
        return () -> new Iterator<Integer>() {
            private int i = set.nextSetBit(0);

            @Override
            public boolean hasNext() {
                return i != -1;
            }

            @Override
            public Integer next() {
                int result = i;
                i = set.nextSetBit(i + 1);
                return result;
            }
        };
    }

    public static BitSet asBitSet(Iterable<Integer> integers) {
        final BitSet bitSet = new BitSet();
        for (Integer i : integers) {
            bitSet.set(i);
        }
        return bitSet;
    }

    /**
     * compare two bit sets
     *
     * @param a
     * @param b
     * @return comparison
     */
    public static int compare(BitSet a, BitSet b) {
        int i = a.nextSetBit(0);
        int j = b.nextSetBit(0);
        while (i == j) {
            if (i == -1)
                return 0;
            i = a.nextSetBit(i + 1);
            j = b.nextSetBit(j + 1);
        }
        if (i < j)
            return -1;
        else
            return 1;
    }

    /**
     * get a comparator that compares by decreasing cardinality
     *
     * @return comparator
     */
    public static Comparator<BitSet> getComparatorByDecreasingCardinality() {
        return (a, b) -> {
            if (a.cardinality() > b.cardinality())
                return -1;
            else if (a.cardinality() < b.cardinality())
                return 1;
            else
                return compare(a, b);
        };
    }

    /**
     * gets an array in which the i-th component has value i, if and only if i is contained in bits
     *
     * @param max  maximum value
     * @param bits
     * @return values and 0s
     */
    public static int[] asArrayWith0s(int max, BitSet bits) {
        final int[] array = new int[max + 1];
        for (Integer t : members(bits))
            array[t] = t;
        return array;
    }

    /**
     * get the largest member of the set
     *
     * @param bits
     * @return maximum member
     */
    public static int max(BitSet bits) {
        int max = 0;
        for (int value = bits.nextSetBit(0); value != -1; value = bits.nextSetBit(value + 1)) {
            max = value;
        }
        return max;
    }
}
