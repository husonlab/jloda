/*
 * BitSetUtils.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.util;

import java.util.*;
import java.util.stream.Stream;

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
     * does first set contain second set?
     *
     * @param set
     * @param subset
     * @return true first set contains second set
     */
    public static boolean contains(BitSet set, BitSet subset) {
        return intersection(set, subset).cardinality() == subset.cardinality();
    }

    /**
     * iterable over all members
     */
    public static Iterable<Integer> members(BitSet set) {
        return members(set, 0);
    }

    /**
     * iterable over all members
     */
    public static Iterable<Integer> members(BitSet set, int start) {
        return () -> new Iterator<>() {
            private int i = set.nextSetBit(start);

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

    public static Stream<Integer> asStream(BitSet set) {
        return IteratorUtils.asStream(members(set));
    }

    public static BitSet asBitSet(Iterable<Integer> bits) {
        final BitSet bitSet = new BitSet();
        for (Integer i : bits) {
            bitSet.set(i);
        }
        return bitSet;
    }

    public static BitSet asBitSet(int... bits) {
        final BitSet bitSet = new BitSet();
        for (Integer i : bits) {
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

    public static int min(BitSet bits) {
        return bits.nextSetBit(0);
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

    public static Collection<? extends Integer> asList(BitSet... sets) {
        final BitSet set = new BitSet();
        for (BitSet b : sets) {
            set.or(b);
        }
        final ArrayList<Integer> list = new ArrayList<>(set.cardinality());
        for (Integer a : members(set)) {
            list.add(a);
        }
        return list;
    }

    public static Iterable<Integer> range(int startInclusive, int endExclusive) {
        return () -> new Iterator<>() {
            private int i = startInclusive;

            @Override
            public boolean hasNext() {
                return i < endExclusive;
            }

            @Override
            public Integer next() {
                return i++;
            }
        };
    }

    public static void addAll(BitSet bits, int... values) {
        for (int i : values)
            bits.set(i);
    }

    public static void addAll(BitSet bits, Iterable<Integer> values) {
        for (int i : values)
            bits.set(i);
    }

    /**
     * the set X - A
     *
     * @param setX
     * @param setA
     */
    public static BitSet minus(BitSet setX, BitSet setA) {
        final BitSet result = new BitSet();
        result.or(setX);
        result.andNot(setA);
        return result;
    }

    public static BitSet copy(BitSet bitSet) {
        final BitSet copy = new BitSet();
        copy.or(bitSet);
        return copy;
    }

    /**
     * get bits as list of integers
     *
     * @return list of integers
     */
    public static List<Integer> asList(BitSet bits) {
        var result = new ArrayList<Integer>();
        if (bits != null) {
            for (int i = bits.nextSetBit(0); i != -1; i = bits.nextSetBit(i + 1))
                result.add(i);
        }
        return result;
    }

    /**
     * get list of integers as bit set
     *
     * @return bits
     */
    public static BitSet asBitSet(List<Integer> integers) {
        var bits = new BitSet();
        if (integers != null) {
            for (Integer i : integers) {
                bits.set(i);
            }
        }
        return bits;
    }
}
