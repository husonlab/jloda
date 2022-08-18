/*
 * BitSetUtils.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * some bit set convenience methods
 * Daniel Huson, 3.2018
 */
public class BitSetUtils {
    /**
     * get the union of some bit sets
     *
     * @return union
     */
    public static BitSet union(BitSet... sets) {
        final var result = new BitSet();
        for (var a : sets)
            result.or(a);
        return result;
    }

    /**
     * get the union of some bit sets
     *
     * @return union
     */
    public static BitSet union(Collection<BitSet> sets) {
        final var result = new BitSet();
        for (var a : sets)
            result.or(a);
        return result;
    }

    /**
     * get the intersection of some bit sets
     *
     * @return union
     */
    public static BitSet intersection(BitSet... sets) {
        final var result = new BitSet();
        var first = true;
        for (var b : sets) {
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
                var result = i;
                i = set.nextSetBit(i + 1);
                return result;
            }
        };
    }

    public static Stream<Integer> asStream(BitSet set) {
        return IteratorUtils.asStream(members(set));
    }

    public static BitSet asBitSet(Iterable<Integer> bits) {
        final var bitSet = new BitSet();
        for (var i : bits) {
            bitSet.set(i);
        }
        return bitSet;
    }

    public static BitSet asBitSet(int... bits) {
        final var bitSet = new BitSet();
        for (var i : bits) {
            if (i >= 0)
                bitSet.set(i);
        }
        return bitSet;
    }

    /**
     * compare two bit sets
     *
     * @return comparison
     */
    public static int compare(BitSet a, BitSet b) {
        var i = a.nextSetBit(0);
        var j = b.nextSetBit(0);
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
     * @return values and 0s
     */
    public static int[] asArrayWith0s(int max, BitSet bits) {
        final var array = new int[max + 1];
        for (var t : members(bits))
            array[t] = t;
        return array;
    }

    public static int min(BitSet bits) {
        return bits.nextSetBit(0);
    }

    /**
     * get the largest member of the set
     *
     * @return maximum member
     */
    public static int max(BitSet bits) {
        return bits.stream().max().orElse(0);
     }

    public static Collection<? extends Integer> asList(BitSet... sets) {
        final var set = new BitSet();
        for (var b : sets) {
            set.or(b);
        }
        final var list = new ArrayList<Integer>(set.cardinality());
        for (var a : members(set)) {
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
        for (var i : values)
            bits.set(i);
    }

    public static void addAll(BitSet bits, Iterable<Integer> values) {
        for (var i : values)
            bits.set(i);
    }

    public static BitSet add(BitSet bits, int... values) {
        var result = new BitSet();
        result.or(bits);
        addAll(result, values);
        return result;
    }

    /**
     * the set X - A
     */
    public static BitSet minus(BitSet setX, BitSet setA) {
        final var result = new BitSet();
        result.or(setX);
        result.andNot(setA);
        return result;
    }

    public static BitSet copy(BitSet bitSet) {
        final var copy = new BitSet();
        copy.or(bitSet);
        return copy;
    }

    /**
     * get bits as list of integers
     *
     * @return list of integers
     */
    public static List<Integer> asList(BitSet bits) {
        return asStream(bits).collect(Collectors.toList());
    }

    /**
     * get bits as list of integers
     *
     * @return list of integers
     */
    public static Set<Integer> asSet(BitSet bits) {
        return asStream(bits).collect(Collectors.toSet());
    }

    /**
     * get list of integers as bit set
     *
     * @return bits
     */
    public static BitSet asBitSet(List<Integer> integers) {
        var bits = new BitSet();
        if (integers != null) {
            for (var i : integers) {
				bits.set(i);
			}
		}
		return bits;
	}

	public static BitSet getComplement(BitSet bitSet, int firstBitInclusive, int lastBitExclusive) {
		var result = copy(bitSet);
		result.flip(firstBitInclusive, lastBitExclusive);
		return result;
	}

	/**
	 * do the three  bitsets intersects?
	 *
	 * @return true, if non-empty   intersection
	 */
	public static boolean intersects(BitSet a, BitSet b, BitSet c) {
		for (int i = a.nextSetBit(1); i >= 0; i = a.nextSetBit(i + 1)) {
			if (b.get(i) && c.get(i))
				return true;
		}
		return false;
	}
}
