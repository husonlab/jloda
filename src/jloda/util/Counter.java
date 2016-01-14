/**
 * Counter.java 
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

/**
 * object for counting. All methods are thread-safe (except addUnsynchronized)
 * Daniel Huson, 10.2011
 */
public class Counter {
    private long value;

    /**
     * constructor
     */
    public Counter() {
        this.value = 0;
    }

    /**
     * constructor
     *
     * @param value
     */
    public Counter(long value) {
        this.value = value;
    }

    /**
     * getter
     */
    public long get() {
        synchronized (this) {
            return value;
        }
    }

    /**
     * settter
     *
     * @param value
     */
    public void set(long value) {
        synchronized (this) {
            this.value = value;
        }
    }

    /**
     * increment
     */
    public void increment() {
        synchronized (this) {
            value++;
        }
    }

    /**
     * increment
     */
    public void add(long add) {
        synchronized (this) {
            value += add;
        }
    }

    /**
     * increment by value, unsynchronized
     *
     * @param add
     */
    public void addUnsynchronized(long add) {
        value += add;
    }

    /**
     * decrement
     */
    public void decrement() {
        synchronized (this) {
            value--;
        }
    }

    public String toString() {
        return "" + get();
    }
}
