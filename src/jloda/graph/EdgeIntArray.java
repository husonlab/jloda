/*
 * EdgeIntArray.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph;


/**
 * edge float array
 * Daniel Huson, 2003
 */
public class EdgeIntArray extends EdgeArray<Integer> {
    /**
     * Construct an edge array with default value null
     */
    public EdgeIntArray(Graph g) {
        super(g);
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeIntArray(EdgeArray<Integer> src) {
        super(src);
    }


    /**
     * get the entry for edge e
     *
     * @return integer or 0
     */
    public int getInt(Edge e) {
        var value = get(e);
        return value != null ? value : 0;
    }

    public void set(Edge e, int value) {
        put(e, value);
    }

    /**
     * increase the count by one.
     *
	 */
    public void increment(Edge e) {
        set(e, getInt(e) + 1);
    }

    /**
     * increase the count by the given value
     *
	 */
    public void increment(Edge e, int value) {
        set(e, getInt(e) + value);
    }

    /**
     * decrease the count by one.
     *
	 */
    public void decrement(Edge e) {
        set(e, getInt(e) - 1);
    }

    /**
     * decrease the count by the given value
     *
	 */
    public void decrement(Edge e, int value) {
        set(e, getInt(e) - value);
    }
}

// EOF
