/**
 * Pair.java 
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

import java.util.Comparator;

/**
 * a generic pair of objects
 *
 * @author huson
 *         Date: 14-May-2004
 */
public class Pair<S, T> implements Comparable<Pair<S, T>>, Comparator<Pair<S, T>> {
    S first;
    T second;

    public Pair() {

    }

    public Pair(S first, T second) {
        setFirst(first);
        setSecond(second);

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

    public int getFirstInt() {
        return ((Integer) first);
    }


    public int getSecondInt() {
        return ((Integer) second);
    }

    public double getFirstDouble() {
        return ((Double) first);
    }

    public long getFirstLong() {
        return ((Long) first);
    }


    public long getSecondLong() {
        return (Long) second;
    }

    public double getSecondDouble() {
        return ((Double) second);
    }

    public float getFirstFloat() {
        return ((Float) first);
    }


    public float getSecondFloat() {
        return ((Float) second);
    }

    public String toString() {
        return "[" + first.toString() + " ; " + second.toString() + "]";
    }

    public int hashCode() {
        if (first == null || second == null)
            return 0;
        else
            return first.hashCode() + 37 * second.hashCode();
    }

    public int compareTo(Pair<S, T> p) {
        int value = ((Comparable<S>) this.getFirst()).compareTo(p.getFirst());
        if (value != 0)
            return value;
        else
            return ((Comparable<T>) this.getSecond()).compareTo(p.getSecond());
    }

    public boolean equals(Object other) {
        boolean good = false;
        if (other instanceof Pair) {
            Pair p = (Pair) other;
            if (first == null) {
                good = (p.first == null);
            } else {
                good = first.equals(p.first);
            }
            if (good) {
                if (second == null) {
                    good = (p.second == null);
                } else {
                    good = second.equals(p.second);
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
     *         first argument is less than, equal to, or greater than the
     *         second.
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

    public S get1() {
        return first;
    }

    public T get2() {
        return second;
    }

    public void set1(S first) {
        this.first = first;
    }

    public void set2(T second) {
        this.second = second;
    }

    public boolean contains(Object x) {
        return x.equals(first) || x.equals(second);
    }
}
