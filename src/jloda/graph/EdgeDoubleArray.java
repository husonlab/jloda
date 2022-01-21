/*
 * EdgeDoubleArray.java Copyright (C) 2022 Daniel H. Huson
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
 * edge double array
 * Daniel Huson, 11.2017
 */
public class EdgeDoubleArray extends EdgeArray<Double> {
    /**
     * Construct an edge array with default value null
     */
    public EdgeDoubleArray(Graph g) {
        super(g);
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeDoubleArray(EdgeArray<Double> src) {
        super(src);
    }

    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return double or 0
     */
    public double getDouble(Edge e) {
        var value = get(e);
        return value != null ? value : 0.0;
    }
}

// EOF
