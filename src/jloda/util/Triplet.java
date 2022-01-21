/*
 * Triplet.java Copyright (C) 2022 Daniel H. Huson
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
import java.util.Iterator;
import java.util.Objects;

/**
 * triplet
 * Daniel Huson, 2015
 */
public class Triplet<T1, T2, T3> implements Comparable<Triplet<T1, T2, T3>>, Iterable<Object> {
    private T1 first;
    private T2 second;
    private T3 third;

    public Triplet() {
    }

    public Triplet(T1 first, T2 second, T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    public T3 getThird() {
        return third;
    }

    public void setFirst(T1 first) {
        this.first = first;
    }

    public void setSecond(T2 second) {
        this.second = second;
    }

    public void setThird(T3 third) {
        this.third = third;
    }

    public String toString() {
        return first + ", " + second + ", " + third;
    }

    public int compareTo(Triplet<T1, T2, T3> p) {
        int value = ((Comparable<T1>) this.getFirst()).compareTo(p.getFirst());
        if (value != 0)
            return value;
        else
            value = ((Comparable<T2>) this.getSecond()).compareTo(p.getSecond());
        if (value != 0)
            return value;
        else
            return ((Comparable<T3>) this.getThird()).compareTo(p.getThird());
    }

    /**
     * Compare two Triplets
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
    public int compare(Triplet<T1, T2, T3> p1, Triplet<T1, T2, T3> p2) {
        return p1.compareTo(p2);
    }

    /**
     * clone this Triplet
     *
     * @return a shallow clone of this Triplet
     */
    public Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            Basic.caught(e);
        }
        return new Triplet<>(getFirst(), getSecond(), getThird());
    }

    @Override
    public Iterator<Object> iterator() {
        return IteratorUtils.iteratorNonNullElements(Arrays.asList(first, second, third).iterator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
        return Objects.equals(first, triplet.first) && Objects.equals(second, triplet.second) && Objects.equals(third, triplet.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
