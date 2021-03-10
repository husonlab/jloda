/*
 * NodeFloatArray.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jloda.graph;


/**
 * Node float array
 * Daniel Huson, 2003
 */

public class NodeFloatArray extends NodeArray<Float> {
    /**
     * Construct a node array with default value null
     */
    public NodeFloatArray(Graph g) {
        super(g);
    }

    /**
     * Construct a node array for the given graph and set the default value
     */
    public NodeFloatArray(Graph g, Float defaultValue) {
        super(g, defaultValue);
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeFloatArray(NodeArray<Float> src) {
        super(src);
    }

    public float get(Node v) {
        final Float value = getValue(v);
        if (value != null)
            return value;
        else
            return getDefaultValue() != null ? getDefaultValue() : 0f;

    }

    public void set(Node v, float value) {
        put(v, value);
    }

    @Override
    public void put(Node v, Float value) {
        setValue(v, value);
    }
}

// EOF
