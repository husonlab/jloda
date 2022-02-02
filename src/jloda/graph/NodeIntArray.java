/*
 * NodeIntArray.java Copyright (C) 2022 Daniel H. Huson
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
 * Node integer array
 * Daniel Huson, 2003
 */

public class NodeIntArray extends NodeArray<Integer> {

    /**
     * Construct a node array with default value null
     */
    public NodeIntArray(Graph g) {
        super(g);
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeIntArray(NodeArray<Integer> src) {
        super(src);
    }


    /**
     * Get the entry for node v or 0
     *
     * @param v Node
     * @return value or 0
     */
    public int getInt(Node v) {
        final Integer value = get(v);
        return value != null ? value : 0;
    }

    public void set(Node v, int value) {
        put(v, value);
    }

    /**
     * increase the count by one.
     *
	 */
    public void increment(Node v) {
        set(v, getInt(v) + 1);
    }

    /**
     * increase the count by the given value
     *
	 */
    public void increment(Node v, int value) {
        set(v, getInt(v) + value);
    }

    /**
     * decrease the count by one.
     *
	 */
    public void decrement(Node v) {
        set(v, getInt(v) - 1);
    }

    /**
     * decrease the count by the given value
     *
	 */
    public void decrement(Node v, int value) {
        set(v, getInt(v) - value);
    }
}

// EOF
