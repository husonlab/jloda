/*
 * Pair.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

/**
 * a generic pair of objects
 *
 * @author huson
 * Date: 14-May-2004
 */
public class Pair<S, T> implements Comparable<Pair<S, T>>, Comparator<Pair<S, T>>, Iterable<Object> {
    private S first;
    private T second;

    public Pair() {

    }

    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public S getFirst() {
        return first;
    }

    public void setFirst(S first) {
        this.first = first;
    }

    public T getSecond() {
        return second;
    }

    public void setSecond(T second) {
        this.second = second;
    }


    @Deprecated
    public int getFirstInt() {
        return ((Integer) first);
    }

    @Deprecated
    public int getSecondInt() {
        return ((Integer) second);
    }

    @Deprecated
    public double getFirstDouble() {
        return ((Double) first);
    }

    @Deprecated
    public long getFirstLong() {
        return ((Long) first);
    }

    @Deprecated
    public long getSecondLong() {
        return (Long) second;
    }

    @Deprecated
    public double getSecondDouble() {
        return ((Double) second);
    }

    @Deprecated
    public float getFirstFloat() {
        return ((Float) first);
    }

    @Deprecated
    public float getSecondFloat() {
        return ((Float) second);
    }

    public String toString() {
        return "[" + first.toString() + " ; " + second.toString() + "]";
    }

    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) + (second == null ? 0 : 37 * second.hashCode());
    }

    public int compareTo(Pair<S, T> p) {
        int value;
        if (first == null && p.first != null)
            return -1;
        if (first != null && p.first == null)
            return 1;
        if (first == null) //  && p.first==null
            value = 0;
        else
            value = ((Comparable) first).compareTo(p.getFirst());
        if (value != 0)
            return value;

        if (second == null && p.second != null)
            return -1;
        if (second != null && p.second == null)
            return 1;
        if (second == null) //  && p.second==null
            value = 0;
        else
            value = ((Comparable) second).compareTo(p.getSecond());
        return value;
    }

    public boolean equals(Object other) {
        boolean good = false;
        if (other instanceof Pair) {
            final Pair that = (Pair) other;
            if (first == null) {
                good = (that.first == null);
            } else {
                good = first.equals(that.first);
            }
            if (good) {
                if (second == null) {
                    good = (that.second == null);
                } else {
                    good = second.equals(that.second);
                }
            }
        }
        return good;
    }

    /**
     * Compare two pairs
     * "Note: this comparator imposes orderings that are inconsistent with equals."
     *
     * @param p1 the first object to be compared.
     * @param p2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equals to, or greater than the
     * second.
     * @throws ClassCastException if the arguments' types prevent them from
     *                            being compared by this comparator.
     */
    public int compare(Pair<S, T> p1, Pair<S, T> p2) {
        return p1.compareTo(p2);
    }

    /**
     * clone this pair
     *
     * @return a shallow clone of this pair
     */
    public Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            Basic.caught(e);
        }
        return new Pair<>(getFirst(), getSecond());
    }

    public void set(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public boolean contains(Object x) {
        return x.equals(first) || x.equals(second);
    }

    @Override
    public Iterator<Object> iterator() {
        return IteratorUtils.iteratorNonNullElements(Arrays.asList(first, second).iterator());
    }
}
