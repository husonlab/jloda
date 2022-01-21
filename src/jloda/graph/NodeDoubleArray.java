/*
 * NodeDoubleArray.java Copyright (C) 2022 Daniel H. Huson
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
 * Node array
 * Daniel Huson, 2003
 */

public class NodeDoubleArray extends NodeArray<Double> {

    /**
     * Construct a node array with default value null
     */
    public NodeDoubleArray(Graph g) {
        super(g);
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeDoubleArray(NodeArray<Double> src) {
        this(src.getOwner());
        getOwner().nodeStream().forEach(e -> put(e, src.get(e)));
    }

    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return value or 0
     */
    public double getDouble(Node v) {
        final Double value = get(v);
        return value != null ? value : 0.0;
    }
}

// EOF
