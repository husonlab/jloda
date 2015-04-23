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
        return new Single<S>(get());
    }
}
