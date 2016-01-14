/**
 * Single.java 
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
 * a mutable object
 *
 * @author huson
 *         Date: 14-May-2004
 */
public class Single<S> implements Comparable<Single<S>>, Comparator<Single<S>> {
    S value;

    public Single() {

    }

    public Single(S value) {
        set(value);
    }

    public S get() {
        return value;
    }

    public void set(S s) {
        this.value = s;
    }

    public String toString() {
        return value.toString();
    }

    public int hashCode() {
        return value.hashCode();
    }

    public int compareTo(Single<S> p) {
        int value = ((Comparable<S>) this.get()).compareTo(p.get());
        if (value != 0)
            return value;
        else
            return ((Comparable<S>) this.get()).compareTo(p.get());
    }

    public boolean equals(Object other) {
        boolean good = false;
        if (other instanceof Single) {
            Single p = (Single) other;
            if (value == null) {
                good = (p.value == null);
            } else {
                good = value.equals(p.value);
            }
        }
        return good;
    }

    /**
     * Compare
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
    public int compare(Single<S> p1, Single<S> p2) {
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
        return new Single<>(get());
    }
}
